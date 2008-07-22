package org.jbpm.command;

import org.jbpm.JbpmContext;

/**
 * The current authorizes actor starts to work on the TaskInstance
 * so the actor is set to the given actor
 * 
 * @author Bernd Ruecker
 */
public class StartWorkOnTaskCommand implements Command {

	private static final long serialVersionUID = 53004484398726736L;

	private long taskInstanceId;
	
	private boolean overwriteSwimlane = false;

	public StartWorkOnTaskCommand(long taskInstanceId, boolean overwriteSwimlane) {
		this.taskInstanceId = taskInstanceId;
		this.overwriteSwimlane = overwriteSwimlane;
	}

	public StartWorkOnTaskCommand() {		
	}
	
	public Object execute(JbpmContext jbpmContext) throws Exception {
		jbpmContext.getTaskInstance(taskInstanceId).start(jbpmContext.getActorId(), overwriteSwimlane);
		return null;
	}

	public boolean isOverwriteSwimlane() {
		return overwriteSwimlane;
	}

	public void setOverwriteSwimlane(boolean overwriteSwimlane) {
		this.overwriteSwimlane = overwriteSwimlane;
	}

	public long getTaskInstanceId() {
		return taskInstanceId;
	}

	public void setTaskInstanceId(long taskInstanceId) {
		this.taskInstanceId = taskInstanceId;
	}

}
