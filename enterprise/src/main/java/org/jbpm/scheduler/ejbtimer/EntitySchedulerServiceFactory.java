package org.jbpm.scheduler.ejbtimer;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jbpm.JbpmException;
import org.jbpm.ejb.LocalTimerEntityHome;
import org.jbpm.job.Timer;
import org.jbpm.svc.Service;
import org.jbpm.svc.ServiceFactory;

/**
 * The entity scheduler service builds on the transactional notification 
 * service for timed events provided by the EJB container to schedule business
 * process {@linkplain Timer timers}.
 * 
 * <h3>Configuration</h3>
 * 
 * The EJB scheduler service factory has the configurable field described below.
 * 
 * <ul>
 * <li>timerEntityHomeJndiName</li>
 * </ul>
 * 
 * Refer to the jBPM manual for details.
 *
 * @author Tom Baeyens
 * @author Fady Matar
 */
public class EntitySchedulerServiceFactory implements ServiceFactory {

  private static final long serialVersionUID = 1L;

  String timerEntityHomeJndiName = "java:comp/env/ejb/LocalTimerEntityBean";

  private LocalTimerEntityHome timerEntityHome;

  public LocalTimerEntityHome getTimerEntityHome() {
    if (timerEntityHome == null) {
      try {
        timerEntityHome = (LocalTimerEntityHome) new InitialContext().lookup(timerEntityHomeJndiName);
      } catch (NamingException e) {
        throw new JbpmException("ejb timer entity lookup problem", e);
      }
    }
    return timerEntityHome;
  }

  public Service openService() {
    return new EntitySchedulerService(getTimerEntityHome());
  }

  public void close() {
    timerEntityHome = null;
  }
}
