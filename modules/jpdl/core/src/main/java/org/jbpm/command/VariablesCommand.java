package org.jbpm.command;

import java.util.Map;

import org.jbpm.JbpmContext;
import org.jbpm.context.exe.VariableContainer;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.taskmgmt.exe.TaskInstance;

public class VariablesCommand implements Command {
  
  private static final long serialVersionUID = 1L;

  long tokenId = 0;
  long taskInstanceId = 0;
  Map variables = null;
  
  ProcessInstance previousProcessInstance = null;
  Token previousToken = null;
  TaskInstance previousTaskInstance = null;

  public Object execute(JbpmContext jbpmContext) throws Exception {
    VariableContainer variableContainer = getVariableContainer(jbpmContext);
    if ( (variableContainer!=null)
         && (variables!=null)
       ) {
      variableContainer.addVariables(variables);
    }
    return variableContainer;
  }

  protected VariableContainer getVariableContainer(JbpmContext jbpmContext) {
    if (previousProcessInstance!=null) {
      return getVariableContainer(previousProcessInstance.getRootToken());
    }
    if (previousToken!=null) {
      return getVariableContainer(previousToken);
    }
    if (previousTaskInstance!=null) {
      return previousTaskInstance;
    }
    
    if (tokenId!=0) {
      return getVariableContainer(jbpmContext.getToken(tokenId));
    }
    if (taskInstanceId!=0) {
      return jbpmContext.getTaskInstance(taskInstanceId);
    }
    return null;
  }

  protected VariableContainer getVariableContainer(Token token) {
    return token.getProcessInstance().getContextInstance().getTokenVariableMap(token);
  }

  public long getTaskInstanceId() {
    return taskInstanceId;
  }
  public void setTaskInstanceId(long taskInstanceId) {
    this.taskInstanceId = taskInstanceId;
  }
  public long getTokenId() {
    return tokenId;
  }
  public void setTokenId(long tokenId) {
    this.tokenId = tokenId;
  }
  public Map getVariables() {
    return variables;
  }
  public void setVariables(Map variables) {
    this.variables = variables;
  }
}
