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

// $Id:$

/**
 * Represents a node in the process definition.
 * 
 * A node is an immutable object.
 * 
 * @author thomas.diesler@jboss.com
 * @since 18-Jun-2008
 */
public abstract class Node {

  private ProcessDefinition pdef;
  private String name;
  
  protected Node(ProcessDefinition pdef, String name) {
    if (pdef == null)
      throw new IllegalArgumentException("Process definition cannot be null");
    if (name == null)
      throw new IllegalArgumentException("Node name cannot be null");
    
    this.pdef = pdef;
    this.name = name;
  }

  /** Get the associated process definition */
  public ProcessDefinition getProcessDefinition() {
    return pdef;
  }

  /** Get the node name */
  public String getName() {
    return name;
  }
  
  /** Returns a string representation of the object. */
  public String toString(){
    return "Node[" + name + "]";
  }
}