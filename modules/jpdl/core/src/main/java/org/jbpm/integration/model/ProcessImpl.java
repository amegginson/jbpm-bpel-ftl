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
package org.jbpm.integration.model;

//$Id$

import org.jboss.bpm.client.ProcessManager;
import org.jboss.bpm.model.FlowObject;
import org.jboss.bpm.model.Process;
import org.jboss.bpm.runtime.Token;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.Execution;

/**
 * A jBPM3 implementation of a process definition
 * 
 * @author thomas.diesler@jboss.com
 * @since 18-Jun-2008
 */
public class ProcessImpl extends Process
{
  private ProcessDefinition oldPD;
  
  public ProcessImpl(ProcessDefinition oldPD)
  {
    this.oldPD = oldPD;
    initName(oldPD.getName());
  }

  private void initName(String name)
  {
    if (name == null)
    {
      ProcessManager pdm = ProcessManager.locateProcessManager();
      name = "AnonymousProcess#" + pdm.getProcesses().size();
      setName(name);
    }
  }

  // Provide public access
  public void addFlowObject(FlowObject flowObject)
  {
    super.addFlowObject(flowObject);
  }

  public String getName()
  {
    return oldPD.getName();
  }

  public void setName(String name)
  {
    oldPD.setName(name);
  }

  public void executeOverwrite(Token token)
  {
    Process proc = token.getProcess();
    StartEventImpl start = (StartEventImpl)proc.getStartEvent();
    start.execute(token);
    
    // Repeatetly signal the Execution until we reach the end
    Execution oldEx = start.getExecution();
    while (oldEx.getRootToken().hasEnded() == false)
    {
      oldEx.signal();
    }
  }
}
