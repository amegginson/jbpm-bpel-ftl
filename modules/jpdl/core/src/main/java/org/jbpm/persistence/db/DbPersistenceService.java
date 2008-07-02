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
package org.jbpm.persistence.db;

import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;
import org.jbpm.JbpmContext;
import org.jbpm.JbpmException;
import org.jbpm.db.ContextSession;
import org.jbpm.db.GraphSession;
import org.jbpm.db.JobSession;
import org.jbpm.db.LoggingSession;
import org.jbpm.db.TaskMgmtSession;
import org.jbpm.persistence.JbpmPersistenceException;
import org.jbpm.persistence.PersistenceService;
import org.jbpm.svc.Service;
import org.jbpm.svc.Services;
import org.jbpm.tx.TxService;

public class DbPersistenceService implements Service, PersistenceService {
  
  private static final long serialVersionUID = 1L;

  protected DbPersistenceServiceFactory persistenceServiceFactory = null;

  protected Connection connection = null;
  protected boolean mustConnectionBeClosed = false;

  protected Transaction transaction = null;
  protected boolean isTransactionEnabled = true;
  protected boolean isCurrentSessionEnabled = false;

  // boolean isRollbackOnly = false;

  protected Session session;
  protected boolean mustSessionBeFlushed = false;
  protected boolean mustSessionBeClosed = false;

  protected Services services = null;

  protected GraphSession graphSession = null;
  protected TaskMgmtSession taskMgmtSession = null;
  protected JobSession jobSession = null;
  protected ContextSession contextSession = null;
  protected LoggingSession loggingSession = null;

  public DbPersistenceService(DbPersistenceServiceFactory persistenceServiceFactory) {
    this(persistenceServiceFactory, getCurrentServices());
  }

  static Services getCurrentServices() {
    Services services = null;
    JbpmContext currentJbpmContext = JbpmContext.getCurrentJbpmContext();
    if (currentJbpmContext!=null) {
      services = currentJbpmContext.getServices();
    }
    return services;
  }

  DbPersistenceService(DbPersistenceServiceFactory persistenceServiceFactory, Services services) {
    this.persistenceServiceFactory = persistenceServiceFactory;
    this.isTransactionEnabled = persistenceServiceFactory.isTransactionEnabled();
    this.isCurrentSessionEnabled = persistenceServiceFactory.isCurrentSessionEnabled();
    this.services = services;
  }

  public SessionFactory getSessionFactory() {
    return session != null ? session.getSessionFactory() : persistenceServiceFactory.getSessionFactory();
  }

  public Session getSession() {
    if ( (session==null)
         && (getSessionFactory()!=null) 
       ) {
      Connection connection = getConnection(false);
      if (isCurrentSessionEnabled) {
        log.debug("using current hibernate session");
        session = getSessionFactory().getCurrentSession();
        mustSessionBeClosed = false;
        mustSessionBeFlushed = false;
        mustConnectionBeClosed = false;
      } else if (connection!=null) {
        log.debug("creating hibernate session with connection "+connection);
        session = getSessionFactory().openSession(connection);
        mustSessionBeClosed = true;
        mustSessionBeFlushed = true;
        mustConnectionBeClosed = false;
      } else {
        log.debug("creating hibernate session");
        session = getSessionFactory().openSession();
        mustSessionBeClosed = true;
        mustSessionBeFlushed = true;
        mustConnectionBeClosed = false;
      }
      
      if (isTransactionEnabled) {
        beginTransaction();
      }
    }
    return session;
  }

  public void beginTransaction() {
    log.debug("beginning hibernate transaction");
    transaction = session.beginTransaction();
    log.debug("begun hibernate transaction " + transaction.toString());
  }

  public void endTransaction() {
    if ( (isTransactionEnabled)
         && (transaction!=null) 
       ) {
      if (isRollbackOnly()) {
        try {
          log.debug("rolling back hibernate transaction " + transaction.toString());
          mustSessionBeFlushed = false; // flushing updates that will be rolled back is not very clever :-) 
          transaction.rollback();
        } catch (Exception e) {
          // NOTE that Error's are not caught because that might halt the JVM and mask the original Error.
          throw new JbpmPersistenceException("couldn't rollback hibernate session", e);
        }
      } else {
        try {
          log.debug("committing hibernate transaction " + transaction.toString());
          mustSessionBeFlushed = false; // commit does a flush anyway 
          transaction.commit();
        } catch (Exception e) {
          // NOTE that Error's are not caught because that might halt the JVM and mask the original Error.
          try {
            // if the commit fails, we must do a rollback
            transaction.rollback();
          } catch (Exception e2) {
            // if the rollback fails, we did what we could and you're in 
            // deep shit :-(
            log.error("problem rolling back after failed commit", e2);
          }
          throw new JbpmPersistenceException("couldn't commit hibernate session", e);
        }
      }
    }
  }

  public Connection getConnection() {
    return getConnection(true);
  }

  public Connection getConnection(boolean resolveSession) {
    if (connection==null) {
      if (persistenceServiceFactory.getDataSource()!=null) { 
        try {
          log.debug("fetching jdbc connection from datasource");
          connection = persistenceServiceFactory.getDataSource().getConnection();
          mustConnectionBeClosed = true;
        } catch (Exception e) {
          // NOTE that Error's are not caught because that might halt the JVM and mask the original Error.
          throw new JbpmException("couldn't obtain connection from datasource", e);
        }
      } else {
        if (resolveSession) {
          // initializes the session member
          getSession();
        }
        if (session!=null) {
          connection = session.connection();
          log.debug("fetching connection from hibernate session. this transfers responsibility for closing the jdbc connection to the user! "+connection);
          mustConnectionBeClosed = false;
        }
      }
    }
    return connection;
  }
  
  protected boolean isTransactionActive() {
      return (isTransactionEnabled && (transaction != null)) ; 
  }
  
  public void close() {

    if ( (session!=null)
         && !isTransactionActive()
         && (isRollbackOnly())
       ) {
      throw new JbpmException("setRollbackOnly was invoked while configuration specifies user managed transactions");
    }
    
    if ( (isTransactionEnabled)
         && (transaction!=null) 
       ) {

      if (! isRollbackOnly()) {
        Exception commitException = commit();
        if (commitException!=null) {
          rollback();
          closeSession();
          closeConnection();
          throw new JbpmPersistenceException("hibernate commit failed", commitException);
        }

      } else { // isRollbackOnly==true
        Exception rollbackException = rollback();
        if (rollbackException!=null) {
          closeSession();
          closeConnection();
          throw new JbpmPersistenceException("hibernate rollback failed", rollbackException);
        }
      }
    }
    
    Exception flushException = flushSession();
    if (flushException!=null) {
      rollback();
      closeSession();
      closeConnection();
      throw new JbpmPersistenceException("hibernate flush failed", flushException);
    }

    Exception closeSessionException = closeSession();
    if (closeSessionException!=null) {
      closeConnection();
      throw new JbpmPersistenceException("hibernate close session failed", closeSessionException);
    }

    Exception closeConnectionException = closeConnection();
    if (closeConnectionException!=null) {
      throw new JbpmPersistenceException("hibernate close connection failed", closeConnectionException);
    }
  }

  Exception commit() {
    try {
      log.debug("committing hibernate transaction " + transaction.toString());
      mustSessionBeFlushed = false; // commit does a flush anyway 
      transaction.commit();
    } catch (StaleObjectStateException e) {
      log.info("optimistic locking failed");
      StaleObjectLogConfigurer.staleObjectExceptionsLog.error("optimistic locking failed", e);
      return e;
    } catch (Exception e) {
      log.error("hibernate commit failed", e);
      return e;
    }
    return null;
  }

  Exception flushSession() {
    if (mustSessionBeFlushed) {
      try {
        log.debug("flushing hibernate session " + session.toString());
        session.flush();
      } catch (Exception e) {
        log.error("hibernate flush failed", e);
        return e;
      }
    }
    return null;
  }

  Exception closeConnection() {
    if (mustConnectionBeClosed) {
      try {
        if ( (connection!=null)
            && (! connection.isClosed())
           ) {
          log.debug("closing jdbc connection");
          connection.close();
        } else {
          log.warn("jdbc connection was already closed");
        }
      } catch (Exception e) {
        log.error("hibernate session close failed", e);
        return e;
      }
    }
    return null;
  }

  Exception rollback() {
    try {
      log.debug("rolling back hibernate transaction");
      mustSessionBeFlushed = false; // flushing updates that will be rolled back is not very clever :-) 
      transaction.rollback();
    } catch (Exception e) {
      log.error("hibernate rollback failed", e);
      return e;
    }
    return null;
  }

  Exception closeSession() {
    if (mustSessionBeClosed) {
      try {
        if(session.isOpen()) {
          log.debug("closing hibernate session");
          session.close();
        } else {
          log.warn("hibernate session was already closed");
        }
      } catch (Exception e) {
        return e;
      }
    }
    return null;
  }

  public void assignId(Object object) {
    try {
      getSession().save(object);
    } catch (Exception e) {
      // NOTE that Error's are not caught because that might halt the JVM and mask the original Error.
      throw new JbpmPersistenceException("couldn't assign id to "+object, e);
    }
  }

  // getters and setters //////////////////////////////////////////////////////

  public GraphSession getGraphSession() {
    if (graphSession==null) {
      Session session = getSession();
      if (session!=null) {
        graphSession = new GraphSession(session);
      }
    }
    return graphSession;
  }
  public LoggingSession getLoggingSession() {
    if (loggingSession==null) {
      Session session = getSession();
      if (session!=null) {
        loggingSession = new LoggingSession(session);
      }
    }
    return loggingSession;
  }
  public JobSession getJobSession() {
    if (jobSession==null) {
      Session session = getSession();
      if (session!=null) {
        jobSession = new JobSession(session);
      }
    }
    return jobSession;
  }
  public ContextSession getContextSession() {
    if (contextSession==null) {
      Session session = getSession();
      if (session!=null) {
        contextSession = new ContextSession(session);
      }
    }
    return contextSession;
  }
  public TaskMgmtSession getTaskMgmtSession() {
    if (taskMgmtSession==null) {
      Session session = getSession();
      if (session!=null) {
        taskMgmtSession = new TaskMgmtSession(session);
      }
    }
    return taskMgmtSession;
  }

  public DataSource getDataSource() {
    return persistenceServiceFactory.dataSource;
  }

  /**
   * @deprecated use {@link org.jbpm.tx.TxService} instead.
   */
  public boolean isRollbackOnly() {
    TxService txService = (services!=null ? services.getTxService() : null);
    if (txService==null) {
      throw new JbpmException("no jbpm tx service configured");
    }
    return txService.isRollbackOnly();
  }
  /**
   * @deprecated use {@link org.jbpm.tx.TxService} instead.
   */
  public void setRollbackOnly(boolean isRollbackOnly) {
    throw new UnsupportedOperationException("method setRollbackOnly has been removed.  Use TxService instead.");
  }
  /**
   * @deprecated use {@link org.jbpm.tx.TxService} instead.
   */
  public void setRollbackOnly() {
    TxService txService = (services!=null ? services.getTxService() : null);
    if (txService==null) {
      throw new JbpmException("no jbpm tx service configured");
    }
    txService.setRollbackOnly();
  }

  public void setSession(Session session) {
    this.session = session;
    log.debug("injecting a session disables transaction");
    isTransactionEnabled = false;
  }

  public void setSessionWithoutDisablingTx(Session session) {
    this.session = session;
  }

  public void setConnection(Connection connection) {
    this.connection = connection;
  }
  public void setContextSession(ContextSession contextSession) {
    this.contextSession = contextSession;
  }
  public void setDataSource(DataSource dataSource) {
    this.persistenceServiceFactory.dataSource = dataSource;
  }
  public void setGraphSession(GraphSession graphSession) {
    this.graphSession = graphSession;
  }
  public void setLoggingSession(LoggingSession loggingSession) {
    this.loggingSession = loggingSession;
  }
  public void setJobSession(JobSession jobSession) {
    this.jobSession = jobSession;
  }
  public void setTaskMgmtSession(TaskMgmtSession taskMgmtSession) {
    this.taskMgmtSession = taskMgmtSession;
  }
  public void setSessionFactory(SessionFactory sessionFactory) {
    this.persistenceServiceFactory.sessionFactory = sessionFactory;
  }
  public Transaction getTransaction() {
    return transaction;
  }
  public void setTransaction(Transaction transaction) {
    this.transaction = transaction;
  }
  public boolean isTransactionEnabled() {
    return isTransactionEnabled;
  }
  public void setTransactionEnabled(boolean isTransactionEnabled) {
    this.isTransactionEnabled = isTransactionEnabled;
  }
  private static Log log = LogFactory.getLog(DbPersistenceService.class);
}
