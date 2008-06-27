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
package org.jbpm.api.client;

// $Id: $

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the structural definition of a BPM process
 * 
 * @author thomas.diesler@jboss.com
 * @since 18-Jun-2008
 */
public class ProcessDefinition {

  /** The protected name */
  private String name;
  /** The protected list of nodes */
  private List<Node> nodes = new ArrayList<Node>();

  /**
   * Construct the process definition with a given name
   */
  protected ProcessDefinition(String name) {
    if (name == null)
      throw new IllegalArgumentException("Process definition name cannot be null");
    this.name = name;
  }

  /**
   * Get the name of this process definition
   */
  public String getName() {
    return name;
  }

  /** Get an unmutable list of node objects */
  public List<Node> getNodes() {
    return Collections.unmodifiableList(nodes);
  }
  
  /**
   * Find a node for a given name
   * @return null if the node is not defined
   */
  public Node findNode(String name) {
    Node apiNode = null;
    for (Node aux : nodes) {
      if (aux.getName().equals(name)) {
        apiNode = aux;
        break;
      }
    }
    return apiNode;
  }
  
  // Add a node
  protected void addNode(Node apiNode) {
    nodes.add(apiNode);
  }
  
  /** 
   * Create an execution for this process definition.
   * 
   * Note, that this will automatically create a new process instance. 
   */
  public Execution createExecution() {
    ProcessEngine engine = ProcessEngineLocator.locateProcessEngine();
    ProcessInstanceManager pim = engine.getProcessInstanceManager();
    ProcessInstance pinst = pim.createProcessInstance(this);
    ExecutionManager pem = engine.getExecutionManager();
    return pem.createExecution(pinst);
  }
}