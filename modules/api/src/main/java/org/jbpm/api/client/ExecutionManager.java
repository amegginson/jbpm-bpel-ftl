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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The execution manager is the entry point to create, find and otherwise 
 * manage process executions.
 * 
 * @author thomas.diesler@jboss.com
 * @since 18-Jun-2008
 */
public abstract class ExecutionManager {

  // Injected through the MC
  protected ProcessEngine engine;
  
  // The map of executions 
  private Map<String, Execution> execs = new HashMap<String, Execution>();
  
  /** Get the associated process engine */
  public ProcessEngine getProcessEngine() {
    if (engine == null)
      throw new IllegalStateException("ProcessEngine not available through kernel configuration");
    
    return engine;
  }

  /**
   * Create an execution for a given process instance
   */
  public abstract Execution createExecution(ProcessInstance pinst);

  /**
   * Get the set of registered executions
   */
  public Set<Execution> getExecutions() {
    return Collections.unmodifiableSet((Set<Execution>) execs.values());
  }

  /**
   * Find executions for a given process instance
   */
  public Set<Execution> findExecutions(ProcessInstance pinst) {
    Set<Execution> subset = new HashSet<Execution>();
    for (Execution ex : execs.values())
    {
      if (ex.getProcessInstance() == pinst)
        subset.add(ex);
    }
    return subset;
  }
  
  /**
   * Find an execution for a given name
   * @return null if the execution is not defined
   */
  public Execution findExecution(String key) {
    return execs.get(key); 
  }
  
  /**
   * Remove an execution
   */
  public void removeExecution(Execution ex) {
    execs.remove(ex.getKey());
  }

  // Add an execution
  protected void addExecution(Execution ex) {
    execs.put(ex.getKey(), ex);
  }
}