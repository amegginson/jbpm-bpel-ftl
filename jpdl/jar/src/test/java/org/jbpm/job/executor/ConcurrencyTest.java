package org.jbpm.job.executor;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.ProcessInstance;

/**
 * test case for http://jira.jboss.com/jira/browse/JBPM-983
 */
public class ConcurrencyTest extends TestCase {

  private static Log log = LogFactory.getLog(ConcurrencyTest.class);

  protected static JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance("org/jbpm/jbpm.test.cfg.xml");

  protected void setUp() throws Exception {
    jbpmConfiguration.createSchema();

    JobExecutor jobExecutor = jbpmConfiguration.getJobExecutor();
    jobExecutor.setNbrOfThreads(5);
    jobExecutor.start();

    log.debug("");
    log.debug("### starting " + getName() + " ####################################################");
  }

  protected void tearDown() throws Exception {
    log.debug("### " + getName() + " done ####################################################");
    log.debug("");

    jbpmConfiguration.getJobExecutor().stop();

    jbpmConfiguration.dropSchema();
  }

  static String SUBPROCESS_XML = "<?xml version='1.0' encoding='UTF-8'?>"
      + "<process-definition xmlns='urn:jbpm.org:jpdl-3.2' name='simplesubtest'>"
      + "<start-state name='start-state1'>"
      + "   <description>start of the process</description>"
      + "   <transition name='start-to-check' to='fileCheck' />"
      + "</start-state>"
      + ""
      + "<node name='fileCheck' async='exclusive'>"
      + "   <action name='action_filecheck' class='"
      + TestAction.class.getName()
      + "'>"
      + "   </action>"
      + "   <transition name='check-to-do' to='doWhatever'></transition>"
      + "</node>"
      + ""
      + "<node name='doWhatever' async='exclusive'>"
      + "   <action name='action_do' class='"
      + TestAction.class.getName()
      + "'>"
      + "   </action>"
      + "   <transition name='check-to-end' to='end-state-success'></transition>"
      + "</node>"
      + ""
      + "<end-state name='end-state-success'>"
      + "   <description>process finished normally</description>"
      + "</end-state>"
      + "</process-definition>";

  static String PROCESS_XML = "<?xml version='1.0' encoding='UTF-8'?>"
      + "<process-definition xmlns='urn:jbpm.org:jpdl-3.2' name='simpletest'>"
      + "<start-state name='start-state1'>"
      + "   <description>start of the process</description>"
      + "   <transition name='start-to-check' to='fileCheck' />"
      + "</start-state>"
      + ""
      + "<node name='fileCheck' async='true'>"
      + "   <action name='action_check' class='"
      + TestAction.class.getName()
      + "'>"
      + "   </action>"
      + "   <transition name='check-to-fork' to='fork1'></transition>"
      + "</node>"
      + ""
      + "<fork name='fork1'>"
      + "   <transition name='toNode1' to='node1'></transition>"
      + "   <transition name='toNode2' to='node2'></transition>"
      + "</fork>"
      + ""
      + "<process-state name='node1' async='exclusive'>"
      + "   <sub-process name='simplesubtest' />"
      + "   <transition name='node1toJoin1' to='join1'></transition>"
      + "</process-state>"
      + ""
      + "<process-state name='node2' async='exclusive'>"
      + "   <sub-process name='simplesubtest' />"
      + "   <transition name='node2toJoin1' to='join1'></transition>"
      + "</process-state>"
      + ""
      + "<join name='join1'>"
      + "   <transition name='joinToEnd' to='end-state-success'></transition>"
      + "</join>"
      + ""
      + "<end-state name='end-state-success'>"
      + "   <description>process finished normally</description>"
      + "</end-state>"
      + "</process-definition>";

  public void testSimple() throws Exception {
    assertTrue(jbpmConfiguration.getJobExecutor().getNbrOfThreads() > 1);

    log.info("### TEST: deploy + start processes ###");

    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      jbpmContext.deployProcessDefinition(ProcessDefinition.parseXmlString(SUBPROCESS_XML));
      jbpmContext.deployProcessDefinition(ProcessDefinition.parseXmlString(PROCESS_XML));

    }
    finally {
      jbpmContext.close();
    }

    // create test properties
    Map testVariables = new HashMap();
    testVariables.put("test", "true");

    for (int i = 0; i < 10; i++) {
      log.info("#################### TEST: starting process " + i + " ####################");

      ProcessInstance pi = null;

      jbpmContext = jbpmConfiguration.createJbpmContext();
      try {
        pi = jbpmContext.newProcessInstance("simpletest");
        pi.getContextInstance().addVariables(testVariables);
        jbpmContext.save(pi);
        pi.signal();

      }
      finally {
        jbpmContext.close();
      }
      log.info("### TEST: wait for process completion ###");

      waitFor(pi.getId());

      jbpmContext = jbpmConfiguration.createJbpmContext();
      try {
        ProcessInstance pi2 = jbpmContext.getProcessInstance(pi.getId());
        assertEquals("end-state-success", pi2.getRootToken().getNode().getName());
      }
      finally {
        jbpmContext.close();
      }

      log.info("#################### TEST: finished ####################");
    }
  }

  protected void waitFor(final long piId) throws Exception {
    long startTime = System.currentTimeMillis();
    int SECONDS = 30;

    while (true) {
      if (System.currentTimeMillis() - startTime > SECONDS * 1000) {
        fail("Aborting after " + SECONDS + " seconds.");
        return;
      }

      log.info("waiting for workflow completion...."); // + pi.getRootToken().getNode().getName()
      // );
      try {
        Thread.sleep(5000);
      }
      catch (InterruptedException e) {
        log.error("wait for workflow was interruputed", e);
        fail();
      }

      JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
      try {
        if (jbpmContext.getProcessInstance(piId).hasEnded())
          break;
      }
      finally {
        jbpmContext.close();
      }
    }
  }

  public static class TestAction implements ActionHandler {

    private static final long serialVersionUID = 1L;

    public void execute(ExecutionContext executionContext) throws Exception {
      String processName = executionContext.getProcessDefinition().getName()
          + ":"
          + executionContext.getProcessInstance().getId();
      String nodeName = executionContext.getToken().getNode().getName();
      String tokenName = executionContext.getToken().toString();

      log.info("ACTION (process="
          + processName
          + ",node="
          + nodeName
          + ",token="
          + tokenName
          + "): begin");

      for (int i = 0; i < 5; i++) {
        Thread.sleep(500);
        log.info("ACTION (process="
            + processName
            + ",node="
            + nodeName
            + ",token="
            + tokenName
            + "): working...");
        Thread.sleep(500);
      }

      log.info("ACTION (process="
          + processName
          + ",node="
          + nodeName
          + ",token="
          + tokenName
          + "): end");

      executionContext.leaveNode();
    }
  }
}
