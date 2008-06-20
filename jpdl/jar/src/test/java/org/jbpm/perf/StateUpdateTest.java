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
package org.jbpm.perf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;

public class StateUpdateTest extends AbstractPerfTestCase {

  static {
    deploy( 
      ProcessDefinition.parseXmlString(
        "<process-definition name='states'>" +
        "  <start-state>" +
        "    <transition to='one' />" +
        "  </start-state>" +
        "  <state name='one'>" +
        "    <transition to='two' />" +
        "  </state>" +
        "  <state name='two'>" +
        "    <transition to='end' />" +
        "  </state>" +
        "  <end-state name='end' />" +
        "</process-definition>"
      )
    );
  }

  public void testStates() {
    log.info("");
    log.info("=== CREATING PROCESS INSTANCE =======================================================");
    log.info("");
    ProcessInstance processInstance = jbpmContext.newProcessInstanceForUpdate("states");
    processInstance.signal();

    newTransaction();
    long processInstanceId = processInstance.getId();
    
    log.info("");
    log.info("=== STATE CHANGE ONE --> TWO =======================================================");
    log.info("");
    processInstance = jbpmContext.loadProcessInstanceForUpdate(processInstanceId);
    processInstance.signal();

    newTransaction();
    
    log.info("");
    log.info("=== STATE CHANGE TWO --> END =======================================================");
    log.info("");
    processInstance = jbpmContext.loadProcessInstanceForUpdate(processInstanceId);
    processInstance.signal();
  }
  
  private static final Log log = LogFactory.getLog(StateUpdateTest.class);
}
