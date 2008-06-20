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
package org.jbpm.graph.action;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.jbpm.context.def.VariableAccess;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.DelegationException;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.Token;
import org.jbpm.jpdl.xml.JpdlXmlReader;
import org.jbpm.jpdl.xml.Parsable;

import bsh.Interpreter;
import bsh.TargetError;

public class Script extends Action implements Parsable {
  
  private static final long serialVersionUID = 1L;
  
  protected String expression = null;
  protected Set variableAccesses = null;

  public void read(Element scriptElement, JpdlXmlReader jpdlReader) {
    if (scriptElement.isTextOnly()) {
      expression = scriptElement.getText();
    } else {
      this.variableAccesses = new HashSet(jpdlReader.readVariableAccesses(scriptElement));
      expression = scriptElement.element("expression").getText();
    }
  }

  public void execute(ExecutionContext executionContext) throws Exception {
    Map outputMap = eval(executionContext);
    setVariables(outputMap, executionContext);
  }

  public Map eval(Token token) throws Exception {
    return eval(new ExecutionContext(token));
  }

  public Map eval(ExecutionContext executionContext) throws Exception {
    Map inputMap = createInputMap(executionContext);
    Set outputNames = getOutputNames();
    return eval(inputMap, outputNames);
  }

  public Map createInputMap(ExecutionContext executionContext) {
    Token token = executionContext.getToken();

    Map inputMap = new HashMap();
    inputMap.put( "executionContext", executionContext );
    inputMap.put( "token", token );
    inputMap.put( "node", executionContext.getNode() );
    inputMap.put( "task", executionContext.getTask() );
    inputMap.put( "taskInstance", executionContext.getTaskInstance() );
    
    // if no readable variableInstances are specified, 
    ContextInstance contextInstance = executionContext.getContextInstance();
    if (! hasReadableVariable()) {
      // we copy all the variableInstances of the context into the interpreter 
      Map variables = contextInstance.getVariables(token);
      if ( variables != null ) {
        Iterator iter = variables.entrySet().iterator();
        while( iter.hasNext() ) {
          Map.Entry entry = (Map.Entry) iter.next();
          String variableName = (String) entry.getKey();
          Object variableValue = entry.getValue();
          inputMap.put(variableName, variableValue);
        }
      }

    } else {
      // we only copy the specified variableInstances into the interpreterz
      Iterator iter = variableAccesses.iterator();
      while (iter.hasNext()) {
        VariableAccess variableAccess = (VariableAccess) iter.next();
        if (variableAccess.isReadable()) {
          String variableName = variableAccess.getVariableName();
          String mappedName = variableAccess.getMappedName();
          Object variableValue = contextInstance.getVariable(variableName, token);
          inputMap.put(mappedName, variableValue);
        }
      }
    }
    
    return inputMap;
  }

  public Map eval(Map inputMap, Set outputNames) throws Exception {
    Map outputMap = new HashMap();
    
    try {
      log.debug("script input: "+inputMap);
      Interpreter interpreter = new Interpreter();
      Iterator iter = inputMap.keySet().iterator();
      while (iter.hasNext()) {
        String inputName = (String) iter.next();
        Object inputValue = inputMap.get(inputName);
        interpreter.set(inputName, inputValue);
     }
      interpreter.eval(expression);
      iter = outputNames.iterator();
      while (iter.hasNext()) {
        String outputName = (String) iter.next();
        Object outputValue = interpreter.get(outputName);
        outputMap.put(outputName, outputValue);
      }
      log.debug("script output: "+outputMap);
    } catch (TargetError e) {
      throw new DelegationException("script evaluation exception", e.getTarget());
    } catch (Exception e) {
      log.warn("exception during evaluation of script expression", e);
      // try to throw the cause of the EvalError
      if (e.getCause() instanceof Exception) {
        throw (Exception) e.getCause();
      } else if (e.getCause() instanceof Error) {
        throw (Error) e.getCause();
      } else {
        throw e;
      }
    }

    return outputMap;
  }

  public void addVariableAccess(VariableAccess variableAccess) {
    if (variableAccesses==null) variableAccesses = new HashSet();
    variableAccesses.add(variableAccess);
  }

  Set getOutputNames() {
    Set outputNames = new HashSet();
    if (variableAccesses!=null) {
      Iterator iter = variableAccesses.iterator();
      while (iter.hasNext()) {
        VariableAccess variableAccess = (VariableAccess) iter.next();
        if (variableAccess.isWritable()) {
          outputNames.add(variableAccess.getMappedName());
        }
      }
    }
    return outputNames;
  }

  boolean hasReadableVariable() {
    if (variableAccesses==null) return false;
    Iterator iter = variableAccesses.iterator();
    while (iter.hasNext()) {
      VariableAccess variableAccess = (VariableAccess) iter.next();
      if (variableAccess.isReadable()) {
        return true;
      }
    }
    return false;
  }

  void setVariables(Map outputMap, ExecutionContext executionContext) {
    if ( (outputMap!=null)
         && (!outputMap.isEmpty()) 
         && (executionContext!=null)
       ) {
      Map variableNames = getVariableNames();
      ContextInstance contextInstance = executionContext.getContextInstance();
      Token token = executionContext.getToken();
      
      Iterator iter = outputMap.keySet().iterator();
      while (iter.hasNext()) {
        String mappedName = (String) iter.next();
        String variableName = (String) variableNames.get(mappedName);
        contextInstance.setVariable(variableName, outputMap.get(mappedName), token);
      }
    }
  }

  Map getVariableNames() {
    Map variableNames = new HashMap();
    Iterator iter = variableAccesses.iterator();
    while (iter.hasNext()) {
      VariableAccess variableAccess = (VariableAccess) iter.next();
      if (variableAccess.isWritable()) {
        variableNames.put(variableAccess.getMappedName(), variableAccess.getVariableName());
      }
    }
    return variableNames;
  }

  public String getExpression() {
    return expression;
  }
  public void setExpression(String expression) {
    this.expression = expression;
  }
  public Set getVariableAccesses() {
    return variableAccesses;
  }
  public void setVariableAccesses(Set variableAccesses) {
    this.variableAccesses = variableAccesses;
  }
  
  private static final Log log = LogFactory.getLog(Script.class);
}
