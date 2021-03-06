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

import org.w3c.dom.Element;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlAnyElement;
import java.util.List;

@XmlType
  (
  name = "signalRequest",
  namespace = "http://jbpm.org/jpdl/ws/01/2008/"
)
public class SignalRequest
{
   private long tokenId;
   private  String transitionName;
   private List<Element> any;

   public SignalRequest()
   {
   }

   public SignalRequest(long tokenId, String transitionName)
   {
      this.tokenId = tokenId;
      this.transitionName = transitionName;
   }

   @XmlAttribute(required = true)
   public long getTokenId()
   {
      return tokenId;
   }

   public void setTokenId(long tokenId)
   {
      this.tokenId = tokenId;
   }

   @XmlElement(required = false)
   public String getTransitionName()
   {
      return transitionName;
   }

   public void setTransitionName(String transitionName)
   {
      this.transitionName = transitionName;
   }

   @XmlAnyElement
   public List<Element> getAny()
   {
      return any;
   }

   public void setAny(List<Element> any)
   {
      this.any = any;
   }
}
