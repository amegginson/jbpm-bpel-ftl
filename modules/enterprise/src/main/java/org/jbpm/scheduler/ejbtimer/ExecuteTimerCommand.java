package org.jbpm.scheduler.ejbtimer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmContext;
import org.jbpm.command.Command;
import org.jbpm.db.JobSession;
import org.jbpm.job.Job;

public class ExecuteTimerCommand implements Command {

  private static final long serialVersionUID = 1L;
  
  final long timerId;

  public ExecuteTimerCommand(long timerId) {
    this.timerId = timerId;
  }

  public Object execute(JbpmContext jbpmContext) throws Exception {
    JobSession jobSession = jbpmContext.getJobSession();
    Job timer = jobSession.getJob(timerId);
    if (timer!=null) {
      log.debug("executing timer "+timerId);
      if (timer.execute(jbpmContext)) {
        log.debug("deleting timer "+timerId);
        jobSession.deleteJob(timer);
      }
    } else {
      log.info("timer "+timerId+" was deleted, cannot execute it");
    }
    return timer;
  }

  private static Log log = LogFactory.getLog(ExecuteTimerCommand.class);
}
