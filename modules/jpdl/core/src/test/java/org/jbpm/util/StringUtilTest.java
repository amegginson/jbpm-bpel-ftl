package org.jbpm.util;

import junit.framework.TestCase;


public class StringUtilTest extends TestCase {

  public void testConv() {
    System.out.println(StringUtil.toHexString(new byte[]{(byte) 0x5a,(byte) 0x23,(byte) 0x7c,(byte) 0x0b}));
    System.out.println(StringUtil.toHexStringHibernate(new byte[]{(byte) 0x5a,(byte) 0x23,(byte) 0x7c,(byte) 0x0b}));
  }
}
