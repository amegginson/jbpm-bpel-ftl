package org.jbpm.job.executor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.ProcessInstance;

public class JobLoadJoinTest extends TestCase {

  private static Log log = LogFactory.getLog(JobLoadJoinTest.class);

  private static JbpmConfiguration jbpmConfiguration = 
    JbpmConfiguration.getInstance("org/jbpm/job/executor/loadtests.jbpm.cfg.xml");

  static Set finishedProcesses = Collections.synchronizedSet(new HashSet());
  long start;

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
    int processes = 20;
    int maxWait  = 20000;

    deployProcess();

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
        ProcessInstance processInstance = jbpmContext.newProcessInstance("asyncjoin");
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

  public void deployProcess() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition name='asyncjoin' initial='yenom'>" +
      "  <node name='yenom' async='true'>" +
      "    <transition to='rof' />" +
      "  </node>" +
      "  <fork name='rof'>" +
      "    <transition to='gnihton' />" +
      "    <transition to='skihc' />" +
      "  </fork>" +
      "  <node name='gnihton' async='true'>" +
      "    <transition to='eerf' />" +
      "  </node>" +
      "  <node name='skihc' async='true'>" +
      "    <transition to='eerf' />" +
      "  </node>" +
      "  <join name='eerf'>" +
      "    <transition to='ymtnawi' />" +
      "  </join>" +
      "  <node name='ymtnawi' async='true'>" +
      "    <transition to='vtm' />" +
      "  </node>" +
      "  <end-state name='vtm' />" +
      "  <event type='process-end'>" +
      "    <action class='"+ProcessFinished.class.getName()+"' />" +
      "  </event>" +
      "</process-definition>"
    );
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      jbpmContext.deployProcessDefinition(processDefinition);
    } finally {
      jbpmContext.close();
    }
  }
}
