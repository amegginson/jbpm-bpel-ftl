package org.jbpm.mail;

import junit.framework.Test;
import junit.framework.TestSuite;

public class MailTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for org.jbpm.mail");
    //$JUnit-BEGIN$
    suite.addTestSuite(TaskMailTest.class);
    suite.addTestSuite(MailTest.class);
    //$JUnit-END$
    return suite;
  }

}
