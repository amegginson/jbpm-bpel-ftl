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
package org.jbpm.graph.log;

import org.jbpm.graph.exe.*;
import org.jbpm.logging.log.*;

public class TokenCreateLog extends ProcessLog {

  private static final long serialVersionUID = 1L;
  
  protected Token child = null;
  
  public TokenCreateLog() {
  }

  public TokenCreateLog(Token child) {
    this.child = child;
  }

  public String toString() {
    return "tokencreate["+child.getFullName()+"]";
  }
  
  public Token getChild() {
    return child;
  }
}
