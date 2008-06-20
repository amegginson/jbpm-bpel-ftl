package org.jbpm.job.executor;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.db.JobSession;
import org.jbpm.job.Job;

public class LockMonitorThread extends Thread {
  
  JbpmConfiguration jbpmConfiguration;
  int lockMonitorInterval;
  int maxLockTime;
  int lockBufferTime;

  boolean isActive = true;

  public LockMonitorThread(JbpmConfiguration jbpmConfiguration, int lockMonitorInterval, int maxLockTime, int lockBufferTime) {
    this.jbpmConfiguration = jbpmConfiguration;
    this.lockMonitorInterval = lockMonitorInterval;
    this.maxLockTime = maxLockTime;
    this.lockBufferTime = lockBufferTime;
  }

  public void run() {
    try {
      while (isActive) {
        try {
          unlockOverdueJobs();
          if ( (isActive) 
               && (lockMonitorInterval>0)
             ) {
            sleep(lockMonitorInterval);
          }
        } catch (InterruptedException e) {
          log.info("lock monitor thread '"+getName()+"' got interrupted");
        } catch (Exception e) {
          log.error("exception in lock monitor thread. waiting "+lockMonitorInterval+" milliseconds", e);
          try {
            sleep(lockMonitorInterval);
          } catch (InterruptedException e2) {
            log.debug("delay after exception got interrupted", e2);
          }
        }
      }
    } catch (Exception e) {
      log.error("exception in lock monitor thread", e);
    } finally {
      log.info(getName()+" leaves cyberspace");
    }
  }

    
  protected void unlockOverdueJobs() {
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      JobSession jobSession = jbpmContext.getJobSession();
      
      Date treshold = new Date(System.currentTimeMillis()-maxLockTime-lockBufferTime);
      List jobsWithOverdueLockTime = jobSession.findJobsWithOverdueLockTime(treshold);
      Iterator iter = jobsWithOverdueLockTime.iterator();
      while (iter.hasNext()) {
        Job job = (Job) iter.next();
        // unlock
        log.debug("unlocking "+job+ " owned by thread "+job.getLockOwner());
        job.setLockOwner(null);
        job.setLockTime(null);
        jobSession.saveJob(job);
      }

    } finally {
      try {
        jbpmContext.close();
      } catch (RuntimeException e) {
        log.error("problem committing job execution transaction", e);
        throw e;
      }
    }
  }

  public void setActive(boolean isActive) {
    this.isActive = isActive;
  }

  private static Log log = LogFactory.getLog(LockMonitorThread.class);
}
