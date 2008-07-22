package org.jbpm.context.exe;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.hibernate.Query;
import org.jbpm.db.AbstractDbTestCase;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;

public class VariableQueryDbTest extends AbstractDbTestCase {

  public void testStringVariableQuery() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition name='variables'>" +
      "  <start-state name='start'/>" +
      "</process-definition>"
    );
    jbpmContext.deployProcessDefinition(processDefinition);
    newTransaction();
    
    ProcessInstance one = jbpmContext.newProcessInstanceForUpdate("variables");
    one.getContextInstance().setVariable("category", "overpaid");
    one.getContextInstance().setVariable("duedate", "tomorrow");
    
    ProcessInstance two = jbpmContext.newProcessInstanceForUpdate("variables");
    two.getContextInstance().setVariable("category", "overpaid");
    two.getContextInstance().setVariable("duedate", "yesterday");
    
    ProcessInstance three = jbpmContext.newProcessInstanceForUpdate("variables");
    three.getContextInstance().setVariable("category", "underpaid");
    three.getContextInstance().setVariable("duedate", "today");
    
    newTransaction();
    
    Query query = session.createQuery(
      "select pi " +
      "from org.jbpm.context.exe.variableinstance.StringInstance si " +
      "     join si.processInstance pi " +
      "where si.name = 'category'" +
      "  and si.value = 'overpaid'"
    );
    
    Set expectedPids = new HashSet();
    expectedPids.add(new Long(one.getId()));
    expectedPids.add(new Long(two.getId()));
    
    Set retrievedPids = new HashSet();
    Iterator iter = query.list().iterator();
    while (iter.hasNext()) {
      ProcessInstance pi = (ProcessInstance) iter.next();
      retrievedPids.add(new Long(pi.getId()));
    }
    
    assertEquals(expectedPids, retrievedPids);

    newTransaction();
    
    query = session.createQuery(
      "select pi " +
      "from org.jbpm.context.exe.variableinstance.StringInstance si " +
      "     join si.processInstance pi " +
      "where si.name = 'category'" +
      "  and si.value = 'underpaid'"
    );
    
    expectedPids = new HashSet();
    expectedPids.add(new Long(three.getId()));
    
    retrievedPids = new HashSet();
    iter = query.list().iterator();
    while (iter.hasNext()) {
      ProcessInstance pi = (ProcessInstance) iter.next();
      retrievedPids.add(new Long(pi.getId()));
    }
    
    assertEquals(expectedPids, retrievedPids);
  }
}
