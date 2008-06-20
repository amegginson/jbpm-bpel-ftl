package org.jbpm;

import junit.framework.TestCase;

public class JbpmDefaultConfigTest extends TestCase {

  // this test should be ran without jbpm.cfg.xml on the 
  // classpath.
  public void testWithoutJbpmCfgXml() {
    JbpmConfiguration.getInstance().createJbpmContext().close();
  }
}
