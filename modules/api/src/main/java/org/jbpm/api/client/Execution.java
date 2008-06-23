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
 * Represents an execution of a process instance
 * 
 * @author thomas.diesler@jboss.com
 * @since 18-Jun-2008
 */
public abstract class Execution {

  private String key;
  private ProcessInstance pinst;
  
  protected Execution(ProcessInstance pinst, String key) {
    if (pinst == null)
      throw new IllegalArgumentException("Process instance cannot be null");
    if (key == null)
      throw new IllegalArgumentException("Execution key cannot be null");
    
    this.pinst = pinst;
    this.key = key;
  }

  /** Get the associated process instance */
  public ProcessInstance getProcessInstance() {
    return pinst;
  }

  /** Get the key for this execution */
  public String getKey() {
    return key;
  }

  /** 
   * Get the current node for this execution
   */
  public abstract Node getNode();
  

  /** 
   * Signal this execution
   */
  public abstract void signal();

}