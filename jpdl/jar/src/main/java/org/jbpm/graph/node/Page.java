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
package org.jbpm.graph.node;

import org.dom4j.Element;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.jpdl.xml.JpdlXmlReader;
import org.jbpm.jpdl.xml.Parsable;
import org.jbpm.taskmgmt.exe.TaskInstance;

public class Page extends Node implements Parsable {

  private static final long serialVersionUID = 1L;
  
  String url;
  boolean isConversationEnd = false;
  String outcome;

  public void read(Element nodeElement, JpdlXmlReader jpdlXmlReader) {
    url = nodeElement.attributeValue("url");
    Element conversationEndElement = nodeElement.element("conversation-end");
    if (conversationEndElement!=null) {
      isConversationEnd = true;
      outcome = conversationEndElement.attributeValue("outcome");
    }
  }

  public void execute(ExecutionContext executionContext) {
    if (isConversationEnd) {
      // get the outer business process task instance
      ContextInstance contextInstance = executionContext.getContextInstance();
      String variableName = "taskInstance";
      TaskInstance taskInstance = (TaskInstance) contextInstance.getVariable(variableName);
      
      // complete the task
      if (outcome==null) {
        taskInstance.end();
      } else {
        taskInstance.end(outcome);
      }
    }
  }

  public boolean isConversationEnd() {
    return isConversationEnd;
  }
  public String getOutcome() {
    return outcome;
  }
  public String getUrl() {
    return url;
  }
}
