package org.jbpm.jpdl.el;

import junit.framework.Test;
import junit.framework.TestSuite;

public class JpdlElDbTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("org.jbpm.jpdl.el.impl");
    //$JUnit-BEGIN$
    suite.addTestSuite(ActionExpressionDbTest.class);
    //$JUnit-END$
    return suite;
  }

}
