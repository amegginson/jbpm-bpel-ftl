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


public class ContextSessionDbTest extends AbstractDbTestCase {

  public void testOne() {
    /*
    ProcessDefinition processDefinition = new ProcessDefinition();
    processDefinition.addDefinition(new ContextDefinition());
    graphSession.saveProcessDefinition(processDefinition);
    
    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    ContextInstance contextInstance = processInstance.getContextInstance();
    contextInstance.setVariable("a", new Integer(5));
    graphSession.saveProcessInstance(processInstance);
    
    newTransaction();
    
    Map variableInstances = new HashMap();
    variableInstances.put("a", new Integer(5));
    
    List tokens = findTokensByVariables(variableInstances);
    assertEquals(1, tokens.size());
    */
  }
/*
  private List findTokensByVariables(Map variableInstances) {
    
    // create the variable instances
    Map variableInstances = new HashMap();
    Iterator iter = variableInstances.keySet().iterator();
    while (iter.hasNext()) {
      String variableName = (String) iter.next();
      Object value = variableInstances.get(variableName);
      VariableInstance variableInstance = VariableInstance.createVariableInstance(value.getClass(), jbpmSession);
      variableInstance.setValue(value);
      variableInstances.put(variableName, variableInstance);
    }

    // build the from clause
    String query = "select t \n"+
                   "from org.jbpm.graph.exe.Token as t";
    
    iter = variableInstances.keySet().iterator();
    int i = 0; 
    while (iter.hasNext()) {
      VariableInstance variableInstance = (VariableInstance) variableInstances.get(iter.next());
      String alias = "vi_"+i;
      query += ",\n     "+variableInstance.getClass().getName()+" as "+alias;
      i++;
    }
    
    // build the where clause

    // set the variableInstances

    return null;
  }
*/
}

  