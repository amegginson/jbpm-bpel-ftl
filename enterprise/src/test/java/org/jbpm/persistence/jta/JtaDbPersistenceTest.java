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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import org.apache.cactus.ServletTestCase;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;

public class JtaDbPersistenceTest extends ServletTestCase {

  private JbpmConfiguration jbpmConfiguration;
  private UserTransaction tx;
  private boolean rollback;

  private static long definitionId;

  protected void setUp() throws Exception {
    jbpmConfiguration = JbpmConfiguration.getInstance();
    getUserTransaction();
  }

  public void testUserTx() throws Exception {
    tx = getUserTransaction();
    testServiceTx();
  }

  public void testUserTxRollback() throws Exception {
    tx = getUserTransaction();
    testServiceTxRollback();
  }

  public void testServiceTx() throws Exception {
    long definitionId = deployProcess();
    long instanceId = launchProcess(definitionId);
    signal(instanceId);
    assertTrue(hasEnded(instanceId));
  }

  public void testServiceTxRollback() throws Exception {
    rollback = true;
    long definitionId = deployProcess();
    long instanceId = launchProcess(definitionId);
    signal(instanceId);
    assertFalse(hasEnded(instanceId));
  }

  private long deployProcess() throws Exception {
    if (definitionId == 0) {
      try {
        if (tx != null) tx.begin();
        JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
        try {
          assertEquals(tx == null, isTxCreatedByService(jbpmContext));
          ProcessDefinition definition = ProcessDefinition.parseXmlString("<process-definition name='tx'>"
              + "  <start-state name='start'>"
              + "    <transition to='midway' />"
              + "  </start-state>"
              + "  <state name='midway'>"
              + "    <transition to='end' />"
              + "  </state>"
              + "  <end-state name='end' />"
              + "</process-definition>");
          jbpmContext.deployProcessDefinition(definition);
          definitionId = definition.getId();
        }
        catch (RuntimeException e) {
          if (tx == null) jbpmContext.setRollbackOnly();
          throw e;
        }
        finally {
          jbpmContext.close();
        }
        if (tx != null) tx.commit();
      }
      catch (Exception e) {
        if (tx != null) tx.rollback();
        throw e;
      }
    }
    return definitionId;
  }

  private long launchProcess(long definitionId) throws Exception {
    try {
      if (tx != null) tx.begin();
      JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
      ProcessInstance instance;
      try {
        assertEquals(tx == null, isTxCreatedByService(jbpmContext));
        ProcessDefinition definition = jbpmContext.getGraphSession().loadProcessDefinition(
            definitionId);
        instance = new ProcessInstance(definition);
        instance.signal();
        jbpmContext.save(instance);
      }
      catch (RuntimeException e) {
        if (tx == null) jbpmContext.setRollbackOnly();
        throw e;
      }
      finally {
        jbpmContext.close();
      }
      if (tx != null) tx.commit();
      return instance.getId();
    }
    catch (Exception e) {
      if (tx != null) tx.rollback();
      throw e;
    }
  }

  private void signal(long instanceId) throws Exception {
    try {
      if (tx != null) tx.begin();
      JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
      try {
        assertEquals(tx == null, isTxCreatedByService(jbpmContext));
        ProcessInstance instance = jbpmContext.loadProcessInstanceForUpdate(instanceId);
        instance.signal();
        if (rollback && tx == null) jbpmContext.setRollbackOnly();
      }
      catch (RuntimeException e) {
        if (tx == null) jbpmContext.setRollbackOnly();
        throw e;
      }
      finally {
        jbpmContext.close();
      }
      if (tx != null) {
        if (rollback) 
          tx.rollback();
        else
          tx.commit();
      }
    }
    catch (Exception e) {
      if (tx != null) tx.rollback();
      throw e;
    }
  }

  private boolean hasEnded(long instanceId) throws Exception {
    try {
      if (tx != null) tx.begin();
      JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
      ProcessInstance instance;
      try {
        assertEquals(tx == null, isTxCreatedByService(jbpmContext));
        instance = jbpmContext.loadProcessInstanceForUpdate(instanceId);
      }
      catch (RuntimeException e) {
        if (tx == null) jbpmContext.setRollbackOnly();
        throw e;
      }
      finally {
        jbpmContext.close();
      }
      if (tx != null) tx.commit();
      return instance.hasEnded();
    }
    catch (Exception e) {
      if (tx != null) tx.rollback();
      throw e;
    }
  }

  private boolean isTxCreatedByService(JbpmContext jbpmContext) {
    JtaDbPersistenceService persistenceService = (JtaDbPersistenceService) jbpmContext.getServices()
        .getPersistenceService();
    return persistenceService.isJtaTxCreated();
  }

  private static UserTransaction getUserTransaction() throws NamingException {
    Context initial = new InitialContext();
    return (UserTransaction) initial.lookup("java:comp/UserTransaction");
  }
}
