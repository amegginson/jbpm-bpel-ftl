package org.jbpm.logging.exe;

import org.hibernate.Query;
import org.jbpm.JbpmConfiguration;
import org.jbpm.db.AbstractDbTestCase;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;

public class LoggingConfigDbTest extends AbstractDbTestCase {
  
  protected JbpmConfiguration getJbpmConfiguration() {
    return JbpmConfiguration.parseResource("org/jbpm/logging/exe/nologging.jbpm.cfg.xml");
  }

  public void testLoggingconfiguration() {
    jbpmContext.deployProcessDefinition(new ProcessDefinition("logging"));
    ProcessInstance processInstance = jbpmContext.newProcessInstance("logging");
    processInstance.getContextInstance().setVariable("a", "1");
    newTransaction();
    
    Query query = session.createQuery("from org.jbpm.logging.log.ProcessLog");
    assertEquals(0, query.list().size());
  }
}
