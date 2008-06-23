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
package org.jbpm.test.samples.basic;

// $Id: $

import junit.framework.TestCase;

import org.jbpm.api.client.Execution;
import org.jbpm.api.client.ExecutionManager;
import org.jbpm.api.client.Node;
import org.jbpm.api.client.ProcessDefinition;
import org.jbpm.api.client.ProcessDefinitionManager;
import org.jbpm.api.client.ProcessEngine;
import org.jbpm.api.client.ProcessEngineLocator;
import org.jbpm.api.client.ProcessInstance;
import org.jbpm.api.client.ProcessInstanceManager;

public class BasicSequenceTest extends TestCase {
  
  String jpdl = 
      "<process-definition>" +
      "  <start-state>" +
      "    <transition to='stateA' />" +
      "  </start-state>" +
      "  <state name='stateA'>" +
      "    <transition to='end' />" +
      "  </state>" +
      "  <end-state name='end' />" +
      "</process-definition>";

  public void testBasicSequence() throws Exception {

    // Locate the ProcessEngine
    ProcessEngine engine = ProcessEngineLocator.locateProcessEngine();
	  
    // Create a ProcessDefinition through the ProcessDefinitionManager
    ProcessDefinitionManager pdm = engine.getProcessDefinitionManager();
    ProcessDefinition pd = pdm.createProcessDefinition(jpdl);
    
    // Create a ProcessInstance through the ProcessInstanceManager
    ProcessInstanceManager pim = engine.getProcessInstanceManager();
    ProcessInstance pi = pim.createProcessInstance(pd);

    // Create an Execution through the ExecutionManager
    ExecutionManager pem = engine.getExecutionManager();
    Execution ex = pem.createExecution(pi);
    
    // Signal the execution
    ex.signal();

    // Verify the nodes
    Node expNode = pd.findNode("stateA");
    Node wasNode = ex.getNode();
    assertEquals(expNode, wasNode);
    assertEquals(expNode.getName(), wasNode.getName());
  }

  public void testBasicSequenceShortcut() throws Exception {

    // Create a ProcessDefinition through the ProcessDefinitionManager
    ProcessDefinitionManager pdm = ProcessDefinitionManager.locateProcessDefinitionManager();
    ProcessDefinition pd = pdm.createProcessDefinition(jpdl);
    
    // Create an Execution through the ProcessDefinition
    Execution ex = pd.createExecution();
    
    // Signal the execution
    ex.signal();

    // Verify the nodes
    Node expNode = pd.findNode("stateA");
    Node wasNode = ex.getNode();
    assertEquals(expNode, wasNode);
    assertEquals(expNode.getName(), wasNode.getName());
  }

}
