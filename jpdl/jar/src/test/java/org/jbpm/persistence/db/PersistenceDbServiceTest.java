package org.jbpm.persistence.db;

import java.sql.Connection;

import junit.framework.TestCase;

import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;

public class PersistenceDbServiceTest extends TestCase {

  JbpmConfiguration jbpmConfiguration = null;
  JbpmContext jbpmContext = null;
  MockSessionFactory mockSessionFactory = null;
  
  public void setUp() {
    // for all of these tests, the default configuration is used
    jbpmConfiguration = JbpmConfiguration.getInstance();
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
    assertNotNull(mockSession.transaction);
    jbpmContext.close();

    assertTrue(mockSession.transaction.wasCommitted);
    assertFalse(mockSession.transaction.wasRolledBack);
    assertTrue(mockSession.isClosed);
    assertFalse(mockSession.isFlushed);
  }

  public void testDefaultRollback() {
    MockSession mockSession = (MockSession) jbpmContext.getSession();
    assertNotNull(mockSession.transaction);
    jbpmContext.setRollbackOnly();
    jbpmContext.close();

    assertFalse(mockSession.transaction.wasCommitted);
    assertTrue(mockSession.transaction.wasRolledBack);
    assertTrue(mockSession.isClosed);
    assertFalse(mockSession.isFlushed);
  }
  
  public void testDefaultCommitAfterGetConnection() {
    MockSession mockSession = (MockSession) jbpmContext.getSession();
    assertNotNull(mockSession.transaction);
    jbpmContext.getConnection();
    jbpmContext.close();

    assertTrue(mockSession.transaction.wasCommitted);
    assertFalse(mockSession.transaction.wasRolledBack);
    assertTrue(mockSession.isClosed);
    assertFalse(mockSession.isFlushed);
  }

  public void testDefaultFollbackAfterGetConnection() {
    MockSession mockSession = (MockSession) jbpmContext.getSession();
    assertNotNull(mockSession.transaction);
    jbpmContext.setRollbackOnly();
    jbpmContext.getConnection();
    jbpmContext.close();

    assertFalse(mockSession.transaction.wasCommitted);
    assertTrue(mockSession.transaction.wasRolledBack);
    assertTrue(mockSession.isClosed);
    assertFalse(mockSession.isFlushed);
  }

  // with hibernate transactions
  // given creates connections

  public void testGivenConnectionCommit() {
    // inject given session
	MockConnectionHelper connectionHelper = new MockConnectionHelper();
    Connection mockConnection = connectionHelper.createMockConnection();
    jbpmContext.setConnection(mockConnection);

    MockSession mockSession = (MockSession) jbpmContext.getSession();
    assertNotNull(mockSession.transaction);
    jbpmContext.close();

    assertTrue(mockSession.transaction.wasCommitted);
    assertFalse(mockSession.transaction.wasRolledBack);
    assertTrue(mockSession.isClosed);
    assertFalse(mockSession.isFlushed);

    assertFalse(connectionHelper.wasClosed);
    assertFalse(connectionHelper.wasCommitted);
    assertFalse(connectionHelper.wasRolledBack);
  }
  
  public void testGivenConnectionRollback() {
    // inject given session
	MockConnectionHelper connectionHelper = new MockConnectionHelper();
    Connection mockConnection = connectionHelper.createMockConnection();
    jbpmContext.setConnection(mockConnection);

    MockSession mockSession = (MockSession) jbpmContext.getSession();
    assertNotNull(mockSession.transaction);
    jbpmContext.setRollbackOnly();
    jbpmContext.close();

    assertFalse(mockSession.transaction.wasCommitted);
    assertTrue(mockSession.transaction.wasRolledBack);
    assertTrue(mockSession.isClosed);
    assertFalse(mockSession.isFlushed);

    assertFalse(connectionHelper.wasClosed);
    assertFalse(connectionHelper.wasCommitted);
    assertFalse(connectionHelper.wasRolledBack);
  }

  public void testGivenConnectionCommitAfterGetConnection() {
    // inject given session
	MockConnectionHelper connectionHelper = new MockConnectionHelper();
    Connection mockConnection = connectionHelper.createMockConnection();
    jbpmContext.setConnection(mockConnection);

    MockSession mockSession = (MockSession) jbpmContext.getSession();
    assertNotNull(mockSession.transaction);
    jbpmContext.getConnection();
    jbpmContext.close();

    assertTrue(mockSession.transaction.wasCommitted);
    assertFalse(mockSession.transaction.wasRolledBack);
    assertTrue(mockSession.isClosed);
    assertFalse(mockSession.isFlushed);

    assertFalse(connectionHelper.wasClosed);
    assertFalse(connectionHelper.wasCommitted);
    assertFalse(connectionHelper.wasRolledBack);
  }

  public void testGivenConnectionRollbackAfterGetConnection() {
    // inject given session
	MockConnectionHelper connectionHelper = new MockConnectionHelper();
    Connection mockConnection = connectionHelper.createMockConnection();
    jbpmContext.setConnection(mockConnection);

    MockSession mockSession = (MockSession) jbpmContext.getSession();
    assertNotNull(mockSession.transaction);
    jbpmContext.getConnection();
    jbpmContext.setRollbackOnly();
    jbpmContext.close();

    assertFalse(mockSession.transaction.wasCommitted);
    assertTrue(mockSession.transaction.wasRolledBack);
    assertTrue(mockSession.isClosed);
    assertFalse(mockSession.isFlushed);

    assertFalse(connectionHelper.wasClosed);
    assertFalse(connectionHelper.wasCommitted);
    assertFalse(connectionHelper.wasRolledBack);
  }
}
