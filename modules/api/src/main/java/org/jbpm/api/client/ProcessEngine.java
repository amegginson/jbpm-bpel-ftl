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

/**
 * The process engine is an agregator of various manger objects used by the BPM engine
 * 
 * @author thomas.diesler@jboss.com
 * @since 18-Jun-2008
 */
public class ProcessEngine {

  /** The process engine bean name - jBPMEngine */
  public static final String BEAN_NAME_JBPMENGINE = "jBPMProcessEngine";
  
  // Injected through the MC
  protected ProcessDefinitionManager processDefinitionManager;
  // Injected through the MC
  protected ProcessInstanceManager processInstanceManager;
  // Injected through the MC
  protected ExecutionManager executionManager;

  /**
   * Get the configured instance of the process definition manager
   * @return The process definition manager
   */
  public ProcessDefinitionManager getProcessDefinitionManager() {
    if (processDefinitionManager == null)
      throw new IllegalStateException("ProcessDefinitionManager not available through kernel configuration");
    
    return processDefinitionManager;
  }

  /**
   * Get the configured instance of the process instance manager
   * @return The process instance manager
   */
  public ProcessInstanceManager getProcessInstanceManager() {
    if (processInstanceManager == null)
      throw new IllegalStateException("ProcessInstanceManager not available through kernel configuration");
    
    return processInstanceManager;
  }

  /**
   * Get the configured instance of the execution manager
   * @return The execution manager
   */
  public ExecutionManager getExecutionManager() {
    if (executionManager == null)
      throw new IllegalStateException("ExecutionManager not available through kernel configuration");
    
    return executionManager;
  }
}