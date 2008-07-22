package org.jbpm.command;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.jbpm.JbpmContext;
import org.jbpm.JbpmException;
import org.jbpm.db.JbpmSchema;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.taskmgmt.def.Task;
import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * <b>THIS COMMAND IS NOT YET STABLE, BUT FEEL FREE TO TEST :-)</b>
 * 
 * change the version of a running process instance. This works only, if the
 * current node is also available in the new version of the process definition
 * (identified by name, so the name has to be exactly the same). One problem
 * with this approach ist also, that if a task with the same name is moved to
 * another node (but this is a rare case)
 * 
 * 
 * make trouble, if there are 2 tokens in the process, because only one actual
 * node is used...
 * 
 * Possible workaround: use process id instead of node id.
 * 
 * TODO: new hibernate query for that? Proposal Fluffi "select distinct task " +
 * "from " + Task.class.getName() + " task " + "where task.name = :taskName " + "
 * and task.processDefinition.id = :processDefinitionId ";
 * 
 * @author Bernd Ruecker (bernd.ruecker@camunda.com)
 */
public class ChangeProcessInstanceVersionCommand implements Command {

    private static final long serialVersionUID = 2277080393930008224L;

    /**
     * process id of process to update. If set, this special process is updated
     */
    private long processId = -1;
    
    /**
     * if set, all running processes of the process with this name are updated
     */
    private String processName;

    /**
     * new version of process, if <=0, the latest process definition is used
     */
    private int newVersion = -1;

    private static final Log log = LogFactory.getLog(JbpmSchema.class);

    private transient JbpmContext jbpmContext = null;

    /**
     * the map configures for every node-name in the old process definition (as
     * key) which node-name to use in the new process definition.
     * 
     * if a node is not mentioned in this Map, old node name = new node name is
     * applied
     */
    private Map nameMapping = new HashMap();
    
    private transient ProcessDefinition newDef;

    public ChangeProcessInstanceVersionCommand() {
    }

    public ChangeProcessInstanceVersionCommand(long processId, int newVersion) {
        this.processId = processId;
        this.newVersion = newVersion;
    }
    
    private ProcessDefinition getNewDef(String processName) {
        if(newDef==null) {
            if (newVersion<=0)
                newDef = jbpmContext.getGraphSession().findLatestProcessDefinition(processName);
            else
                newDef = jbpmContext.getGraphSession().findProcessDefinition(processName, newVersion);            
        }
        return newDef;
    }

    /**
     * @return always null
     * @see org.jbpm.command.Command#execute(org.jbpm.JbpmContext)
     */
    public Object execute(JbpmContext jbpmContext) throws Exception {
        this.jbpmContext = jbpmContext;
        if (processId>-1) {
	        ProcessInstance pi = jbpmContext.getGraphSession().loadProcessInstance(processId);
	        changeProcessVersion(pi);
        }
        if (processName!=null && processName.length()>0) {
        	changeAllProcessInstances(processName);
        }
        return null;
    }

	private void changeProcessVersion(ProcessInstance pi) {
		changeTokenVersion(jbpmContext, pi.getRootToken());

        ProcessDefinition oldDef = pi.getProcessDefinition();
        ProcessDefinition newDef = getNewDef(oldDef.getName());

        log.debug("changes process id " + pi.getId() + " from version " + pi.getProcessDefinition().getVersion() + " to new version " + newDef.getVersion());

        pi.setProcessDefinition(newDef);

        log.debug("process id " + pi.getId() + " changed to version " + pi.getProcessDefinition().getVersion());
	}
    
    private void changeAllProcessInstances(String processName) throws Exception {
    	log.debug("changing version all processes '" + processName + "'");

    	GetProcessInstancesCommand cmd = new GetProcessInstancesCommand();
    	cmd.setProcessName(processName);
    	cmd.setOnlyRunning(true);
    	
    	List instances = (List)cmd.execute(jbpmContext);
    	for (Iterator iter = instances.iterator(); iter.hasNext();) {
    		ProcessInstance pi = (ProcessInstance) iter.next();
    		changeProcessVersion(pi);
    	}
    }

    private void changeTokenVersion(JbpmContext jbpmContext, Token token) {
        Node oldNode = token.getNode();

        ProcessDefinition oldDef = token.getProcessInstance().getProcessDefinition();
        ProcessDefinition newDef = getNewDef(oldDef.getName());
        
        Node newNode = newDef.findNode(getNewNodeName(oldNode));

        if (newNode == null) {
            throw new JbpmException("node with name '" + getNewNodeName(oldNode) + "' not found in new process definition");
        }

        log.debug("change token id " + token.getId() + " from version " + oldDef.getVersion() + " to new version " + newDef.getVersion());

        token.setNode(newNode);

        // TODO: Change timers too!

        // change tasks
        Iterator iter = getTasksForToken(token).iterator();
        while (iter.hasNext()) {
            TaskInstance ti = (TaskInstance) iter.next();

            Task oldTask = ti.getTask();
            // find new task
            Query q = jbpmContext.getSession().getNamedQuery("TaskMgmtSession.findTaskForNode");
            q.setString("taskName", oldTask.getName());
            q.setLong("taskNodeId", newNode.getId());
            // TODO: q.setLong("processDefinitionId", newDef.getId());

            Task newTask = (Task) q.uniqueResult();

            if (newTask == null) {
                throw new JbpmException("node '" + newNode.getName() + "' has no Task configured! Check the new process definition");
            }

            ti.setTask(newTask);
            log.debug("change dependent task-instance with id " + oldTask.getId());
        }

        // change childs recursive
        Iterator childIter = token.getChildren().values().iterator();
        while (childIter.hasNext()) {
            changeTokenVersion(jbpmContext, (Token) childIter.next());
        }
    }

    /**
     * @param oldNode
     * @return the name of the new node (given in the map or return default
     *         value, which is the old node name)
     */
    private String getNewNodeName(Node oldNode) {
        String oldName = oldNode.getFullyQualifiedName();
        if (nameMapping.containsKey(oldName)) {
            return (String) nameMapping.get(oldName);
        }
        // return new node name = old node name as default
        return oldName;
    }

    /**
     * We may still have open tasks, even though their parent tokens have been
     * ended. So we'll simply get all tasks from this process instance and
     * cancel them if they are still active.
     * 
     */
    private List getTasksForToken(Token token) {
        Query query = jbpmContext.getSession().getNamedQuery("TaskMgmtSession.findTaskInstancesByTokenId");
        query.setLong("tokenId", token.getId());
        return query.list();

    }

    public Map getNameMapping() {
        return nameMapping;
    }

    public void setNameMapping(Map nameMapping) {
        if (nameMapping==null)
            this.nameMapping = new HashMap();
        else
            this.nameMapping = nameMapping;
    }

    public int getNewVersion() {
        return newVersion;
    }

    public void setNewVersion(int newVersion) {
        this.newVersion = newVersion;
    }

    public long getProcessId() {
        return processId;
    }

    public void setProcessId(long processId) {
        this.processId = processId;
    }

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

}
