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
package org.jbpm.context.exe.converter;

import org.jbpm.context.exe.*;

public class BooleanToStringConverter implements Converter {
  
  private static final long serialVersionUID = 1L;
  
  public static final String TRUE_TEXT = "T";
  public static final String FALSE_TEXT = "F";

  public boolean supports(Object value) {
    if (value==null) return true;
    return (value.getClass()==Boolean.class);
  }

  public Object convert(Object o) {
    String convertedValue = FALSE_TEXT;
    if (((Boolean)o).booleanValue()) {
      convertedValue = TRUE_TEXT;
    }
    return convertedValue;
  }

  public Object revert(Object o) {
    Boolean revertedValue = Boolean.FALSE;
    if (TRUE_TEXT.equals(o)) {
      revertedValue = Boolean.TRUE;
    }
    return revertedValue;
  }
}
