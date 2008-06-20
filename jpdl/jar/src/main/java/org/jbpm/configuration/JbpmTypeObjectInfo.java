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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.context.exe.Converter;
import org.jbpm.context.exe.JbpmType;
import org.jbpm.context.exe.JbpmTypeMatcher;
import org.jbpm.context.exe.VariableInstance;
import org.jbpm.db.hibernate.Converters;
import org.jbpm.util.ClassLoaderUtil;
import org.jbpm.util.XmlUtil;
import org.w3c.dom.Element;

public class JbpmTypeObjectInfo extends AbstractObjectInfo {

  private static final long serialVersionUID = 1L;
  
  ObjectInfo typeMatcherObjectInfo = null;
  Converter converter = null;
  Class variableInstanceClass = null;
  
  public JbpmTypeObjectInfo(Element jbpmTypeElement, ObjectFactoryParser objectFactoryParser) {
    super(jbpmTypeElement, objectFactoryParser);
  
    try {
      Element typeMatcherElement = XmlUtil.element(jbpmTypeElement, "matcher");
      if (typeMatcherElement==null) {
        throw new ConfigurationException("matcher is a required element in a jbpm-type: "+XmlUtil.toString(jbpmTypeElement));
      }
      Element typeMatcherBeanElement = XmlUtil.element(typeMatcherElement);
      typeMatcherObjectInfo = objectFactoryParser.parse(typeMatcherBeanElement);
      
      Element converterElement = XmlUtil.element(jbpmTypeElement, "converter");
      if (converterElement!=null) {
        if (! converterElement.hasAttribute("class")) {
          throw new ConfigurationException("class attribute is required in a converter element: "+XmlUtil.toString(jbpmTypeElement));
        }
        String converterClassName = converterElement.getAttribute("class");
        converter = Converters.getConverterByClassName(converterClassName);
      }
      
      Element variableInstanceElement = XmlUtil.element(jbpmTypeElement, "variable-instance");
      if (! variableInstanceElement.hasAttribute("class")) {
        throw new ConfigurationException("class is a required attribute in element variable-instance: "+XmlUtil.toString(jbpmTypeElement));
      }
      String variableInstanceClassName = variableInstanceElement.getAttribute("class");
      variableInstanceClass = ClassLoaderUtil.loadClass(variableInstanceClassName);
      if (! VariableInstance.class.isAssignableFrom(variableInstanceClass)) {
        throw new ConfigurationException("variable instance class '"+variableInstanceClassName+"' is not a VariableInstance");
      }
    } catch (ConfigurationException e){
      throw e;
    } catch (Exception e) {
      // NOTE that Error's are not caught because that might halt the JVM and mask the original Error.
      // Probably the user doesn't need support for this type and doesn't have a required library in the path.
      // So let's log and ignore
      log.debug("jbpm variables type "+XmlUtil.toString(jbpmTypeElement)+" couldn't be instantiated properly: "+e.toString());
      // now, let's make sure that this JbpmType is ignored by always returning false in the JbpmTypeMatcher
      typeMatcherObjectInfo = new ObjectInfo() {
        private static final long serialVersionUID = 1L;
        public boolean hasName() {return false;}
        public String getName() {return null;}
        public boolean isSingleton() {return true;}
        public Object createObject(ObjectFactoryImpl objectFactory) {
          return new JbpmTypeMatcher() {
            private static final long serialVersionUID = 1L;
            public boolean matches(Object value) {
              return false;
            }
          };
        }
      };
      converter = null;
      variableInstanceClass = null;
    }
  }

  public Object createObject(ObjectFactoryImpl objectFactory) {
    JbpmTypeMatcher jbpmTypeMatcher = (JbpmTypeMatcher) objectFactory.createObject(typeMatcherObjectInfo);
    return new JbpmType(jbpmTypeMatcher, converter, variableInstanceClass);
  }
  
  private static Log log = LogFactory.getLog(JbpmTypeObjectInfo.class);
}
