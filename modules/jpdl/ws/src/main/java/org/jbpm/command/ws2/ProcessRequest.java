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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * All that you need about a Process
 *
 * @author Salaboy21 (mailto:salaboy@gmail.com)
 * 
 */

@XmlType(
  name = "processRequest",
  namespace = "http://jbpm.org/jpdl/ws/01/2008/"
)
public class ProcessRequest
{
   private String processDefinitionName;
   private long version;
   private boolean start;

   public ProcessRequest()
   {
   }

   public ProcessRequest(String processDefinitionName, boolean start)
   {
      this.processDefinitionName = processDefinitionName;
      this.start = start;
   }

   @XmlElement(required = true)
   public String getProcessDefinitionName()
   {
      return processDefinitionName;
   }

   public void setProcessDefinitionName(String processDefinitionName)
   {
      this.processDefinitionName = processDefinitionName;
   }

   @XmlAttribute(name = "start", required = false)
   public boolean isStart()
   {
      return start;
   }

   public void setStart(boolean start)
   {
      this.start = start;
   }

   public long getVersion() {
	  return version;
   }

   public void setVersion(long version) {
	   this.version = version;
   }
}
