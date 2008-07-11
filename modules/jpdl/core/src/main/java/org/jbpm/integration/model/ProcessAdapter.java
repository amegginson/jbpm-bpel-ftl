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
package org.jbpm.integration.model;

// $Id$

import java.util.List;

import org.jboss.bpm.InvalidProcessException;
import org.jboss.bpm.model.FlowObject;
import org.jboss.bpm.model.Process;
import org.jboss.bpm.model.Task;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.node.EndState;
import org.jbpm.graph.node.StartState;
import org.jbpm.graph.node.State;

/**
 * Adapts on jBPM3 ProcessDefinition to an API Process
 * 
 * @author thomas.diesler@jboss.com
 * @since 18-Jun-2008
 */
public class ProcessAdapter
{

  public static Process buildProcess(org.jbpm.graph.def.ProcessDefinition oldPD)
  {
    ProcessImpl apiProc = new ProcessImpl(oldPD);
    List<org.jbpm.graph.def.Node> oldNodes = oldPD.getNodes();
    for (org.jbpm.graph.def.Node oldNode : oldNodes)
    {
      FlowObject flowObject = NodeAdapter.adaptNode(apiProc, oldNode);
      apiProc.addFlowObject(flowObject);
    }
    return apiProc;
  }

  static class NodeAdapter
  {
    static FlowObject adaptNode(Process apiProc, Node oldNode)
    {
      FlowObject flowObject;
      if (oldNode instanceof StartState)
      {
        flowObject = new StartEventImpl(apiProc, oldNode);
      }
      else if (oldNode instanceof EndState)
      {
        flowObject = new EndEventImpl(apiProc, oldNode);
      }
      else if (oldNode instanceof State)
      {
        Task delegate = null;
        Event event = oldNode.getEvent(Event.EVENTTYPE_NODE_ENTER);
        if (event != null)
        {
          List actions = event.getActions();
          if (actions == null || actions.size() == 0)
            throw new InvalidProcessException("Cannot find action on event: " + event);
          if (actions.size() > 1)
            throw new InvalidProcessException("Multiple actions not supported: " + event);

          Action action = (Action)actions.get(0);
          Object obj = action.getActionDelegation().getInstance();
          if (obj instanceof Task == false)
            throw new InvalidProcessException("Node action is not of type Task");

          delegate = (Task)obj;
        }
        flowObject = new TaskImpl(apiProc, oldNode, delegate);
      }
      else
      {
        throw new InvalidProcessException("Unsupported node type: " + oldNode);
      }
      return flowObject;
    }
  }
}
