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
package org.jbpm.graph.node;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.jbpm.JbpmException;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.instantiation.Delegation;
import org.jbpm.jpdl.el.impl.JbpmExpressionEvaluator;
import org.jbpm.jpdl.xml.JpdlXmlReader;
import org.jbpm.jpdl.xml.Parsable;

/**
 * decision node.
 */
public class Decision extends Node implements Parsable {
  
  static final String NEWLINE = System.getProperty("line.separator");
  static final String DECISION_CONDITION_RESULT = "decision_condition_result";
  static final long serialVersionUID = 1L;

  List decisionConditions = null;
  Delegation decisionDelegation = null;
  String decisionExpression = null;

  public Decision() {
  }

  public Decision(String name) {
    super(name);
  }

  public void read(Element decisionElement, JpdlXmlReader jpdlReader) {

    String expression = decisionElement.attributeValue("expression");
    Element decisionHandlerElement = decisionElement.element("handler");

    if (expression!=null) {
      decisionExpression = expression;

    } else if (decisionHandlerElement!=null) {
      decisionDelegation = new Delegation();
      decisionDelegation.read(decisionHandlerElement, jpdlReader);
    }
  }

  String getConditionExpression(Element conditionElement) {
    String expression = conditionElement.attributeValue("expression");
    if (expression!=null) {
      return expression;
    } else {
      return conditionElement.getText();
    }
  }

  public void execute(ExecutionContext executionContext) {
    Transition transition = null;
    
    try {
      if (decisionDelegation!=null) {
        DecisionHandler decisionHandler = (DecisionHandler) decisionDelegation.instantiate();
        String transitionName = decisionHandler.decide(executionContext);
        transition = getLeavingTransition(transitionName);
        if (transition==null) {
          throw new JbpmException("decision '"+name+"' selected non existing transition '"+transitionName+"'" );
        }
        
      } else if (decisionExpression!=null) {
        Object result = JbpmExpressionEvaluator.evaluate(decisionExpression, executionContext);
        if (result==null) {
          throw new JbpmException("decision expression '"+decisionExpression+"' returned null");
        }
        String transitionName = result.toString();
        transition = getLeavingTransition(transitionName);
        if (transition==null) {
          throw new JbpmException("decision '"+name+"' selected non existing transition '"+transitionName+"'" );
        }
        
      } else if (decisionConditions!=null && !decisionConditions.isEmpty()) {
        // backwards compatible mode based on separate DecisionCondition's
        Iterator iter = decisionConditions.iterator();
        while (iter.hasNext() && (transition==null)) {
          DecisionCondition decisionCondition = (DecisionCondition) iter.next();
          Object result = JbpmExpressionEvaluator.evaluate(decisionCondition.getExpression(), executionContext);
          if (Boolean.TRUE.equals(result)) {
            String transitionName = decisionCondition.getTransitionName();
            transition = getLeavingTransition(transitionName);
            if (transition!=null) {
              transition.removeConditionEnforcement();
            }
          }
        }
        
      } else {
        // new mode based on conditions in the transition itself 
        Iterator iter = leavingTransitions.iterator();
        while (iter.hasNext() && (transition==null)) {
          Transition candidate = (Transition) iter.next();
          
          String conditionExpression = candidate.getCondition();
          if (conditionExpression!=null) {
            Object result = JbpmExpressionEvaluator.evaluate(conditionExpression, executionContext);
            if (Boolean.TRUE.equals(result)) {
              transition = candidate;
            }
          }
        }

      }

      if (transition==null) {
        transition = getDefaultLeavingTransition();
        log.debug("decision didn't select transition, taking default "+transition);
      }

      // since the decision node evaluates condition expressions, the condition of the 
      // taken transition will always be met.  therefor we can safely turn off the 
      // standard condition enforcement in the transitions after a decision node. 
      transition.removeConditionEnforcement();

    } catch (Exception exception) {
      raiseException(exception, executionContext);
    }

    log.debug("decision "+name+" is taking '"+transition+"'");
    executionContext.leaveNode(transition);
  }

  public List getDecisionConditions() {
    return decisionConditions;
  }
  
  public void setDecisionDelegation(Delegation decisionDelegation) {
    this.decisionDelegation = decisionDelegation;
  }
  
  private static Log log = LogFactory.getLog(Decision.class);
}
