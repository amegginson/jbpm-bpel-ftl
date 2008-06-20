package org.jbpm.persistence.db;

import java.sql.Connection;

import junit.framework.TestCase;

import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.JbpmException;

public class PersistenceDbServiceNoTxTest extends TestCase {

  JbpmConfiguration jbpmConfiguration = null;
  JbpmContext jbpmContext = null;
  MockSessionFactory mockSessionFactory = null;
  
  public void setUp() {
    // for all of these tests, the default configuration is used
    jbpmConfiguration = JbpmConfiguration.parseXmlString(
      "<jbpm-configuration>" +
      "  <jbpm-context>" +
      "    <service name='tx' factory='org.jbpm.tx.TxServiceFactory' />" +
      "    <service name='persistence'>" +
      "      <factory>" +
      "        <bean class='org.jbpm.persistence.db.DbPersistenceServiceFactory'>" +
      "          <field name='isTransactionEnabled'><false /></field>" +
      "        </bean>" +
      "      </factory>" +
      "    </service>" +
      "    <service name='message' factory='org.jbpm.msg.db.DbMessageServiceFactory' />" +
      "    <service name='scheduler' factory='org.jbpm.scheduler.db.DbSchedulerServiceFactory' />" +
      "    <service name='logging' factory='org.jbpm.logging.db.DbLoggingServiceFactory' />" +
      "    <service name='authentication' factory='org.jbpm.security.authentication.DefaultAuthenticationServiceFactory' />" +
      "  </jbpm-context>" +
      "</jbpm-configuration>"
    );
    jbpmContext = jbpmConfiguration.createJbpmContext();
    mockSessionFactory = new MockSessionFactory();
    jbpmContext.setSessionFactory(mockSessionFactory);
  }
  
  public void tearDown() {
    jbpmConfiguration = null;
    jbpmContext = null;
    mockSessionFactory = null;
  }

  // with hibernate transactions
  // hibernate creates connections

  public void testDefaultCommit() {
    MockSession mockSession = (MockSession) jbpmContext.getSession();
    assertNull(mockSession.transaction);
    jbpmContext.close();

    assertNull(mockSession.transaction);
    assertTrue(mockSession.isClosed);
    // FLUSHED !
    assertTrue(mockSession.isFlushed);
  }

  public void testDefaultRollback() {
    jbpmContext.getSession();
    
    // no transaction config cannot be combined with calling setRollbackOnly
    jbpmContext.setRollbackOnly();
    try {
      jbpmContext.close();
      fail("expected exception");
    } catch (JbpmException e) {
      // OK
    }
  }
  
  public void testDefaultCommitAfterGetConnection() {
    MockSession mockSession = (MockSession) jbpmContext.getSession();
    jbpmContext.getConnection();
    jbpmContext.close();

    assertNull(mockSession.transaction);
    assertTrue(mockSession.isClosed);
    // FLUSHED !
    assertTrue(mockSession.isFlushed);
  }

  public void testDefaultRollbackAfterGetConnection() {
    jbpmContext.getSession();
    jbpmContext.setRollbackOnly();
    jbpmContext.getConnection();
    try {
      jbpmContext.close();
      fail("expected exception");
    } catch (JbpmException e) {
      // OK
    }
  }

  // with hibernate transactions
  // given creates connections

  public void testGivenConnectionCommit() {
    // inject given session
	MockConnectionHelper connectionHelper = new MockConnectionHelper();
    Connection mockConnection = connectionHelper.createMockConnection();
    jbpmContext.setConnection(mockConnection);

    MockSession mockSession = (MockSession) jbpmContext.getSession();
    jbpmContext.close();

    assertNull(mockSession.transaction);
    assertTrue(mockSession.isClosed);
    // FLUSHED !
    assertTrue(mockSession.isFlushed);

    assertFalse(connectionHelper.wasClosed);
    assertFalse(connectionHelper.wasCommitted);
    assertFalse(connectionHelper.wasRolledBack);
  }
  
  public void testGivenConnectionRollback() {
    // inject given session
	MockConnectionHelper connectionHelper = new MockConnectionHelper();
	Connection mockConnection = connectionHelper.createMockConnection();
    jbpmContext.setConnection(mockConnection);

    jbpmContext.getSession();
    jbpmContext.setRollbackOnly();
    try {
      jbpmContext.close();
      fail("expected exception");
    } catch (JbpmException e) {
      // OK
    }
  }

  public void testGivenConnectionCommitAfterGetConnection() {
    // inject given session
	MockConnectionHelper connectionHelper = new MockConnectionHelper();
	Connection mockConnection = connectionHelper.createMockConnection();
    jbpmContext.setConnection(mockConnection);

    MockSession mockSession = (MockSession) jbpmContext.getSession();
    jbpmContext.getConnection();
    jbpmContext.close();

    assertNull(mockSession.transaction);
    assertTrue(mockSession.isClosed);
    // FLUSHED !
    assertTrue(mockSession.isFlushed);

    assertFalse(connectionHelper.wasClosed);
    assertFalse(connectionHelper.wasCommitted);
    assertFalse(connectionHelper.wasRolledBack);
  }

  public void testGivenConnectionRollbackAfterGetConnection() {
    // inject given session
	MockConnectionHelper connectionHelper = new MockConnectionHelper();
	Connection mockConnection = connectionHelper.createMockConnection();
    jbpmContext.setConnection(mockConnection);

    jbpmContext.getSession();
    jbpmContext.getConnection();
    jbpmContext.setRollbackOnly();
    
    try {
      jbpmContext.close();
      fail("expected exception");
    } catch (JbpmException e) {
      // OK
    }
  }
}
