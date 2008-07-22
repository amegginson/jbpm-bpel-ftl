package org.jbpm.job.executor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.ProcessInstance;

import junit.framework.TestCase;

public class JobLoadSubProcessTest extends TestCase {

  // TODO see JobLoadJoinTest, but instead of the joins, use a process with a sub process
  private static Log log = LogFactory.getLog(JobLoadJoinTest.class);

  static Set finishedProcesses = Collections.synchronizedSet(new HashSet());
  long start;

  private static JbpmConfiguration jbpmConfiguration = 
    JbpmConfiguration.getInstance("org/jbpm/job/executor/loadtests.jbpm.cfg.xml");

  static {   
    jbpmConfiguration.createSchema();      
  }
		  
  protected void setUp() throws Exception {
	  super.setUp();
	  start = System.currentTimeMillis();
  }
  
  private boolean timeIsUp(int maxWait) {
	  return System.currentTimeMillis() - start > maxWait;
  }

  public void testJobLoadWithJoin() throws Exception {
    int processes = 1;
    int maxWait  = 20000;

    deployProcesses();

    jbpmConfiguration.startJobExecutor();
    
    Set expectedProcesses = new HashSet();
    for (int i = 0; i < processes; i++) {
      Integer number = new Integer(i);
      expectedProcesses.add(number);
      Thread thread = new StartNewExecutionThread(number);
      thread.start();
    }
    
    while (! expectedProcesses.equals(finishedProcesses) && !timeIsUp(maxWait)) {
      Thread.sleep(200);
    }
    log.info("number of finished processes: " + finishedProcesses);
    log.info("number of expected processes: " + expectedProcesses);
    assertEquals(expectedProcesses, finishedProcesses);
  }
  
  public static class StartNewExecutionThread extends Thread {
    Integer number;
    public StartNewExecutionThread(Integer number) {
      this.number = number;
    }
    public void run() {
      JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
      try {
        ProcessInstance processInstance = jbpmContext.newProcessInstance("asyncmaster");
        processInstance.getContextInstance().setVariable("number", number);
        jbpmContext.save(processInstance);
      } finally {
        jbpmContext.close();
      }
    }
  }

  public static class ProcessFinished implements ActionHandler {
    private static final long serialVersionUID = 1L;
    public void execute(ExecutionContext executionContext) throws Exception {
      Integer number = (Integer) executionContext.getVariable("number");
      log.info("process "+number+" finished");
      finishedProcesses.add(number);
    }
  }

  public void deployProcesses() {
	String subProcess =
      "<process-definition name='asyncsub' initial='ssarg'>" +
      "  <node name='ssarg' async='true'>" +
      "    <transition to='trv' />" +
      "  </node>" +
      "  <end-state name='trv' />" +
      "  <event type='process-end'>" +
      "    <action class='"+ProcessFinished.class.getName()+"' />" +
      "  </event>" +
      "</process-definition>";
    String masterProcess = 
      "<process-definition name='asyncmaster' initial='yenom'>" +
      "  <node name='yenom' async='true'>" +
      "    <transition to='rof' />" +
      "  </node>" +
      "  <process-state name='rof'>" +
      "    <sub-process name='asyncsub'>" +
      "      <variable name='number' />" +
      "    </sub-process>" +
      "    <transition to='vtm' />" +
      "  </process-state>" +
      "  <end-state name='vtm' />" +
      "  <event type='process-end'>" +
      "    <action class='"+ProcessFinished.class.getName()+"' />" +
      "  </event>" +
      "</process-definition>";
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      jbpmContext.deployProcessDefinition(ProcessDefinition.parseXmlString(subProcess));
      jbpmContext.deployProcessDefinition(ProcessDefinition.parseXmlString(masterProcess));
    } finally {
      jbpmContext.close();
    }
  }
	  
}
