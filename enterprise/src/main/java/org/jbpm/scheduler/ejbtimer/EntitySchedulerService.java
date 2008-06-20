package org.jbpm.scheduler.ejbtimer;

import java.util.Collection;
import java.util.Iterator;

import javax.ejb.FinderException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmContext;
import org.jbpm.JbpmException;
import org.jbpm.db.JobSession;
import org.jbpm.ejb.LocalTimerEntity;
import org.jbpm.ejb.LocalTimerEntityHome;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.job.Timer;
import org.jbpm.scheduler.SchedulerService;

public class EntitySchedulerService implements SchedulerService {

	private static final long serialVersionUID = 1L;

	JobSession jobSession;
	LocalTimerEntityHome timerEntityHome;

	public EntitySchedulerService(LocalTimerEntityHome timerEntityHome) {
		JbpmContext jbpmContext = JbpmContext.getCurrentJbpmContext();
		if(jbpmContext == null) {
			throw new JbpmException(
					"instantiation of the EjbSchedulerService requires a current JbpmContext");
		}
		this.jobSession = jbpmContext.getJobSession();
		this.timerEntityHome = timerEntityHome;
	}

	public void createTimer(Timer timer) {
		log.debug("creating timer " + timer);
		jobSession.saveJob(timer);
		try {
			LocalTimerEntity timerEntity = timerEntityHome.findByPrimaryKey(new Long(timer.getId()));
			timerEntity.createTimer(timer);
		}
		catch (FinderException e) {
			log.error("failed to retrieve entity for timer " + timer, e);
		}
	}

	public void deleteTimersByName(String timerName, Token token) {
		log.debug("deleting timers by name " + timerName);
		Collection timerEntities = null;
		try {
			if(timerName == null || timerName.equals("")) {
				timerEntities = timerEntityHome.findByTokenId(new Long(token.getId()));
			}
			else {
				timerEntities = timerEntityHome.findByTokenIdAndName(new Long(token.getId()), timerName);
			}
			for (Iterator i = timerEntities.iterator(); i.hasNext();) {
				LocalTimerEntity timerEntity = (LocalTimerEntity) i.next();
				timerEntity.cancelTimersByName(timerName, token);
			}
		}
		catch (FinderException e) {
			log.error("failed to retrieve timer entities for name " + timerName + " and token " + token, e);
		}
		jobSession.cancelTimersByName(timerName, token);
	}

	public void deleteTimersByProcessInstance(ProcessInstance processInstance) {
		log.debug("deleting timers for process instance " + processInstance);
		try {
			Collection timerEntities = timerEntityHome.findByProcessInstanceId(new Long(processInstance.getId()));
			for (Iterator i = timerEntities.iterator(); i.hasNext();) {
				LocalTimerEntity timerEntity = (LocalTimerEntity) i.next();
				timerEntity.deleteTimersForProcessInstance(processInstance);
			}
		}
		catch (FinderException e) {
			log.error("failed to retrieve timer entities for process instance " + processInstance, e);
		}
		jobSession.deleteJobsForProcessInstance(processInstance);
	}

	public void close() {
	  timerEntityHome = null;
	}

	private static Log log = LogFactory.getLog(EntitySchedulerService.class);
}
