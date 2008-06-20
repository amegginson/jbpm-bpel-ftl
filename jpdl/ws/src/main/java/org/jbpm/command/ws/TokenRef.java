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
package org.jbpm.command.ws;

import org.jbpm.graph.exe.Token;

import javax.xml.bind.annotation.XmlType;

/**
 * Reference info about a token
 *
 * @author Heiko.Braun@jboss.com
 * 
 */
@XmlType(
  name = "tokenReference",
  namespace = "http://jbpm.org/jpdl/ws/01/2008/"
)
public class TokenRef
{
   long tokenId;
   String nodeName;
   long processInstanceId;
   String processDefinitionName;

   public TokenRef()
   {
   }

   public TokenRef(long tokenId, String nodeName, long processInstanceId, String processDefinitionName)
   {
      this.tokenId = tokenId;
      this.nodeName = nodeName;
      this.processInstanceId = processInstanceId;
      this.processDefinitionName = processDefinitionName;
   }
   
   public TokenRef(Token token)
   {
      this(
        token.getId(),
        token.getNode().getName(),
        token.getProcessInstance().getId(),
        token.getProcessInstance().getProcessDefinition().getName()
      );      
   }


   public long getTokenId()
   {
      return tokenId;
   }

   public void setTokenId(long tokenId)
   {
      this.tokenId = tokenId;
   }

   public String getNodeName()
   {
      return nodeName;
   }

   public void setNodeName(String nodeName)
   {
      this.nodeName = nodeName;
   }

   public long getProcessInstanceId()
   {
      return processInstanceId;
   }

   public void setProcessInstanceId(long processInstanceId)
   {
      this.processInstanceId = processInstanceId;
   }

   public String getProcessDefinitionName()
   {
      return processDefinitionName;
   }

   public void setProcessDefinitionName(String processDefinitionName)
   {
      this.processDefinitionName = processDefinitionName;
   }

}
