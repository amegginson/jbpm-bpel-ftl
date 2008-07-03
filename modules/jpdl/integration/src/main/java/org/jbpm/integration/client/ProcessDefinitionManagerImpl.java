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

import org.jboss.bpm.InvalidProcessDefinitionException;
import org.jboss.bpm.client.ProcessDefinition;
import org.jboss.bpm.client.ProcessDefinitionManager;
import org.jboss.bpm.client.ProcessEngine;
import org.jbpm.jpdl.JpdlException;

/**
 * An implementation of a process definition manager
 * 
 * @author thomas.diesler@jboss.com
 * @since 18-Jun-2008
 */
public class ProcessDefinitionManagerImpl extends ProcessDefinitionManager
{

  public void setProcessEngine(ProcessEngine engine)
  {
    this.engine = engine;
  }

  @Override
  protected ProcessDefinition createProcessDefinitionOverride(String jpdl)
  {
    org.jbpm.graph.def.ProcessDefinition oldPD;
    try
    {
      oldPD = org.jbpm.graph.def.ProcessDefinition.parseXmlString(jpdl);
    }
    catch (JpdlException ex)
    {
      throw new InvalidProcessDefinitionException(ex);
    }
    ProcessDefinition pdef = ProcessDefinitionAdapter.buildProcessDefinition(oldPD);
    addProcessDefinition(pdef);
    return pdef;
  }

  @Override
  protected ProcessDefinition createProcessDefinitionOverride(URL jpdl) throws IOException
  {
    org.jbpm.graph.def.ProcessDefinition oldPD;
    try
    {
      oldPD = org.jbpm.graph.def.ProcessDefinition.parseXmlInputStream(jpdl.openStream());
    }
    catch (JpdlException ex)
    {
      throw new InvalidProcessDefinitionException(ex);
    }
    ProcessDefinition pdef = ProcessDefinitionAdapter.buildProcessDefinition(oldPD);
    addProcessDefinition(pdef);
    return pdef;
  }
}
