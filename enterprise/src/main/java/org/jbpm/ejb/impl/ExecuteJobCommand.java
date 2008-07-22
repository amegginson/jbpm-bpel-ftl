package org.jbpm.ejb.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmContext;
import org.jbpm.command.Command;
import org.jbpm.db.JobSession;
import org.jbpm.job.Job;

public class ExecuteJobCommand implements Command {

  private static final long serialVersionUID = 1L;
  
  long jobId;
  
  public ExecuteJobCommand(long jobId) {
    this.jobId = jobId;
  }

  public Object execute(JbpmContext jbpmContext) throws Exception {
    JobSession jobSession = jbpmContext.getJobSession();
    Job job = jobSession.loadJob(jobId);
    log.debug("executing job "+jobId);
    if (job.execute(jbpmContext)) {
      log.debug("deleting job "+jobId);
      jobSession.deleteJob(job);
    }
    return job;
  }

  private static Log log = LogFactory.getLog(ExecuteJobCommand.class);
}
