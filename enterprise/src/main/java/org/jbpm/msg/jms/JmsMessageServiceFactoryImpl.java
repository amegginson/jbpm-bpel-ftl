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
package org.jbpm.msg.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jbpm.JbpmException;
import org.jbpm.ejb.impl.JobListenerBean;
import org.jbpm.svc.Service;
import org.jbpm.svc.ServiceFactory;

/**
 * The JMS message service leverages the reliable communication infrastructure
 * accessed through JMS interfaces to deliver asynchronous continuation
 * messages to the {@link JobListenerBean}.
 * 
 * <h3>Configuration</h3>
 * 
 * The JMS message service factory exposes the following configurable fields.
 * 
 * <ul>
 * <li><code>connectionFactoryJndiName</code></li>
 * <li><code>destinationJndiName</code></li>
 * <li><code>isCommitEnabled</code></li>
 * </ul>
 * 
 * Refer to the jBPM manual for details.
 * 
 * @author Tom Baeyens
 */
public class JmsMessageServiceFactoryImpl implements ServiceFactory {

  private static final long serialVersionUID = 1L;
  
  String connectionFactoryJndiName = "java:comp/env/jms/JbpmConnectionFactory";
  String destinationJndiName = "java:comp/env/jms/JobQueue";
  boolean isCommitEnabled = false;

  private ConnectionFactory connectionFactory;
  private Destination destination;

  public ConnectionFactory getConnectionFactory() {
    if (connectionFactory == null) {
      try {
        connectionFactory = (ConnectionFactory) lookup(connectionFactoryJndiName);
      }
      catch (NamingException e) {
        throw new JbpmException("could not retrieve message connection factory", e);
      }
    }
    return connectionFactory;
  }

  public Destination getDestination() {
    if (destination == null) {
      try {
        destination = (Destination) lookup(destinationJndiName);
      }
      catch (NamingException e) {
        throw new JbpmException("could not retrieve message destination", e);
      }
    }
    return destination;
  }

  private static Object lookup(String name) throws NamingException {
    Context initialContext = new InitialContext();
    try {
      return initialContext.lookup(name);
    }
    finally {
      initialContext.close();
    }
  }

  public Service openService() {
    try {
      Connection connection = getConnectionFactory().createConnection();

      // If you use an XA connection factory in JBoss, the parameters will be ignored.  It will always take part in the global JTA transaction.
      // If you use a non XQ connection factory, the first parameter specifies whether you want to have all message productions and 
      // consumptions as part of one transaction (TRUE) or whether you want all productions and consumptions to be instantanious (FALSE)
      // Of course, we never want messages to be received before the current jbpm transaction commits so we just set it to true.
      Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

      return new JmsMessageServiceImpl(connection, session, getDestination(), isCommitEnabled);
    }
    catch (JMSException e) {
      throw new JbpmException("couldn't open jms message session", e);
    }
  }

  public void close() {
    connectionFactory = null;
    destination = null;
  }

}
