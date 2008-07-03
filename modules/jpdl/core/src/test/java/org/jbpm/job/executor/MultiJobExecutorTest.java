package org.jbpm.job.executor;

import java.sql.SQLException;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.command.Command;
import org.jbpm.command.CommandService;
import org.jbpm.command.DeployProcessCommand;
import org.jbpm.command.StartProcessInstanceCommand;
import org.jbpm.command.impl.CommandServiceImpl;
import org.jbpm.configuration.ObjectFactory;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.Token;

/**
 * Test case for JBPM-1072.
 * @author Jiri Pechanec
 * @author Alejandro Guizar
 */
public class MultiJobExecutorTest extends TestCase {

  private static final int EXECUTOR_COUNT = 16;
  private static final int JOB_COUNT = 1;
  private static final int IDLE_INTERVAL = 1000;
  private static final String PROCESS_NAME = "TestProcess";

  private static JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance();
  private static CommandService commandService = new CommandRetryService(new CommandServiceImpl(jbpmConfiguration), 3);

  private static final Log log = LogFactory.getLog(MultiJobExecutorTest.class);

  private static final String PROCESS_DEFINITION = "<process-definition name='" 
      + PROCESS_NAME 
      + "'>"
      + "<event type='process-end'>"
      + "<action name='endAction' class='"
      + EndAction.class.getName()
      + "'/>"
      + "</event>"
      + "<start-state name='start'>"
      + "<transition to='fork'/>"
      + "</start-state>"
      + "<node name='fork'>"
      + "<action class='"
      + MultiFork.class.getName()
      + "'/>"
      + "<transition to='async'/>"
      + "</node>"
      + "<node name='async' async='true'>"
      + "<action class='"
      + AsyncAction.class.getName()
      + "'/>"
      + "<transition to='join'/>"
      + "</node>"
      + "<join name='join'>" 
      + "<transition to='end'/>" 
      + "</join>"
      + "<end-state name='end'/>"
      + "</process-definition>";

  protected void setUp() throws SQLException {
    jbpmConfiguration.createSchema();
    commandService.execute(new DeployProcessCommand(PROCESS_DEFINITION));
  }

  public void testMultipleExecutors() {
    // create job executors
    JobExecutor[] jobExecutors = new JobExecutor[EXECUTOR_COUNT];
    // jobExecutors[0] = jbpmConfiguration.getJobExecutor();

    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      ObjectFactory objectFactory = jbpmContext.getObjectFactory();
      for (int i = 0; i < jobExecutors.length; i++) {
        JobExecutor jobExecutor = (JobExecutor) objectFactory.createObject("jbpm.job.executor");
        jobExecutor.setName(jobExecutor.getName() + '-' + i);
        jobExecutor.setIdleInterval(IDLE_INTERVAL);
        jobExecutors[i] = jobExecutor;
      }
    }
    finally {
      jbpmContext.close();
    }

    // start job executors
    for (int i = 0; i < jobExecutors.length; i++) {
      jobExecutors[i].start();
    }

    // kick off process instance
    StartProcessInstanceCommand startCommand = new StartProcessInstanceCommand();
    startCommand.setProcessName(PROCESS_NAME);
    commandService.execute(startCommand);
  
    // wait till process instance ends
    EndAction.waitFor(IDLE_INTERVAL);
  
    // stop job executors
    for (int i = jobExecutors.length - 1; i >= 0; i--) {
      try {
        jobExecutors[i].stopAndJoin();
      }
      catch (InterruptedException e) {
        // continue to next executor
      }
    }
  
    assertEquals(JOB_COUNT, AsyncAction.getCount());
  }

  protected void tearDown() {
    jbpmConfiguration.dropSchema();
  }

  static class CommandRetryService implements CommandService {
  
    private final CommandService commandService;
    private final int retryCount;

    CommandRetryService(CommandService commandService, int retryCount) {
      this.commandService = commandService;
      this.retryCount = retryCount;
    }

    public Object execute(Command command) {
      for (int i = 1;; i++) {
        try {
          return commandService.execute(command);
        }
        catch (RuntimeException e) {
          log.error("attempt " + i + " to execute command failed", e);
          if (i == retryCount)
            throw e;
        }
      }
    }
  }

  public static class MultiFork implements ActionHandler {

    private static final long serialVersionUID = 1L;

    public void execute(ExecutionContext ctx) throws Exception {
      Token parentToken = ctx.getToken();
      for (int i = 0; i < JOB_COUNT; i++) {
        Token childToken = new Token(parentToken, Integer.toString(i));
        new ExecutionContext(childToken).leaveNode();
      }
    }
  }

  public static class AsyncAction implements ActionHandler {

    private static volatile int count = 0;

    private static final long serialVersionUID = 1L;

    public void execute(ExecutionContext exeContext) throws Exception {
      log.info("execution count: " + incrementCount());
      exeContext.leaveNode();
    }

    private static synchronized int incrementCount() {
      return ++count;
    }

    public static int getCount() {
      return count;
    }
  }

  public static class EndAction implements ActionHandler {

    private static final Object lock = new Object();
    private static volatile boolean ended;

    private static final long serialVersionUID = 1L;

    public void execute(ExecutionContext exeContext) throws Exception {
      ended = true;
      log.debug("process-end event fired, notifying");
      synchronized (lock) {
        lock.notify();
      }
    }

    public static void waitFor(long timeout) {
      while (!ended) {
        log.debug("process not ended, waiting");
        try {
          synchronized (lock) {
            lock.wait(timeout);
          }
        }
        catch (InterruptedException e) {
          // check condition again
        }
        log.debug("checking whether process has ended");
      }
      log.debug("process ended, proceeding");
    }
  }
}
