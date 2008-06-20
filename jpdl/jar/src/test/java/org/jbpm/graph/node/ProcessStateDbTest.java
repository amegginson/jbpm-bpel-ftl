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
package org.jbpm.graph.node;

import java.util.List;

import org.jbpm.JbpmException;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.db.AbstractDbTestCase;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.graph.log.ProcessStateLog;

public class ProcessStateDbTest extends AbstractDbTestCase {

  public void testProcessStateName() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition>" +
      "  <process-state name='subprocess' />" +
      "</process-definition>");
  
    processDefinition = saveAndReload(processDefinition);
  
    ProcessState processState = (ProcessState) processDefinition.getNode("subprocess");
    assertEquals("subprocess", processState.getName());
  }

  public void testProcessStateSubProcessDefinition() {
    // create the subprocess
    ProcessDefinition subProcessDefinition = new ProcessDefinition("sub");
    // store the subprocess in the database
    graphSession.saveProcessDefinition(subProcessDefinition);

    // create the super process
    ProcessDefinition superProcessDefinition = ProcessDefinition.parseXmlString(
      "<process-definition name='super'>" +
      "  <process-state name='subprocess' />" +
      "</process-definition>");
    // resolve the reference to the subprocess
    ProcessState processState = (ProcessState) superProcessDefinition.getNode("subprocess");
    processState.setSubProcessDefinition(subProcessDefinition);
  
    // save and reload the superprocess
    superProcessDefinition = saveAndReload(superProcessDefinition);
  
    processState = (ProcessState) superProcessDefinition.getNode("subprocess");
    assertNotNull("sub", processState.getSubProcessDefinition().getName());
  }

  public void testProcessStateStartVariableMappings() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition>" +
      "  <process-state name='subprocess'>" +
      "    <variable name='startsuperone' access='read,write' mapped-name='startsubone' />" +
      "    <variable name='startsupertwo' access='read,write' mapped-name='startsubtwo' />" +
      "    <variable name='startsuperthree' access='read,write' mapped-name='startsubthree' />" +
      "  </process-state>" +
      "</process-definition>");
    
    processDefinition = saveAndReload(processDefinition);

    ProcessState processState = (ProcessState) processDefinition.getNode("subprocess");
    assertEquals(3, processState.variableAccesses.size() );
  }

  public void testAverageSubProcess() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition name='subprocess'>" +
      "  <start-state name='start'>" +
      "    <transition to='wait' />" +
      "  </start-state>" +
      "  <state name='wait'>" +
      "    <transition to='end' />" +
      "  </state>" +
      "  <end-state name='end' />" +
      "</process-definition>");
    jbpmContext.deployProcessDefinition(processDefinition);
    
    processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition name='superprocess'>" +
      "  <start-state name='start'>" +
      "    <transition to='sub process state' />" +
      "  </start-state>" +
      "  <process-state name='sub process state'>" +
      "    <sub-process name='subprocess' />" +
      "    <variable name='a' access='read' mapped-name='A' />" +
      "    <variable name='b' mapped-name='B' />" +
      "    <variable name='c' access='write' mapped-name='C' />" +
      "    <transition to='wait' />" +
      "  </process-state>" +
      "  <state name='wait' />" +
      "</process-definition>");
    jbpmContext.deployProcessDefinition(processDefinition);
    
    newTransaction();
    
    ProcessInstance processInstance = jbpmContext.newProcessInstanceForUpdate("superprocess");
    ContextInstance contextInstance = processInstance.getContextInstance();
    contextInstance.setVariable("a", "1");
    contextInstance.setVariable("b", "1");
    contextInstance.setVariable("c", "1");
    processInstance.signal();
    
    newTransaction();

    long processInstanceId = processInstance.getId();
    long subProcessInstanceId = processInstance.getRootToken().getSubProcessInstance().getId();

    processInstance = jbpmContext.loadProcessInstance(processInstanceId);
    assertNotNull(processInstance.getRootToken().getSubProcessInstance());
    assertEquals("sub process state", processInstance.getRootToken().getNode().getName());
    contextInstance = processInstance.getContextInstance();
    assertEquals("1", contextInstance.getVariable("a") );
    assertEquals("1", contextInstance.getVariable("b") );
    assertEquals("1", contextInstance.getVariable("c") );

    ProcessInstance subProcessInstance = jbpmContext.loadProcessInstance(subProcessInstanceId);
    assertEquals("wait", subProcessInstance.getRootToken().getNode().getName());
    ContextInstance subContextInstance = subProcessInstance.getContextInstance();
    assertEquals("1", subContextInstance.getVariable("A") );
    assertEquals("1", subContextInstance.getVariable("B") );
    assertNull(subContextInstance.getVariable("C") );
    
    subContextInstance.setVariable("A", "2");
    subContextInstance.setVariable("B", "2");
    subContextInstance.setVariable("C", "2");
    subProcessInstance.signal();

    jbpmContext.save(subProcessInstance);

    newTransaction();

    assertTrue(jbpmContext.loadProcessInstance(subProcessInstanceId).hasEnded());
    
    processInstance = jbpmContext.loadProcessInstance(processInstanceId);
    assertEquals("wait", processInstance.getRootToken().getNode().getName());
    contextInstance = processInstance.getContextInstance();
    assertEquals("1", contextInstance.getVariable("a") );
    assertEquals("2", contextInstance.getVariable("b") );
    assertEquals("2", contextInstance.getVariable("c") );
  }

  public void testSubProcessBindingByVersion() {
    ProcessDefinition processDefinition = new ProcessDefinition("the ultimate subprocess");
    jbpmContext.deployProcessDefinition(processDefinition);

    newTransaction();

    processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition>" +
      "  <process-state name='the sub process state'>" +
      "    <sub-process name='the ultimate subprocess' version='1' />" +
      "  </process-state>" +
      "</process-definition>"
    );
    processDefinition = saveAndReload(processDefinition);

    ProcessState processState = (ProcessState) processDefinition.getNode("the sub process state");
    assertEquals("the ultimate subprocess", processState.getSubProcessDefinition().getName() );
    assertEquals(1, processState.getSubProcessDefinition().getVersion() );
  }

  public void testSubProcessBindingWithLatestVersion() {
    ProcessDefinition processDefinition = new ProcessDefinition("the multiversion subprocess");
    jbpmContext.deployProcessDefinition(processDefinition);
    jbpmContext.deployProcessDefinition(processDefinition);
    jbpmContext.deployProcessDefinition(processDefinition);
    
    newTransaction();
    
    processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition>" +
      "  <process-state name='the sub process state'>" +
      "    <sub-process name='the multiversion subprocess'/>" +
      "  </process-state>" +
      "</process-definition>"
    );
    processDefinition = saveAndReload(processDefinition);

    ProcessState processState = (ProcessState) processDefinition.getNode("the sub process state");
    assertEquals("the multiversion subprocess", processState.getSubProcessDefinition().getName() );
    assertEquals(3, processState.getSubProcessDefinition().getVersion() );
  }

  public void testRecursiveProcessDefinition() {
    ProcessDefinition recursiveProcessDefinition = ProcessDefinition.parseXmlString(
      "<process-definition name='recursive process'>" +
      "  <start-state>" +
      "    <transition to='first wait' />" +
      "  </start-state>" +
      "  <state name='first wait'>" +
      "    <transition to='subprocessnode' />" +
      "    <transition name='done' to='end' />" +
      "  </state>" +
      "  <process-state name='subprocessnode'>" +
      "    <sub-process name='recursive process' />" +
      "    <transition to='end' />" +
      "  </process-state>" +
      "  <end-state name='end' />" +
      "</process-definition>" 
    );

    recursiveProcessDefinition = saveAndReload(recursiveProcessDefinition);

    ProcessInstance superProcessInstance = new ProcessInstance(recursiveProcessDefinition);
    superProcessInstance.signal();
    superProcessInstance.signal();

    superProcessInstance = saveAndReload(superProcessInstance);

    Token superToken = superProcessInstance.getRootToken();
    assertEquals("subprocessnode", superToken.getNode().getName());
    
    ProcessInstance subProcessInstance = superToken.getSubProcessInstance();
    assertNotNull(subProcessInstance);
    assertEquals("recursive process", subProcessInstance.getProcessDefinition().getName());
    Token subToken = subProcessInstance.getRootToken();

    assertEquals("first wait", subToken.getNode().getName());
    
    subProcessInstance.signal("done");

    subProcessInstance = saveAndReload(subProcessInstance);
    superProcessInstance = graphSession.loadProcessInstance(superProcessInstance.getId());

    assertTrue(subProcessInstance.hasEnded());    
    assertTrue(superProcessInstance.hasEnded());    
  }
  
  public void testSubProcessLogs() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition name='subprocess'>" +
      "  <start-state name='start'>" +
      "    <transition to='wait' />" +
      "  </start-state>" +
      "  <state name='wait'>" +
      "    <transition to='end' />" +
      "  </state>" +
      "  <end-state name='end' />" +
      "</process-definition>");
    jbpmContext.deployProcessDefinition(processDefinition);
    
    processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition name='superprocess'>" +
      "  <start-state name='start'>" +
      "    <transition to='sub process state' />" +
      "  </start-state>" +
      "  <process-state name='sub process state'>" +
      "    <sub-process name='subprocess' />" +
      "    <transition to='wait' />" +
      "  </process-state>" +
      "  <state name='wait' />" +
      "</process-definition>");
    jbpmContext.deployProcessDefinition(processDefinition);
    
    newTransaction();
    
    ProcessInstance processInstance = jbpmContext.newProcessInstanceForUpdate("superprocess");
    processInstance.signal();
    
    newTransaction();

    long subProcessInstanceId = processInstance.getRootToken().getSubProcessInstance().getId();
    ProcessInstance subProcessInstance = jbpmContext.loadProcessInstance(subProcessInstanceId);
    subProcessInstance.signal();
    jbpmContext.save(subProcessInstance);

    newTransaction();

    List processStateLogs = session.createQuery(
      "select l " +
      "from org.jbpm.graph.log.ProcessStateLog l "
    ).list();
    
    assertEquals(1, processStateLogs.size());
    ProcessStateLog processStateLog = (ProcessStateLog) processStateLogs.get(0);
    
    assertEquals(subProcessInstanceId, processStateLog.getSubProcessInstance().getId());
    assertEquals(processInstance.getId(), processStateLog.getToken().getProcessInstance().getId());
  }
  
  public void testDynamicProcessBinding() {
    ProcessDefinition processDefinition1 = ProcessDefinition.parseXmlString(
      "<process-definition name='subprocess1'>" +
      "  <start-state name='start'>" +
      "    <transition to='wait subprocess 1' />" +
      "  </start-state>" +
      "  <state name='wait subprocess 1'>" +
      "    <transition to='end' />" +
      "  </state>" +
      "  <end-state name='end' />" +
      "</process-definition>");
    jbpmContext.deployProcessDefinition(processDefinition1);
    
    ProcessDefinition processDefinition2 = ProcessDefinition.parseXmlString(
        "<process-definition name='subprocess2'>" +
        "  <start-state name='start'>" +
        "    <transition to='wait subprocess 2' />" +
        "  </start-state>" +
        "  <state name='wait subprocess 2'>" +
        "    <transition to='end' />" +
        "  </state>" +
        "  <end-state name='end' />" +
        "</process-definition>");
      jbpmContext.deployProcessDefinition(processDefinition2);

    
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition name='superprocess'>" +
      "  <start-state name='start'>" +
      "    <transition to='sub process state' />" +
      "  </start-state>" +
      "  <process-state name='sub process state'>" +
      "    <sub-process name='#{mySubProcess}' binding='late'/>" +
      "    <transition to='wait' />" +
      "  </process-state>" +
      "  <state name='wait' />" +
      "</process-definition>");
    jbpmContext.deployProcessDefinition(processDefinition);
    
    newTransaction();
    
    ProcessInstance processInstance1 = jbpmContext.newProcessInstanceForUpdate("superprocess");
    ContextInstance contextInstance = processInstance1.getContextInstance();
    contextInstance.setVariable("mySubProcess", "subprocess1");
    processInstance1.signal();
    
    newTransaction();

    long processInstanceId = processInstance1.getId();
    long subProcessInstanceId = processInstance1.getRootToken().getSubProcessInstance().getId();
    System.out.println("process ID: " + processInstanceId + "  subprocess ID: " + subProcessInstanceId);
    
    processInstance1 = jbpmContext.loadProcessInstance(processInstanceId);
    assertNotNull(processInstance1.getRootToken().getSubProcessInstance());
    assertEquals("sub process state", processInstance1.getRootToken().getNode().getName());
    contextInstance = processInstance1.getContextInstance();
    assertEquals("subprocess1", contextInstance.getVariable("mySubProcess") );

    ProcessInstance subProcessInstance1 = jbpmContext.loadProcessInstance(subProcessInstanceId);
    assertEquals("subprocess1", subProcessInstance1.getProcessDefinition().getName());
    assertEquals("wait subprocess 1", subProcessInstance1.getRootToken().getNode().getName());
    subProcessInstance1.signal();

    jbpmContext.save(subProcessInstance1);

    newTransaction();

    assertTrue(jbpmContext.loadProcessInstance(subProcessInstanceId).hasEnded());
    
    processInstance1 = jbpmContext.loadProcessInstance(processInstanceId);
    assertEquals("wait", processInstance1.getRootToken().getNode().getName());
           
    newTransaction();
    
    ProcessInstance processInstance2 = jbpmContext.newProcessInstanceForUpdate("superprocess");
    ContextInstance contextInstance2 = processInstance2.getContextInstance();
    contextInstance2.setVariable("mySubProcess", "subprocess2");
    processInstance2.signal();
    
    newTransaction();

    long processInstanceId2 = processInstance2.getId();
    long subProcessInstanceId2 = processInstance2.getRootToken().getSubProcessInstance().getId();
    System.out.println("process ID: " + processInstanceId2 + "  subprocess ID: " + subProcessInstanceId2);
    
    processInstance2 = jbpmContext.loadProcessInstance(processInstanceId2);
    assertNotNull(processInstance2.getRootToken().getSubProcessInstance());
    assertEquals("sub process state", processInstance2.getRootToken().getNode().getName());
    contextInstance2 = processInstance2.getContextInstance();
    assertEquals("subprocess2", contextInstance2.getVariable("mySubProcess") );

    ProcessInstance subProcessInstance2 = jbpmContext.loadProcessInstance(subProcessInstanceId2);
    assertEquals("subprocess2", subProcessInstance2.getProcessDefinition().getName());
    assertEquals("wait subprocess 2", subProcessInstance2.getRootToken().getNode().getName());
    subProcessInstance2.signal();

    jbpmContext.save(subProcessInstance2);

    newTransaction();

    assertTrue(jbpmContext.loadProcessInstance(subProcessInstanceId2).hasEnded());
    
    processInstance2 = jbpmContext.loadProcessInstance(processInstanceId2);
    assertEquals("wait", processInstance2.getRootToken().getNode().getName());
    
  }  


  public void testUnboundSubProcess() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition name='superprocess'>" +
      "  <start-state name='start'>" +
      "    <transition to='sub process state' />" +
      "  </start-state>" +
      "  <process-state name='sub process state'>" +
      "    <sub-process name='subprocess' binding='late' />" +
      "    <transition to='wait' />" +
      "  </process-state>" +
      "  <state name='wait' />" +
      "</process-definition>");
    jbpmContext.deployProcessDefinition(processDefinition);
    
    newTransaction();
    
    ProcessInstance processInstance = jbpmContext.newProcessInstanceForUpdate("superprocess");
    try {
      processInstance.signal();
      fail("expected exception");
    } catch (JbpmException e) {
      // OK
    }
  }
  
}
