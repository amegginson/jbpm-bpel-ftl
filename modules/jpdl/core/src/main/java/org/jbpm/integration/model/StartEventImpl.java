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

import org.jboss.bpm.model.Process;
import org.jboss.bpm.model.StartEvent;
import org.jboss.bpm.runtime.Token;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.def.GraphElement;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.exe.Execution;
import org.jbpm.integration.runtime.ExecutionContextImpl;

/**
 * TODO
 * 
 * @author thomas.diesler@jboss.com
 * @since 18-Jun-2008
 */
public class StartEventImpl extends StartEvent
{
  private Execution oldEx;
  
  StartEventImpl(Process proc, Node oldNode)
  {
    setImplObject(oldNode);
  }

  @Override
  public void execute(Token token)
  {
    super.execute(token);
    oldEx.signal();
  }

  @Override
  protected void executeOverwrite(Token token)
  {
    // Create a new Execution and copy the attachments
    GraphElement oldEl = (GraphElement)getImplObject();
    oldEx = new Execution(oldEl.getProcessDefinition());
    ContextInstance ctxInst = oldEx.getContextInstance();
    new ExecutionContextImpl(ctxInst).copyAttachments(token.getExecutionContext());
    ctxInst.setTransientVariable(Process.class.getName(), getProcess());
  }

  public Execution getExecution()
  {
    return oldEx;
  }
}
