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

import junit.framework.*;

public class TaskMgmtExeDbTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("org.jbpm.taskmgmt.exe");
    //$JUnit-BEGIN$
    suite.addTestSuite(BlockingTaskDbTest.class);
    suite.addTestSuite(EndTasksDbTest.class);
    suite.addTestSuite(SwimlaneDbTest.class);
    suite.addTestSuite(TaskAssignmentDbTest.class);
    suite.addTestSuite(TaskInstanceDbTest.class);
    suite.addTestSuite(TaskMgmtInstanceDbTest.class);
    suite.addTestSuite(TaskTimerExecutionDbTest.class);
    suite.addTestSuite(TaskVariableAccessDbTest.class);
    suite.addTestSuite(TaskVariablesDbTest.class);
    //$JUnit-END$
    return suite;
  }
}
