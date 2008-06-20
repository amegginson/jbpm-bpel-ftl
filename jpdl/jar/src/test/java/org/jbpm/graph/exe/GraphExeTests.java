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
package org.jbpm.graph.exe;

import org.jbpm.graph.action.ScriptTest;
import org.jbpm.graph.node.EndStateTest;
import org.jbpm.graph.node.ProcessStateTest;

import junit.framework.*;

public class GraphExeTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("org.jbpm.graph.exe");
    //$JUnit-BEGIN$
    suite.addTestSuite(ActionExceptionsTest.class);
    suite.addTestSuite(ActionExecutionTest.class);
    suite.addTestSuite(EndStateTest.class);
    suite.addTestSuite(EventPropagationTest.class);
    suite.addTestSuite(ExceptionHandlingTest.class);
    suite.addTestSuite(InitialNodeTest.class);
    suite.addTestSuite(NodeActionTest.class);
    suite.addTestSuite(ProcessStateTest.class);
    suite.addTestSuite(RuntimeActionsTest.class);
    suite.addTestSuite(ScriptTest.class);
    suite.addTestSuite(SuperStateActionExecutionTest.class);
    suite.addTestSuite(SuperStateTest.class);
    suite.addTestSuite(TokenNameTest.class);
    //$JUnit-END$
    return suite;
  }
}
