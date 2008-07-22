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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class StaticUtil {

  /*
  public class MyClass ... {
    static AType aStaticInMyClass = null;
    static AnotherType anotherStaticInMyClass = null;
    
    static {
      new StaticUtil.Initializer(MyClass.class) {
        public void init() {
          // initialize static members here
          aStaticInMyClass = ...;
          anotherStaticInMyClass = ...;
        }
      };
    }
    ...
  }
  */
  
  static Map initializers = Collections.synchronizedMap(new HashMap());

  public abstract static class Initializer {
    public Initializer(Class clazz) {
      add(clazz, this);
      init();
    }
    public abstract void init();
  }

  public static void add(Class clazz, Initializer initializer) {
    initializers.put(clazz, initializer);
  }

  public static void remove(Class clazz) {
    initializers.remove(clazz);
  }

  public static void reinitialize() {
    Iterator iter = initializers.values().iterator();
    while (iter.hasNext()) {
      Initializer initializer = (Initializer) iter.next();
      initializer.init();
    }
  }
}
