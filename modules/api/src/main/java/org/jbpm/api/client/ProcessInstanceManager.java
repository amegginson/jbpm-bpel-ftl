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

//$Id: $

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The process instance manager is the entry point to create, find and otherwise 
 * manage process instances.
 * 
 * @author thomas.diesler@jboss.com
 * @since 18-Jun-2008
 */
public abstract class ProcessInstanceManager {

  // Injected through the MC
  protected ProcessEngine engine;
  
  // The set of process instances 
  private Set<ProcessInstance> pinstSet = new HashSet<ProcessInstance>();
  
  /** Get the associated process engine */
  public ProcessEngine getProcessEngine() {
    if (engine == null)
      throw new IllegalStateException("ProcessEngine not available through kernel configuration");
    
    return engine;
  }

  /**
   * Create a process instance for a given process definition
   * @param pd The process definition
   * @return A process instance
   */
  public abstract ProcessInstance createProcessInstance(ProcessDefinition pdef);

  /**
   * Get the set of registered process instances
   */
  public Set<ProcessInstance> getProcessInstances() {
    return Collections.unmodifiableSet(pinstSet);
  }

  /**
   * Get the set of registered process instances for a given process definition
   */
  public Set<ProcessInstance> findProcessInstances(ProcessDefinition pdef) {
    Set<ProcessInstance> subset = new HashSet<ProcessInstance>();
    for (ProcessInstance pinst : pinstSet)
    {
      if (pinst.getProcessDefinition() == pdef)
        subset.add(pinst);
    }
    return subset;
  }

  /**
   * Remove a process instances and all its associated process executions
   */
  public void removeProcessInstance(ProcessInstance pinst) {
    // Remove executions
    ExecutionManager exm = engine.getExecutionManager();
    Set<Execution> execs = exm.findExecutions(pinst);
    for (Execution ex : execs)
    {
      exm.removeExecution(ex);
    }
    
    // Remove process instance
    pinstSet.remove(pinst);
  }
  
  // Add a process instance
  protected void addProcessInstance(ProcessInstance pinst) {
    pinstSet.add(pinst);
  }
}
