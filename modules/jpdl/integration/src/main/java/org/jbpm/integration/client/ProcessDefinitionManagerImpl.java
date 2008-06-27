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
package org.jbpm.integration.client;

// $Id$

import java.io.IOException;
import java.net.URL;

import org.jbpm.api.client.ProcessDefinition;
import org.jbpm.api.client.ProcessDefinitionManager;
import org.jbpm.api.client.ProcessEngine;

/**
 * An implementation of a process definition manager
 * 
 * @author thomas.diesler@jboss.com
 * @since 18-Jun-2008
 */
public class ProcessDefinitionManagerImpl extends ProcessDefinitionManager {

  public void setProcessEngine(ProcessEngine engine) {
    this.engine = engine;
  }

  public ProcessDefinition createProcessDefinition(String jpdl) {
    org.jbpm.graph.def.ProcessDefinition oldPD = org.jbpm.graph.def.ProcessDefinition.parseXmlString(jpdl);
    ProcessDefinition pdef = ProcessDefinitionAdapter.buildProcessDefinition(oldPD);
    addProcessDefinition(pdef);
    return pdef;
  }
  
  public ProcessDefinition createProcessDefinition(URL jpdl) throws IOException {
    org.jbpm.graph.def.ProcessDefinition oldPD = org.jbpm.graph.def.ProcessDefinition.parseXmlInputStream(jpdl.openStream());
    ProcessDefinition pdef = ProcessDefinitionAdapter.buildProcessDefinition(oldPD);
    addProcessDefinition(pdef);
    return pdef;
  }
}
