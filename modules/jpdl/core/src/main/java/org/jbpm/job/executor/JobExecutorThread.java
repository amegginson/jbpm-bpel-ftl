package org.jbpm.job.executor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.db.JobSession;
import org.jbpm.job.Job;
import org.jbpm.job.Timer;
import org.jbpm.persistence.JbpmPersistenceException;
import org.jbpm.persistence.db.StaleObjectLogConfigurer;

public class JobExecutorThread extends Thread {

  public JobExecutorThread( String name,
                            JobExecutor jobExecutor,
                            JbpmConfiguration jbpmConfiguration,
                            int idleInterval,
                            int maxIdleInterval,
                            long maxLockTime,
                            int maxHistory
                          ) {
    super(name);
    this.jobExecutor = jobExecutor;
    this.jbpmConfiguration = jbpmConfiguration;
    this.idleInterval = idleInterval;
    this.maxIdleInterval = maxIdleInterval;
    this.maxLockTime = maxLockTime;
  }

  final JobExecutor jobExecutor; 
  final JbpmConfiguration jbpmConfiguration;
  final int idleInterval;
  final int maxIdleInterval;
  final long maxLockTime;

  int currentIdleInterval;
  volatile boolean isActive = true;

  public void run() {
    try {
      currentIdleInterval = idleInterval;
      while (isActive) {
        try {
          Collection acquiredJobs = acquireJobs();

          if (! acquiredJobs.isEmpty()) {
            Iterator iter = acquiredJobs.iterator();
            while (iter.hasNext() && isActive) {
              Job job = (Job) iter.next();
              executeJob(job);
            }

          } else { // no jobs acquired
            if (isActive) {
              long waitPeriod = getWaitPeriod();
              if (waitPeriod>0) {
                synchronized(jobExecutor) {
                  jobExecutor.wait(waitPeriod);
                }
              }
            }
          }
          
          // no exception so resetting the currentIdleInterval
          currentIdleInterval = idleInterval;

        } catch (InterruptedException e) {
          log.info((isActive? "active" : "inactive")+" job executor thread '"+getName()+"' got interrupted");
        } catch (Exception e) {
          log.error("exception in job executor thread. waiting "+currentIdleInterval+" milliseconds", e);
          try {
            synchronized(jobExecutor) {
              jobExecutor.wait(currentIdleInterval);
            }
          } catch (InterruptedException e2) {
            log.debug("delay after exception got interrupted", e2);
          }
          // after an exception, the current idle interval is doubled to prevent 
          // continuous exception generation when e.g. the db is unreachable
          currentIdleInterval <<= 1;
          if (currentIdleInterval > maxIdleInterval || currentIdleInterval < 0) {
            currentIdleInterval = maxIdleInterval;
          }
        }
      }
    } catch (Exception e) {
      // NOTE that Error's are not caught because that might halt the JVM and mask the original Error.
      log.error("exception in job executor thread", e);
    } finally {
      log.info(getName()+" leaves cyberspace");
    }
  }

  protected Collection acquireJobs() {
    Collection acquiredJobs;
    synchronized (jobExecutor) {
      Collection jobsToLock = new ArrayList();
      log.debug("acquiring jobs for execution...");
      JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
      try {
        JobSession jobSession = jbpmContext.getJobSession();
        log.debug("querying for acquirable job...");
        Job job = jobSession.getFirstAcquirableJob(getName());
        if (job!=null) {
          if (job.isExclusive()) {
            log.debug("exclusive acquirable job found ("+job+"). querying for other exclusive jobs to lock them all in one tx...");
            List otherExclusiveJobs = jobSession.findExclusiveJobs(getName(), job.getProcessInstance());
            jobsToLock.addAll(otherExclusiveJobs);
            log.debug("trying to obtain a process-instance exclusive locks for '"+otherExclusiveJobs+"'");
          } else {
            log.debug("trying to obtain a lock for '"+job+"'");
            jobsToLock.add(job);
          }
          
          Iterator iter = jobsToLock.iterator();
          while (iter.hasNext()) {
            job = (Job) iter.next();
            job.setLockOwner(getName());
            job.setLockTime(new Date());
            // jbpmContext.getSession().update(job);
          }

          // HACKY HACK : this is a workaround for a hibernate problem that is fixed in hibernate 3.2.1
          if (job instanceof Timer) {
            Hibernate.initialize(((Timer)job).getGraphElement());
          }
        } else {
          log.debug("no acquirable jobs in job table");
        }
      } finally {
        try {
          jbpmContext.close();
          acquiredJobs = jobsToLock;
          log.debug("obtained lock on jobs: "+acquiredJobs);
        }
        catch (JbpmPersistenceException e) {
          // if this is a stale object exception, the jbpm configuration has control over the logging
          if ("org.hibernate.StaleObjectStateException".equals(e.getCause().getClass().getName())) {
            log.info("problem committing job acquisition transaction: optimistic locking failed");
            StaleObjectLogConfigurer.staleObjectExceptionsLog.error("problem committing job acquisition transaction: optimistic locking failed", e);
          } else {
            // TODO run() will log this exception, log it here too?
            log.error("problem committing job acquisition transaction", e);
            throw e;
          }
          acquiredJobs = Collections.EMPTY_LIST;
          log.debug("couldn't obtain lock on jobs: "+jobsToLock); 
        }
      }
    }
    return acquiredJobs;
  }

  protected void executeJob(Job job) {
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      JobSession jobSession = jbpmContext.getJobSession();
      job = jobSession.loadJob(job.getId());

      try {
        log.debug("executing job "+job);
        if (job.execute(jbpmContext)) {
          jobSession.deleteJob(job);
        }

      } catch (Exception e) {
        log.debug("exception while executing '"+job+"'", e);
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        job.setException(sw.toString());
        job.setRetries(job.getRetries()-1);
      }
      
      // if this job is locked too long
      long totalLockTimeInMillis = System.currentTimeMillis() - job.getLockTime().getTime(); 
      if (totalLockTimeInMillis>maxLockTime) {
        jbpmContext.setRollbackOnly();
      }

    } finally {
      try {
        jbpmContext.close();
      } catch (JbpmPersistenceException e) {
        // if this is a stale object exception, the jbpm configuration has control over the logging
        if ("org.hibernate.StaleObjectStateException".equals(e.getCause().getClass().getName())) {
          log.info("problem committing job execution transaction: optimistic locking failed");
          StaleObjectLogConfigurer.staleObjectExceptionsLog.error("problem committing job execution transaction: optimistic locking failed", e);
        } else {
          // TODO run() will log this exception, log it here too?
          log.error("problem committing job execution transaction", e);
          throw e;
        }
      }
    }
  }
  protected Date getNextDueDate() {
    Date nextDueDate = null;
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      JobSession jobSession = jbpmContext.getJobSession();
      Collection jobIdsToIgnore = jobExecutor.getMonitoredJobIds();
      Job job = jobSession.getFirstDueJob(getName(), jobIdsToIgnore);
      if (job!=null) {
        nextDueDate = job.getDueDate();
        jobExecutor.addMonitoredJobId(getName(), job.getId());
      }
    } finally {
      jbpmContext.close();
    }
    return nextDueDate;
  }

  protected long getWaitPeriod() {
    long interval = currentIdleInterval;
    Date nextDueDate = getNextDueDate();
    if (nextDueDate!=null) {
      long currentTimeMillis = System.currentTimeMillis();
      long nextDueDateTime = nextDueDate.getTime();
      if (nextDueDateTime < currentTimeMillis+currentIdleInterval) {
        interval = nextDueDateTime-currentTimeMillis;
      }
    }
    if (interval<0) {
      interval = 0;
    }
    return interval;
  }

  /**
   * @deprecated As of jBPM 3.2.3, replaced by {@link #deactivate()}
   */
  public void setActive(boolean isActive) {
    if (isActive == false) 
      deactivate();
  }

  /**
   * Signals this thread to stop running. Execution should cease shortly afterwards.
   */
  public void deactivate() {
    if (isActive) {
      isActive = false;
      interrupt();      
    }
  }

  private static Log log = LogFactory.getLog(JobExecutorThread.class);
}
