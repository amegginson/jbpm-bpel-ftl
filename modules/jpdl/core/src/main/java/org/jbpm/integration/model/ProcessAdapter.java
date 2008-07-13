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

import java.util.Iterator;
import java.util.List;

import org.jboss.bpm.InvalidProcessException;
import org.jboss.bpm.model.ExecutionHandler;
import org.jboss.bpm.model.FlowObject;
import org.jboss.bpm.model.MultipleOutFlowSupport;
import org.jboss.bpm.model.Process;
import org.jboss.bpm.model.SequenceFlow;
import org.jboss.bpm.model.SingleOutFlowSupport;
import org.jboss.bpm.model.Task;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.Transition;
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
        initTranstions(flowObject, oldNode);
      }
      else if (oldNode instanceof EndState)
      {
        flowObject = new EndEventImpl(apiProc, oldNode);
        initTranstions(flowObject, oldNode);
      }
      else if (oldNode instanceof State)
      {
        ExecutionHandler delegate = null;
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
          if (obj instanceof ExecutionHandler == false)
            throw new InvalidProcessException("Node action is not of type ExecutionHandler");

          delegate = (ExecutionHandler)obj;
        }
        flowObject = new TaskImpl(apiProc, oldNode, delegate);
        initTranstions(flowObject, oldNode);
      }
      else
      {
        throw new InvalidProcessException("Unsupported node type: " + oldNode);
      }
      return flowObject;
    }

    private static void initTranstions(FlowObject flowObject, Node oldNode)
    {
      if (flowObject instanceof SingleOutFlowSupport)
      {
        SingleOutFlowSupport sof = (SingleOutFlowSupport)flowObject;
        List outTrans = oldNode.getLeavingTransitions();
        if (outTrans != null && outTrans.size() > 0)
        {
          Transition trans = (Transition)outTrans.get(0);
          SequenceFlow flow = new SequenceFlow(trans.getTo().getName());
          sof.setOutFlow(flow);
        }
      }
      if (flowObject instanceof MultipleOutFlowSupport)
      {
        MultipleOutFlowSupport mof = (MultipleOutFlowSupport)flowObject;
        List outTrans = oldNode.getLeavingTransitions();
        if (outTrans != null && outTrans.size() > 0)
        {
          Iterator it = outTrans.iterator();
          while (it.hasNext())
          {
            Transition trans = (Transition)it.next();
            SequenceFlow flow = new SequenceFlow(trans.getTo().getName());
            mof.addOutFlow(flow);
          }
        }
      }
    }
  }
}
