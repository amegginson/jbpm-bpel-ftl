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
package org.jbpm.taskmgmt.exe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmException;
import org.jbpm.calendar.BusinessCalendar;
import org.jbpm.calendar.Duration;
import org.jbpm.graph.def.DelegationException;
import org.jbpm.graph.def.GraphElement;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.Token;
import org.jbpm.instantiation.Delegation;
import org.jbpm.instantiation.UserCodeInterceptorConfig;
import org.jbpm.jpdl.el.impl.JbpmExpressionEvaluator;
import org.jbpm.module.exe.ModuleInstance;
import org.jbpm.security.SecurityHelper;
import org.jbpm.svc.Services;
import org.jbpm.taskmgmt.TaskInstanceFactory;
import org.jbpm.taskmgmt.def.AssignmentHandler;
import org.jbpm.taskmgmt.def.Swimlane;
import org.jbpm.taskmgmt.def.Task;
import org.jbpm.taskmgmt.def.TaskMgmtDefinition;
import org.jbpm.taskmgmt.log.TaskCreateLog;
import org.jbpm.util.Clock;

/**
 * process instance extension for managing tasks on a process instance.
 */
public class TaskMgmtInstance extends ModuleInstance {

  private static final long serialVersionUID = 1L;

  TaskMgmtDefinition taskMgmtDefinition = null;
  Map swimlaneInstances = null;
  Set taskInstances = null;
  /** non persistent collection that stores all the task instances that have
   * variable updates*/
  Collection taskInstanceVariableUpdates = null; 
  
  public TaskMgmtInstance() {
  }
  
  public TaskMgmtInstance(TaskMgmtDefinition taskMgmtDefinition) {
    this.taskMgmtDefinition = taskMgmtDefinition;
  }

  // task instances ///////////////////////////////////////////////////////////

  public TaskInstance createTaskInstance() {
    return createTaskInstance(null, (ExecutionContext)null);
  }

  public TaskInstance createTaskInstance(Task task) {
    return createTaskInstance(task, (ExecutionContext)null);
  }

  public TaskInstance createTaskInstance(Token token) {
    return createTaskInstance(null, new ExecutionContext(token));
  }

  /**
   * creates a new task instance on the given token, for the given task.
   */
  public TaskInstance createTaskInstance(Task task, Token token) {
    ExecutionContext executionContext = new ExecutionContext(token);
    executionContext.setTask(task);
    return createTaskInstance(task, executionContext);
  }

  /**
   * creates a new task instance on the given task, in the given execution context.
   */
  public TaskInstance createTaskInstance(Task task, ExecutionContext executionContext) {
    // instantiate the new task instance
    TaskInstance taskInstance = instantiateNewTaskInstance(executionContext);

    // bind the task instance to the TaskMgmtInstance
    addTaskInstance(taskInstance);
    // ... add a reference to the process instance
    taskInstance.setProcessInstance(executionContext.getProcessInstance());

    // initialize the task instance
    if (task!=null) taskInstance.setTask(task);

    // assign an id to the task instance
    Services.assignId(taskInstance);

    // copy the task properties
    String description = null;
    if (task!=null) {
      description = task.getDescription();
      taskInstance.setDescription(description);
      taskInstance.setBlocking(task.isBlocking());
      taskInstance.setSignalling(task.isSignalling());
    }

    if (executionContext!=null) {
      Token token = executionContext.getToken();
      taskInstance.setToken(token);
      
      taskInstance.initializeVariables();
      
      if (task != null && task.getDueDate()!=null) {
        Date baseDate = null;
        Date dueDateDate = null;
        String dueDate = task.getDueDate();
        String durationString = null;
        String durationSeparator = null;
        
        if (dueDate.startsWith("#")) {
          String baseDateEL = dueDate.substring(0,dueDate.indexOf("}")+1);
          Object o = JbpmExpressionEvaluator.evaluate(baseDateEL, executionContext);
          if (o instanceof Date) {
            baseDate = (Date) o;          
          } else {
            if (o instanceof Calendar) {
              baseDate = ((Calendar) o).getTime();
            } else {
              throw new JbpmException("Invalid basedate type: " + baseDateEL + " is of type " + o.getClass().getName() +". Only Date and Calendar are supported");
            }
          }
          int endOfELIndex = dueDate.indexOf("}");
          if (endOfELIndex < (dueDate.length() -1) ) {
            durationSeparator = dueDate.substring(endOfELIndex+1).trim().substring(0,1);
            if ( !(durationSeparator.equals("+") || durationSeparator.equals("-") ) ){ 
              throw new JbpmException("Invalid duedate, + or - missing after EL");
            }
            durationString = dueDate.substring(endOfELIndex+1).trim();
          }
        } else {
          durationString = dueDate;
        }
        if (baseDate != null && (durationString == null || "".equals(durationString))) {
          dueDateDate = baseDate;
        } else {
          BusinessCalendar businessCalendar = new BusinessCalendar(); 
          dueDateDate = businessCalendar.add( (baseDate != null) ? baseDate : Clock.getCurrentTime(), new Duration(durationString) );
        }        
        taskInstance.setDueDate(dueDateDate); 
      }
      
      
      try {
        // update the executionContext
        executionContext.setTask(task);
        executionContext.setTaskInstance(taskInstance);
        executionContext.setEventSource(task);

        // evaluate the description
        if ( (description!=null) 
             && (description.indexOf("#{")!=-1)
           ) {
          Object result = JbpmExpressionEvaluator.evaluate(description, executionContext);
          if (result!=null) {
            taskInstance.setDescription(result.toString());
          }
        }

        // create the task instance
        taskInstance.create(executionContext);

        // if this task instance is created for a task, perform assignment
        if (task!=null) {
          taskInstance.assign(executionContext);
        }
        
      } finally {
        // clean the executionContext
        executionContext.setTask(null);
        executionContext.setTaskInstance(null);
        executionContext.setEventSource(null);
      }
      
      // log this creation 
      // WARNING: The events create and assign are fired in the right order, but
      // the logs are still not ordered properly.
      token.addLog(new TaskCreateLog(taskInstance, taskInstance.getActorId()));

    } else {
      taskInstance.create();
    }

    return taskInstance;
  }

  public SwimlaneInstance getInitializedSwimlaneInstance(ExecutionContext executionContext, Swimlane swimlane) {
    // initialize the swimlane
    if (swimlaneInstances==null) swimlaneInstances = new HashMap();
    SwimlaneInstance swimlaneInstance = (SwimlaneInstance) swimlaneInstances.get(swimlane.getName());
    if (swimlaneInstance==null) {
      swimlaneInstance = new SwimlaneInstance(swimlane);
      addSwimlaneInstance(swimlaneInstance);
      // assign the swimlaneInstance
      performAssignment(swimlane.getAssignmentDelegation(), 
                        swimlane.getActorIdExpression(),
                        swimlane.getPooledActorsExpression(),
                        swimlaneInstance, 
                        executionContext);
    }

    return swimlaneInstance;
  }
  
  public void performAssignment(Delegation assignmentDelegation, 
                                String actorIdExpression, 
                                String pooledActorsExpression, 
                                Assignable assignable, 
                                ExecutionContext executionContext) {
    try {
      if (assignmentDelegation!=null) {
        performAssignmentDelegation(assignmentDelegation, assignable, executionContext);
      } else {
        if (actorIdExpression!=null) {
          performAssignmentActorIdExpr(actorIdExpression, assignable, executionContext);
        }
        if (pooledActorsExpression!=null) {
          performAssignmentPooledActorsExpr(pooledActorsExpression, assignable, executionContext);
        }
      }
      
    } catch (Exception exception) {
      GraphElement graphElement = executionContext.getEventSource();
      if (graphElement!=null) {
        graphElement.raiseException(exception, executionContext);
      } else {
        throw new DelegationException(exception, executionContext);
      }
    }
  }

  void performAssignmentDelegation(Delegation assignmentDelegation, Assignable assignable, ExecutionContext executionContext) throws Exception {
    // instantiate the assignment handler
    AssignmentHandler assignmentHandler = (AssignmentHandler) assignmentDelegation.instantiate();
    // invoke the assignment handler
    if (UserCodeInterceptorConfig.userCodeInterceptor!=null) {
      UserCodeInterceptorConfig.userCodeInterceptor.executeAssignment(assignmentHandler, assignable, executionContext);
    } else {
      assignmentHandler.assign(assignable, executionContext);
    }
  }

  void performAssignmentActorIdExpr(String actorIdExpression, Assignable assignable, ExecutionContext executionContext) {
    Object result = null;
    String actorId = null;
    try {
      result = JbpmExpressionEvaluator.evaluate(actorIdExpression, executionContext);
      if (result==null) {
        throw new JbpmException("actor-id expression '"+actorIdExpression+"' returned null");
      }
      actorId = (String) result;
    } catch (ClassCastException e) {
      throw new JbpmException("actor-id expression '"+actorIdExpression+"' didn't resolve to a java.lang.String: '"+result+"' ("+result.getClass().getName()+")");
    }
    assignable.setActorId(actorId);
  }

  void performAssignmentPooledActorsExpr(String pooledActorsExpression, Assignable assignable, ExecutionContext executionContext) {
    String[] pooledActors = null;
    Object result = JbpmExpressionEvaluator.evaluate(pooledActorsExpression, executionContext);
    if (result==null) {
      throw new JbpmException("pooled-actors expression '"+pooledActorsExpression+"' returned null");
    }

    if (result instanceof String[]) {
      pooledActors = (String[]) result;

    } else if (result instanceof Collection) {
      Collection collection = (Collection)result;
      pooledActors = (String[]) collection.toArray(new String[collection.size()]);
      
    } else if (result instanceof String) {
      List pooledActorList = new ArrayList();
      StringTokenizer tokenizer = new StringTokenizer((String) result, ",");
      while (tokenizer.hasMoreTokens()) {
        pooledActorList.add(tokenizer.nextToken().trim());
      }
      pooledActors = (String[]) pooledActorList.toArray(new String[pooledActorList.size()]);
    } else {
      throw new JbpmException("pooled-actors expression '"+pooledActorsExpression+"' didn't resolve to a comma separated String, a Collection or a String[]: '"+result+"' ("+result.getClass().getName()+")");
    }

    assignable.setPooledActors(pooledActors);
  }

  /**
   * creates a task instance on the rootToken, and assigns it 
   * to the currently authenticated user.
   */
  public TaskInstance createStartTaskInstance() {
    TaskInstance taskInstance = null;
    Task startTask = taskMgmtDefinition.getStartTask();
    if (startTask!=null) {
      Token rootToken = processInstance.getRootToken();
      ExecutionContext executionContext = new ExecutionContext(rootToken);
      taskInstance = createTaskInstance(startTask, executionContext);
      taskInstance.setActorId(SecurityHelper.getAuthenticatedActorId());
    }
    return taskInstance;
  }

  TaskInstance instantiateNewTaskInstance(ExecutionContext executionContext) {
    TaskInstanceFactory taskInstanceFactory = (TaskInstanceFactory) JbpmConfiguration.Configs.getObject("jbpm.task.instance.factory");
    if (taskInstanceFactory == null) {
      throw new JbpmException("jbpm.task.instance.factory was not configured in jbpm.cfg.xml");
    }
    return taskInstanceFactory.createTaskInstance(executionContext);
  }

  /**
   * is true if the given token has task instances that keep the 
   * token from leaving the current node.
   */
  public boolean hasBlockingTaskInstances(Token token) {
    boolean hasBlockingTasks = false;
    if (taskInstances!=null) {
      Iterator iter = taskInstances.iterator();
      while ( (iter.hasNext())
              && (! hasBlockingTasks)) {
        TaskInstance taskInstance = (TaskInstance) iter.next();
        if ( (! taskInstance.hasEnded())
             && (taskInstance.isBlocking())
             && (token!=null)
             && (token.equals(taskInstance.getToken())) ) {
          hasBlockingTasks = true;
        }
      }
    }
    return hasBlockingTasks;
  }

  /**
   * is true if the given token has task instances that are not yet ended.
   */
  public boolean hasUnfinishedTasks(Token token) {
    return (getUnfinishedTasks(token).size()>0);
  }

  /**
   * is the collection of {@link TaskInstance}s on the given token that are not ended.
   */
  public Collection getUnfinishedTasks(Token token) {
    Collection unfinishedTasks = new ArrayList();
    if ( taskInstances != null ) {
      Iterator iter = taskInstances.iterator();
      while (iter.hasNext()) {
        TaskInstance task = (TaskInstance) iter.next();
        if ( (!task.hasEnded())
             && (token!=null)
             && (token.equals(task.getToken())) 
           ) {
          unfinishedTasks.add( task );
        }
      }
    }
    return unfinishedTasks;
  }

  /**
   * is true if there are {@link TaskInstance}s on the given token that can trigger 
   * the token to continue.
   */
  public boolean hasSignallingTasks(ExecutionContext executionContext) {
    return (getSignallingTasks(executionContext).size()>0);
  }

  /**
   * is the collection of {@link TaskInstance}s for the given token that can trigger 
   * the token to continue.
   */
  public Collection getSignallingTasks(ExecutionContext executionContext) {
    Collection signallingTasks = new ArrayList();
    if ( taskInstances != null ) {
      Iterator iter = taskInstances.iterator();
      while (iter.hasNext()) {
        TaskInstance taskInstance = (TaskInstance) iter.next();
        if (taskInstance.isSignalling()
            &&(executionContext.getToken().equals(taskInstance.getToken()))) {
          signallingTasks.add(taskInstance);
        }
      }
    }
    return signallingTasks;
  }
  
  /**
   * returns all the taskInstances for the this process instance.  This 
   * includes task instances that have been completed previously.
   */
  public Collection getTaskInstances() {
    return taskInstances;
  }
  public void addTaskInstance(TaskInstance taskInstance) {
    if (taskInstances==null) taskInstances = new HashSet();
    taskInstances.add(taskInstance);
    taskInstance.setTaskMgmtInstance(this);
  }
  public void removeTaskInstance(TaskInstance taskInstance) {
    if (taskInstances!=null) {
      taskInstances.remove(taskInstance);
    }
  }

  // swimlane instances ///////////////////////////////////////////////////////

  public Map getSwimlaneInstances() {
    return swimlaneInstances;
  }
  public void addSwimlaneInstance( SwimlaneInstance swimlaneInstance ) {
    if (swimlaneInstances==null) swimlaneInstances = new HashMap();
    swimlaneInstances.put(swimlaneInstance.getName(), swimlaneInstance);
    swimlaneInstance.setTaskMgmtInstance(this);
  }
  public SwimlaneInstance getSwimlaneInstance(String swimlaneName) {
    return (SwimlaneInstance) (swimlaneInstances!=null ? swimlaneInstances.get(swimlaneName) : null );
  }

  public SwimlaneInstance createSwimlaneInstance(String swimlaneName) {
    Swimlane swimlane = (taskMgmtDefinition!=null ? taskMgmtDefinition.getSwimlane(swimlaneName) : null);
    if (swimlane!=null) {
      return createSwimlaneInstance(swimlane);
    }
    throw new JbpmException("couldn't create swimlane instance for non-existing swimlane "+swimlaneName);
  }

  public SwimlaneInstance createSwimlaneInstance(Swimlane swimlane) {
    if (swimlaneInstances==null) swimlaneInstances = new HashMap();
    SwimlaneInstance swimlaneInstance = new SwimlaneInstance(swimlane);
    try {
      swimlaneInstance.setTaskMgmtInstance(this);
      Class persistentMapClass = swimlaneInstances.getClass();
      Field mapField = persistentMapClass.getDeclaredField("map");
      mapField.setAccessible(true);
      // TODO remove the size when we switch to hibernate 3.2.1 (it's a workaround for a bug)
      swimlaneInstances.size();
      swimlaneInstances.put(swimlaneInstance.getName(), swimlaneInstance);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return swimlaneInstance;
  }
  
  // getters and setters //////////////////////////////////////////////////////

  public TaskMgmtDefinition getTaskMgmtDefinition() {
    return taskMgmtDefinition;
  }

  /**
   * suspends all task instances for this process instance.
   */
  public void suspend(Token token) {
    if (token==null) {
      throw new JbpmException("can't suspend task instances for token null");
    }
    if (taskInstances!=null) {
      Iterator iter = taskInstances.iterator();
      while (iter.hasNext()) {
        TaskInstance taskInstance = (TaskInstance) iter.next();
        if ( (token.equals(taskInstance.getToken()))
             && (taskInstance.isOpen())
           ) {
          taskInstance.suspend();
        }
      }
    }
  }

  /**
   * resumes all task instances for this process instance.
   */
  public void resume(Token token) {
    if (token==null) {
      throw new JbpmException("can't suspend task instances for token null");
    }
    if (taskInstances!=null) {
      Iterator iter = taskInstances.iterator();
      while (iter.hasNext()) {
        TaskInstance taskInstance = (TaskInstance) iter.next();
        if ( (token.equals(taskInstance.getToken()))
            && (taskInstance.isOpen())
          ) {
          taskInstance.resume();
        }
      }
    }
  }
  
  void notifyVariableUpdate(TaskInstance taskInstance) {
    if (taskInstanceVariableUpdates==null) {
      taskInstanceVariableUpdates=new HashSet();
    }
    taskInstanceVariableUpdates.add(taskInstance);
  }
  /**
   * returns the collection of task instance with variable updates.
   */
  public Collection getTaskInstancesWithVariableUpdates() {
    return taskInstanceVariableUpdates;
  }

  /**
   * convenience method to end all tasks related to a given process instance.
   */
  public void endAll() {
    if (taskInstances!=null) {
      Iterator iter = taskInstances.iterator();
      while (iter.hasNext()) {
        TaskInstance taskInstance = (TaskInstance) iter.next();
        if (!taskInstance.hasEnded()) {
          taskInstance.end();
        }
      }
    }
  }

  /**
   * removes signalling capabilities from all task instances related to the given token.
   */
  public void removeSignalling(Token token) {
    if (taskInstances!=null) {
      Iterator iter = taskInstances.iterator();
      while (iter.hasNext()) {
        TaskInstance taskInstance = (TaskInstance) iter.next();
        if ( (token!=null)
             && (token.equals(taskInstance.getToken()))
           ) {
          taskInstance.setSignalling(false);
        }
      }
    }
  }
}
