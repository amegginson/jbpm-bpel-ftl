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
package org.jbpm.integration.runtime;

// $Id$

import org.jboss.bpm.client.Execution;
import org.jboss.bpm.runtime.Activity;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;

/**
 * This is a wrapper arround an API Activity
 * 
 * @author thomas.diesler@jboss.com
 * @since 18-Jun-2008
 */
public class ActivityWrapper implements ActionHandler
{
  private static final long serialVersionUID = 3617376097428098837L;
  
  private Activity activity;
  
  public ActivityWrapper(Action action, Activity activity)
  {
    this.activity = activity;
  }

  public void execute(ExecutionContext executionContext) throws Exception
  {
    ContextInstance ctxInst = executionContext.getContextInstance();
    Execution ex = (Execution)ctxInst.getTransientVariable(Execution.class.getName());
    if (ex == null)
      throw new IllegalStateException("Cannot obtain API Execution");

    activity.execute(ex.getNode(), ex.getContext());
  }
}
