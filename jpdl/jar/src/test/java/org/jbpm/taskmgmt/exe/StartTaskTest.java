package org.jbpm.taskmgmt.exe;

import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;

import junit.framework.TestCase;

public class StartTaskTest extends TestCase {

  public void testStartTaskPresent() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition>" +
      "  <start-state>" +
      "    <task name='lets get it started' />" +
      "    <transition to='going steady' />" +
      "  </start-state>" +
      "  <state name='going steady' />" +
      "</process-definition>"
    );
    
    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    
    TaskInstance taskInstance = processInstance.getTaskMgmtInstance().createStartTaskInstance();
    assertNotNull(taskInstance);
    assertEquals("lets get it started", taskInstance.getName());
    taskInstance.end();
    
    assertEquals("going steady", processInstance.getRootToken().getNode().getName());
  }

  public void testStartTaskAbsent() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition>" +
      "  <start-state>" +
      "    <transition to='going steady' />" +
      "  </start-state>" +
      "  <state name='going steady' />" +
      "</process-definition>"
    );
    
    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    
    TaskInstance taskInstance = processInstance.getTaskMgmtInstance().createStartTaskInstance();
    assertNull(taskInstance);
  }

}
