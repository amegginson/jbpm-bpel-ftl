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
package org.jbpm.examples.websale;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.identity.Entity;
import org.jbpm.identity.hibernate.IdentitySession;
import org.jbpm.identity.xml.IdentityXmlParser;
import org.jbpm.persistence.db.DbPersistenceServiceFactory;
import org.jbpm.svc.Services;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.jbpm.taskmgmt.exe.TaskMgmtInstance;

public class WebsaleTest extends TestCase {
  
  JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance();
  DbPersistenceServiceFactory dbPersistenceServiceFactory = (DbPersistenceServiceFactory) jbpmConfiguration.getServiceFactory(Services.SERVICENAME_PERSISTENCE);

  JbpmContext jbpmContext;

  ProcessInstance processInstance = null;
  ContextInstance contextInstance = null;
  TaskMgmtInstance taskMgmtInstance = null;
  long processInstanceId = -1;

  public void setUp() {
    dbPersistenceServiceFactory.createSchema();
    deployProcess();
    jbpmContext = jbpmConfiguration.createJbpmContext();
  }

  public void tearDown() {
    jbpmContext.close();
    dbPersistenceServiceFactory.dropSchema();
    jbpmContext = null;
  }

  public void newTransaction() {
    jbpmContext.close();
    jbpmContext = jbpmConfiguration.createJbpmContext();
  }
  
  public void deployProcess() {
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      ProcessDefinition processDefinition = 
        ProcessDefinition.parseXmlResource("processdefinition.xml");
      jbpmContext.deployProcessDefinition(processDefinition);
    } finally {
      jbpmContext.close();
    }
  }

  public void loadIdentities() {
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      Entity[] entities = IdentityXmlParser.parseEntitiesResource("hsqldb/identity.db.xml");
      IdentitySession identitySession = new IdentitySession(jbpmContext.getSession());
      for (int i=0; i<entities.length; i++) {
        identitySession.saveEntity(entities[i]);
      }
    } finally {
      jbpmContext.close();
    }
  }

  public TaskInstance createNewProcessInstance() {
    processInstance = jbpmContext.newProcessInstanceForUpdate("websale");
    processInstanceId = processInstance.getId();
    contextInstance = processInstance.getContextInstance();
    taskMgmtInstance = processInstance.getTaskMgmtInstance();
    return processInstance.getTaskMgmtInstance().createStartTaskInstance();
  }
  
  public void reloadProcessInstance() {
    processInstance = jbpmContext.loadProcessInstanceForUpdate(processInstanceId);
    contextInstance = processInstance.getContextInstance();
    taskMgmtInstance = processInstance.getTaskMgmtInstance();
  }
  
  public void testWebSaleOrderTaskParameters() {
    TaskInstance taskInstance = createNewProcessInstance();
    assertEquals("Create new web sale order", taskInstance.getName());
    assertEquals(0, taskInstance.getVariables().size());
  }

  public void testPerformWebSaleOrderTask() {
    jbpmContext.setActorId("user");
    // create a task to start the websale process
    TaskInstance taskInstance = createNewProcessInstance();

    Map taskVariables = new HashMap();
    taskVariables.put("item", "cookies");
    taskVariables.put("quantity", "lots of them");
    taskVariables.put("address", "46 Main St.");
    
    taskInstance.addVariables(taskVariables);
    taskInstance.end();
    
    assertEquals("cookies", contextInstance.getVariable("item"));
    assertEquals("lots of them", contextInstance.getVariable("quantity"));
    assertEquals("46 Main St.", contextInstance.getVariable("address"));
    assertEquals("user", taskMgmtInstance.getSwimlaneInstance("buyer").getActorId());
  }

  public void testEvaluateAssignment() {
    jbpmContext.setActorId("user");
    // create a task to start the websale process
    TaskInstance taskInstance = createNewProcessInstance();
    taskInstance.setVariable("item", "cookies");
    taskInstance.end();
    jbpmContext.save(processInstance);
    processInstanceId = processInstance.getId();
    
    newTransaction();
    
    List sampleManagersTasks = jbpmContext.getTaskMgmtSession().findTaskInstances("manager");
    assertEquals(1, sampleManagersTasks.size());

    TaskInstance evaluateTaskInstance = (TaskInstance) sampleManagersTasks.get(0);
    assertEquals("manager", evaluateTaskInstance.getActorId());
    assertEquals("Evaluate web order", evaluateTaskInstance.getName());
    assertNotNull(evaluateTaskInstance.getToken());
    assertNotNull(evaluateTaskInstance.getCreate());
    assertNull(evaluateTaskInstance.getStart());
    assertNull(evaluateTaskInstance.getEnd());
  }

  public void testEvaluateOk() {
    TaskInstance taskInstance = createNewProcessInstance();
    taskInstance.end();
    jbpmContext.save(processInstance);
    
    newTransaction();
    
    TaskInstance evaluateTaskInstance = (TaskInstance) jbpmContext.getTaskMgmtSession().findTaskInstances("manager").get(0);
    evaluateTaskInstance.end("OK");
    jbpmContext.save(evaluateTaskInstance);

    newTransaction();
    
    List sampleShippersTasks = jbpmContext.getTaskMgmtSession().findTaskInstances("shipper");
    assertEquals(1, sampleShippersTasks.size());

    TaskInstance waitForMoneyTaskInstance = (TaskInstance) sampleShippersTasks.get(0);
    assertEquals("shipper", waitForMoneyTaskInstance.getActorId());
    assertEquals("Wait for money", waitForMoneyTaskInstance.getName());
    assertNotNull(waitForMoneyTaskInstance.getToken());
    assertNotNull(waitForMoneyTaskInstance.getCreate());
    assertNull(waitForMoneyTaskInstance.getStart());
    assertNull(waitForMoneyTaskInstance.getEnd());
  }

  public void testUnwritableVariableException() {
    testEvaluateAssignment();
    newTransaction();
    List sampleManagersTasks = jbpmContext.getTaskMgmtSession().findTaskInstances("manager");
    TaskInstance evaluateTaskInstance = (TaskInstance) sampleManagersTasks.get(0);
    evaluateTaskInstance.end();

    newTransaction();
    
    processInstance = jbpmContext.getGraphSession().loadProcessInstance(processInstanceId);
    contextInstance = processInstance.getContextInstance();
    // so the cookies should still be in the item process variable.
    assertEquals("cookies", contextInstance.getVariable("item"));
  }

  public void testEvaluateNok() {
    testEvaluateAssignment();
    newTransaction();
    
    List sampleManagersTasks = jbpmContext.getTaskMgmtSession().findTaskInstances("manager");
    TaskInstance evaluateTaskInstance = (TaskInstance) sampleManagersTasks.get(0);
    evaluateTaskInstance.setVariable("comment", "wtf");
    evaluateTaskInstance.end("More info needed");
    jbpmContext.save(evaluateTaskInstance);
    
    newTransaction();

    List sampleUserTasks = jbpmContext.getTaskMgmtSession().findTaskInstances("user");
    assertEquals(1, sampleUserTasks.size());
    TaskInstance fixWebOrderDataTaskInstance = (TaskInstance) sampleUserTasks.get(0);
    assertEquals("user", fixWebOrderDataTaskInstance.getActorId());
    assertEquals("wtf", fixWebOrderDataTaskInstance.getVariable("comment"));
  }

  public void testMoreInfoNeeded() {
    jbpmContext.setActorId("user");
      
    // create a task to start the websale process
    TaskInstance taskInstance = createNewProcessInstance();
    taskInstance.end();
    jbpmContext.save(processInstance);
    
    newTransaction();
    
    TaskInstance evaluateTaskInstance = (TaskInstance) jbpmContext.getTaskMgmtSession().findTaskInstances("manager").get(0);
    evaluateTaskInstance.end("More info needed");
    jbpmContext.save(evaluateTaskInstance);

    newTransaction();
    
    List sampleUserTasks = jbpmContext.getTaskMgmtSession().findTaskInstances("user");
    assertEquals(1, sampleUserTasks.size());

    TaskInstance fixWebOrderDataTaskInstance = (TaskInstance) sampleUserTasks.get(0);
    assertEquals("user", fixWebOrderDataTaskInstance.getActorId());
    assertEquals("Fix web order data", fixWebOrderDataTaskInstance.getName());
    assertNotNull(fixWebOrderDataTaskInstance.getToken());
    assertNotNull(fixWebOrderDataTaskInstance.getCreate());
    assertNull(fixWebOrderDataTaskInstance.getStart());
    assertNull(fixWebOrderDataTaskInstance.getEnd());
  }
}
