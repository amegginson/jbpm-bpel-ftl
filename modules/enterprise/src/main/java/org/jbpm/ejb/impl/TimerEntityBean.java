package org.jbpm.ejb.impl;

import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.TimedObject;
import javax.ejb.TimerService;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.ejb.LocalCommandService;
import org.jbpm.ejb.LocalCommandServiceHome;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.job.Timer;
import org.jbpm.scheduler.ejbtimer.ExecuteTimerCommand;
import org.jbpm.scheduler.ejbtimer.TimerInfo;

/**
 * Entity bean that interacts with the EJB timer service to schedule jBPM 
 * {@linkplain Timer timers}.
 * 
 * <h3>Environment</h3>
 * 
 * <p>The environment entries and resources available for customization are
 * summarized in the table below.</p>
 * 
 * <table border="1">
 * <tr>
 * <th>Name</th>
 * <th>Type</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td><code>ejb/LocalCommandServiceBean</code></td>
 * <td>EJB Reference</td>
 * <td>Link to the local {@linkplain CommandServiceBean session bean} that
 * executes timers on a separate jBPM context.
 * </td>
 * </tr>
 * </table>
 * 
 * @author Tom Baeyens
 * @author Alejandro Guizar
 * @author Fady Matar
 */
public abstract class TimerEntityBean implements EntityBean, TimedObject {

	private EntityContext entityContext;
  private LocalCommandService commandService;

  private static final Log log = LogFactory.getLog(TimerEntityBean.class);

	public abstract Long getTimerId();

	public abstract void setTimerId(Long timerId);

	public abstract String getName();

	public abstract void setName(String name);

	public abstract Long getTokenId();

	public abstract void setTokenId(Long tokenId);

	public abstract Long getProcessInstanceId();

	public abstract void setProcessInstanceId(Long processInstanceId);

	public void ejbActivate() {
    try {
      Context initial = new InitialContext();
      LocalCommandServiceHome commandServiceHome = (LocalCommandServiceHome) initial
          .lookup("java:comp/env/ejb/LocalCommandServiceBean");
      commandService = commandServiceHome.create();
    }
    catch (NamingException e) {
      throw new EJBException("failed to retrieve command service home", e);
    }
    catch (CreateException e) {
      throw new EJBException("command service creation failed", e);
    }
	}

	public void ejbPassivate() {
	  commandService = null;
	}

	public void ejbRemove() {
	  commandService = null;
	}

	public void ejbLoad() {}

	public void ejbStore() {}

	public void setEntityContext(EntityContext entityContext) {
		this.entityContext = entityContext;
	}

	public void unsetEntityContext() {
		entityContext = null;
	}

	/**
	 * No ejbCreate operation is allowed. One approach of ensuring that an EJB is
	 * set as read-only.
	 * 
	 * @throws CreateException
	 */
	public Long ejbCreate() throws CreateException {
		throw new CreateException("direct creation of timer entities is prohibited");
	}

	public void ejbPostCreate() {}

	public void ejbTimeout(javax.ejb.Timer ejbTimer) {
		log.debug("ejb timer " + ejbTimer + " fires");
		TimerInfo timerInfo = (TimerInfo) ejbTimer.getInfo();
		Timer timer = (Timer) commandService.execute(new ExecuteTimerCommand(timerInfo.getTimerId()));
		// if the timer has repeat
		if(timer != null && timer.getRepeat() != null) {
			// create a new timer
			log.debug("scheduling timer for repeat at " + timer.getDueDate());
			createTimer(timer);
		}
	}

	public void createTimer(org.jbpm.job.Timer timer) {
		log.debug("Creating timer " + timer + " in the ejb timer service");
		TimerService timerService = entityContext.getTimerService();
		timerService.createTimer(timer.getDueDate(), new TimerInfo(timer));
	}

	public void cancelTimer() {
		log.debug("Cancelling timer: " + this.getName());
		Collection timers = entityContext.getTimerService().getTimers();
		for (Iterator i = timers.iterator(); i.hasNext();) {
			javax.ejb.Timer ejbTimer = (javax.ejb.Timer) i.next();
			ejbTimer.cancel();
		}
	}

	public void cancelTimersByName(String timerName, Token token) {
		log.debug("cancelling timers with name " + timerName
				+ " from the ejb timer service");
		Collection timers = entityContext.getTimerService().getTimers();
		for (Iterator i = timers.iterator(); i.hasNext();) {
			javax.ejb.Timer ejbTimer = (javax.ejb.Timer) i.next();
			TimerInfo timerInfo = (TimerInfo) ejbTimer.getInfo();
			if(timerInfo.matchesName(timerName, token)) {
				ejbTimer.cancel();
			}
		}
	}

	public void deleteTimersForProcessInstance(ProcessInstance processInstance) {
		log.debug("deleting timers for process instance " + processInstance
				+ " from the ejb timer service");
		Collection timers = entityContext.getTimerService().getTimers();
		for (Iterator i = timers.iterator(); i.hasNext();) {
			javax.ejb.Timer ejbTimer = (javax.ejb.Timer) i.next();
			TimerInfo timerInfo = (TimerInfo) ejbTimer.getInfo();
			if(timerInfo.matchesProcessInstance(processInstance)) {
				ejbTimer.cancel();
			}
		}
	}

}
