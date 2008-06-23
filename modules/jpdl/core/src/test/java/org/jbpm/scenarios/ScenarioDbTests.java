package org.jbpm.scenarios;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ScenarioDbTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("org.jbpm.scenarios");
    //$JUnit-BEGIN$
    suite.addTestSuite(TwoSubProcessesInOneTransactionDbTest.class);
    suite.addTestSuite(AsyncTimerAndSubProcessDbTest.class);
    //$JUnit-END$
    return suite;
  }
}
