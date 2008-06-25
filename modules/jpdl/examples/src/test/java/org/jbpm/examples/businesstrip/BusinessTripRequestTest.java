package org.jbpm.examples.businesstrip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.persistence.db.DbPersistenceServiceFactory;
import org.jbpm.svc.Services;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.jbpm.taskmgmt.exe.TaskMgmtInstance;

public class BusinessTripRequestTest extends TestCase {
	Log log = LogFactory.getLog(this.getClass());

	JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance();
	DbPersistenceServiceFactory dbPersistenceServiceFactory = (DbPersistenceServiceFactory) jbpmConfiguration
	    .getServiceFactory(Services.SERVICENAME_PERSISTENCE);

	JbpmContext jbpmContext;

	ProcessInstance processInstance = null;
	ContextInstance contextInstance = null;
	TaskMgmtInstance taskMgmtInstance = null;
	long processInstanceId = -1;

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

	private void newTransaction() {
		jbpmContext.close();
		jbpmContext = jbpmConfiguration.createJbpmContext();
	}

	private void deployProcess() {
		JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
		try {
			ProcessDefinition processDefinition = ProcessDefinition
			    .parseXmlResource("processdefinition.xml");
			jbpmContext.deployProcessDefinition(processDefinition);
		} finally {
			jbpmContext.close();
		}
	}

	private TaskInstance createNewProcessInstance() {
		processInstance = jbpmContext
		    .newProcessInstanceForUpdate("business trip request");
		processInstanceId = processInstance.getId();
		contextInstance = processInstance.getContextInstance();
		taskMgmtInstance = processInstance.getTaskMgmtInstance();
		return processInstance.getTaskMgmtInstance().createStartTaskInstance();
	}

	public void testTaskParameters() {
		TaskInstance taskInstance = createNewProcessInstance();
		assertEquals("submit business trip request", taskInstance.getName());
		assertEquals(0, taskInstance.getVariables().size());
	}

	public void testSubmitRaiseRequestTask() {
		jbpmContext.setActorId("employee");
		TaskInstance taskInstance = createNewProcessInstance();

		Map<String, Object> taskVariables = new HashMap<String, Object>();

		taskVariables.put("purpose", "Conference in MIT");
		taskVariables
		    .put(
		        "description",
		        "This conference is mainly to highlight to impact of ESB technologies on the current industries");
		taskVariables.put("allocated budget", 3000);
		taskVariables.put("start date", "8/12/2009");
		taskVariables.put("end date", "8/21/2009");
		taskInstance.addVariables(taskVariables);
		taskInstance.end();

		assertEquals("Conference in MIT", contextInstance.getVariable("purpose"));
		assertEquals(
		    "This conference is mainly to highlight to impact of ESB technologies on the current industries",
		    contextInstance.getVariable("description"));
		assertEquals(3000, contextInstance.getVariable("allocated budget"));
		assertEquals("8/12/2009", contextInstance.getVariable("start date"));
		assertEquals("8/21/2009", contextInstance.getVariable("end date"));
		assertEquals("employee", taskMgmtInstance.getSwimlaneInstance("employee")
		    .getActorId());
	}

	@SuppressWarnings("unchecked")
	public void testRejectBusinessTripRequest() {
		// Employee submits a business trip request
		jbpmContext.setActorId("employee");
		TaskInstance taskInstance = createNewProcessInstance();

		Map<String, Object> taskVariables = new HashMap<String, Object>();
		taskVariables.put("purpose", "Conference in MIT");
		taskVariables
		    .put(
		        "description",
		        "This conference is mainly to highlight to impact of ESB technologies on the current industries");
		taskVariables.put("allocated budget", 3000);
		taskVariables.put("start date", "8/12/2009");
		taskVariables.put("end date", "8/21/2009");

		taskInstance.addVariables(taskVariables);
		taskInstance.end();
		jbpmContext.save(processInstance);
		processInstanceId = processInstance.getId();

		// Manager rejects the raise request
		newTransaction();
		log.info(jbpmContext.getTaskMgmtSession()
		    .findTaskInstancesByProcessInstance(processInstance).size());

		List<TaskInstance> managerTasksList = jbpmContext.getTaskMgmtSession()
		    .findTaskInstances("manager");
		assertEquals(1, managerTasksList.size());

		TaskInstance managerTask = managerTasksList.get(0);
		managerTask
		    .addComment("Conference theme doesn't align with company's current focus");
		managerTask.end("reject");
		assertEquals("manager", managerTask.getActorId());
	}

	@SuppressWarnings("unchecked")
	public void testAcceptBusinessTripRequest() {
		// Employee submits a raise request
		jbpmContext.setActorId("employee");
		TaskInstance taskInstance = createNewProcessInstance();

		Map<String, Object> taskVariables = new HashMap<String, Object>();
		taskVariables.put("purpose", "Conference in MIT");
		taskVariables
		    .put(
		        "description",
		        "This conference is mainly to highlight to impact of ESB technologies on the current industries");
		taskVariables.put("allocated budget", 3000);
		taskVariables.put("start date", "8/12/2009");
		taskVariables.put("end date", "8/21/2009");
		taskVariables.put("country", "USA");
		taskVariables.put("city", "Kansas");
		taskInstance.addVariables(taskVariables);
		taskInstance.end();
		jbpmContext.save(processInstance);
		processInstanceId = processInstance.getId();

		// Manager rejects the raise request
		newTransaction();
		List<TaskInstance> managerTasksList = jbpmContext.getTaskMgmtSession()
		    .findTaskInstances("manager");
		assertEquals(1, managerTasksList.size());

		TaskInstance managerTask = managerTasksList.get(0);
		managerTask.addComment("Business trip approved");
		managerTask.end("approve");
		assertEquals("manager", managerTask.getActorId());
		jbpmContext.save(managerTask);

		newTransaction();
		List<TaskInstance> accountantTasksList = jbpmContext.getTaskMgmtSession()
		    .findTaskInstances("accountant");
		assertEquals(1, accountantTasksList.size());
		TaskInstance accountantTask = accountantTasksList.get(0);
		accountantTask.end();
		jbpmContext.save(accountantTask);
	}
}
