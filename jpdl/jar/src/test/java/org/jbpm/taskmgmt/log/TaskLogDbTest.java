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
package org.jbpm.taskmgmt.log;

import org.jbpm.db.AbstractDbTestCase;
import org.jbpm.taskmgmt.exe.TaskInstance;

public class TaskLogDbTest extends AbstractDbTestCase {
  
  TaskInstance taskInstance = null;
  
  public void setUp() throws Exception {
    super.setUp();
    
    taskInstance = new TaskInstance();
    session.save(taskInstance);
  }
  
  public void tearDown() throws Exception {
    super.tearDown();
    taskInstance = null;
  }

  public void testTaskCreateLog() {
    TaskCreateLog taskCreateLog = new TaskCreateLog(taskInstance,"someone else");
    session.save(taskCreateLog);
    
    newTransaction();
    
    taskCreateLog = (TaskCreateLog) session.load(TaskCreateLog.class, new Long(taskCreateLog.getId()));
    assertNotNull(taskCreateLog);
    assertNotNull(taskCreateLog.getTaskInstance());
    assertEquals("someone else", (taskCreateLog.getTaskActorId()));
  }

  public void testTaskAssignLog() {
    TaskAssignLog taskAssignLog = new TaskAssignLog(taskInstance,"me","toyou");
    session.save(taskAssignLog);
    
    newTransaction();
    
    taskAssignLog = (TaskAssignLog) session.load(TaskAssignLog.class, new Long(taskAssignLog.getId()));
    assertNotNull(taskAssignLog);
    assertNotNull(taskAssignLog.getTaskInstance());
    assertEquals("me", (taskAssignLog.getTaskOldActorId()));
    assertEquals("toyou", (taskAssignLog.getTaskNewActorId()));
  }

  public void testTaskEndLog() {
    TaskEndLog taskEndLog = new TaskEndLog(taskInstance);
    session.save(taskEndLog);
    
    newTransaction();
    
    taskEndLog = (TaskEndLog) session.load(TaskEndLog.class, new Long(taskEndLog.getId()));
    assertNotNull(taskEndLog);
    assertNotNull(taskEndLog.getTaskInstance());
  }
}
