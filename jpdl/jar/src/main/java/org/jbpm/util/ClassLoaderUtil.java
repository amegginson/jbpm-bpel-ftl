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
package org.jbpm.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jbpm.JbpmException;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.instantiation.ProcessClassLoader;

/**
 * provides centralized classloader lookup. 
 */
public class ClassLoaderUtil {

  public static Class loadClass(String className) {
    try {
      return getClassLoader().loadClass(className);
    } catch (ClassNotFoundException e) {
      throw new JbpmException("class not found '"+className+"'", e);
    }
  }
  
  public static ClassLoader getClassLoader() {
    // if this is made configurable, make sure it's done with the
    // jvm system properties
    // System.getProperty("jbpm.classloader")
    //  - 'jbpm'
    //  - 'context'
    // 
    // or something like Thread.currentThread().getContextClassLoader();
    return ClassLoaderUtil.class.getClassLoader();
  }
  
  public static InputStream getStream(String resource) {
    return getClassLoader().getResourceAsStream(resource);
  }

  public static Properties getProperties(String resource) {
    Properties properties = new Properties();
    try {
      properties.load(getStream(resource));
    } catch (IOException e) {
      throw new JbpmException("couldn't load properties file '"+resource+"'", e);
    }
    return properties;
  }

  /**
   * searches the given resource, first on the root of the classpath and if not 
   * not found there, in the given directory.
  public static InputStream getStream(String resource, String directory) {
    InputStream is = getClassLoader().getResourceAsStream(resource);
    if (is==null) {
      is = getClassLoader().getResourceAsStream(directory+"/"+resource);
    }
    return is;
  }

  public static Properties getProperties(String resource, String directory) {
    Properties properties = new Properties();
    try {
      properties.load(getStream(resource, directory));
    } catch (IOException e) {
      throw new JbpmException("couldn't load properties file '"+resource+"'", e);
    }
    return properties;
  }
  */

  public static ClassLoader getProcessClassLoader(ProcessDefinition processDefinition) {
    return new ProcessClassLoader(ClassLoaderUtil.class.getClassLoader(), processDefinition);
  }

}
