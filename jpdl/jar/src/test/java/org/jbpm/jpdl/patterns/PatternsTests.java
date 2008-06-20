/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jbpm.jpdl.patterns;

import junit.framework.*;

public class PatternsTests extends TestCase {

  public static Test suite() throws Exception {
    TestSuite suite = new TestSuite("org.jbpm.jpdl.patterns");

    suite.addTestSuite( Wfp01SequenceTest.class );
    suite.addTestSuite( Wfp02ParallelSplitTest.class );
    suite.addTestSuite( Wfp03SynchronizationTest.class );
    suite.addTestSuite( Wfp04ExclusiveChoiceTest.class );
    suite.addTestSuite( Wfp05SimpleMergeTest.class );
    suite.addTestSuite( Wfp06MultiChoiceTest.class );
    suite.addTestSuite( Wfp07SynchronizingMergeTest.class );
    suite.addTestSuite( Wfp08MultiMergeTest.class );
    suite.addTestSuite( Wfp09DiscriminatorTest.class );
    suite.addTestSuite( Wfp10NOutOfMJoin.class );
    suite.addTestSuite( Wfp11ArbitraryCyclesTest.class );
    suite.addTestSuite( Wfp12ImplicitTerminationTest.class );
    suite.addTestSuite( Wfp13MiWithoutSynchronizationTest.class );
    suite.addTestSuite( Wfp14MiWithAPrioriDesigntimeKnowledgeTest.class );
    suite.addTestSuite( Wfp15MiWithAPrioriRuntimeKnowledgeTest.class );
    suite.addTestSuite( Wfp16MiWithoutAPrioriRuntimeKnowledge.class );
    suite.addTestSuite( Wfp17DeferredChoiceTest.class );
    suite.addTestSuite( Wfp18InterleavedParallelRoutingTest.class );
    suite.addTestSuite( Wfp19MilestoneTest.class );
    suite.addTestSuite( Wfp20CancelActivityTest.class );
    suite.addTestSuite( Wfp21CancelCaseTest.class );

    return suite;
  }

}
