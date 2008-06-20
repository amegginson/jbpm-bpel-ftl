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
package org.jbpm.svc.save;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.jbpm.JbpmContext;
import org.jbpm.JbpmException;
import org.jbpm.context.exe.VariableContainer;
import org.jbpm.context.exe.VariableInstance;
import org.jbpm.context.exe.variableinstance.UnpersistableInstance;
import org.jbpm.graph.exe.ProcessInstance;

public class CheckUnpersistableVariablesOperation implements SaveOperation {

  private static final long serialVersionUID = 1L;

  public void save(ProcessInstance processInstance, JbpmContext jbpmContext) {
    Collection updatedVariableContainers = VariableContainer.getUpdatedVariableContainers(processInstance);
    if (updatedVariableContainers!=null) {

      // loop over all updated variable containers
      Iterator iter = updatedVariableContainers.iterator();
      while (iter.hasNext()) {
        VariableContainer variableContainer = (VariableContainer) iter.next();
        Map variableInstances = variableContainer.getVariableInstances();
        if (variableInstances!=null) {

          // loop over all variable instances in the container
          Iterator varInstancesIter = variableInstances.entrySet().iterator();
          while (varInstancesIter.hasNext()) {
            Map.Entry entry = (Map.Entry) varInstancesIter.next();
            String name = (String) entry.getKey();
            VariableInstance variableInstance = (VariableInstance) entry.getValue();

            // if the variable is of the unpersistable type...  booom!
            if (variableInstance instanceof UnpersistableInstance) {
              Object value = variableInstance.getValue();
              if (value!=null) {
                throw new JbpmException("variable '"+name+"' in '"+variableContainer+"' contains '"+value+"': type '"+value.getClass().getName()+"' is unpersistable according to the jbpm.varmapping.xml configuration");
              } else {
                throw new JbpmException("variable '"+name+"' in '"+variableContainer+"' was created with a non persistable value");
              }
            }
          }
        }
      }
    }
  }

  // private static Log log = LogFactory.getLog(CheckUnpersistableVariablesOperation.class);
}
