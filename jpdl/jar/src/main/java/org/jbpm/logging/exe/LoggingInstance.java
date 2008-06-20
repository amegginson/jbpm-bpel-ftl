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
package org.jbpm.logging.exe;

import java.util.*;

import org.apache.commons.logging.*;
import org.jbpm.graph.log.*;
import org.jbpm.logging.log.*;
import org.jbpm.module.exe.*;
import org.jbpm.util.Clock;

/**
 * non persisted class that collects {@link org.jbpm.logging.log.ProcessLog}s
 * during process execution.  When the process instance gets saved, the 
 * process logs will be saved by the {@link org.jbpm.db.LoggingSession}.
 */
public class LoggingInstance extends ModuleInstance {

  private static final long serialVersionUID = 1L;
  
  List logs = new ArrayList();
  transient LinkedList compositeLogStack = new LinkedList();
  
  public LoggingInstance() {
  }
  
  public void startCompositeLog(CompositeLog compositeLog) {
    addLog(compositeLog);
    compositeLogStack.addFirst(compositeLog);
  }
  
  public void endCompositeLog() {
    compositeLogStack.removeFirst();
  }

  public void addLog(ProcessLog processLog) {
    if (!compositeLogStack.isEmpty()) {
      CompositeLog currentCompositeLog = (CompositeLog) compositeLogStack.getFirst();
      processLog.setParent(currentCompositeLog);
      currentCompositeLog.addChild(processLog);
    }
    processLog.setDate(Clock.getCurrentTime());
    
    logs.add(processLog);
  }
  
  public List getLogs() {
    return logs;
  }

  /**
   * get logs, filetered by log type.
   */
  public List getLogs(Class filterClass) {
    return getLogs(logs, filterClass);
  }
  
  public static List getLogs(Collection logs, Class filterClass) {
    List filteredLogs = new ArrayList();
    if (logs!=null) {
      Iterator iter = logs.iterator();
      while (iter.hasNext()) {
        Object log = iter.next();
        if (filterClass.isAssignableFrom(log.getClass())) {
          filteredLogs.add(log);
        }
      }
    }
    return filteredLogs;
  }

  LinkedList getCompositeLogStack() {
    return compositeLogStack;
  }

  List getCurrentOperationReversedActionLogs() {
    List actionLogs = new ArrayList();
    ProcessLog operationLog = (ProcessLog) compositeLogStack.getFirst();
    ListIterator listIterator = logs.listIterator(logs.size());
    ProcessLog processLog = (ProcessLog) listIterator.previous();
    while ( (listIterator.hasNext())
            && (processLog!=operationLog) ) {
      if (processLog instanceof ActionLog) {
        actionLogs.add(0, processLog);
      }
    }
    return actionLogs;
  }
  
  public void logLogs() {
    Iterator iter = logs.iterator();
    while (iter.hasNext()) {
      ProcessLog processLog = (ProcessLog) iter.next();
      if (processLog.getParent()==null) {
        logLog("+-", processLog);
      }
    }
  }

  void logLog(String indentation, ProcessLog processLog) {
    log.debug(processLog.getToken()+"["+processLog.getIndex()+"] "+processLog+" on "+processLog.getToken());
    if (processLog instanceof CompositeLog) {
      CompositeLog compositeLog = (CompositeLog) processLog;
      if (compositeLog.getChildren()!=null) {
        Iterator iter = compositeLog.getChildren().iterator();
        while (iter.hasNext()) {
          logLog("| "+indentation, (ProcessLog) iter.next());
        }
      }
    }
  }
  
  Object readResolve() {
    compositeLogStack = new LinkedList();
    return this;
  }

  private static final Log log = LogFactory.getLog(LoggingInstance.class);
}
