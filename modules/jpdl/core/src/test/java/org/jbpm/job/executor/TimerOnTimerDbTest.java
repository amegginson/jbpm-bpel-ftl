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
package org.jbpm.job.executor;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.db.AbstractDbTestCase;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;

/**
 * Test case for JBPM-1135
 * @author Alejandro Guizar
 */
public class TimerOnTimerDbTest extends AbstractDbTestCase {

  private static final Log log = LogFactory.getLog(TimerOnTimerDbTest.class);

  public void testTimerOnTimer() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlResource("org/jbpm/job/executor/timerOnTimer.jpdl.xml");
    jbpmContext.deployProcessDefinition(processDefinition);

    ProcessInstance processInstance = jbpmContext.newProcessInstanceForUpdate("timerTest");
    processInstance.getContextInstance().setVariable("timerTestWorkflow", new WorkflowLogger());
    processInstance.signal();
    commitAndCloseSession();
    long tokenId = processInstance.getRootToken().getId();

    startJobExecutor();
    try {
      sleep(500);
      beginSessionTransaction();
      assertEquals("timerTest", jbpmContext.loadToken(tokenId).getNode().getName());
      commitAndCloseSession();

      sleep(1000);
      beginSessionTransaction();
      assertEquals("secondTimerTest", jbpmContext.loadToken(tokenId).getNode().getName());
      commitAndCloseSession();

      sleep(1000);
      beginSessionTransaction();
      assertTrue(jbpmContext.loadToken(tokenId).getProcessInstance().hasEnded());
    }
    finally {
      stopJobExecutor();
    }
  }

  private void sleep(long millis) {
    try {
      Thread.sleep(millis);
    }
    catch (InterruptedException e) {
      // reassert interruption
      Thread.currentThread().interrupt();
    }    
  }

  public static final class WorkflowLogger implements Serializable {
  
    private static final long serialVersionUID = 1L;
  
    public void logNodeEnter() {
      log.info("entered node");
    }
  
    public void logNodeLeave() {
      log.info("left node");
    }
  
    public void logTaskCreate() {
      log.info("created task");
    }
  
    public void logTimerCreate() {
      log.info("created timer");
    }
  
    public void logTimerFired() {
      log.info("fired timer");
    }
  }
}
