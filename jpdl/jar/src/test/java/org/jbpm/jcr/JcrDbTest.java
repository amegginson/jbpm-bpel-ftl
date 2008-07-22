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
package org.jbpm.jcr;

import javax.jcr.Node;
import javax.jcr.Session;

import org.jbpm.JbpmConfiguration;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.db.AbstractDbTestCase;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.svc.Services;

/**
 * Test the JCR services of jBPM using the Apache JackRabbit JCR implementation.
 * 
 * @author Jim Rigsbee, Tom Baeyens
 */
public class JcrDbTest extends AbstractDbTestCase {
  
  public static JbpmConfiguration jbpmJcrConfiguration = 
      JbpmConfiguration.parseResource("org/jbpm/jcr/jbpm.jcr.cfg.xml");
  
  protected JbpmConfiguration getJbpmConfiguration() {
    return jbpmJcrConfiguration;
  }

  public void testJcrNodeStorageAndRetrieval() throws Exception {
    deployDocumentApprovalProcess();

    newTransaction();
    
    ProcessInstance processInstance = jbpmContext.newProcessInstanceForUpdate("document approval");

    JcrService jcrService = (JcrService) jbpmContext.getServices().getService(Services.SERVICENAME_JCR);
    assertNotNull(jcrService);
    Session session = jcrService.getSession();
    Node rootNode = session.getRootNode();
    Node processInstanceNode = rootNode.addNode("process"+processInstance.getId());
    Node documentNode = processInstanceNode.addNode("document");

    ContextInstance contextInstance = processInstance.getContextInstance();
    contextInstance.setVariable("document", documentNode);
    
    newTransaction();
  }

  public void deployDocumentApprovalProcess() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition name='document approval'>" +
      "  <start-state name='start' />" +
      "</process-definition>"
    );
    jbpmContext.deployProcessDefinition(processDefinition);
  }
}
