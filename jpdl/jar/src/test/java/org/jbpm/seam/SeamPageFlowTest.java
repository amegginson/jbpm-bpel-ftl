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
package org.jbpm.seam;

import java.io.Serializable;
import java.util.Collection;

import junit.framework.TestCase;

import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.graph.node.Page;
import org.jbpm.taskmgmt.exe.TaskInstance;

public class SeamPageFlowTest extends TestCase {
  
  public static class Document implements Serializable {
    private static final long serialVersionUID = 1L;
    String text;
    public Document(String text) {
      this.text = text;
    }
    public String getText() {
      return text;
    }
  }
  
  public void testPageFlow() {
    // Here's a simple business process with one task
    ProcessDefinition businessProcessDefinition = ProcessDefinition.parseXmlString(
      "<process-definition name='business process'>" +
      "  <start-state name='start biz proc'>" +
      "    <transition to='review document' />" +
      "  </start-state>" +
      "  <task-node name='review document'>" +
      "    <task name='business process review task'>" +
             // the task instance must have access to the 
             // document process variable 
      "      <controller>" +
      "        <variable name='document' />" +
      "      </controller>" +
      "    </task>" +
      "    <transition name='approveDocument' to='file horizontally' />" +
      "    <transition name='rejectDocument' to='put in shredder' />" +
      "  </task-node>" +
      "  <state name='file horizontally' />" +
      "  <state name='put in shredder' />" +
      "</process-definition>"
    );

    // let's start a new process instance
    ProcessInstance businessProcessInstance = new ProcessInstance(businessProcessDefinition);
    // the root token is the main path of execution
    Token businessToken = businessProcessInstance.getRootToken();
    assertEquals("start biz proc", businessToken.getNode().getName());
    
    // we put the document somewhere in the process variables
    Document document = new Document("blablabla");
    ContextInstance contextInstance = businessProcessInstance.getContextInstance();
    contextInstance.setVariable("document", document);
    
    // let's kick the execution of the business process
    businessProcessInstance.signal();

    // so the execution should have arrived in the review document node
    assertEquals("review document", businessToken.getNode().getName());

    // there should be 1 task instance created in the business process 
    Collection allTaskInstances = businessProcessInstance.getTaskMgmtInstance().getTaskInstances();
    assertNotNull(allTaskInstances);
    assertEquals(1, allTaskInstances.size());
    TaskInstance taskInstance = (TaskInstance) allTaskInstances.iterator().next();
    document = (Document) taskInstance.getVariable("document");
    assertEquals("blablabla", document.getText());

    
    // Now SEAM will handle the page flow that is specified here:
    ProcessDefinition pageFlowDefinition = ProcessDefinition.parseXmlString(
      "<process-definition name='approve document task'>" +
      "  <start-state name='start page flow'>" +
      "    <transition to='review' />" +
      "  </start-state>" +
      "  <page name='review' url='review.jsp'>" +
      "    <transition name='approve' to='approved' />" +
      "    <transition name='reject' to='rejected' />" +
      "  </page>" +
      "  <page name='approved' url='approved.jsp'>" +
      "    <conversation-end outcome='approveDocument' />" +
      "  </page>" +
      "  <page name='rejected' url='rejected.jsp'>" +
      "    <conversation-end outcome='rejectDocument' />" +
      "  </page>" +
      "</process-definition>"
    );
    
    // A new page flow process is started
    ProcessInstance pageFlowInstance = new ProcessInstance(pageFlowDefinition);
    Token pageFlowToken = pageFlowInstance.getRootToken();

    // This page flow is in the context of a specific user that 
    // clicked an entry in his task list.  The task instance is 
    // injected as a process context variable
    contextInstance = pageFlowInstance.getContextInstance();
    contextInstance.setVariable("taskInstance", taskInstance);

    // With the task instance information, the page flow process is started 
    // and can now decide which is the first page to show the user
    pageFlowInstance.signal();

    // SEAM can expect that when a wait state is entered, the main 
    // path of execution is positioned in a page.  That page 
    // contains the url that SEAM should render.
    // In this simple page flow process, we always start with the 
    // review page.
    Page page = (Page) pageFlowToken.getNode();
    assertNotNull(page);
    assertEquals("review", page.getName());
    assertEquals("review.jsp", page.getUrl());
    
    // so now, the SEAM page flow renders the review page.
    // in review.jsp, the EL expression "taskInstance[document].text" should resolve 
    // to 'blablabla'
    taskInstance = (TaskInstance) contextInstance.getVariable("taskInstance");
    document = (Document) taskInstance.getVariable("document");
    assertEquals("blablabla", document.getText());
    assertEquals("business process review task", taskInstance.getName());

    // suppose the user presses the approve button
    pageFlowToken.signal("approve");

    // now the page flow process should have moved to the 
    // approved page with the approved.jsp
    page = (Page) pageFlowToken.getNode();
    assertNotNull(page);
    assertEquals("approved", page.getName());
    assertEquals("approved.jsp", page.getUrl());

    // ...and, the business process task instance should have ended.
    assertTrue(taskInstance.hasEnded());
    // causing the business process to move to the next state
    assertEquals("file horizontally", businessToken.getNode().getName());
  }
} 
