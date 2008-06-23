package org.jbpm.job.executor;

import java.util.Collections;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.ProcessInstance;

public class JobExecutorDbTest extends TestCase {

  static int nbrOfConcurrentProcessExecutions = 20;
  static int maxWaitTime = 20000;
  static Set collectedResults = Collections.synchronizedSet(new TreeSet());
  
  protected static JbpmConfiguration jbpmConfiguration = 
    JbpmConfiguration.getInstance("org/jbpm/jbpm.test.cfg.xml");
  
  static {
    jbpmConfiguration.getJobExecutor().nbrOfThreads = 5;
  }

  protected JobExecutor jobExecutor;

  public static class AutomaticActivity implements ActionHandler {
    private static final long serialVersionUID = 1L;
    public void execute(ExecutionContext executionContext) throws Exception {
      String id = (String) Long.toString(executionContext.getProcessInstance().getId());
      String nodeName = executionContext.getNode().getName();
      collectedResults.add(id+nodeName);
      executionContext.leaveNode();
    }
  }

  public static class AsyncAction implements ActionHandler {
    private static final long serialVersionUID = 1L;
    public void execute(ExecutionContext executionContext) throws Exception {
      String id = (String) Long.toString(executionContext.getProcessInstance().getId());
      Action action = executionContext.getAction();
      String actionName = action.getName();
      collectedResults.add(id+actionName);
    }
  }

  public void testBulkJobs() {
    jbpmConfiguration.createSchema();
    deployProcess();
    launchProcesses();
    processJobs(maxWaitTime);
    assertEquals(createExpectedResults(), collectedResults);
    jbpmConfiguration.createSchema();
  }

  public void deployProcess() {
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    try {

      ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
        "<process-definition name='bulk messages'>" +
        "  <start-state>" +
        "    <transition to='a' />" +
        "  </start-state>" +
        "  <node name='a' async='true'>" +
        "    <action class='"+AutomaticActivity.class.getName()+"' />" +
        "    <transition to='b' />" +
        "  </node>" +
        "  <node name='b' async='true'>" +
        "    <event type='node-enter'>" +
        "      <action name='X' async='true' class='"+AsyncAction.class.getName()+"' />" +
        "    </event>" +
        "    <action class='"+AutomaticActivity.class.getName()+"' />" +
        "    <transition to='c' />" +
        "  </node>" +
        "  <node name='c' async='true'>" +
        "    <action class='"+AutomaticActivity.class.getName()+"' />" +
        "    <transition to='d'>" +
        "      <action name='Y' async='true' class='"+AsyncAction.class.getName()+"' />" +
        "    </transition>" +
        "  </node>" +
        "  <node name='d' async='true'>" +
        "    <action class='"+AutomaticActivity.class.getName()+"' />" +
        "    <transition to='e' />" +
        "    <event type='node-leave'>" +
        "      <action name='Z' async='true' class='"+AsyncAction.class.getName()+"' />" +
        "    </event>" +
        "  </node>" +
        "  <node name='e' async='true'>" +
        "    <action class='"+AutomaticActivity.class.getName()+"' />" +
        "    <transition to='end' />" +
        "  </node>" +
        "  <end-state name='end'/>" +
        "</process-definition>"
      );
    
      jbpmContext.deployProcessDefinition(processDefinition);
    } finally {
      jbpmContext.close();
    }
  }

  public void launchProcesses() {
    for (int i=0; i<nbrOfConcurrentProcessExecutions; i++) {
      JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
      try {
        ProcessInstance processInstance = jbpmContext.newProcessInstanceForUpdate("bulk messages");
        processInstance.signal();
      } finally {
        jbpmContext.close();
      }
    }
  }

  public Set createExpectedResults() {
    Set expectedResults = new TreeSet();
    for (int i=1; i<nbrOfConcurrentProcessExecutions+1; i++) {
      expectedResults.add(i+"a");
      expectedResults.add(i+"b");
      expectedResults.add(i+"c");
      expectedResults.add(i+"d");
      expectedResults.add(i+"e");
      expectedResults.add(i+"X");
      expectedResults.add(i+"Y");
      expectedResults.add(i+"Z");
    }
    return expectedResults;
  }

  protected void startJobExecutor() {
    jobExecutor = jbpmConfiguration.getJobExecutor();
    jobExecutor.start();
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
      jobExecutor.stopAndJoin();
      
    } catch (InterruptedException e) {
      fail("test execution exceeded treshold of "+maxWait+" milliseconds");
    } finally {
      timer.cancel();
    }
  }

  private int getNbrOfJobsAvailable() {
    int nbrOfJobsAvailable = 0;
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      Session session = jbpmContext.getSession();
      Number jobs = (Number) session.createQuery("select count(*) from org.jbpm.job.Job").uniqueResult();
      log.debug("there are '"+jobs+"' jobs currently in the job table");
      if (jobs!=null) {
        nbrOfJobsAvailable = jobs.intValue();
      }
    } finally {
      jbpmContext.close();
    }
    return nbrOfJobsAvailable;
  }

  protected boolean areJobsAvailable() {
    return (getNbrOfJobsAvailable()>0);
  }
  
  protected void processJobs(long maxWait) {
    try {
      Thread.sleep(300);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    startJobExecutor();
    try {
      processAllJobs(maxWait);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } finally {
      stopJobExecutor();
    }
  }
  
  protected void stopJobExecutor() {
    if (jobExecutor!=null) {
      try {
        jobExecutor.stopAndJoin();
      } catch (InterruptedException e) {
        throw new RuntimeException("waiting for job executor to stop and join got interrupted", e); 
      }
    }
  }
  
  private static Log log = LogFactory.getLog(JobExecutorDbTest.class);
}
