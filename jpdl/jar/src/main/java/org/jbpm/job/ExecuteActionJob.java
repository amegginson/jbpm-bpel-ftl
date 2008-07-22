package org.jbpm.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.Token;

public class ExecuteActionJob extends Job {

  private static final long serialVersionUID = 1L;

  Action action;
  
  public ExecuteActionJob() {
  }
  
  public ExecuteActionJob(Token token) {
    super(token);
  }
  
  public boolean execute(JbpmContext jbpmContext) throws Exception {
    log.debug("job["+id+"] executes "+action);
    ExecutionContext executionContext = new ExecutionContext(token);
    executionContext.setAction(action);
    executionContext.setEvent(action.getEvent());
    
    Node node = (token!=null ? token.getNode() : null);
    if (node!=null) {
      node.executeAction(action, executionContext);
    } else {
      action.execute(executionContext);
    }

    jbpmContext.save(token);

    return true;
  }

  public Action getAction() {
    return action;
  }
  public void setAction(Action action) {
    this.action = action;
  }
  
  private static Log log = LogFactory.getLog(ExecuteActionJob.class);
}
