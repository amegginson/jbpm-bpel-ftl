package org.jbpm.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.Token;

public class ExecuteNodeJob extends Job {

  private static final long serialVersionUID = 1L;
  
  Node node;

  public ExecuteNodeJob() {
  }

  public ExecuteNodeJob(Token token) {
    super(token);
  }
  
  public boolean execute(JbpmContext jbpmContext) throws Exception {
    log.debug("job["+id+"] executes "+node);
    token.unlock(this.toString());
    ExecutionContext executionContext = new ExecutionContext(token);
    node.execute(executionContext);
    jbpmContext.save(processInstance);
    return true;
  }
  
  public Node getNode() {
    return node;
  }
  public void setNode(Node node) {
    this.node = node;
  }

  private static Log log = LogFactory.getLog(ExecuteNodeJob.class);
}
