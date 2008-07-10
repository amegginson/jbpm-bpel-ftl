/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jbpm.command.ws2;

import org.jbpm.graph.exe.Token;

import javax.xml.bind.annotation.XmlType;

/**
 * Reference info about a token
 *
 * @author Heiko.Braun@jboss.com
 * @author Salaboy21 (mailto:salaboy@gmail.com)
 */
@XmlType(
  name = "processInstanceReference",
  namespace = "http://jbpm.org/jpdl/ws/01/2008/"
)
public class ProcessInstanceRef
{
 
   long processInstanceId;
   
   ProcessDefinitionRef processDefinition;
   TokenRef token;
   
   public ProcessInstanceRef()
   {
   }

   public ProcessInstanceRef(long processInstanceId, String processDefinitionName,long processVersion,long tokenId,String nodeName)
   {
      
      this.processInstanceId = processInstanceId;
      this.processDefinition = new ProcessDefinitionRef(processDefinitionName,processVersion);
      this.token = new TokenRef(tokenId,nodeName);
   }
   
   public ProcessInstanceRef(org.jbpm.graph.exe.ProcessInstance processInstance)
   {
      this(
    		  processInstance.getId(),
    		  processInstance.getProcessDefinition().getName(),
    		  processInstance.getVersion(),
    		  processInstance.getRootToken().getId(),
    		  processInstance.getRootToken().getNode().getName()
    		  
      );      
   }


  
   public long getProcessInstanceId()
   {
      return processInstanceId;
   }

   public void setProcessInstanceId(long processInstanceId)
   {
      this.processInstanceId = processInstanceId;
   }

   
	public TokenRef getToken() {
		return token;
	}
	
	public void setToken(TokenRef token) {
		this.token = token;
	}

	public ProcessDefinitionRef getProcessDefinition() {
		return processDefinition;
	}

	public void setProcessDefinition(ProcessDefinitionRef processDefinition) {
		this.processDefinition = processDefinition;
	}
   

}
