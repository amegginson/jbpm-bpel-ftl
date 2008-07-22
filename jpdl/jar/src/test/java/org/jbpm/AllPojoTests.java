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

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jbpm.bytes.BytesTests;
import org.jbpm.calendar.BusinessCalendarTests;
import org.jbpm.configuration.ObjectFactoryTests;
import org.jbpm.context.exe.ContextExeTests;
import org.jbpm.context.log.ContextLogTests;
import org.jbpm.file.def.FileDefTests;
import org.jbpm.graph.action.GraphActionTests;
import org.jbpm.graph.def.GraphDefTests;
import org.jbpm.graph.exe.GraphExeTests;
import org.jbpm.graph.log.GraphLogTests;
import org.jbpm.graph.node.GraphNodeTests;
import org.jbpm.instantiation.InstantiationTests;
import org.jbpm.jpdl.el.JpdlElTests;
import org.jbpm.jpdl.exe.JpdlExeTests;
import org.jbpm.jpdl.patterns.PatternsTests;
import org.jbpm.jpdl.xml.JpdlXmlTests;
import org.jbpm.logging.exe.LoggingExeTests;
import org.jbpm.persistence.db.PersistenceDbPojoTests;
import org.jbpm.scheduler.exe.SchedulerExeTests;
import org.jbpm.svc.SvcTests;
import org.jbpm.taskmgmt.def.TaskMgmtDefTests;
import org.jbpm.taskmgmt.exe.TaskMgmtExeTests;
import org.jbpm.tdd.TddTests;

public class AllPojoTests extends TestCase {

  public static TestSuite suite() throws Exception {
    TestSuite suite = new TestSuite("pojo tests");

    suite.addTest(BytesTests.suite());
    suite.addTest(BusinessCalendarTests.suite());
    suite.addTest(ContextExeTests.suite());
    suite.addTest(ContextLogTests.suite());
    suite.addTest(FileDefTests.suite());
    suite.addTest(GraphActionTests.suite());
    suite.addTest(GraphDefTests.suite());
    suite.addTest(GraphExeTests.suite());
    suite.addTest(GraphLogTests.suite());
    suite.addTest(GraphNodeTests.suite());
    suite.addTest(JpdlElTests.suite());
    suite.addTest(JpdlExeTests.suite());
    suite.addTest(JpdlXmlTests.suite());
    suite.addTest(PatternsTests.suite());
    suite.addTest(LoggingExeTests.suite());
    // mail tests excluded because of my *uckin* virus scanner that 
    // interferes more then your worse mother-in-law 
    // suite.addTest(MailTests.suite());
    suite.addTest(PersistenceDbPojoTests.suite());
    suite.addTest(SchedulerExeTests.suite());
    suite.addTest(SvcTests.suite());
    suite.addTest(TaskMgmtDefTests.suite());
    suite.addTest(TaskMgmtExeTests.suite());
    suite.addTest(TddTests.suite());
    suite.addTest(InstantiationTests.suite());
    suite.addTest(ObjectFactoryTests.suite());

    return suite;
  }
}
