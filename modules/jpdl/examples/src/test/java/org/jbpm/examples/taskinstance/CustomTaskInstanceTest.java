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
package org.jbpm.taskinstance;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.identity.Entity;
import org.jbpm.identity.hibernate.IdentitySession;
import org.jbpm.identity.xml.IdentityXmlParser;
import org.jbpm.persistence.db.DbPersistenceServiceFactory;
import org.jbpm.svc.Services;
import org.jbpm.taskinstance.CustomTaskInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * This example shows how to extend the TaskInstance by adding a custom property.
 */
public class CustomTaskInstanceTest extends TestCase {

	JbpmConfiguration jbpmConfiguration = null;
	//JbpmConfiguration.getInstance();

	DbPersistenceServiceFactory dbPersistenceServiceFactory = null;

	JbpmContext jbpmContext;

	Session s;

	ProcessInstance processInstance = null;

	ContextInstance contextInstance = null;

	public void setUp() {

		// the jbpm.cfg.xml file is modified to add the CustomTaskInstanceFactory
 	    // so we will read in the file from the config directory of this example
	    jbpmConfiguration = JbpmConfiguration.parseResource("jbpm.cfg.xml");

		dbPersistenceServiceFactory = (DbPersistenceServiceFactory) jbpmConfiguration
		.getServiceFactory(Services.SERVICENAME_PERSISTENCE);
	    
		// the CustomTaskInstance mapping file reference  
		//    <mapping resource="org/jbpm/taskinstance/CustomTaskInstance.hbm.xml"/>
	    // has been added to to the bottom of the hibernate.cfg.xml file in the config directory
	    
		// now create the schema
	    // this is also a step that typically would be performed as an independent step
	    // using the jbpm schema created from the jbpm mapping files
		
		dbPersistenceServiceFactory.createSchema();
		deployProcess();
		jbpmContext = jbpmConfiguration.createJbpmContext();
	}

	public void tearDown() {
		jbpmContext.close();
		dbPersistenceServiceFactory.dropSchema();
		jbpmContext = null;
	}

	public void newTransaction() {
		jbpmContext.close();
		jbpmContext = jbpmConfiguration.createJbpmContext();
	}

	public void deployProcess() {
		jbpmContext = jbpmConfiguration.createJbpmContext();
		try {
			ProcessDefinition processDefinition = ProcessDefinition
					.parseXmlResource("CustomTaskInstance/processdefinition.xml");
			jbpmContext.deployProcessDefinition(processDefinition);
		} finally {
			jbpmContext.close();
		}
	}

	public void loadIdentities() {
		jbpmContext = jbpmConfiguration.createJbpmContext();
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

	public void testCustomTaskInstance() {
		// create processInstance
		newTransaction();
		long processInstanceId = createNewProcessInstance();
		assertNotNull(processInstanceId);
		assertFalse("ProcessInstanceId is 0", processInstanceId == 0);

		// perform the task
		newTransaction();
		long taskInstanceId = acquireTask();
		assertNotNull(taskInstanceId);
		assertFalse("TaskInstanceId is 0", taskInstanceId == 0);
		newTransaction();

		completeTask(taskInstanceId);

		newTransaction();
		TaskInstance taskInstance = jbpmContext
				.loadTaskInstance(taskInstanceId);
		Date end = taskInstance.getEnd();
		assertNotNull(end);

		// check process is completed
		newTransaction();
		Date processEnd = null;
		processInstance = jbpmContext.getProcessInstance(processInstanceId);
		processEnd = processInstance.getEnd();
		assertNotNull(processEnd);
	}

	public long createNewProcessInstance() {
		String processDefinitionName = "CustomTaskInstance";
		processInstance = jbpmContext
				.newProcessInstanceForUpdate(processDefinitionName);
		long processInstanceId = processInstance.getId();
		contextInstance = processInstance.getContextInstance();
		contextInstance.setVariable("processDefinitionName",
				processDefinitionName);
		contextInstance.setVariable("customId", "abc");
		Token token = processInstance.getRootToken();
		token.signal();
		return processInstanceId;

	}


	public long acquireTask() {

		List<CustomTaskInstance> tasklist = findPooledTaskListByCustomId("reviewers", "abc");
		Iterator taskIterator = tasklist.iterator();

		CustomTaskInstance taskInstance = null;
		long taskInstanceId = 0;
		while (taskIterator.hasNext()) {
			taskInstance = (CustomTaskInstance) taskIterator.next();
			taskInstanceId = taskInstance.getId();
			taskInstance.start();
			taskInstance.setActorId("tom");
			String customId = taskInstance.getCustomId();
			assertEquals("abc", customId);
			System.out.println(taskInstanceId);
		}
		return taskInstanceId;
	}

	public void completeTask(long taskInstanceId) {
		s = jbpmContext.getSession();
		CustomTaskInstance taskInstance = (CustomTaskInstance) s.load(
				CustomTaskInstance.class, new Long(taskInstanceId));

		taskInstance.end();

	}


	private static final String findPooledTaskInstancesByCustomId = "select distinct ti "
		+ "from org.jbpm.taskinstance.CustomTaskInstance ti"
		+ "     join ti.pooledActors pooledActor "
		+ "where "
		+ "pooledActor.actorId = :pooledActorId "
		+ "  and ti.actorId is null "
		+ "  and ti.end is null "
		+ "  and ti.isCancelled = false"
		+ "  and ti.customId = :customId";

	public List<CustomTaskInstance> findPooledTaskListByCustomId(
			String actorId, String customId) {
		List<CustomTaskInstance> taskList = null;
		s = jbpmContext.getSession();
		Query query = s.createQuery(findPooledTaskInstancesByCustomId);
		query.setString("pooledActorId", actorId);
		query.setString("customId", customId);
		taskList = query.list();
		return taskList;
	}

}
