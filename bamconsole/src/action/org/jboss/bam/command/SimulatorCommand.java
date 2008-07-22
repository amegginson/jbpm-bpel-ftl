package org.jboss.bam.command;

import java.util.Random;

import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;

public class SimulatorCommand extends AbstractSeamCommand {
	
	private static final long serialVersionUID = 1L;
	
	private ProcessDefinition processDefinition = null;
	
	private int instances = 0;
	
	public SimulatorCommand() {
		super();
	}
	
	public SimulatorCommand(ProcessDefinition processDefinition, int instances) {
		this.setInstance(instances);
		this.setProcessDefinition(processDefinition);
	}
	
	public ProcessDefinition getProcessDefinition() {
		return processDefinition;
	}
	
	public void setProcessDefinition(ProcessDefinition processDefinition) {
		this.processDefinition = processDefinition;
	}
	
	public void setInstance(int instances) {
		this.instances = instances;
	}
	
	public int getInstances() {
		return this.instances;
	}
	
	public Object execute(JbpmContext jbpmContext) {
		for (int i = 0; i < this.getInstances(); i++) {
			ProcessInstance processInstance = new ProcessInstance(this.getProcessDefinition());
			processInstance.signal();
			sleepRandomly();
			processInstance.signal();
			sleepRandomly();
			processInstance.signal();
			sleepRandomly();
			jbpmContext.save(processInstance);
		}
		return null;
	}
	
	private void sleepRandomly() {
		try {
			//Sleep an interval between 0 & 10 seconds between node executions
			Thread.sleep(10 * Math.abs(new Random().nextInt(1000)));		
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
