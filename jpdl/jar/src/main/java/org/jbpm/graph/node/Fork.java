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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.jbpm.JbpmException;
import org.jbpm.graph.action.Script;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.Token;
import org.jbpm.jpdl.xml.JpdlXmlReader;
import org.jbpm.jpdl.xml.Parsable;

/**
 * specifies configurable fork behaviour.
 * 
 * <p>if this fork behaviour is not sufficient for your needs, consider 
 * writing your own custom TokenHandler.
 * </p>
 * 
 * <p>this forkhandler can be configured in 3 ways :
 * <ul>
 *   <li>without configuration : in that case the fork will launch one 
 *       new sub-token over each of the leaving tranisions of the fork 
 *       node.
 *   </li>
 *   <li>a script : can be used to calculate a collection of transition 
 *       names at runtime.  if a script is configured, the script must have 
 *       exactly one variable with 'write' access.  that variable 
 *       should be assigned a java.util.Collection in the script 
 *       expression.
 *   </li>
 * </ul>
 * </p>
 */
public class Fork extends Node implements Parsable {

  private static final long serialVersionUID = 1L;

  /**
   * a script that calculates the transitionNames at runtime.
   */
  Script script = null;

  public Fork() {
  }

  public Fork(String name) {
    super(name);
  }

  public void read(Element forkElement, JpdlXmlReader jpdlReader) {
    Element scriptElement = forkElement.element("script");
    if (scriptElement!=null) {
      log.warn("KNOWN LIMITATION: the script in a fork is not persisted.  script in fork might be removed in later versions of jPDL");
      script = new Script();
      script.read(scriptElement, jpdlReader);
    }
  }

  public void execute(ExecutionContext executionContext) {
    Token token = executionContext.getToken();
    
    // phase one: collect all the transitionNames
    Collection transitionNames = null; 
    List forkedTokens = new ArrayList();

    // by default, the fork spawns a token for each leaving transition
    if (script==null) {
      transitionNames = getLeavingTransitionsMap().keySet();

    } else { // a script is specified  
      // if a script is specified, use that script to calculate the set 
      // of leaving transitions to be used for forking tokens.
      Map outputMap = null;
      try {
        outputMap = script.eval(token);
      } catch (Exception e) {
        this.raiseException(e, executionContext);
      }
      if (outputMap.size()==1) {
        Object result = outputMap.values().iterator().next();
        if (result instanceof Collection) {
          transitionNames = (Collection) result;
        }
      }
      if (transitionNames==null) {
        throw new JbpmException("script for fork '"+name+"' should produce one collection (in one writable variable): "+transitionNames);
      }
    }
    
    // TODO add some way of blocking the current token here and disable that blocking when the join reactivates this token
    // Then an exception can be thrown by in case someone tries to signal a token that is waiting in a fork.
    // Suspend and resume can NOT be used for this since that will also suspend any related timers, tasks and messages...
    // So a separate kind of blocking should be created for this. 
    // @see also http://jira.jboss.com/jira/browse/JBPM-642

    // phase two: create forked tokens for the collected transition names
    Iterator iter = transitionNames.iterator();
    while (iter.hasNext()) {
      String transitionName = (String) iter.next();
      forkedTokens.add(createForkedToken(token, transitionName));
    }

    // phase three: launch child tokens from the fork over the given transitions
    iter = forkedTokens.iterator();
    while( iter.hasNext() ) {
      ForkedToken forkedToken = (ForkedToken) iter.next();
      Token childToken = forkedToken.token;
      String leavingTransitionName = forkedToken.leavingTransitionName;
      ExecutionContext childExecutionContext = new ExecutionContext(childToken);
      if (leavingTransitionName!=null) {
        leave(childExecutionContext, leavingTransitionName);
      } else {
        leave(childExecutionContext);
      }
    }
  }

  protected ForkedToken createForkedToken(Token parent, String transitionName) {
    // instantiate the new token
    Token childToken = new Token(parent, getTokenName(parent, transitionName));

    // create a forked token
    ForkedToken forkedToken = null;
    forkedToken = new ForkedToken(childToken, transitionName);
    
    return forkedToken;
  }

  protected String getTokenName(Token parent, String transitionName) {
    String tokenName = null;
    if ( transitionName != null ) {
      if ( ! parent.hasChild( transitionName ) ) {
        tokenName = transitionName;
      } else {
        int i = 2;
        tokenName = transitionName + Integer.toString( i );
        while ( parent.hasChild( tokenName ) ) {
          i++;
          tokenName = transitionName + Integer.toString( i );
        }
      }
    } else { // no transition name
      int size = ( parent.getChildren()!=null ? parent.getChildren().size()+1 : 1 );
      tokenName = Integer.toString(size);
    }
    return tokenName;
  }

  public Script getScript() {
    return script;
  }
  public void setScript(Script script) {
    this.script = script;
  }
  
  static class ForkedToken {
    Token token = null;
    String leavingTransitionName = null;
    public ForkedToken(Token token, String leavingTransitionName) {
      this.token = token;
      this.leavingTransitionName = leavingTransitionName;
    }
  }
  
  private static Log log = LogFactory.getLog(Fork.class);
}
