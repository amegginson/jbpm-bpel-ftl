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
package org.jbpm.context.exe;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmException;
import org.jbpm.configuration.ObjectFactory;
import org.jbpm.configuration.ObjectFactoryParser;
import org.jbpm.util.ClassLoaderUtil;

/**
 * specifies for one java-type how jbpm is able to persist objects of that type in the database. 
 */
public class JbpmType {
  
  static Map jbpmTypesCache = new HashMap();
  
  JbpmTypeMatcher jbpmTypeMatcher = null;
  Converter converter = null;
  Class variableInstanceClass = null;

  public JbpmType(JbpmTypeMatcher jbpmTypeMatcher, Converter converter, Class variableInstanceClass) {
    this.jbpmTypeMatcher = jbpmTypeMatcher;
    this.converter = converter;
    this.variableInstanceClass = variableInstanceClass;
  }
  
  public boolean matches(Object value) {
    return jbpmTypeMatcher.matches(value);
  }
  
  public VariableInstance newVariableInstance() {
    VariableInstance variableInstance = null;
    try {
      variableInstance = (VariableInstance) variableInstanceClass.newInstance();
      variableInstance.converter = converter;
    } catch (Exception e) {
      throw new JbpmException("couldn't instantiate variable instance class '"+variableInstanceClass.getName()+"'");
    }
    return variableInstance;
  }

  public static List getJbpmTypes() {
    List jbpmTypes = null;
    synchronized(jbpmTypesCache) {
      ObjectFactory objectFactory = JbpmConfiguration.Configs.getObjectFactory();
      jbpmTypes = (List) jbpmTypesCache.get(objectFactory);
      if (jbpmTypes==null) {
        if (JbpmConfiguration.Configs.hasObject("jbpm.types")) {
          jbpmTypes = (List) JbpmConfiguration.Configs.getObject("jbpm.types");
        } else {
          jbpmTypes = getDefaultJbpmTypes();
        }
        jbpmTypesCache.put(objectFactory, jbpmTypes);
      }
    }
    return jbpmTypes;
  }

  private static List getDefaultJbpmTypes() {
    String resource = JbpmConfiguration.Configs.getString("resource.varmapping");
    InputStream is = ClassLoaderUtil.getStream(resource);
    ObjectFactory objectFactory = ObjectFactoryParser.parseInputStream(is);
    return (List) objectFactory.createObject("jbpm.types");
  }
}
