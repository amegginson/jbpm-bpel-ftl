package org.jbpm.scheduler.ejbtimer;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

  private static final Log log = LogFactory.getLog(EntitySchedulerServiceFactory.class);

  String timerEntityHomeJndiName = "java:comp/env/ejb/LocalTimerEntityBean";

  private LocalTimerEntityHome timerEntityHome;

  public EntitySchedulerServiceFactory() {
    try {
      Context initial = new InitialContext();
      timerEntityHome = (LocalTimerEntityHome) initial.lookup(timerEntityHomeJndiName);
    } catch (NamingException e) {
      log.error("ejb timer entity lookup problem", e);
      throw new JbpmException("ejb timer entity lookup problem", e);
    }
  }

  public Service openService() {
    return new EntitySchedulerService(timerEntityHome);
  }

  public void close() {
    timerEntityHome = null;
  }
}
