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
package org.jbpm.context.exe;

import java.util.*;

import org.jbpm.context.def.*;
import org.jbpm.context.exe.variableinstance.*;
import org.jbpm.db.*;
import org.jbpm.graph.def.*;
import org.jbpm.graph.exe.*;

public class TokenVariableMapDbTest extends AbstractDbTestCase {
  
  ProcessDefinition processDefinition;
  ProcessInstance processInstance;
  Token token;
  ContextInstance contextInstance;
  Map tokenVariableMaps;
  TokenVariableMap tokenVariableMap;
  
  public void setUp() throws Exception {
    super.setUp();
    
    processDefinition = new ProcessDefinition();
    processDefinition.addDefinition(new ContextDefinition());
    graphSession.saveProcessDefinition(processDefinition);
    
    processInstance = new ProcessInstance(processDefinition);
    token = processInstance.getRootToken();
    
    contextInstance = processInstance.getContextInstance();
    tokenVariableMaps = new HashMap();
    tokenVariableMap = contextInstance.getOrCreateTokenVariableMap(token);
    tokenVariableMaps.put(token, tokenVariableMap);
    contextInstance.tokenVariableMaps = tokenVariableMaps;
  }

  public void testTokenVariableMapContextInstance() {
    processInstance = saveAndReload(processInstance);

    token = processInstance.getRootToken();
    contextInstance = processInstance.getContextInstance();
    tokenVariableMaps = contextInstance.tokenVariableMaps;
    tokenVariableMap = (TokenVariableMap) tokenVariableMaps.get(token);
    
    assertSame(contextInstance, tokenVariableMap.getContextInstance());
  }

  public void testTokenVariableMapToken() {
    processInstance = saveAndReload(processInstance);

    token = processInstance.getRootToken();
    contextInstance = processInstance.getContextInstance();
    tokenVariableMaps = contextInstance.tokenVariableMaps;
    tokenVariableMap = (TokenVariableMap) tokenVariableMaps.get(token);
    
    assertSame(token, tokenVariableMap.getToken());
  }

  public void testTokenVariableMapVariableInstances() {
    StringInstance stringVariable = (StringInstance) VariableInstance.create(token, "one", "hello");
    tokenVariableMap.addVariableInstance(stringVariable);
    stringVariable = (StringInstance) VariableInstance.create(token, "two", "world");
    tokenVariableMap.addVariableInstance(stringVariable);

    processInstance = saveAndReload(processInstance);

    token = processInstance.getRootToken();
    contextInstance = processInstance.getContextInstance();
    tokenVariableMaps = contextInstance.tokenVariableMaps;
    tokenVariableMap = (TokenVariableMap) tokenVariableMaps.get(processInstance.getRootToken());
    
    System.out.println(tokenVariableMap.getVariable("one"));
    System.out.println(tokenVariableMap.getVariable("two"));
    
    assertEquals("hello", ((StringInstance)tokenVariableMap.variableInstances.get("one")).getValue() );
    assertEquals("world", ((StringInstance)tokenVariableMap.variableInstances.get("two")).getValue() );
  }
}
