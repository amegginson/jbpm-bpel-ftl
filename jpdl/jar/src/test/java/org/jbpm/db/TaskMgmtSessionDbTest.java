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
package org.jbpm.db;

import java.util.*;

import org.jbpm.graph.def.*;
import org.jbpm.graph.exe.*;
import org.jbpm.taskmgmt.def.*;
import org.jbpm.taskmgmt.exe.*;

public class TaskMgmtSessionDbTest extends AbstractDbTestCase {

  ProcessDefinition processDefinition = null;
  TaskMgmtDefinition taskMgmtDefinition = null;
  Task laundry = null;
  Task dishes = null;

  ProcessInstance processInstance = null;
  TaskMgmtInstance taskMgmtInstance = null;

  public void setUp() throws Exception {
    super.setUp();
    
    processDefinition = new ProcessDefinition();
    taskMgmtDefinition = new TaskMgmtDefinition();
    processDefinition.addDefinition(taskMgmtDefinition);
    laundry = new Task("laundry");
    taskMgmtDefinition.addTask(laundry);
    dishes = new Task("dishes");
    taskMgmtDefinition.addTask(dishes);
    
    graphSession.saveProcessDefinition(processDefinition);
    
    processInstance = new ProcessInstance(processDefinition);
    processInstance = saveAndReload(processInstance);
    
    processDefinition = processInstance.getProcessDefinition();
    taskMgmtDefinition = processDefinition.getTaskMgmtDefinition();
    laundry = taskMgmtDefinition.getTask("laundry");
    dishes = taskMgmtDefinition.getTask("dishes");
    taskMgmtInstance = processInstance.getTaskMgmtInstance();
  }

  public void testFindTaskInstancesByActorId() {
    taskMgmtInstance.addTaskInstance(new TaskInstance("laundry", "me"));
    taskMgmtInstance.addTaskInstance(new TaskInstance("dishes", "me"));
    taskMgmtInstance.addTaskInstance(new TaskInstance("vacation", "the other guy"));
    
    processInstance = saveAndReload(processInstance);
    
    List taskInstances = taskMgmtSession.findTaskInstances("me");
    assertEquals(2, taskInstances.size());
    TaskInstance taskInstanceOne = (TaskInstance)taskInstances.get(0);
    TaskInstance taskInstanceTwo = (TaskInstance)taskInstances.get(1);
    assertEquals("me", taskInstanceOne.getActorId());
    assertEquals("me", taskInstanceTwo.getActorId());
    assertNotSame(taskInstanceOne, taskInstanceTwo);
  }

  public void testFindTaskInstancesByActorIds() {
    taskMgmtInstance.addTaskInstance(new TaskInstance("laundry", "me"));
    taskMgmtInstance.addTaskInstance(new TaskInstance("dishes", "me"));
    taskMgmtInstance.addTaskInstance(new TaskInstance("write software", "me"));
    taskMgmtInstance.addTaskInstance(new TaskInstance("vacation", "the other guy"));
    taskMgmtInstance.addTaskInstance(new TaskInstance("try", "a nobody"));
    taskMgmtInstance.addTaskInstance(new TaskInstance("pretend", "a nobody"));
    
    processInstance = saveAndReload(processInstance);
    
    List taskInstances = taskMgmtSession.findTaskInstances(new String[]{"me", "the other guy"});
    assertEquals(4, taskInstances.size());
  }

  public void testLoadTaskInstance() {
    TaskInstance taskInstance = new TaskInstance("laundry", "me");
    taskMgmtInstance.addTaskInstance(taskInstance);
    
    processInstance = saveAndReload(processInstance);
    
    taskInstance = taskMgmtSession.loadTaskInstance(taskInstance.getId());
    assertNotNull(taskInstance);
  }

  public void testTaskMgmtFinderMethods() {
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition name='searchable'>" +
      "  <start-state>" +
      "    <transition to='f'/>" +
      "  </start-state>" +
      "  <fork name='f'>" +
      "    <transition name='washing' to='collectLaundry'/>" +
      "    <transition name='cleaning' to='cleanToilets'/>" +
      "  </fork>" +
      "  <task-node name='collectLaundry'>" +
      "    <task name='getLaundryFromBasket' />" +
      "    <task name='askHusbandWhereHeDumpedHisClothes' />" +
      "    <task name='lookUnderChildrensBeds' />" +
      "  </task-node>" +
      "  <task-node name='cleanToilets'>" +
      "    <task name='cleanToilets'/>" +
      "  </task-node>" +
      "</process-definition>"
    );
    jbpmContext.deployProcessDefinition(processDefinition);
    
    newTransaction();
    
    ProcessInstance processInstance = jbpmContext.newProcessInstance("searchable");
    processInstance.signal();
    
    processInstance = saveAndReload(processInstance);
    
    List taskInstances = taskMgmtSession.findTaskInstancesByProcessInstance(processInstance);
    Set collectedTaskInstanceNames = new HashSet();
    Iterator iter = taskInstances.iterator();
    while (iter.hasNext()) {
      TaskInstance taskInstance = (TaskInstance) iter.next();
      collectedTaskInstanceNames.add(taskInstance.getName());
    }
    
    Set expectedTaskInstanceNames = new HashSet();
    expectedTaskInstanceNames.add("getLaundryFromBasket");
    expectedTaskInstanceNames.add("askHusbandWhereHeDumpedHisClothes");
    expectedTaskInstanceNames.add("lookUnderChildrensBeds");
    expectedTaskInstanceNames.add("cleanToilets");
    
    assertEquals(expectedTaskInstanceNames, collectedTaskInstanceNames);
    
    List nodes = graphSession.findActiveNodesByProcessInstance(processInstance);
    Set collectedNodeNames = new HashSet();
    iter = nodes.iterator();
    while (iter.hasNext()) {
      Node node = (Node) iter.next();
      collectedNodeNames.add(node.getName());
    }
    
    Set expectedNodeNames = new HashSet();
    expectedNodeNames.add("collectLaundry");
    expectedNodeNames.add("cleanToilets");

    assertEquals(expectedNodeNames, collectedNodeNames);
  }
}
