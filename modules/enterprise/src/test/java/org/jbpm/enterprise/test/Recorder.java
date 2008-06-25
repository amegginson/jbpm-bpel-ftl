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
package org.jbpm.enterprise.test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;

public class Recorder implements ActionHandler {

  private static final long serialVersionUID = 1L;
  
  public static Set collectedProcessInstanceIds = null;
  public static Set collectedResults = null;
  public volatile static int executions = 0;

  public static void resetCollections() {
    collectedProcessInstanceIds = Collections.synchronizedSet(new HashSet());
    collectedResults = Collections.synchronizedSet(new TreeSet());
    executions = 0;
  }

  public void execute(ExecutionContext executionContext) throws Exception {
    synchronized (Recorder.class) {
      executions++;
    }
    String id = (String) Long.toString(executionContext.getProcessInstance().getId());
    collectedProcessInstanceIds.add(id);
    record(id, executionContext);
  }

  public void record(String processInstanceId, ExecutionContext executionContext) {
    String nodeName = executionContext.getNode().getName();
    collectedResults.add(processInstanceId+nodeName);
    executionContext.leaveNode();
  }
}
