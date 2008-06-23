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
package org.jbpm.jpdl.patterns;

import junit.framework.TestCase;

import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;

/**
 * http://is.tm.tue.nl/research/patterns/download/swf/pat_2.swf
 */
public class Wfp02ParallelSplitTest extends TestCase {

  public void testParallelSplit() {
    ProcessDefinition pd = ProcessDefinition.parseXmlString(
      "<process-definition>" +
      "  <start-state name='start'>" +
      "    <transition to='and' />" +
      "  </start-state>" +
      "  <fork name='and'>" +
      "    <transition name='first' to='a' />" +
      "    <transition name='second' to='b' />" +
      "  </fork>" +
      "  <state name='a'/>" +
      "  <state name='b'/>" +
      "</process-definition>"
    );

    ProcessInstance pi = new ProcessInstance( pd );
    pi.signal();
    Token root = pi.getRootToken();
    assertNotNull( root );
    
    Token firstToken = root.getChild( "first" );
    assertNotNull( firstToken );
    assertSame( pd.getNode("a"), firstToken.getNode() );
    
    Token secondToken = root.getChild( "second" );
    assertNotNull( secondToken );
    assertSame( pd.getNode("b"), secondToken.getNode() );
  }
}
