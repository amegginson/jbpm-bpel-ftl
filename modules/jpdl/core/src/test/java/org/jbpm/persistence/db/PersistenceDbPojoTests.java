package org.jbpm.persistence.db;

import junit.framework.Test;
import junit.framework.TestSuite;

public class PersistenceDbPojoTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("org.jbpm.persistence.db");
    //$JUnit-BEGIN$
    suite.addTestSuite(PersistenceDbServiceNoTxTest.class);
    suite.addTestSuite(PersistenceDbServiceTest.class);
    //$JUnit-END$
    return suite;
  }

}
