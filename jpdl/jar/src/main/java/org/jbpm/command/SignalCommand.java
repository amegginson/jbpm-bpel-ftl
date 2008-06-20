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
package org.jbpm.command;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmContext;
import org.jbpm.JbpmException;
import org.jbpm.graph.exe.Token;
import org.jbpm.graph.exe.ProcessInstance;

/**
 * Signals a token. After signalling the token is returned
 * 
 * @author ??, Bernd Ruecker
 */
public class SignalCommand implements Command {

    private static final long serialVersionUID = 1L;

    private long tokenId = 0;

    private String transitionName = null;
    
    /**
     * if given, it is checked if the state is as expected. If not, a exception is thrown
     * Ignored if null
     */
    private String expectedStateName = null;

    private Token previousToken = null;

    private ProcessInstance previousProcessInstance = null;

    private Map variables;

    public SignalCommand() {
    }

    public SignalCommand(long tokenId, String transitionName) {
        this.tokenId = tokenId;
        this.transitionName = transitionName;
    }

    public Object execute(JbpmContext jbpmContext) {
        log.debug("executing " + this);
        if (previousProcessInstance != null) {

            if (variables != null && variables.size() > 0)
                previousProcessInstance.getContextInstance().addVariables(variables);

            if (transitionName == null) {
                previousProcessInstance.signal();
            }
            else {
                previousProcessInstance.signal(transitionName);
            }
            return previousProcessInstance.getRootToken();
        }
        else {
            Token token = getToken(jbpmContext);
            
            if (expectedStateName!=null && !expectedStateName.equals( token.getNode().getName() ))
                throw new JbpmException("token is not in expected state '"+expectedStateName+"' but in '" + token.getNode().getName() + "'");

            if (variables != null && variables.size() > 0)
                token.getProcessInstance().getContextInstance().addVariables(variables);

            if (transitionName == null) {
                token.signal();
            }
            else {
                token.signal(transitionName);
            }
            return token;
        }
    }

    protected Token getToken(JbpmContext jbpmContext) {
        if (previousToken != null) {
            return previousToken;
        }
        return jbpmContext.loadTokenForUpdate(tokenId);
    }

    public long getTokenId() {
        return tokenId;
    }

    public void setTokenId(long tokenId) {
        this.tokenId = tokenId;
    }

    public String getTransitionName() {
        return transitionName;
    }

    public void setTransitionName(String transitionName) {
        this.transitionName = transitionName;
    }

    private static Log log = LogFactory.getLog(SignalCommand.class);

    public Map getVariables() {
        return variables;
    }

    public void setVariables(Map variables) {
        this.variables = variables;
    }

    public String getExpectedStateName() {
        return expectedStateName;
    }

    public void setExpectedStateName(String expectedStateName) {
        this.expectedStateName = expectedStateName;
    }
}
