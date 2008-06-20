package org.jbpm.graph.exe;

import org.jbpm.db.AbstractDbTestCase;
import org.jbpm.graph.def.ProcessDefinition;

public final class StateDbTest extends AbstractDbTestCase {

  public void testDbState() throws Exception {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition>" +
      "  <start-state name='zero'>" +
      "    <transition to='one' />" +
      "  </start-state>" +
      "  <state name='one'>" +
      "    <transition to='two' />" +
      "  </state>" +
      "  <state name='two'>" +
      "    <transition to='three' />" +
      "  </state>" +
      "  <state name='three'>" +
      "    <transition to='end' />" +
      "  </state>" +
      "  <end-state name='end' />" +
      "</process-definition>"
    );
    processDefinition = saveAndReload(processDefinition);

    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    long instanceId = processInstance.getId();
    Token rootToken = processInstance.getRootToken();
    assertEquals("zero", rootToken.getNode().getName());
    newTransaction();
    processInstance = jbpmContext.loadProcessInstance(instanceId);
    rootToken = processInstance.getRootToken();
    processInstance.signal();
    assertEquals("one", rootToken.getNode().getName());
    newTransaction();
    processInstance = jbpmContext.loadProcessInstance(instanceId);
    rootToken = processInstance.getRootToken();
    processInstance.signal();
    assertEquals("two", rootToken.getNode().getName());
    newTransaction();
    processInstance = jbpmContext.loadProcessInstance(instanceId);
    rootToken = processInstance.getRootToken();
    processInstance.signal();
    assertEquals("three", rootToken.getNode().getName());
    newTransaction();
    processInstance = jbpmContext.loadProcessInstance(instanceId);
    rootToken = processInstance.getRootToken();
    processInstance.signal();
    assertEquals("end", rootToken.getNode().getName());
  }
}
