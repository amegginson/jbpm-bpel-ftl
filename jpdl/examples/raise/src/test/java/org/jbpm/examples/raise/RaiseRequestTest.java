package org.jbpm.examples.raise;

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

public class RaiseRequestTest extends TestCase {
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
		processInstance = jbpmContext.newProcessInstanceForUpdate("raise request");
		processInstanceId = processInstance.getId();
		contextInstance = processInstance.getContextInstance();
		taskMgmtInstance = processInstance.getTaskMgmtInstance();
		return processInstance.getTaskMgmtInstance().createStartTaskInstance();
	}

	public void testTaskParameters() {
		TaskInstance taskInstance = createNewProcessInstance();
		assertEquals("submit raise request", taskInstance.getName());
		assertEquals(0, taskInstance.getVariables().size());
	}

	public void testSubmitRaiseRequestTask() {
		jbpmContext.setActorId("employee");
		TaskInstance taskInstance = createNewProcessInstance();

		Map<String, Object> taskVariables = new HashMap<String, Object>();
		taskVariables.put("reason", "I need to buy a jet");
		taskVariables.put("amount", 600);

		taskInstance.addVariables(taskVariables);
		taskInstance.end();

		assertEquals("I need to buy a jet", contextInstance.getVariable("reason"));
		assertEquals(600, contextInstance.getVariable("amount"));
		assertEquals("employee", taskMgmtInstance.getSwimlaneInstance("employee")
		    .getActorId());
	}

	@SuppressWarnings("unchecked")
  public void testManagerEvaluationReject() {
		TaskInstance taskInstance = createNewProcessInstance();
		Map<String, Object> taskVariables = new HashMap<String, Object>();
		taskVariables.put("reason", "I need to buy a jet");
		taskVariables.put("amount", 600);
		taskInstance.addVariables(taskVariables);
		taskInstance.end();
		jbpmContext.save(processInstance);
		newTransaction();

		List<TaskInstance> managerTasks = jbpmContext.getTaskMgmtSession()
		    .findTaskInstances("manager");
		assertEquals(1, managerTasks.size());

		TaskInstance managerTask = managerTasks.get(0);
		managerTask.end("reject");

		List<TaskInstance> foTasks = jbpmContext.getTaskMgmtSession()
		    .findTaskInstances("fo");
		assertEquals(0, foTasks.size());
	}

	@SuppressWarnings("unchecked")
  public void testManagerEvaluationAcceptFOReject() {
		TaskInstance taskInstance = createNewProcessInstance();
		Map<String, Object> taskVariables = new HashMap<String, Object>();
		taskVariables.put("reason", "I need to buy a jet");
		taskVariables.put("amount", 600);
		taskInstance.addVariables(taskVariables);
		taskInstance.end();
		jbpmContext.save(processInstance);
		newTransaction();

		List<TaskInstance> managerTasks = jbpmContext.getTaskMgmtSession()
		    .findTaskInstances("manager");
		assertEquals(1, managerTasks.size());
		TaskInstance managerTask = managerTasks.get(0);
		managerTask.start();
		managerTask.end("accept");

		newTransaction();
		List<TaskInstance> foTasks = jbpmContext.getTaskMgmtSession()
		    .findTaskInstances("fo");
		assertEquals(1, foTasks.size());

		TaskInstance foTask = foTasks.get(0);
		foTask.start();
		foTask.addComment("Justify two consecutive raises");
		foTask.end("reject");

		managerTasks = jbpmContext.getTaskMgmtSession()
		    .findTaskInstances("manager");
		assertEquals(0, managerTasks.size());
	}

	@SuppressWarnings("unchecked")
  public void testManagerEvaluationAcceptFOAccpet() {
		TaskInstance taskInstance = createNewProcessInstance();
		Map<String, Object> taskVariables = new HashMap<String, Object>();
		taskVariables.put("reason", "I need to buy a jet");
		taskVariables.put("amount", 600);
		taskInstance.addVariables(taskVariables);
		taskInstance.end();
		jbpmContext.save(processInstance);
		newTransaction();

		List<TaskInstance> managerTasks = jbpmContext.getTaskMgmtSession()
		    .findTaskInstances("manager");
		assertEquals(1, managerTasks.size());
		TaskInstance managerTask = managerTasks.get(0);
		managerTask.start();
		managerTask.end("accept");

		newTransaction();
		List<TaskInstance> foTasks = jbpmContext.getTaskMgmtSession()
		    .findTaskInstances("fo");
		assertEquals(1, foTasks.size());

		TaskInstance foTask = foTasks.get(0);
		foTask.start();
		foTask.addComment("Justify two consecutive raises");
		foTask.end("accept");

		newTransaction();
		List<TaskInstance> accountantTasks = jbpmContext.getTaskMgmtSession()
		    .findTaskInstances("accountant");
		assertEquals(1, accountantTasks.size());

		TaskInstance accountantTask = accountantTasks.get(0);
		accountantTask.start();
		accountantTask.addComment("ERP updated");
		accountantTask.end("terminate");
	}

	@SuppressWarnings("unchecked")
  public void testManagerEvaluationAcceptFOMultipleIterationsAccpet() {
		TaskInstance taskInstance = createNewProcessInstance();
		Map<String, Object> taskVariables = new HashMap<String, Object>();
		taskVariables.put("reason", "I need to buy a jet");
		taskVariables.put("amount", 600);
		taskInstance.addVariables(taskVariables);
		taskInstance.end();
		jbpmContext.save(processInstance);
		newTransaction();

		List<TaskInstance> managerTasks = jbpmContext.getTaskMgmtSession()
		    .findTaskInstances("manager");
		assertEquals(1, managerTasks.size());
		TaskInstance managerTask = managerTasks.get(0);
		managerTask.start();
		managerTask.end("accept");

		newTransaction();
		List<TaskInstance> foTasks = jbpmContext.getTaskMgmtSession()
		    .findTaskInstances("fo");
		assertEquals(1, foTasks.size());

		TaskInstance foTask = foTasks.get(0);
		foTask.start();
		foTask.addComment("Justify two consecutive raises");
		foTask.end("more justification required");

		newTransaction();
		managerTasks = jbpmContext.getTaskMgmtSession()
		    .findTaskInstances("manager");
		assertEquals(1, managerTasks.size());

		managerTask = managerTasks.get(0);
		managerTask.start();
		managerTask.addComment("The guy exceeds all the expectations");
		managerTask.end("accept");

		newTransaction();
		foTasks = jbpmContext.getTaskMgmtSession().findTaskInstances("fo");
		assertEquals(1, foTasks.size());

		foTask = foTasks.get(0);
		foTask.start();
		foTask.addComment("justification accepted");
		foTask.end("accept");
		
		newTransaction();
		List<TaskInstance> accountantTasks = jbpmContext.getTaskMgmtSession()
		    .findTaskInstances("accountant");
		assertEquals(1, accountantTasks.size());

		TaskInstance accountantTask = accountantTasks.get(0);
		accountantTask.start();
		accountantTask.addComment("ERP updated");
		accountantTask.end("terminate");
	}
}
