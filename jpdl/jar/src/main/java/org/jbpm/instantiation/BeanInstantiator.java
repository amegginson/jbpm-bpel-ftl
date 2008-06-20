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
package org.jbpm.instantiation;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

public class BeanInstantiator extends FieldInstantiator implements Instantiator {
  
  protected void setPropertyValue(Class clazz, Object newInstance, String propertyName, Element propertyElement) {
    try {
      // create the setter method name from the property name
      String setterMethodName = "set"+propertyName.substring(0,1).toUpperCase()+propertyName.substring(1);
      
      // find the setter method
      Method method = findSetter(clazz, setterMethodName);
      Class propertyType = method.getParameterTypes()[0];

      // if the setter method was found
      if (method!=null) {
        // make it accessible
        method.setAccessible(true);
        // invoke it
        method.invoke(newInstance, new Object[]{ getValue(propertyType, propertyElement) });
      } else {
        log.error( "couldn't set property '"+propertyName+"' to value '"+propertyElement.asXML()+"'" );
      }
    } catch (Exception e) {
      log.error( "couldn't parse property '"+propertyName+"' to value '"+propertyElement.asXML()+"'", e );
    }
  }
  
  private Method findSetter(Class clazz, String setterMethodName) {
    Method method = null;
    Method[] methods = clazz.getDeclaredMethods();
    for( int i=0; ( (i<methods.length)
                    && (method==null) ); i++) {
      if ( (setterMethodName.equals(methods[i].getName()))
           && (methods[i].getParameterTypes()!=null)
           && (methods[i].getParameterTypes().length==1) ) {
        method = methods[i];
      }
    }
    if ( (method==null)
         && (clazz!=Object.class)
       ) {
      method = findSetter(clazz.getSuperclass(), setterMethodName);
    }
    return method;
  }

  private static final Log log = LogFactory.getLog(BeanInstantiator.class);
}
