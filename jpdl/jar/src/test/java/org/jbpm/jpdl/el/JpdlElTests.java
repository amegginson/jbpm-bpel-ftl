package org.jbpm.jpdl.el;

import junit.framework.Test;
import junit.framework.TestSuite;

public class JpdlElTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("org.jbpm.jpdl.el.impl");
    //$JUnit-BEGIN$
    suite.addTestSuite(DecisionExpressionTest.class);
    suite.addTestSuite(ExpressionTest.class);
    suite.addTestSuite(FunctionMapperTest.class);
    suite.addTestSuite(TaskExpressionTest.class);
    //$JUnit-END$
    return suite;
  }

}
