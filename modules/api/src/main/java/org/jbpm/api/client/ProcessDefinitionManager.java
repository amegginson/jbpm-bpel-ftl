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

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The process definition manager is the entry point to create, find and otherwise manage process definitions.
 * 
 * @author thomas.diesler@jboss.com
 * @since 18-Jun-2008
 */
public abstract class ProcessDefinitionManager {

  // Injected through the MC
  protected ProcessEngine engine;
  // The map of process definitions 
  private Map<String, ProcessDefinition> pdefs = new HashMap<String, ProcessDefinition>();

  /** Get the associated process engine */
  public ProcessEngine getProcessEngine() {
    if (engine == null)
      throw new IllegalStateException("ProcessEngine not available through kernel configuration");
    
    return engine;
  }

  /** Locate the process definition manager */
  public static ProcessDefinitionManager locateProcessDefinitionManager() {
    ProcessEngine engine = ProcessEngineLocator.locateProcessEngine();
    return engine.getProcessDefinitionManager();
  }
  
  /**
   * Create a process defintion from a XML string in one of the supported formats
   */
  public abstract ProcessDefinition createProcessDefinition(String pdDescriptor);

  /**
   * Create a process defintion from an URL to a XML descritor in one of the supported formats
   */
  public abstract ProcessDefinition createProcessDefinition(URL pdURL) throws IOException;

  /**
   * Get the set of registered process definitions
   */
  public Set<ProcessDefinition> getProcessDefinitions() {
    return Collections.unmodifiableSet((Set<ProcessDefinition>) pdefs.values());
  }

  /**
   * Find a process definition for a given name
   * 
   * @return null if the process definition is not defined
   */
  public ProcessDefinition findProcessDefinition(String name) {
    return pdefs.get(name);
  }

  /**
   * Remove a process definition and all its associated process instances
   */
  public void removeProcessDefinition(ProcessDefinition pdef) {

    // Remove the process instances
    ProcessInstanceManager pim = engine.getProcessInstanceManager();
    Set<ProcessInstance> subset = pim.findProcessInstances(pdef);
    for (ProcessInstance pinst : subset)
    {
      pim.removeProcessInstance(pinst);
    }
    
    // Remove the preocess definition
    pdefs.remove(pdef.getName());
  }
  
  // Add a process definition
  protected void addProcessDefinition (ProcessDefinition pdef) {
    pdefs.put(pdef.getName(), pdef);
  }
}