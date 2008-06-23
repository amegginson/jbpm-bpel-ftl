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
package org.jbpm.configuration;

import org.jbpm.JbpmException;
import org.jbpm.util.XmlUtil;
import org.w3c.dom.Element;

public class RefInfo extends AbstractObjectInfo {

  private static final long serialVersionUID = 1L;
  
  String bean = null;
  
  public RefInfo(Element refElement, ObjectFactoryParser configParser) {
    super(refElement, configParser);

    if (refElement.hasAttribute("bean")) {
      bean = refElement.getAttribute("bean");
    } else {
      throw new JbpmException("element ref must have a 'bean' attribute : "+XmlUtil.toString(refElement));
    }
  }

  public Object createObject(ObjectFactoryImpl objectFactory) {
    return objectFactory.getObject(bean);
  }
}
