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

import java.util.List;

import org.jbpm.api.client.Node;
import org.jbpm.api.client.ProcessDefinition;
import org.jbpm.graph.node.EndState;
import org.jbpm.graph.node.StartState;

/**
 * Adapts on jBPM3 ProcessDefinition to an API ProcessDefinition
 * 
 * @author thomas.diesler@jboss.com
 * @since 18-Jun-2008
 */
class ProcessDefinitionAdapter
{

  static ProcessDefinition buildProcessDefinition(org.jbpm.graph.def.ProcessDefinition oldPD)
  {
    ProcessDefinitionImpl apiPD = new ProcessDefinitionImpl(oldPD);
    List<org.jbpm.graph.def.Node> oldNodes = oldPD.getNodes();
    for (org.jbpm.graph.def.Node oldNode : oldNodes)
    {
      Node apiNode = NodeAdapter.adaptNode(apiPD, oldNode);
      apiPD.addNode(apiNode);
    }
    
    // validate
    validateProcessDefinition(apiPD);
    
    return apiPD;
  }

  private static void validateProcessDefinition(ProcessDefinitionImpl apiPD)
  {
    // These methods are expected to throw exceptions if there are no such states
    apiPD.getStartNode();
    apiPD.getEndNodes();
  }

  static class NodeAdapter
  {

    static Node adaptNode(ProcessDefinition pDef, org.jbpm.graph.def.Node oldNode)
    {
      String oldName = oldNode.getName();
      Node apiNode;
      if (oldNode instanceof StartState)
      {
        apiNode = new StartStateImpl(pDef, oldName);
      }
      else if (oldNode instanceof EndState)
      {
        apiNode = new EndNodeImpl(pDef, oldName);
      }
      else
      {
        apiNode = new NodeImpl(pDef, oldName);
      }
      return apiNode;
    }
  }
}
