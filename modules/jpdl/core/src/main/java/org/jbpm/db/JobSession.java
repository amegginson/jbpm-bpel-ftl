package org.jbpm.db;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbpm.JbpmContext;
import org.jbpm.JbpmException;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.job.ExecuteNodeJob;
import org.jbpm.job.Job;
import org.jbpm.job.Timer;
import org.jbpm.svc.save.SaveOperation;

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

  private static class DeleteJobsOperation implements SaveOperation {

    private ProcessInstance targetProcessInstance;

    private static final long serialVersionUID = 1L;

    DeleteJobsOperation(ProcessInstance processInstance) {
      targetProcessInstance = processInstance;
    }

    public void save(ProcessInstance processInstance, JbpmContext jbpmContext) {
      // avoid deleting jobs for process instances that did not request job deletion
      if (!targetProcessInstance.equals(processInstance)) {
        log.debug("forgiving " + processInstance + ", it isn't the target of this operation");
        return;
      }

      // the bulk delete was replaced with a query and session.deletes on 
      // the retrieved elements to prevent stale object exceptions.  
      // With a bulk delete, the hibernate session is not aware and gives a problem 
      // if a later session.delete doesn't return 1.
      log.debug("deleting timers for process instance "+processInstance);
      Session session = jbpmContext.getSession();
      Query query = session.getNamedQuery("JobSession.getTimersForProcessInstance");
      query.setParameter("processInstance", processInstance);
      List timers = query.list();
      for (Iterator i = timers.iterator(); i.hasNext();) {
        Timer timer = (Timer) i.next();
        session.delete(timer);
      }
      log.debug(timers.size()+" remaining timers for '"+processInstance+"' were deleted");

      log.debug("deleting execute-node-jobs for process instance "+processInstance);
      query = session.getNamedQuery("JobSession.getExecuteNodeJobsForProcessInstance");
      query.setParameter("processInstance", processInstance);
      List jobs = query.list();
      for (Iterator i = jobs.iterator(); i.hasNext();) {
        ExecuteNodeJob job = (ExecuteNodeJob) i.next();
        session.delete(job);
      }
      log.debug(jobs.size()+" remaining execute-node-jobs for '"+processInstance+"' are deleted");
    }
    
  }

  public void deleteJobsForProcessInstance(ProcessInstance processInstance) {
    SaveOperation operation = new DeleteJobsOperation(processInstance);
    JbpmContext.getCurrentJbpmContext().getServices().addSaveOperation(operation);
  }


  public List findJobsWithOverdueLockTime(Date treshold) {
    Query query = session.getNamedQuery("JobSession.findJobsWithOverdueLockTime");
    query.setDate("now", treshold);
    return query.list();
  }

  private static Log log = LogFactory.getLog(JobSession.class);
}
