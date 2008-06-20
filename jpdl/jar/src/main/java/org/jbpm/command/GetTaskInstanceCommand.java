package org.jbpm.command;

import java.util.Iterator;
import java.util.List;

import org.jbpm.JbpmContext;
import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * This command can retrieve a task instance (for client) with the given task-id
 * or the token-id (then, the first task for the token is searched)
 * 
 * @author Bernd Ruecker (bernd.ruecker@camunda.com)
 */
public class GetTaskInstanceCommand extends AbstractGetObjectBaseCommand {

    private static final long serialVersionUID = -8436697080972165601L;

    private long taskInstanceId;

    /**
     * if given, all tasks for this token are searched and a List of
     * TaskInstances is given
     */
    private long tokenId;

    /**
     * if given, all tasks for this process are searched and a List of
     * TaskInstances is given
     */
    private long processInstanceId;

    /**
     * NOT YET USED! JUST TO DOCUMENT THE IDEA...
     * 
     * result of the Command: a gui element, configured via the TaskController.
     * Can be used to identify a Swing-Class, JSF-Site, ...
     */
    private String configuredGuiElement;

    public GetTaskInstanceCommand() {
    }

    public GetTaskInstanceCommand(long taskInstanceId) {
        this.taskInstanceId = taskInstanceId;
    }

    public GetTaskInstanceCommand(long taskInstanceId, boolean includeVariables, boolean includeLogs) {
        super(includeVariables, includeLogs);
        this.taskInstanceId = taskInstanceId;
    }

    public GetTaskInstanceCommand(long taskInstanceId, String[] variablesToInclude) {
        super(variablesToInclude);
        this.taskInstanceId = taskInstanceId;
    }

    public Object execute(JbpmContext jbpmContext) throws Exception {

        if (taskInstanceId > 0) {
            TaskInstance taskInstance = jbpmContext.getTaskInstance(taskInstanceId);
            if (taskInstance != null) {
                retrieveTaskInstanceDetails(taskInstance);
            }

            return taskInstance;
        }
        else if (tokenId > 0) {
            List result = jbpmContext.getTaskMgmtSession().findTaskInstancesByToken(tokenId);
            for (Iterator iter = result.iterator(); iter.hasNext();) {
                TaskInstance ti = (TaskInstance) iter.next();
                retrieveTaskInstanceDetails(ti);
            }
            return result;
        }
        else if (processInstanceId > 0) {
            List result = jbpmContext.getTaskMgmtSession().findTaskInstancesByProcessInstance(jbpmContext.getProcessInstance(processInstanceId));
            for (Iterator iter = result.iterator(); iter.hasNext();) {
                TaskInstance ti = (TaskInstance) iter.next();
                retrieveTaskInstanceDetails(ti);
            }
            return result;
        }
        else
            return null;
    }

    public long getTaskInstanceId() {
        return taskInstanceId;
    }

    public void setTaskInstanceId(long taskInstanceId) {
        this.taskInstanceId = taskInstanceId;
    }

    public long getTokenId() {
        return tokenId;
    }

    public void setTokenId(long tokenId) {
        this.tokenId = tokenId;
    }

    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

}
