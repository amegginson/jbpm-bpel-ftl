/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jbpm.msg.jms;

import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import javax.naming.InitialContext;

import org.apache.cactus.ServletTestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.jbpm.JbpmContext;
import org.jbpm.command.Command;
import org.jbpm.command.DeployProcessCommand;
import org.jbpm.command.StartProcessInstanceCommand;
import org.jbpm.ejb.LocalCommandService;
import org.jbpm.ejb.LocalCommandServiceHome;
import org.jbpm.enterprise.test.ActionRecorder;
import org.jbpm.enterprise.test.Recorder;

public class AsyncProcessingTest extends ServletTestCase {

  private LocalCommandService commandService;

  static int nbrOfConcurrentProcessExecutions = 20;
  static int maxWaitTime = 30000;

  protected void setUp() throws Exception {
    InitialContext initialContext = new InitialContext();
    LocalCommandServiceHome localCommandServiceHome = (LocalCommandServiceHome) initialContext.lookup("java:comp/env/ejb/LocalCommandServiceBean");
    commandService = localCommandServiceHome.create();
  }

  public void testBulkJobs() {
    Recorder.resetCollections();
    deleteAllJobs();
    deployProcess();
    launchProcesses();
    processAllJobs(maxWaitTime);
    Set expectedResults = createExpectedResults();
    assertEquals(Recorder.collectedProcessInstanceIds.toString()+" wasn't the expected size: "+nbrOfConcurrentProcessExecutions, nbrOfConcurrentProcessExecutions, Recorder.collectedProcessInstanceIds.size());
    assertEquals(expectedResults, Recorder.collectedResults);
  }

  protected void deleteAllJobs() {
    execute( new Command() {
      private static final long serialVersionUID = 1L;
      public Object execute(JbpmContext jbpmContext) throws Exception {
        Session session = jbpmContext.getSession();
        session.createQuery("delete from org.jbpm.job.Job").executeUpdate();
        return null;
      }
    });
  }

  protected Object execute(Command command) {
    return commandService.execute(command);
  }
  

  protected void deployProcess() {
    log.debug("start deploy process");
    execute(new DeployProcessCommand(
        "<process-definition name='bulk messages'>" +
        "  <start-state>" +
        "    <transition to='a' />" +
        "  </start-state>" +
        "  <node name='a' async='true'>" +
        "    <action class='"+Recorder.class.getName()+"' />" +
        "    <transition to='b' />" +
        "  </node>" +
        "  <node name='b' async='true'>" +
        "    <event type='node-enter'>" +
        "      <action name='X' async='true' class='"+ActionRecorder.class.getName()+"' />" +
        "    </event>" +
        "    <action class='"+Recorder.class.getName()+"' />" +
        "    <transition to='c' />" +
        "  </node>" +
        "  <node name='c' async='true'>" +
        "    <action class='"+Recorder.class.getName()+"' />" +
        "    <transition to='d'>" +
        "      <action name='Y' async='true' class='"+ActionRecorder.class.getName()+"' />" +
        "    </transition>" +
        "  </node>" +
        "  <node name='d' async='true'>" +
        "    <action class='"+Recorder.class.getName()+"' />" +
        "    <transition to='e' />" +
        "    <event type='node-leave'>" +
        "      <action name='Z' async='true' class='"+ActionRecorder.class.getName()+"' />" +
        "    </event>" +
        "  </node>" +
        "  <node name='e' async='true'>" +
        "    <action class='"+Recorder.class.getName()+"' />" +
        "    <transition to='end' />" +
        "  </node>" +
        "  <end-state name='end'/>" +
        "</process-definition>"));
  }
  
  protected void launchProcesses() {
    for (int i=0; i<nbrOfConcurrentProcessExecutions; i++) {
      StartProcessInstanceCommand command = new StartProcessInstanceCommand();
      command.setProcessName("bulk messages");
      execute(command);
    }
  }

  protected Set createExpectedResults() {
    Set expectedResults = new TreeSet();
    Iterator iter = Recorder.collectedProcessInstanceIds.iterator();
    while (iter.hasNext()) {
      String id = (String) iter.next();
      expectedResults.add(id+"a");
      expectedResults.add(id+"b");
      expectedResults.add(id+"c");
      expectedResults.add(id+"d");
      expectedResults.add(id+"e");
      expectedResults.add(id+"X");
      expectedResults.add(id+"Y");
      expectedResults.add(id+"Z");
    }
    return expectedResults;
  }

  private void processAllJobs(final long maxWait) {
    boolean jobsAvailable = true;

    // install a timer that will interrupt if it takes too long
    // if that happens, it will lead to an interrupted exception and the test will fail
    TimerTask interruptTask = new TimerTask() {
      Thread testThread = Thread.currentThread();
      public void run() {
        log.debug("test "+getName()+" took too long. going to interrupt...");
        testThread.interrupt();
      }
    };
    Timer timer = new Timer();
    timer.schedule(interruptTask, maxWait);
    
    try {
      while (jobsAvailable) {
        log.debug("going to sleep for 200 millis, waiting for the job executor to process more jobs");
        Thread.sleep(200);
        jobsAvailable = areJobsAvailable();
      }
      
    } catch (InterruptedException e) {
      fail("test execution exceeded treshold of "+maxWait+" milliseconds");
    } finally {
      timer.cancel();
    }
  }

  protected int getNbrOfJobsAvailable() {
    Integer nbrOfJobsAvailable = (Integer) execute( new Command() {
      private static final long serialVersionUID = 1L;
      public Object execute(JbpmContext jbpmContext) throws Exception {
        Integer nbrOfJobsAvailable = null;
        Session session = jbpmContext.getSession();
        Number jobs = (Number) session.createQuery("select count(*) from org.jbpm.job.Job").uniqueResult();
        log.debug("there are '"+jobs+"' jobs currently in the job table");
        if (jobs!=null) {
          nbrOfJobsAvailable = new Integer(jobs.intValue());
        }
        return nbrOfJobsAvailable;
      }
    });
    return nbrOfJobsAvailable.intValue();
  }

  protected boolean areJobsAvailable() {
    return (getNbrOfJobsAvailable()>0);
  }
  
  private static Log log = LogFactory.getLog(AsyncProcessingTest.class);
}
