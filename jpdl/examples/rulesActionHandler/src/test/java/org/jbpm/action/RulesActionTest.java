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
package org.jbpm.action;

import java.util.Date;

import junit.framework.TestCase;

import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.persistence.db.DbPersistenceServiceFactory;
import org.jbpm.svc.Services;
import org.jbpm.graph.exe.Token;

public class RulesActionTest extends TestCase {

	JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance();

	DbPersistenceServiceFactory dbPersistenceServiceFactory = (DbPersistenceServiceFactory) jbpmConfiguration
			.getServiceFactory(Services.SERVICENAME_PERSISTENCE);

	JbpmContext jbpmContext;
	long processInstanceId;

	public void setUp() {
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
		JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
		try {
			ProcessDefinition processDefinition = ProcessDefinition
					.parseXmlResource("RulesAction/processdefinition.xml");
			jbpmContext.deployProcessDefinition(processDefinition);
		} finally {
			jbpmContext.close();
		}
	}


	public void testRulesAssignment() {
		// start process
		newTransaction();
		processInstanceId = createNewProcessInstance();
		assertNotNull(processInstanceId);

		newTransaction();
		ProcessInstance processInstance = getProcessInstance(processInstanceId);
		
		assertNotNull(processInstance);
		
		Date processEnd = processInstance.getEnd();
		assertNotNull(processEnd);
		String shipper = (String) processInstance.getContextInstance().getVariable("shipper"); 
		assertEquals("shipper is FEDX", shipper, "FEDX");

	}

	public long createNewProcessInstance() {
		String processDefinitionName = "RulesAction";
		ProcessInstance processInstance = jbpmContext
				.newProcessInstanceForUpdate(processDefinitionName);
		long id = processInstance.getId();
		ContextInstance contextInstance = processInstance.getContextInstance();
		contextInstance.setVariable("processDefinitionName",
				processDefinitionName);
		Order order = new Order(300);
		Customer customer = new Customer("Fred", new Integer(5), new Integer(25), new Long (100000));
		contextInstance.setVariable("order", order);
		contextInstance.setVariable("customer", customer);
		Token token = processInstance.getRootToken();
		token.signal();
		return id;
	}

	public ProcessInstance getProcessInstance(long processInstanceId) {
		ProcessInstance processInstance = (ProcessInstance) jbpmContext.loadProcessInstanceForUpdate(processInstanceId);
		return processInstance;
	}
}
