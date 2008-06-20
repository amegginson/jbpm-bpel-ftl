package org.jbpm.job.executor;

import java.sql.SQLException;

import junit.framework.TestCase;

import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.command.CommandService;
import org.jbpm.command.SignalCommand;
import org.jbpm.command.StartProcessInstanceCommand;
import org.jbpm.command.impl.CommandServiceImpl;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.ProcessInstance;

/**
 * Test case for JBPM-1072.
 * @author Jiri Pechanec
 * @author Alejandro Guizar
 */
public class MultiJobExecutorTest extends TestCase {

  private static final int EXECUTOR_COUNT = 20;
  public static final String PROCESS_NAME = "TestProcess";

  private JobExecutor[] executors = new JobExecutor[EXECUTOR_COUNT];

  private static JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance();
  private static CommandService commandService = new CommandServiceImpl(jbpmConfiguration);

  public static final String PROCESS_DEFINITION = "<?xml version='1.0' encoding='UTF-8'?>"
      + "<process-definition xmlns='' name='TestProcess'>"
      + "<event type='process-end'>"
      + "<action name='endAction' class='"
      + EndAction.class.getName()
      + "' />"
      + "</event>"
      + "<start-state name='start-state1'>"
      + "<transition to='Service 1'></transition>"
      + "</start-state>"
      + "<node name='Service 1'>"
      + "<action name='esbAction' "
      + "class='"
      + SimpleAction.class.getName()
      + "'>"
      + "</action>"
      + "<transition to='Service 2'></transition>"
      + "</node>"
      + "<node name='Service 2' async='true'>"
      + "<action name='esbAction' "
      + "class='"
      + SimpleAction2.class.getName()
      + "'>"
      + "</action>"
      + "<transition to='end-state1'></transition>"
      + "</node>"
      + "<end-state name='end-state1'></end-state>"
      + "</process-definition>";

  protected void setUp() throws SQLException {
    jbpmConfiguration.createSchema();

    // deploy process definition
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      jbpmContext.deployProcessDefinition(ProcessDefinition.parseXmlString(PROCESS_DEFINITION));
      System.out.println("Isolation " + jbpmContext.getConnection().getTransactionIsolation());
    }
    finally {
      jbpmContext.close();
    }
  }

  public void testMultipleExecutors() {
    // start job executors
    for (int i = 0; i < executors.length; i++) {
      executors[i] = (JobExecutor) JbpmConfiguration.Configs.getObjectFactory().createObject(
          "jbpm.job.executor");
      executors[i].setName("JbpmJobExecutor/" + (i + 1));
      executors[i].start();
    }

    // kick off process instance
    StartProcessInstanceCommand startCommand = new StartProcessInstanceCommand();
    startCommand.setProcessName(PROCESS_NAME);
    ProcessInstance pi = (ProcessInstance) commandService.execute(startCommand);

    // signal service 1
    SignalCommand signalCommand = new SignalCommand();
    signalCommand.setTokenId(pi.getRootToken().getId());
    commandService.execute(signalCommand);

    // wait for process end
    EndAction.waitFor();

    // stop job executors
    for (int i = executors.length - 1; i >= 0; i--) {
      try {
        executors[i].stopAndJoin();
      }
      catch (InterruptedException e) {
        // continue to next executor
      }
    }

    assertEquals(1, SimpleAction2.getExecutionCount());
  }

  protected void tearDown() {
    jbpmConfiguration.dropSchema();
  }

  public static class SimpleAction implements ActionHandler {

    private static final long serialVersionUID = -9065054081909009083L;

    public void execute(ExecutionContext ctx) throws Exception {
      System.out.println("Action 1");
    }

  }

  public static class SimpleAction2 implements ActionHandler {

    private static int executionCount = 0;

    private static final long serialVersionUID = -9065054081909009083L;

    public void execute(ExecutionContext ctx) throws Exception {
      System.out.println("Action 2: " + incrementCount());
      ctx.getNode().leave(ctx);
    }

    private static synchronized int incrementCount() {
      return ++executionCount;
    }

    public static synchronized int getExecutionCount() {
      return executionCount;
    }
  }

  public static class EndAction implements ActionHandler {

    private static final Object monitor = new Object();

    private static final long serialVersionUID = 1L;

    public void execute(ExecutionContext executionContext) throws Exception {
      synchronized (monitor) {
        monitor.notify();
      }
    }

    public static void waitFor() {
      try {
        synchronized (monitor) {
          monitor.wait(60000);
        }
      }
      catch (InterruptedException e) {
      }
    }
  }
}
