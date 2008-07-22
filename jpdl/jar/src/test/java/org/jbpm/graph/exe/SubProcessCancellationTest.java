package org.jbpm.graph.exe;

import java.util.Iterator;

import org.jbpm.db.AbstractDbTestCase;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.taskmgmt.exe.TaskInstance;

public class SubProcessCancellationTest extends AbstractDbTestCase {
  
  void deployProcessDefinitions() {
    ProcessDefinition subProcess = ProcessDefinition.parseXmlString(
      "<process-definition name='sub'>" +
      "  <start-state>" +
      "    <transition to='wait' />" +
      "  </start-state>" +
      "  <task-node name='wait'>" +
      "    <task>" +
      "      <timer duedate='2 seconds' class='MyTimerClass' />" +
      "    </task>" +
      "    <transition to='end' />" +
      "  </task-node>" +
      "  <end-state name='end' />" +
      "</process-definition>"
    );
    jbpmContext.deployProcessDefinition(subProcess);
    
    ProcessDefinition superProcess = ProcessDefinition.parseXmlString(
      "<process-definition name='super'>" +
      "  <start-state>" +
      "    <transition to='subprocess' />" +
      "  </start-state>" +
      "  <process-state name='subprocess'>" +
      "    <sub-process name='sub' />" +
      "    <transition to='s'/>" +
      "  </process-state>" +
      "  <state name='s' />" +
      "</process-definition>"
    );
    jbpmContext.deployProcessDefinition(superProcess);
    
    newTransaction();
  }
  
  public void testWithSubProcess() {
    deployProcessDefinitions();
    
    ProcessInstance pi = jbpmContext.newProcessInstanceForUpdate("super");
    pi.signal();
    
    ProcessInstance subPi = pi.getRootToken().getSubProcessInstance();
    assertEquals("wait", subPi.getRootToken().getNode().getName());
    
    newTransaction();

    pi = jbpmContext.loadProcessInstance(pi.getId());
    subPi = pi.getRootToken().getSubProcessInstance();
    pi.end();
    pi.getTaskMgmtInstance().endAll();
    jbpmContext.save(pi);

    assertTrue(pi.hasEnded());
    assertTrue(subPi.hasEnded());
    Iterator iter = subPi.getTaskMgmtInstance().getTaskInstances().iterator();
    while (iter.hasNext()) {
      TaskInstance taskInstance = (TaskInstance) iter.next();
      assertFalse(taskInstance.isSignalling());
      assertFalse(taskInstance.hasEnded());
    }
  }
}
