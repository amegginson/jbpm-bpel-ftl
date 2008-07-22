package org.jbpm.mail;

import junit.framework.TestCase;

import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.svc.Service;
import org.jbpm.svc.ServiceFactory;

public class RealServerMailTestCase extends TestCase {

  public static class TestAddressResolver implements AddressResolver, ServiceFactory, Service {
    private static final long serialVersionUID = 1L;
    public Object resolveAddress(String actorId) {
      return actorId+"@localhost";
    }
    public Service openService() {
      return this;
    }
    public void close() {
    }
  }


  static JbpmConfiguration jbpmConfiguration = JbpmConfiguration.parseXmlString(
    "<jbpm-configuration>" +
    "  <jbpm-context>" +
    "    <service name='addressresolver' factory='org.jbpm.mail.RealServerMailTestCase$TestAddressResolver' />" +
    "  </jbpm-context>" +
    "  <string name='resource.mail.properties' value='org/jbpm/mail/test.mail.properties' />" +
    "  <bean name='jbpm.variable.resolver' class='org.jbpm.jpdl.el.impl.JbpmVariableResolver' singleton='true' />" +
    "  <string name='resource.hibernate.cfg.xml' value='hibernate.cfg.xml' />" +
    "  <string name='resource.business.calendar' value='org/jbpm/calendar/jbpm.business.calendar.properties' />" +
    "  <string name='resource.default.modules' value='org/jbpm/graph/def/jbpm.default.modules.properties' />" +
    "  <string name='resource.converter' value='org/jbpm/db/hibernate/jbpm.converter.properties' />" +
    "  <string name='resource.action.types' value='org/jbpm/graph/action/action.types.xml' />" +
    "  <string name='resource.node.types' value='org/jbpm/graph/node/node.types.xml' />" +
    "  <string name='resource.parsers' value='org/jbpm/jpdl/par/jbpm.parsers.xml' />" +
    "  <string name='resource.varmapping' value='org/jbpm/context/exe/jbpm.varmapping.xml' />" +
    "  <string name='resource.repository.cfg.xml' value='repository.cfg.xml' />" +
    "</jbpm-configuration>"
  );

  JbpmContext jbpmContext = null;

  public void setUp() {
    jbpmContext = jbpmConfiguration.createJbpmContext();
  }

  public void tearDown() {
    jbpmContext.close();
  }

  
  public void testSimplMail() {
    String actors = "sample.manager@localhost";
    String subject = "latest news";
    String text = "roy is assurancetourix";
    
    Mail mail = new Mail(null, actors, null, subject, text);
    mail.send();
  }
  
  public void testMailNodeElements() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition>" +
      "  <start-state>" +
      "    <transition to='send email' />" +
      "  </start-state>" +
      "  <mail-node name='send email' actors='manager'>" +
      "    <subject>readmylips</subject>" +
      "    <text><![CDATA[ no \n more \n taxes ]]></text>" +
      "    <transition to='end' />" +
      "  </mail-node>" +
      "  <end-state name='end' />" +
      "</process-definition>"
    );
    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    processInstance.signal();
  }

}
