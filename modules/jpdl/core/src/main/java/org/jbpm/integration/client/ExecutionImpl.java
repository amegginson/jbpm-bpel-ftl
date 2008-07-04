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

//$Id$

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jboss.bpm.client.ProcessInstance;
import org.jboss.bpm.client.internal.AbstractExecution;
import org.jboss.bpm.process.Execution;
import org.jboss.bpm.process.ExecutionContext;
import org.jboss.bpm.process.Node;

/**
 * Represents an execution of a process instance. 
 * 
 * @author thomas.diesler@jboss.com
 * @since 18-Jun-2008
 */
public class ExecutionImpl extends AbstractExecution
{
  private org.jbpm.graph.exe.Execution oldEx;
  private ExecutionContext context = new ContextImpl();

  ExecutionImpl(ProcessInstance pi, org.jbpm.graph.exe.Execution oldEx)
  {
    super(pi);
    this.oldEx = oldEx;
    init(oldEx.getKey());
  }

  @Override
  protected Node getNodeOverride()
  {
    org.jbpm.graph.def.Node oldNode = oldEx.getRootToken().getNode();
    Node apiNode = getProcessInstance().getProcessDefinition().findNode(oldNode.getName());
    return apiNode;
  }

  @Override
  protected void signalOverride()
  {
    oldEx.getContextInstance().setTransientVariable(Execution.class.getName(), this);
    oldEx.signal();
  }

  @Override
  public String getName()
  {
    return oldEx.getKey();
  }

  @Override
  protected void setName(String name)
  {
    oldEx.setKey(name);
  }

  @Override
  public ExecutionContext getContext()
  {
    return context;
  }
  
  private class ContextImpl extends ExecutionContext
  {
    @SuppressWarnings("unchecked")
    public <T> T addAttachment(Class<T> clazz, Object value)
    {
      Key key = new Key(clazz, null);
      oldEx.getContextInstance().setTransientVariable(key, value);
      return (T)value;
    }

    @SuppressWarnings("unchecked")
    public <T> T addAttachment(Class<T> clazz, String name, Object value)
    {
      Key key = new Key(clazz, name);
      oldEx.getContextInstance().setTransientVariable(key, value);
      return (T)value;
    }

    @SuppressWarnings("unchecked")
    public <T> T addAttachment(String name, Object value)
    {
      Key key = new Key(null, name);
      oldEx.getContextInstance().setTransientVariable(key, value);
      return (T)value;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttachment(Class<T> clazz)
    {
      Key key = new Key(clazz, null);
      Object value = oldEx.getContextInstance().getTransientVariable(key);
      return (T)value;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttachment(Class<T> clazz, String name)
    {
      Key key = new Key(clazz, name);
      Object value = oldEx.getContextInstance().getTransientVariable(key);
      return (T)value;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttachment(String name)
    {
      Key key = new Key(null, name);
      Object value = oldEx.getContextInstance().getTransientVariable(key);
      return (T)value;
    }

    public Collection<Key> getAttachmentKeys()
    {
      Set<Key> keys = new HashSet<Key>();
      Iterator<?> itKeys = oldEx.getContextInstance().getTransientVariables().keySet().iterator();
      while (itKeys.hasNext())
      {
        Object key = itKeys.next();
        if (key instanceof Key)
        {
          keys.add((Key)key);
        }
      }
      return keys;
    }

    @SuppressWarnings("unchecked")
    public <T> T removeAttachment(Class<T> clazz)
    {
      Key key = new Key(clazz, null);
      Object value = oldEx.getContextInstance().deleteTransientVariable(key);
      return (T)value;
    }

    @SuppressWarnings("unchecked")
    public <T> T removeAttachment(Class<T> clazz, String name)
    {
      Key key = new Key(clazz, name);
      Object value = oldEx.getContextInstance().deleteTransientVariable(key);
      return (T)value;
    }

    @SuppressWarnings("unchecked")
    public <T> T removeAttachment(String name)
    {
      Key key = new Key(null, name);
      Object value = oldEx.getContextInstance().deleteTransientVariable(key);
      return (T)value;
    }
  }
}
