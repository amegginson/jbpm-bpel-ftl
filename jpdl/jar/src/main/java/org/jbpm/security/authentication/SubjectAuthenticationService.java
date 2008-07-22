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
package org.jbpm.security.authentication;

import java.security.AccessController;
import java.security.Principal;
import java.util.Set;

import javax.security.auth.Subject;

import org.jbpm.JbpmConfiguration;
import org.jbpm.security.AuthenticationService;
import org.jbpm.util.ClassLoaderUtil;

/**
 * gets the authenticated actor id from the current Subject.
 * This Authenticator requires another configuration parameter 
 * 'jbpm.authenticator.principal.classname'.  This configuration property 
 * specifies the class name of the principal that should be used from 
 * the current subject.  The name of that principal is used as the 
 * currently authenticated actorId. 
 */
public class SubjectAuthenticationService implements AuthenticationService {
  
  private static final long serialVersionUID = 1L;
  
  private static final String principalClassName = JbpmConfiguration.Configs.getString("jbpm.authenticator.principal.classname");
  private static Class principalClass = ClassLoaderUtil.loadClass(principalClassName);

  public String getActorId() {
    String authenticatedActorId = null;
    Subject subject = Subject.getSubject(AccessController.getContext());
    Set principals = subject.getPrincipals(principalClass);
    if ( (principals!=null)
         && (!principals.isEmpty()) 
       ) {
      Principal principal = (Principal) principals.iterator().next();
      authenticatedActorId = principal.getName();
    }
    return authenticatedActorId;
  }

  public void close() {
  }
}
