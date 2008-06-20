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
package org.jbpm.taskmgmt.exe;

import java.util.HashMap;

import org.jbpm.context.exe.ContextInstance;
import org.jbpm.db.AbstractDbTestCase;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;

public class TaskVariablesDbTest extends AbstractDbTestCase {

  public void testDefaultVariablePersistence() {
    ProcessDefinition processDefinition = ProcessDefinition.createNewProcessDefinition();
    session.save(processDefinition);
    
    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    TaskInstance taskInstance = processInstance.getTaskMgmtInstance().createTaskInstance(processInstance.getRootToken());
    taskInstance.setVariable("key", "value");

    taskInstance = saveAndReload(taskInstance);
    
    assertNotNull(taskInstance);
    assertEquals("value", taskInstance.getVariable("key"));
  }
  
  public void testSetOnTaskInstanceGetOnProcess() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition>" +
      "  <start-state>" +
      "    <transition to='t' />" +
      "  </start-state>" +
      "  <task-node name='t'>" +
      "    <task name='vartask' />" +
      "  </task-node>" +
      "</process-definition>"
    );
    processDefinition = saveAndReload(processDefinition);

    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    processInstance.signal();
    
    processInstance = saveAndReload(processInstance);
    ContextInstance contextInstance = processInstance.getContextInstance();
    TaskMgmtInstance taskMgmtInstance = processInstance.getTaskMgmtInstance();
    TaskInstance taskInstance = (TaskInstance) taskMgmtInstance.getTaskInstances().iterator().next();
    
    HashMap expectedVariables = new HashMap();
    assertEquals(expectedVariables, taskInstance.getVariables());
    assertFalse(taskInstance.hasVariable("a"));
    assertNull(taskInstance.getVariable("a"));
    
    assertNull(contextInstance.getVariable("a"));
    
    taskInstance.setVariable("a", "1");
    jbpmContext.save(taskInstance);
    newTransaction();
    
    processInstance = jbpmContext.loadProcessInstance(processInstance.getId());
    contextInstance = processInstance.getContextInstance();
    taskMgmtInstance = processInstance.getTaskMgmtInstance();
    taskInstance = (TaskInstance) taskMgmtInstance.getTaskInstances().iterator().next();
    
    expectedVariables.put("a", "1");
    assertEquals(expectedVariables, taskInstance.getVariables());

    assertTrue(taskInstance.hasVariable("a"));
    assertEquals("1", taskInstance.getVariable("a"));
    assertEquals("1", contextInstance.getVariable("a"));
  }

  public void testSetOnProcessGetOnTaskInstance() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition>" +
      "  <start-state>" +
      "    <transition to='t' />" +
      "  </start-state>" +
      "  <task-node name='t'>" +
      "    <task name='vartask' />" +
      "  </task-node>" +
      "</process-definition>"
    );
    processDefinition = saveAndReload(processDefinition);
    
    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    ContextInstance contextInstance = processInstance.getContextInstance();
    TaskMgmtInstance taskMgmtInstance = processInstance.getTaskMgmtInstance();
    processInstance.signal();
    TaskInstance taskInstance = (TaskInstance) taskMgmtInstance.getTaskInstances().iterator().next();
    HashMap expectedVariables = new HashMap();
    jbpmContext.save(processInstance);
    
    newTransaction();
    
    processInstance = jbpmContext.loadProcessInstance(processInstance.getId());
    long taskInstanceId = taskInstance.getId();
    taskInstance = jbpmContext.loadTaskInstance(taskInstanceId);
    contextInstance = processInstance.getContextInstance();
    taskMgmtInstance = processInstance.getTaskMgmtInstance();
    
    contextInstance.setVariable("a", "1");
    jbpmContext.save(processInstance);
    
    newTransaction();
    
    processInstance = jbpmContext.loadProcessInstance(processInstance.getId());
    taskInstance = jbpmContext.loadTaskInstance(taskInstanceId);
    contextInstance = processInstance.getContextInstance();
    taskMgmtInstance = processInstance.getTaskMgmtInstance();
    
    expectedVariables.put("a", "1");
    assertEquals(expectedVariables, taskInstance.getVariables());

    newTransaction();
    
    processInstance = jbpmContext.loadProcessInstance(processInstance.getId());
    taskInstance = jbpmContext.loadTaskInstance(taskInstanceId);
    contextInstance = processInstance.getContextInstance();
    taskMgmtInstance = processInstance.getTaskMgmtInstance();

    assertTrue(taskInstance.hasVariable("a"));
    assertEquals("1", taskInstance.getVariable("a"));
    assertEquals("1", contextInstance.getVariable("a"));
  }

  public void testSetLocally() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition>" +
      "  <start-state>" +
      "    <transition to='t' />" +
      "  </start-state>" +
      "  <task-node name='t'>" +
      "    <task name='vartask' />" +
      "  </task-node>" +
      "</process-definition>"
    );
    processDefinition = saveAndReload(processDefinition);

    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    ContextInstance contextInstance = processInstance.getContextInstance();
    TaskMgmtInstance taskMgmtInstance = processInstance.getTaskMgmtInstance();
    processInstance.signal();
    TaskInstance taskInstance = (TaskInstance) taskMgmtInstance.getTaskInstances().iterator().next();

    jbpmContext.save(processInstance);
    
    newTransaction();
    
    processInstance = jbpmContext.loadProcessInstance(processInstance.getId());
    taskInstance = jbpmContext.loadTaskInstance(taskInstance.getId());
    contextInstance = processInstance.getContextInstance();
    taskMgmtInstance = processInstance.getTaskMgmtInstance();

    HashMap expectedVariables = new HashMap();
    assertEquals(expectedVariables, taskInstance.getVariables());
    assertFalse(taskInstance.hasVariable("a"));
    assertNull(taskInstance.getVariable("a"));
    assertNull(contextInstance.getVariable("a"));
    
    taskInstance.setVariableLocally("a", "1");
    
    jbpmContext.save(taskInstance);

    newTransaction();
    
    processInstance = jbpmContext.loadProcessInstance(processInstance.getId());
    taskInstance = jbpmContext.loadTaskInstance(taskInstance.getId());
    contextInstance = processInstance.getContextInstance();
    taskMgmtInstance = processInstance.getTaskMgmtInstance();

    expectedVariables.put("a", "1");
    assertEquals(expectedVariables, taskInstance.getVariables());

    assertTrue(taskInstance.hasVariable("a"));
    assertEquals("1", taskInstance.getVariable("a"));
    assertNull(contextInstance.getVariable("a"));
  }
  
  public void testCopyWithController() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition>" +
      "  <start-state>" +
      "    <transition to='t' />" +
      "  </start-state>" +
      "  <task-node name='t'>" +
      "    <task name='vartask'>" +
      "      <controller>" +
      "        <variable name='a' />" +
      "        <variable name='b' />" +
      "      </controller>" +
      "    </task>" +
      "  </task-node>" +
      "</process-definition>"
    );
    processDefinition = saveAndReload(processDefinition);

    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    ContextInstance contextInstance = processInstance.getContextInstance();
    TaskMgmtInstance taskMgmtInstance = processInstance.getTaskMgmtInstance();
    
    contextInstance.setVariable("a", "1");
    contextInstance.setVariable("b", "2");
    contextInstance.setVariable("c", "3");
    
    processInstance.signal();
    TaskInstance taskInstance = (TaskInstance) taskMgmtInstance.getTaskInstances().iterator().next();

    jbpmContext.save(processInstance);
    
    newTransaction();
    
    processInstance = jbpmContext.loadProcessInstance(processInstance.getId());
    taskInstance = jbpmContext.loadTaskInstance(taskInstance.getId());
    contextInstance = processInstance.getContextInstance();
    taskMgmtInstance = processInstance.getTaskMgmtInstance();

    HashMap expectedVariables = new HashMap();
    expectedVariables.put("a", "1");
    expectedVariables.put("b", "2");
    expectedVariables.put("c", "3");
    assertEquals(expectedVariables, taskInstance.getVariables());
    
    taskInstance.setVariable("a", "1 modified");
    taskInstance.setVariable("b", "2 modified");
    taskInstance.setVariable("c", "3 modified");
    
    jbpmContext.save(processInstance);

    newTransaction();
    
    processInstance = jbpmContext.loadProcessInstance(processInstance.getId());
    taskInstance = jbpmContext.loadTaskInstance(taskInstance.getId());
    contextInstance = processInstance.getContextInstance();
    taskMgmtInstance = processInstance.getTaskMgmtInstance();

    expectedVariables = new HashMap();
    expectedVariables.put("a", "1 modified");
    expectedVariables.put("b", "2 modified");
    expectedVariables.put("c", "3 modified");
    assertEquals(expectedVariables, taskInstance.getVariables());

    newTransaction();
    
    processInstance = jbpmContext.loadProcessInstance(processInstance.getId());
    taskInstance = jbpmContext.loadTaskInstance(taskInstance.getId());
    contextInstance = processInstance.getContextInstance();
    taskMgmtInstance = processInstance.getTaskMgmtInstance();

    expectedVariables = new HashMap();
    expectedVariables.put("a", "1"); // task instance had local copy for var a
    expectedVariables.put("b", "2"); // task instance had local copy for var b
    expectedVariables.put("c", "3 modified");
    assertEquals(expectedVariables, contextInstance.getVariables());
  }

  public void testOverwriteNullValue() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition>" +
      "  <start-state>" +
      "    <transition to='t' />" +
      "  </start-state>" +
      "  <task-node name='t'>" +
      "    <task name='vartask'>" +
      "      <controller>" +
      "        <variable name='v' />" +
      "      </controller>" +
      "    </task>" +
      "  </task-node>" +
      "</process-definition>"
    );
    processDefinition = saveAndReload(processDefinition);

    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    processInstance.signal();
    
    processInstance = saveAndReload(processInstance);
    TaskMgmtInstance taskMgmtInstance = processInstance.getTaskMgmtInstance();
    TaskInstance taskInstance = (TaskInstance) taskMgmtInstance.getTaskInstances().iterator().next();
    
    assertNull(taskInstance.getVariable("v"));
    taskInstance.setVariable("v", "facelets is great");
    jbpmContext.save(taskInstance);
    
    newTransaction();
    
    taskInstance = jbpmContext.loadTaskInstance(taskInstance.getId());
    assertEquals("facelets is great", taskInstance.getVariable("v"));
  }

  public void testNewTaskInstanceVariablesWithoutController() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition>" +
      "  <start-state>" +
      "    <transition to='t' />" +
      "  </start-state>" +
      "  <task-node name='t'>" +
      "    <task name='vartask'>" +
      "    </task>" +
      "    <transition to='u' />" +
      "  </task-node>" +
      "  <state name='u' />" +
      "</process-definition>"
    );
    processDefinition = saveAndReload(processDefinition);

    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    TaskMgmtInstance taskMgmtInstance = processInstance.getTaskMgmtInstance();
    processInstance.signal();

    TaskInstance taskInstance = (TaskInstance) taskMgmtInstance.getTaskInstances().iterator().next();

    taskInstance.setVariableLocally("a", "value-a");
    taskInstance.setVariableLocally("b", "value-b");
    
    jbpmContext.save(processInstance);
    
    processInstance = saveAndReload(processInstance);
    ContextInstance contextInstance = processInstance.getContextInstance();
    taskMgmtInstance = processInstance.getTaskMgmtInstance();
    taskInstance = (TaskInstance) taskMgmtInstance.getTaskInstances().iterator().next();
    
    assertFalse( contextInstance.hasVariable("a") );
    assertFalse( contextInstance.hasVariable("b") );
    
    assertEquals("value-a", taskInstance.getVariable("a"));
    assertEquals("value-b", taskInstance.getVariable("b"));
    
    taskInstance.end();
    
    assertEquals("value-a", contextInstance.getVariable("a"));
    assertEquals("value-b", contextInstance.getVariable("b"));
    
    processInstance = saveAndReload(processInstance);
    contextInstance = processInstance.getContextInstance();
    
    assertEquals("value-a", contextInstance.getVariable("a"));
    assertEquals("value-b", contextInstance.getVariable("b"));
  }
}
