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

import junit.framework.*;

import org.jbpm.context.def.*;
import org.jbpm.context.exe.*;
import org.jbpm.graph.action.Script;
import org.jbpm.graph.def.*;
import org.jbpm.graph.exe.*;
import org.jbpm.graph.node.*;

/**
 * http://is.tm.tue.nl/research/patterns/download/swf/pat_6.swf
 */
public class Wfp06MultiChoiceTest extends TestCase {
  
  private static ProcessDefinition multiChoiceProcessDefinition = createMultiChoiceProcessDefinition();

  public static ProcessDefinition createMultiChoiceProcessDefinition() {
    ProcessDefinition pd = new ProcessDefinition(
      new String[]{"start-state start",
                   "state a",
                   "fork multichoice",
                   "state b",
                   "state c",
                   "join syncmerge",
                   "end-state end"}, 
      new String[]{"start --> a",
                   "a --> multichoice",
                   "multichoice --to b--> b",
                   "multichoice --to c--> c",
                   "b --> syncmerge",
                   "c --> syncmerge",
                   "syncmerge --> end"});
    
    // create the script
    Script script = new Script();
    script.addVariableAccess(new VariableAccess("transitionNames","write",null));
    script.setExpression(
      "transitionNames = new ArrayList();" +
      "if ( scenario == 1 ) {" +
      "  transitionNames.add( \"to b\" );" +
      "} else if ( scenario == 2 ) {" +
      "  transitionNames.add( \"to c\" );" +
      "} else if ( scenario >= 3 ) {" +
      "  transitionNames.add( \"to b\" );" +
      "  transitionNames.add( \"to c\" );" +
      "}" );
    
    // put the script in the multichoice handler
    Fork fork = (Fork) pd.getNode("multichoice");
    fork.setScript( script );
    
    pd.addDefinition( new ContextDefinition() );
    
    return pd;
  }

  public void testMultiChoiceScenario1() {
    ProcessDefinition pd = multiChoiceProcessDefinition;
    Token root = executeScenario(pd, 1);
    Token tokenB = root.getChild("to b"); // the default token names are extracted from the leaving transitions 
    Token tokenC = root.getChild("to c"); // the default token names are extracted from the leaving transitions 
    assertNotNull( tokenB );
    assertNull( tokenC );
    assertEquals( 1, root.getChildren().size() );
    assertSame( pd.getNode("b"), tokenB.getNode() );
  }

  public void testMultiChoiceScenario2() {
    ProcessDefinition pd = multiChoiceProcessDefinition;
    Token root = executeScenario(pd, 2);
    Token tokenB = root.getChild("to b"); // the default token names are extracted from the leaving transitions 
    Token tokenC = root.getChild("to c"); // the default token names are extracted from the leaving transitions 
    assertNull( tokenB );
    assertNotNull( tokenC );
    assertEquals( 1, root.getChildren().size() );
    assertSame( pd.getNode("c"), tokenC.getNode() );
  }

  public void testMultiChoiceScenario3() {
    ProcessDefinition pd = multiChoiceProcessDefinition;
    Token root = executeScenario(pd, 3);
    Token tokenB = root.getChild("to b"); // the default token names are extracted from the leaving transitions 
    Token tokenC = root.getChild("to c"); // the default token names are extracted from the leaving transitions 
    assertNotNull( tokenB );
    assertNotNull( tokenC );
    assertEquals( 2, root.getChildren().size() );
    assertSame( pd.getNode("b"), tokenB.getNode() );
    assertSame( pd.getNode("c"), tokenC.getNode() );
  }

  public static Token executeScenario(ProcessDefinition pd, int scenario) {
    ProcessInstance pi = new ProcessInstance( pd );
    ContextInstance ci = (ContextInstance) pi.getInstance( ContextInstance.class );
    pi.signal();
    Token root = pi.getRootToken();
    assertSame( pd.getNode("a"), root.getNode() );
    ci.setVariable( "scenario", new Integer(scenario) );
    root.signal();
    return root;
  }
}
