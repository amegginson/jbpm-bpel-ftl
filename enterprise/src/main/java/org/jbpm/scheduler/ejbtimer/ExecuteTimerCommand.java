package org.jbpm.scheduler.ejbtimer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmContext;
import org.jbpm.command.Command;
import org.jbpm.job.Timer;

public class ExecuteTimerCommand implements Command {

  private static final long serialVersionUID = 1L;
  
  long timerId;

  public ExecuteTimerCommand(long timerId) {
    this.timerId = timerId;
  }

  public Object execute(JbpmContext jbpmContext) throws Exception {
    Timer timer = (Timer) jbpmContext.getJobSession().getJob(timerId);
    if (timer!=null) {
      log.debug("executing timer "+timerId);
      timer.execute(jbpmContext);
    } else {
      log.info("execution of timer "+timerId+" was skipped cause the timer was deleted from the database");
    }
    return timer;
  }

  private static Log log = LogFactory.getLog(ExecuteTimerCommand.class);
}
