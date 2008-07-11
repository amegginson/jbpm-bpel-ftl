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
package org.jbpm.integration.client;

// $Id$

import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.bpm.client.ExecutionManager;
import org.jboss.bpm.client.ProcessEngine;
import org.jboss.bpm.client.internal.InitialToken;
import org.jboss.bpm.model.ExecutableFlowObject;
import org.jboss.bpm.model.Process;
import org.jboss.bpm.model.Result;
import org.jboss.bpm.model.Signal;
import org.jboss.bpm.runtime.Attachments;
import org.jboss.bpm.runtime.Token;
import org.jbpm.graph.exe.Execution;
import org.jbpm.integration.model.StartEventImpl;

/**
 * The process manager is the entry point to create, find and otherwise manage processes.
 * 
 * @author thomas.diesler@jboss.com
 * @since 18-Jun-2008
 */
public class ExecutionManagerImpl extends ExecutionManager
{
  // provide logging
  private static final Log log = LogFactory.getLog(ExecutionManagerImpl.class);

  public void setProcessEngine(ProcessEngine engine)
  {
    this.engine = engine;
  }
  
  public Future<Result> startProcess(Process proc, Attachments att)
  {
    throwSignal(new Signal(proc, Signal.Type.ENTER_PROCESS));
    try
    {
      // Repeatetly signal the Execution until we reach the end
      StartEventImpl start = (StartEventImpl)proc.getStartEvent();
      
      Token token = new InitialToken(proc, att);
      start.execute(token);
      
      Execution oldEx = start.getExecution();
      while (oldEx.getRootToken().hasEnded() == false)
      {
        oldEx.signal();
      }
    }
    finally
    {
      throwSignal(new Signal(proc, Signal.Type.EXIT_PROCESS));
    }
    return new ResultFuture(proc);
  }
}