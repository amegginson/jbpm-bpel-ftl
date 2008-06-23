package org.jbpm.graph.exe;

import org.jbpm.db.AbstractDbTestCase;
import org.jbpm.graph.def.ProcessDefinition;

public class BusinessKeyDbTest extends AbstractDbTestCase {

  public void testSimpleBusinessKey() {
    ProcessDefinition processDefinition = new ProcessDefinition("businesskeytest");
    jbpmContext.deployProcessDefinition(processDefinition);
    
    newTransaction();
    
    ProcessInstance processInstance = jbpmContext.newProcessInstanceForUpdate("businesskeytest");
    processInstance.setKey("businesskey1");

    newTransaction();
    
    processInstance = jbpmContext.newProcessInstanceForUpdate("businesskeytest");
    processInstance.setKey("businesskey2");

    newTransaction();

    processDefinition = jbpmContext.getGraphSession().findLatestProcessDefinition("businesskeytest");
    processInstance = jbpmContext.getProcessInstance(processDefinition, "businesskey1");
    assertEquals("businesskey1", processInstance.getKey());
  }

/*
  
  problematic unique key constraint.  see also http://jira.jboss.com/jira/browse/JBPM-913
  
  public void testDuplicateBusinessKey() {
    ProcessDefinition processDefinition = new ProcessDefinition("businesskeytest");
    jbpmContext.deployProcessDefinition(processDefinition);
    
    newTransaction();
    
    jbpmContext.newProcessInstanceForUpdate("businesskeytest").setKey("duplicatekey");
    jbpmContext.newProcessInstanceForUpdate("businesskeytest").setKey("duplicatekey");
    
    try {
      jbpmContext.close();
      fail("expected exception");
    } catch (JbpmException e) {
      e.printStackTrace();
    } catch (Throwable t) {
      t.printStackTrace();
    }

    beginSessionTransaction();
    jbpmContext.getSession();
  }
*/
  public void testDuplicateBusinessKeyInDifferentProcesses() {
    ProcessDefinition processDefinition = new ProcessDefinition("businesskeytest1");
    jbpmContext.deployProcessDefinition(processDefinition);
    
    processDefinition = new ProcessDefinition("businesskeytest2");
    jbpmContext.deployProcessDefinition(processDefinition);
    
    newTransaction();
    
    jbpmContext.newProcessInstanceForUpdate("businesskeytest1").setKey("duplicatekey");
    jbpmContext.newProcessInstanceForUpdate("businesskeytest2").setKey("duplicatekey");
  }
}
