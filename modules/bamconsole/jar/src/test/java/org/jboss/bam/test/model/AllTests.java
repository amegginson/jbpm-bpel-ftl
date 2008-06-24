package org.jboss.bam.test.model;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {
	
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.jboss.bam.test.model");
		//$JUnit-BEGIN$
		suite.addTestSuite(DashboardQueryTest.class);
		//$JUnit-END$
		return suite;
	}
	
}
