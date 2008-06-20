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
package org.jbpm.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbpm.JbpmException;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

public class TaskMgmtSession implements Serializable {

  private static final long serialVersionUID = 1L;
  
  JbpmSession jbpmSession = null;
  Session session = null;
  
  public TaskMgmtSession(JbpmSession jbpmSession) {
    this.jbpmSession = jbpmSession;
    this.session = jbpmSession.getSession();
  }
  
  public TaskMgmtSession(Session session) {
    this.session = session;
    this.jbpmSession = new JbpmSession(session);
  }

  /**
   * get the tasllist for a given actor.
   */
  public List findTaskInstances(String actorId) {
    List result = null;
    try {
      Query query = session.getNamedQuery("TaskMgmtSession.findTaskInstancesByActorId");
      query.setString("actorId", actorId);
      result = query.list();
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new JbpmException("couldn't get task instances list for actor '"+actorId+"'", e);
    } 
    return result;
  }
  
  /**
   * get all the task instances for all the given actorIds.
   * @return a list of task instances.  An empty list is returned in case no task instances are found.
   */
  public List findTaskInstances(List actorIds) {
    if (actorIds==null) return new ArrayList(0);
    return findTaskInstances((String[])actorIds.toArray(new String[actorIds.size()]));
  }

  /**
   * get all the task instances for all the given actorIds.
   */
  public List findTaskInstances(String[] actorIds) {
    List result = null;
    try {
      Query query = session.getNamedQuery("TaskMgmtSession.findTaskInstancesByActorIds");
      query.setParameterList("actorIds", actorIds);
      result = query.list();
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new JbpmException("couldn't get task instances list for actors '"+actorIds+"'", e);
    } 
    return result;
  }

  /**
   * get the taskinstances for which the given actor is in the pool.
   */
  public List findPooledTaskInstances(String actorId) {
    List result = null;
    try {
      Query query = session.getNamedQuery("TaskMgmtSession.findPooledTaskInstancesByActorId");
      query.setString("swimlaneActorId", actorId);
      result = query.list();
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new JbpmException("couldn't get pooled task instances list for actor '"+actorId+"'", e);
    } 
    return result;
  }
  
  /**
   * get the taskinstances for which the given actor is in the pool.
   */
  public List findPooledTaskInstances(List actorIds) {
    List result = null;
    try {
      Query query = session.getNamedQuery("TaskMgmtSession.findPooledTaskInstancesByActorIds");
      query.setParameterList("actorIds", actorIds);
      result = query.list();
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new JbpmException("couldn't get pooled task instances list for actors '"+actorIds+"'", e);
    } 
    return result;
  }

  /**
   * get active taskinstances for a given token.
   */
  public List findTaskInstancesByToken(long tokenId) {
    List result = null;
    try {
      Query query = session.getNamedQuery("TaskMgmtSession.findTaskInstancesByTokenId");
      query.setLong("tokenId", tokenId);
      result = query.list();
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new JbpmException("couldn't get task instances by token '"+tokenId+"'", e);
    } 
    return result;
  }
  
  /**
   * get active taskinstances for a given token.
   */
  public List findTaskInstancesByProcessInstance(ProcessInstance processInstance) {
    List result = null;
    try {
      Query query = session.getNamedQuery("TaskMgmtSession.findTaskInstancesByProcessInstance");
      query.setEntity("processInstance", processInstance);
      result = query.list();
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new JbpmException("couldn't get task instances by process instance '"+processInstance+"'", e);
    } 
    return result;
  }
  

  /**
   * get the task instance for a given task instance-id.
   */
  public TaskInstance loadTaskInstance(long taskInstanceId) {
    TaskInstance taskInstance = null;
    try {
      taskInstance = (TaskInstance) session.load(TaskInstance.class, new Long(taskInstanceId));
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new JbpmException("couldn't get task instance '"+taskInstanceId+"'", e);
    } 
    return taskInstance;
  }
  
  /**
   * get the task instance for a given task instance-id.
   */
  public TaskInstance getTaskInstance(long taskInstanceId) {
    TaskInstance taskInstance = null;
    try {
      taskInstance = (TaskInstance) session.get(TaskInstance.class, new Long(taskInstanceId));
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new JbpmException("couldn't get task instance '"+taskInstanceId+"'", e);
    } 
    return taskInstance;
  }
  
  public List findTaskInstancesByIds(List taskInstanceIds) {
    List result = null;
    try {
      Query query = session.getNamedQuery("TaskMgmtSession.findTaskInstancesByIds");
      query.setParameterList("taskInstanceIds", taskInstanceIds);
      result = query.list();
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new JbpmException("couldn't get task instances by ids '"+taskInstanceIds+"'", e);
    } 
    return result;
  }

  private static final Log log = LogFactory.getLog(TaskMgmtSession.class);
}
