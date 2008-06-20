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
package org.jbpm.assignment;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.hibernate.cfg.Configuration;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.identity.Entity;
import org.jbpm.identity.hibernate.IdentitySession;
import org.jbpm.identity.xml.IdentityXmlParser;
import org.jbpm.persistence.db.DbPersistenceServiceFactory;
import org.jbpm.svc.Services;
import org.jbpm.graph.exe.Token;
import org.jbpm.taskmgmt.exe.TaskInstance;

public class RulesAssignmentTest extends TestCase {

	JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance();

	DbPersistenceServiceFactory dbPersistenceServiceFactory = (DbPersistenceServiceFactory) jbpmConfiguration
			.getServiceFactory(Services.SERVICENAME_PERSISTENCE);

	JbpmContext jbpmContext;
	long processInstanceId;
	
	public void setUp() {
		dbPersistenceServiceFactory.createSchema();

		loadIdentities();
		deployProcess();
		jbpmContext = jbpmConfiguration.createJbpmContext();
	}

	protected void tearDown() {
		jbpmContext.close();
		dbPersistenceServiceFactory.dropSchema();
		jbpmContext = null;
	}
		
		 
	public void deployProcess() {
		JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
		try {
			ProcessDefinition processDefinition = ProcessDefinition
					.parseXmlResource("RulesAssignment/processdefinition.xml");
			jbpmContext.deployProcessDefinition(processDefinition);
		} finally {
			jbpmContext.close();
		}
	}

	public void loadIdentities() {
		JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
		try {
			Entity[] entities = IdentityXmlParser
					.parseEntitiesResource("identity.db.xml");
			IdentitySession identitySession = new IdentitySession(jbpmContext
					.getSession());
			for (int i = 0; i < entities.length; i++) {
				identitySession.saveEntity(entities[i]);
			}
		} finally {
			jbpmContext.close();
		}
	}

	public void newTransaction() {
		jbpmContext.close();
		jbpmContext = jbpmConfiguration.createJbpmContext();
	}

	public void testRulesAssignment() {
		// start process
		newTransaction();
		processInstanceId = createNewProcessInstance();
		assertNotNull(processInstanceId);
		
		// perform task
		newTransaction();
		long taskInstanceId = acquireTask("tom");
		assertNotNull(taskInstanceId);

		newTransaction();
		completeTask(taskInstanceId);

		newTransaction();
		TaskInstance taskInstance = jbpmContext
				.loadTaskInstance(taskInstanceId);
		Date end = taskInstance.getEnd();
		assertNotNull(end);
	
		// complete process 
		newTransaction();
		ProcessInstance processInstance = getProcessInstance(processInstanceId);
		Date processEnd = processInstance.getEnd();
		assertNotNull(processEnd);

	}

	public long createNewProcessInstance() {
		String processDefinitionName = "RulesAssignment";
		ProcessInstance processInstance = jbpmContext
				.newProcessInstanceForUpdate(processDefinitionName);
		long id = processInstance.getId();
		ContextInstance contextInstance = processInstance.getContextInstance();
		contextInstance.setVariable("processDefinitionName",
				processDefinitionName);
		Order order = new Order(300);
		contextInstance.setVariable("order", order);
		Token token = processInstance.getRootToken();
		token.signal();
		return id;
	}

	public long acquireTask(String actorId) {
		List<TaskInstance> tasklist = getTaskList(actorId);
		Iterator taskIterator = tasklist.iterator();

		TaskInstance taskInstance = null;
		long taskInstanceId = 0;
		while (taskIterator.hasNext()) {
			taskInstance = (TaskInstance) taskIterator.next();

			taskInstanceId = taskInstance.getId();
			taskInstance.start();
		}
		return taskInstanceId;
	}

	public void completeTask(long taskInstanceId) {
		TaskInstance taskInstance = jbpmContext.getTaskInstance(taskInstanceId);
		taskInstance.end();
	}

	public List<TaskInstance> getTaskList(String actorId) {
		newTransaction();
		List<TaskInstance> taskList = jbpmContext.getTaskList(actorId);
		return taskList;
	}

	public ProcessInstance getProcessInstance(long processInstanceId) {
		ProcessInstance processInstance = (ProcessInstance) jbpmContext.loadProcessInstanceForUpdate(processInstanceId);
		return processInstance;
	}


}
