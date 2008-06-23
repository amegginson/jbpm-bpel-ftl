package org.jbpm.mail;

import java.util.Arrays;
import java.util.Iterator;

import junit.framework.TestCase;

import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.mail.AddressResolver;
import org.jbpm.mail.Mail;
import org.jbpm.taskmgmt.def.Swimlane;
import org.jbpm.taskmgmt.exe.SwimlaneInstance;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;

public class MailTest extends TestCase {
  
  public static class TestAddressResolver implements AddressResolver {
    private static final long serialVersionUID = 1L;
    public Object resolveAddress(String actorId) {
      return actorId+"@example.domain";
    }
  }

  static JbpmConfiguration jbpmConfiguration = JbpmConfiguration.parseXmlString(
    "<jbpm-configuration>" +
    "  <jbpm-context />" +
    "  <string name='resource.mail.properties' value='org/jbpm/mail/test.mail.properties' />" +
    "  <bean name='jbpm.mail.address.resolver' class='org.jbpm.mail.MailTest$TestAddressResolver' singleton='true' />" +
    "</jbpm-configuration>"
  );

  SimpleSmtpServer server = null;
  JbpmContext jbpmContext = null;

  public void setUp() {
    server = SimpleSmtpServer.start(23583);
    jbpmContext = jbpmConfiguration.createJbpmContext();
  }

  public void tearDown() {
    jbpmContext.close();
    server.stop();
  }

  public void testWithoutAddressResolving() {
    String to = "sample.shipper@example.domain";
    String subject = "latest news";
    String text = "roy is assurancetourix";
    
    Mail mail = new Mail(null, null, to, subject, text);
    mail.send();
    
    assertTrue(server.getReceivedEmailSize() == 1);
    Iterator emailIter = server.getReceivedEmail();
    SmtpMessage email = (SmtpMessage) emailIter.next();
    assertEquals("latest news", email.getHeaderValue("Subject"));
    assertEquals("roy is assurancetourix", email.getBody());
    assertEquals("sample.shipper@example.domain", email.getHeaderValue("To"));
  }

  public void testMailWithAddressResolving() {
    String actors = "manager";
    String subject = "latest news";
    String text = "roy is assurancetourix";
    
    Mail mail = new Mail(null, actors, null, subject, text);
    mail.send();
    
    assertTrue(server.getReceivedEmailSize() == 1);
    Iterator emailIter = server.getReceivedEmail();
    SmtpMessage email = (SmtpMessage) emailIter.next();
    assertEquals("latest news", email.getHeaderValue("Subject"));
    assertEquals("roy is assurancetourix", email.getBody());
    assertEquals("manager@example.domain", email.getHeaderValue("To"));
  }
  
  public void testMailWithBccAddress() {
    String to = "sample.shipper@example.domain";
    String bcc = "bcc@example.domain";
    String subject = "latest news";
    String text = "roy is assurancetourix";
    
    Mail mail = new Mail(null, null, to, null, bcc, subject, text);
    mail.send();
    
    assertTrue(server.getReceivedEmailSize() == 2);
    Iterator emailIter = server.getReceivedEmail();
    SmtpMessage email1 = (SmtpMessage) emailIter.next();
    assertEquals("latest news", email1.getHeaderValue("Subject"));
    assertEquals("roy is assurancetourix", email1.getBody());
    assertEquals("sample.shipper@example.domain", email1.getHeaderValue("To"));

    SmtpMessage email2 = (SmtpMessage) emailIter.next();
    assertEquals("latest news", email2.getHeaderValue("Subject"));
    assertEquals("roy is assurancetourix", email2.getBody());
    assertEquals("bcc@example.domain", email2.getHeaderValue("To"));
}  
  
  public void testMailNodeAttributes() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition>" +
      "  <start-state>" +
      "    <transition to='send email' />" +
      "  </start-state>" +
      "  <mail-node name='send email' actors='george' subject='readmylips' text='nomoretaxes'>" +
      "    <transition to='end' />" +
      "  </mail-node>" +
      "  <end-state name='end' />" +
      "</process-definition>"
    );
    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    processInstance.signal();
    
    assertTrue(server.getReceivedEmailSize() == 1);
    Iterator emailIter = server.getReceivedEmail();
    SmtpMessage email = (SmtpMessage) emailIter.next();
    assertEquals("readmylips", email.getHeaderValue("Subject"));
    assertEquals("nomoretaxes", email.getBody());
    assertEquals("george@example.domain", email.getHeaderValue("To"));
  }
  
  public void testMailNodeElements() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition>" +
      "  <start-state>" +
      "    <transition to='send email' />" +
      "  </start-state>" +
      "  <mail-node name='send email' actors='george'>" +
      "    <subject>readmylips</subject>" +
      "    <text>nomoretaxes</text>" +
      "    <transition to='end' />" +
      "  </mail-node>" +
      "  <end-state name='end' />" +
      "</process-definition>"
    );
    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    processInstance.signal();
    
    assertTrue(server.getReceivedEmailSize() == 1);
    Iterator emailIter = server.getReceivedEmail();
    SmtpMessage email = (SmtpMessage) emailIter.next();
    assertEquals("readmylips", email.getHeaderValue("Subject"));
    assertEquals("nomoretaxes", email.getBody());
    assertEquals("george@example.domain", email.getHeaderValue("To"));
  }

  public void testMailActionAttributes() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition>" +
      "  <start-state>" +
      "    <transition to='end'>" +
      "      <mail name='send email' actors='george' subject='readmylips' text='nomoretaxes' />" +
      "    </transition>" +
      "  </start-state>" +
      "  <end-state name='end' />" +
      "</process-definition>"
    );
    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    processInstance.signal();
    
    assertTrue(server.getReceivedEmailSize() == 1);
    Iterator emailIter = server.getReceivedEmail();
    SmtpMessage email = (SmtpMessage) emailIter.next();
    assertEquals("readmylips", email.getHeaderValue("Subject"));
    assertEquals("nomoretaxes", email.getBody());
    assertEquals("george@example.domain", email.getHeaderValue("To"));
  }

  public void testMailActionElements() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition>" +
      "  <start-state>" +
      "    <transition to='end'>" +
      "      <mail actors='george'>" +
      "        <subject>readmylips</subject>" +
      "        <text>nomoretaxes</text>" +
      "      </mail>" +
      "    <transition to='end' />" +
      "    </transition>" +
      "  </start-state>" +
      "  <end-state name='end' />" +
      "</process-definition>"
    );
    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    processInstance.signal();
    
    assertTrue(server.getReceivedEmailSize() == 1);
    Iterator emailIter = server.getReceivedEmail();
    SmtpMessage email = (SmtpMessage) emailIter.next();
    assertEquals("readmylips", email.getHeaderValue("Subject"));
    assertEquals("nomoretaxes", email.getBody());
    assertEquals("george@example.domain", email.getHeaderValue("To"));
  }

  public void testMultipleRecipients() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition>" +
      "  <start-state>" +
      "    <transition to='end'>" +
      "      <mail name='send email' actors='george; barbara; suzy' subject='readmylips' text='nomoretaxes' />" +
      "    </transition>" +
      "  </start-state>" +
      "  <end-state name='end' />" +
      "</process-definition>"
    );
    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    processInstance.signal();
    
    assertEquals(1, server.getReceivedEmailSize());
    Iterator emailIter = server.getReceivedEmail();
    SmtpMessage email = (SmtpMessage) emailIter.next();
    assertEquals("readmylips", email.getHeaderValue("Subject"));
    assertEquals("nomoretaxes", email.getBody());
    assertEquals(Arrays.asList(new String[]{"george@example.domain", "barbara@example.domain", "suzy@example.domain"}), Arrays.asList(email.getHeaderValues("To")));
  }

  public void testMailWithoutAddressResolving() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition>" +
      "  <start-state>" +
      "    <transition to='end'>" +
      "      <mail name='send email' to='george@humpydumpy.gov; spiderman@hollywood.ca.us' subject='readmylips' text='nomoretaxes' />" +
      "    </transition>" +
      "  </start-state>" +
      "  <end-state name='end' />" +
      "</process-definition>"
    );
    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    processInstance.signal();
    
    assertEquals(1, server.getReceivedEmailSize());
    Iterator emailIter = server.getReceivedEmail();
    SmtpMessage email = (SmtpMessage) emailIter.next();
    assertEquals(Arrays.asList(new String[]{"george@humpydumpy.gov", "spiderman@hollywood.ca.us"}), Arrays.asList(email.getHeaderValues("To")));
  }
  
  public static class User {
	String email;
	public User(String email) {
      this.email = email;
	}
	public String getEmail() {
	  return email;
	}
  }

  public void testToVariableExpression() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition>" +
      "  <start-state>" +
      "    <transition to='end'>" +
      "      <mail name='send email' to='#{user.email}' subject='s' text='t' />" +
      "    </transition>" +
      "  </start-state>" +
      "  <end-state name='end' />" +
      "</process-definition>"
    );
    
    User mrNobody = new User("hucklebuck@example.domain");
    
    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    processInstance.getContextInstance().setVariable("user", mrNobody);
    processInstance.signal();
    
    assertEquals(1, server.getReceivedEmailSize());
    Iterator emailIter = server.getReceivedEmail();
    SmtpMessage email = (SmtpMessage) emailIter.next();
    assertEquals("hucklebuck@example.domain", email.getHeaderValue("To"));
  }

  public void testToSwimlaneExpression() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition>" +
      "  <start-state>" +
      "    <transition to='end'>" +
      "      <mail name='send email' actors='#{initiator}' subject='s' text='t' />" +
      "    </transition>" +
      "  </start-state>" +
      "  <end-state name='end' />" +
      "</process-definition>"
    );
    
    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    Swimlane initiator = new Swimlane("initiator");
    SwimlaneInstance initiatorInstance = new SwimlaneInstance(initiator);
    initiatorInstance.setActorId("huckelberry");
    processInstance.getTaskMgmtInstance().addSwimlaneInstance(initiatorInstance);
    processInstance.signal();
    
    assertEquals(1, server.getReceivedEmailSize());
    Iterator emailIter = server.getReceivedEmail();
    SmtpMessage email = (SmtpMessage) emailIter.next();
    assertEquals("huckelberry@example.domain", email.getHeaderValue("To"));
  }

  public void testSubjectExpression() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition>" +
      "  <start-state>" +
      "    <transition to='end'>" +
      "      <mail name='send email' actors='me' subject='your ${item} order' text='t' />" +
      "    </transition>" +
      "  </start-state>" +
      "  <end-state name='end' />" +
      "</process-definition>"
    );
    
    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    processInstance.getContextInstance().setVariable("item", "cookies");
    processInstance.signal();
    
    assertEquals(1, server.getReceivedEmailSize());
    Iterator emailIter = server.getReceivedEmail();
    SmtpMessage email = (SmtpMessage) emailIter.next();
    assertEquals("your cookies order", email.getHeaderValue("Subject"));
  }

  public void testTextExpression() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition>" +
      "  <start-state>" +
      "    <transition to='end'>" +
      "      <mail name='send email' actors='me' text='your ${item} order' />" +
      "    </transition>" +
      "  </start-state>" +
      "  <end-state name='end' />" +
      "</process-definition>"
    );
    
    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    processInstance.getContextInstance().setVariable("item", "cookies");
    processInstance.signal();
    
    assertEquals(1, server.getReceivedEmailSize());
    Iterator emailIter = server.getReceivedEmail();
    SmtpMessage email = (SmtpMessage) emailIter.next();
    assertEquals("your cookies order", email.getBody());
  }
}
