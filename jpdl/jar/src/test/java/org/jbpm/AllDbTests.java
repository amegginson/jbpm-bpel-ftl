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
package org.jbpm;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jbpm.bytes.BytesDbTests;
import org.jbpm.context.exe.ContextExeDbTests;
import org.jbpm.context.log.ContextLogDbTests;
import org.jbpm.db.DbTests;
import org.jbpm.graph.action.GraphActionDbTests;
import org.jbpm.graph.def.GraphDefDbTests;
import org.jbpm.graph.exe.GraphExeDbTests;
import org.jbpm.graph.log.GraphLogDbTests;
import org.jbpm.graph.node.GraphNodeDbTests;
import org.jbpm.jpdl.el.JpdlElDbTests;
import org.jbpm.jpdl.exe.JpdlExeDbTests;
import org.jbpm.jpdl.par.JpdlParDbTests;
import org.jbpm.logging.log.LoggingLogDbTests;
import org.jbpm.msg.command.MsgCommandTests;
import org.jbpm.persistence.db.PersistenceDbTests;
import org.jbpm.scenarios.ScenarioDbTests;
import org.jbpm.scheduler.exe.SchedulerExeDbTests;
import org.jbpm.taskmgmt.def.TaskMgmtDefDbTests;
import org.jbpm.taskmgmt.exe.TaskMgmtExeDbTests;
import org.jbpm.taskmgmt.log.TaskMgmtLogDbTests;

public class AllDbTests extends TestCase {

	public static Test suite() throws Exception {
    TestSuite suite = new TestSuite("db tests");

    try {
      // standard schema tests
      suite.addTest(BytesDbTests.suite());
      suite.addTest(DbTests.suite());
      suite.addTest(ContextExeDbTests.suite());
      suite.addTest(ContextLogDbTests.suite());
      suite.addTest(GraphActionDbTests.suite());
      suite.addTest(GraphDefDbTests.suite());
      suite.addTest(GraphExeDbTests.suite());
      suite.addTest(GraphLogDbTests.suite());
      suite.addTest(GraphNodeDbTests.suite());
      suite.addTest(JpdlElDbTests.suite());
      suite.addTest(JpdlExeDbTests.suite());
      suite.addTest(JpdlParDbTests.suite());
      suite.addTest(LoggingLogDbTests.suite());
      suite.addTest(MsgCommandTests.suite());
      suite.addTest(PersistenceDbTests.suite());
      suite.addTest(ScenarioDbTests.suite());
      suite.addTest(SchedulerExeDbTests.suite());
      suite.addTest(TaskMgmtDefDbTests.suite());
      suite.addTest(TaskMgmtExeDbTests.suite());
      suite.addTest(TaskMgmtLogDbTests.suite());
      /*
			 * Removed the JCR test because jackrabbit has too many external
			 * dependencies suite.addTest(JcrDbTests.suite());
			 */

    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
    
    return suite;
  }
}
