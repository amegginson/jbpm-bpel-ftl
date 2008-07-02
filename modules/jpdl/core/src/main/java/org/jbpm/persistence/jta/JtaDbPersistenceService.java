/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jbpm.persistence.jta;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.util.JTAHelper;
import org.jbpm.JbpmException;
import org.jbpm.persistence.db.DbPersistenceService;
import org.jbpm.persistence.db.DbPersistenceServiceFactory;

public class JtaDbPersistenceService extends DbPersistenceService {

  private static final long serialVersionUID = 1L;
  
  private static Log log = LogFactory.getLog(JtaDbPersistenceService.class);  
  
  private UserTransaction userTransaction;

  public JtaDbPersistenceService(DbPersistenceServiceFactory persistenceServiceFactory) {
    super(persistenceServiceFactory);
    
    if (!isJtaTransactionInProgress()) {
      beginJtaTransaction();
    }
  }

  protected boolean isTransactionActive() {
    return isJtaTxCreated(); 
  }
  
  public void close() {
    super.close();

    if (userTransaction != null) {
      endJtaTransaction();
    }
  }

  boolean isJtaTransactionInProgress() {
    SessionFactoryImplementor sessionFactory = (SessionFactoryImplementor) getSessionFactory();
    return JTAHelper.isTransactionInProgress(sessionFactory);
  }

  void beginJtaTransaction() {
    try {
    	log.debug("start user JTA transaction");
    	userTransaction = getUserTransaction();
      userTransaction.begin();
    } catch (Exception e) {
      throw new JbpmException("couldn't start JTA transaction", e);
    }
  }

  void endJtaTransaction() {
    if (isRollbackOnly() || JTAHelper.isRollback(getJtaTransactionStatus())) {
      log.debug("end jta transation with ROLLBACK");
      try {
        userTransaction.rollback();
      } catch (Exception e) {
        throw new JbpmException("couldn't rollback JTA transaction", e);
      }
    } else {
      log.debug("end jta transation with COMMIT");
      try {
        userTransaction.commit();
      } catch (Exception e) {
        throw new JbpmException("couldn't commit JTA transaction", e);
      }
    }
  }

  UserTransaction getUserTransaction() {
    String jndiName = persistenceServiceFactory.getConfiguration().getProperty("jta.UserTransaction");
    if (jndiName == null) {
      /*
       * EJB 2.1 section 20.9 The container must make the UserTransaction interface available to the
       * enterprise beans that are allowed to use this interface (only session and message-
       * driven beans with bean-managed transaction demarcation are allowed to use this 
       * interface) in JNDI under the name java:comp/UserTransaction.
       * J2EE 1.4 section 4.2.1.1 The J2EE platform must provide an object implementing the
       * UserTransaction interface to all web components. The platform must publish the 
       * UserTransaction object in JNDI under the name java:comp/UserTransaction.
       */
      jndiName = "java:comp/UserTransaction";
    }
    try {
      return (UserTransaction) new InitialContext().lookup(jndiName);
    } catch (NamingException e) {
      throw new JbpmException("couldn't lookup UserTransaction in JNDI with name "+jndiName, e);
    }
  }

  int getJtaTransactionStatus() {
    try {
      return userTransaction.getStatus();
    } catch (SystemException e) {
      throw new JbpmException("couldn't get status for user transaction", e); 
    }
  }

  public boolean isJtaTxCreated() {
    return userTransaction != null;
  }
}
