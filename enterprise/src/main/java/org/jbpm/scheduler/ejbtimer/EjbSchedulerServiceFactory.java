package org.jbpm.scheduler.ejbtimer;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jbpm.JbpmException;
import org.jbpm.svc.Service;
import org.jbpm.svc.ServiceFactory;

/**
 * @author Tom Baeyens
 * @deprecated replaced by {@link EntitySchedulerServiceFactory}
 */
public class EjbSchedulerServiceFactory implements ServiceFactory {

  private static final long serialVersionUID = 1L;

  String timerServiceHomeJndiName = "java:comp/env/ejb/LocalTimerServiceBean";

  private LocalTimerServiceHome timerServiceHome;

  public LocalTimerServiceHome getTimerServiceHome() {
    if (timerServiceHome == null) {
      try {
        timerServiceHome = (LocalTimerServiceHome) new InitialContext().lookup(timerServiceHomeJndiName);
      } catch (NamingException e) {
        throw new JbpmException("ejb timer service lookup problem", e);
      }
    }
    return timerServiceHome;
  }

  public Service openService() {
    return new EjbSchedulerService(getTimerServiceHome());
  }

  public void close() {
    timerServiceHome = null;
  }
}
