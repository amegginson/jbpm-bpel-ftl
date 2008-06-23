package org.jbpm.db;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.transaction.Synchronization;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jbpm.JbpmException;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.job.Job;
import org.jbpm.job.Timer;

public class JobSession {

  private Session session;

  public JobSession(Session session) {
    this.session = session;
  }

  public Job getFirstAcquirableJob(String lockOwner) {
    Job job = null;
    try {
      Query query = session.getNamedQuery("JobSession.getFirstAcquirableJob");
      query.setString("lockOwner", lockOwner);
      query.setTimestamp("now", new Date());
      query.setMaxResults(1);
      job = (Job) query.uniqueResult();

    } catch (Exception e) {
      log.error(e);
      throw new JbpmException("couldn't get acquirable jobs", e);
    }
    return job;
  }

  public List findExclusiveJobs(String lockOwner, ProcessInstance processInstance) {
    List jobs = null;
    try {
      Query query = session.getNamedQuery("JobSession.findExclusiveJobs");
      query.setString("lockOwner", lockOwner);
      query.setTimestamp("now", new Date());
      query.setParameter("processInstance", processInstance);
      jobs = query.list();

    } catch (Exception e) {
      log.error(e);
      throw new JbpmException("couldn't find exclusive jobs for thread '"+lockOwner+"' and process instance '"+processInstance+"'", e);
    }
    return jobs;
  }

  public Job getFirstDueJob(String lockOwner, Collection jobIdsToIgnore) {
    Job job = null;
    try {
      Query query = null;
      if ( (jobIdsToIgnore==null)
           || (jobIdsToIgnore.isEmpty() )
         ) {
        query = session.getNamedQuery("JobSession.getFirstDueJob");
        query.setString("lockOwner", lockOwner);
        
      } else {
        query = session.getNamedQuery("JobSession.getFirstDueJobExlcMonitoredJobs");
        query.setString("lockOwner", lockOwner);
        query.setParameterList("jobIdsToIgnore", jobIdsToIgnore);
        
      }
      query.setMaxResults(1);
      job = (Job) query.uniqueResult();

    } catch (Exception e) {
      log.error(e);
      throw new JbpmException("couldn't get acquirable jobs", e);
    }
    return job;
  }

  public void saveJob(Job job) {
    session.saveOrUpdate(job);
    if (job instanceof Timer) {
      Timer timer = (Timer) job;
      Action action = timer.getAction();
      if ( (action!=null) 
           && (! session.contains(action))
         ) {
        log.debug("cascading timer save to timer action");
        session.save(action);
      }
    }
  }

  public void reattachUnmodifiedJob(Job job) {
    session.lock(job, LockMode.NONE);
  }

  public void deleteJob(Job job) {
    log.debug("deleting "+job);
    session.delete(job);
  }

  public Job loadJob(long jobId) {
    try {
      return (Job) session.load(Job.class, new Long(jobId));
    } catch (Exception e) {
      log.error(e);
      throw new JbpmException("couldn't load job '"+jobId+"'", e);
    }
  }


  public Job getJob(long jobId) {
    try {
      return (Job) session.get(Job.class, new Long(jobId));
    } catch (Exception e) {
      log.error(e);
      throw new JbpmException("couldn't get job '"+jobId+"'", e);
    }
  }

  public void suspendJobs(Token token) {
    try {
      Query query = session.getNamedQuery("JobSession.suspendJobs");
      query.setParameter("token", token);
      query.executeUpdate();

    } catch (Exception e) {
      log.error(e);
      throw new JbpmException("couldn't suspend jobs for "+token, e);
    }
  }

  public void resumeJobs(Token token) {
    try {
      Query query = session.getNamedQuery("JobSession.resumeJobs");
      query.setParameter("token", token);
      query.executeUpdate();

    } catch (Exception e) {
      log.error(e);
      throw new JbpmException("couldn't resume jobs for "+token, e);
    }
  }

  public void cancelTimersByName(String name, Token token) {
    try {
      // the bulk delete was replaced with a query and session.deletes on 
      // the retrieved elements to prevent stale object exceptions.  
      // With a bulk delete, the hibernate session is not aware and gives a problem 
      // if a later session.delete doesn't return 1.
      Query query = session.getNamedQuery("JobSession.getTimersByName");
      query.setString("name", name);
      query.setParameter("token", token);
      List results = query.list();
      if (results!=null) {
        Iterator iter = results.iterator();
        while (iter.hasNext()) {
          Timer timer = (Timer) iter.next();
          log.debug("deleting timer "+timer+" by name "+name);
          session.delete(timer);
        }
      }

    } catch (Exception e) {
      log.error(e);
      throw new JbpmException("couldn't cancel timers '"+name+"' for '"+token+"'", e);
    }
  }

  private class DeleteJobsSynchronization implements Synchronization, Serializable {
    private static final long serialVersionUID = 1L;
    ProcessInstance processInstance;
    public DeleteJobsSynchronization(ProcessInstance processInstance) {
      this.processInstance = processInstance;
    }
    public void beforeCompletion() {
      log.debug("deleting timers for process instance "+processInstance);
      Query query = session.getNamedQuery("JobSession.deleteTimersForProcessInstance");
      query.setParameter("processInstance", processInstance);
      int result = query.executeUpdate();
      log.debug(Integer.toString(result)+" remaining timers for '"+processInstance+"' are deleted");
      
      log.debug("deleting execute-node-jobs for process instance "+processInstance);
      query = session.getNamedQuery("JobSession.deleteExecuteNodeJobsForProcessInstance");
      query.setParameter("processInstance", processInstance);
      result = query.executeUpdate();
      log.debug(Integer.toString(result)+" remaining execute-node-jobs for '"+processInstance+"' are deleted");
    }
    public void afterCompletion(int arg0) {
    }
  }

  public void deleteJobsForProcessInstance(ProcessInstance processInstance) {
    try {
      Transaction transaction = session.getTransaction();
      transaction.registerSynchronization(new DeleteJobsSynchronization(processInstance));
    } catch (Exception e) {
      log.error(e);
      throw new JbpmException("couldn't delete jobs for '"+processInstance+"'", e);
    }
  }


  public List findJobsWithOverdueLockTime(Date treshold) {
    Query query = session.getNamedQuery("JobSession.findJobsWithOverdueLockTime");
    query.setDate("now", treshold);
    return query.list();
  }

  private static Log log = LogFactory.getLog(JobSession.class);
}
