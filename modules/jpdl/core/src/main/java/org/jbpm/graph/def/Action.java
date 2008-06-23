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
package org.jbpm.graph.def;

import java.io.Serializable;
import java.util.Map;

import org.dom4j.Element;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.instantiation.Delegation;
import org.jbpm.jpdl.el.impl.JbpmExpressionEvaluator;
import org.jbpm.jpdl.xml.JpdlXmlReader;
import org.jbpm.jpdl.xml.Parsable;
import org.jbpm.util.EqualsUtil;

public class Action implements ActionHandler, Parsable, Serializable {

  private static final long serialVersionUID = 1L;
  
  long id = 0;
  protected String name = null;
  protected boolean isPropagationAllowed = true;
  protected boolean isAsync = false;
  protected boolean isAsyncExclusive = false;
  protected Action referencedAction = null;
  protected Delegation actionDelegation  = null;
  protected String actionExpression = null;
  protected Event event = null;
  protected ProcessDefinition processDefinition = null;

  public Action() {
  }

  public Action(Delegation actionDelegate) {
    this.actionDelegation = actionDelegate;
  }
  
  public String toString() {
    String toString = null;
    if (name!=null) {
      toString = "action["+name+"]";
    } else if (actionExpression!=null) {
      toString = "action["+actionExpression+"]";
    } else {
      String className = getClass().getName(); 
      className = className.substring(className.lastIndexOf('.')+1);
      if (name!=null) {
        toString = className+"("+name+")";
      } else {
        toString = className+"("+Integer.toHexString(System.identityHashCode(this))+")";
      }
    }
    return toString;
  }

  public void read(Element actionElement, JpdlXmlReader jpdlReader) {
    String expression = actionElement.attributeValue("expression");
    if (expression!=null) {
      actionExpression = expression;

    } else if (actionElement.attribute("ref-name")!=null) {
      jpdlReader.addUnresolvedActionReference(actionElement, this);

    } else if (actionElement.attribute("class")!=null) {
      actionDelegation = new Delegation();
      actionDelegation.read(actionElement, jpdlReader);
      
    } else {
      jpdlReader.addWarning("action does not have class nor ref-name attribute "+actionElement.asXML());
    }

    String acceptPropagatedEvents = actionElement.attributeValue("accept-propagated-events");
    if ("false".equalsIgnoreCase(acceptPropagatedEvents)
        || "no".equalsIgnoreCase(acceptPropagatedEvents) 
        || "off".equalsIgnoreCase(acceptPropagatedEvents)) {
      isPropagationAllowed = false;
    }

    String asyncText = actionElement.attributeValue("async");
    if ("true".equalsIgnoreCase(asyncText)) {
      isAsync = true;
    } else if ("exclusive".equalsIgnoreCase(asyncText)) {
      isAsync = true;
      isAsyncExclusive = true;
    }
  }

  public void write(Element actionElement) {
    if (actionDelegation!=null) {
      actionDelegation.write(actionElement);
    }
  }

  public void execute(ExecutionContext executionContext) throws Exception {
    if (referencedAction!=null) {
      referencedAction.execute(executionContext);

    } else if (actionExpression!=null) {
      JbpmExpressionEvaluator.evaluate(actionExpression, executionContext);

    } else if (actionDelegation!=null) {
      ActionHandler actionHandler = (ActionHandler)actionDelegation.getInstance();
      actionHandler.execute(executionContext);
    }
  }

  public void setName(String name) {
    // if the process definition is already set
    if (processDefinition!=null) {
      // update the process definition action map
      Map actionMap = processDefinition.getActions();
      // the != string comparison is to avoid null pointer checks.  it is no problem if the body is executed a few times too much :-)
      if ( (this.name != name)
           && (actionMap!=null) ) {
        actionMap.remove(this.name);
        actionMap.put(name, this);
      }
    }

    // then update the name
    this.name = name;
  }
  
  // equals ///////////////////////////////////////////////////////////////////
  // hack to support comparing hibernate proxies against the real objects
  // since this always falls back to ==, we don't need to overwrite the hashcode
  public boolean equals(Object o) {
    return EqualsUtil.equals(this, o);
  }
  
  // getters and setters //////////////////////////////////////////////////////

  public boolean acceptsPropagatedEvents() {
    return isPropagationAllowed;
  }

  public boolean isPropagationAllowed() {
    return isPropagationAllowed;
  }
  public void setPropagationAllowed(boolean isPropagationAllowed) {
    this.isPropagationAllowed = isPropagationAllowed;
  }

  public long getId() {
    return id;
  }
  public String getName() {
    return name;
  }
  public Event getEvent() {
    return event;
  }
  public ProcessDefinition getProcessDefinition() {
    return processDefinition;
  }
  public void setProcessDefinition(ProcessDefinition processDefinition) {
    this.processDefinition = processDefinition;
  }
  public Delegation getActionDelegation() {
    return actionDelegation;
  }
  public void setActionDelegation(Delegation instantiatableDelegate) {
    this.actionDelegation = instantiatableDelegate;
  }
  public Action getReferencedAction() {
    return referencedAction;
  }
  public void setReferencedAction(Action referencedAction) {
    this.referencedAction = referencedAction;
  }
  public boolean isAsync() {
    return isAsync;
  }
  public boolean isAsyncExclusive() {
    return isAsyncExclusive;
  }
  public String getActionExpression() {
    return actionExpression;
  }
  public void setActionExpression(String actionExpression) {
    this.actionExpression = actionExpression;
  }
  public void setEvent(Event event) {
    this.event = event;
  }
  public void setAsync(boolean isAsync) {
    this.isAsync = isAsync;
  }
}
