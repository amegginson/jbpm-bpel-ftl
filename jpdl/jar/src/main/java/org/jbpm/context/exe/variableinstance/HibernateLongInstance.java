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
package org.jbpm.context.exe.variableinstance;

import org.jbpm.context.exe.*;
import org.jbpm.context.log.variableinstance.*;

public class HibernateLongInstance  extends VariableInstance {

  private static final long serialVersionUID = 1L;
  
  protected Object value = null;

  public boolean isStorable(Object value) {
    return true;
  }

  protected Object getObject() {
    return value;
  }

  protected void setObject(Object value) {
    if (token!=null) token.addLog(new HibernateLongUpdateLog(this, this.value, value));
    this.value = value;
  }
}