package org.jbpm.scheduler.ejbtimer;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmException;
import org.jbpm.svc.Service;
import org.jbpm.svc.ServiceFactory;

public class EjbSchedulerServiceFactory implements ServiceFactory {

  private static final long serialVersionUID = 1L;

  private static final Log log = LogFactory.getLog(EjbSchedulerServiceFactory.class);

  String timerServiceHomeJndiName = "java:comp/env/ejb/LocalTimerServiceBean";

  private LocalTimerServiceHome timerServiceHome;

  public EjbSchedulerServiceFactory() {
    try {
      Context initial = new InitialContext();
      timerServiceHome = (LocalTimerServiceHome) initial.lookup(timerServiceHomeJndiName);
    } catch (NamingException e) {
      log.error("ejb timer service lookup problem", e);
      throw new JbpmException("ejb timer service lookup problem", e);
    }
  }

  public Service openService() {
    return new EjbSchedulerService(timerServiceHome);
  }

  public void close() {
    timerServiceHome = null;
  }
}
