package org.jboss.bam.action.simulator;

import java.util.Calendar;
import java.util.Random;

import org.jboss.bam.command.DeployProcessCommand;
import org.jboss.bam.command.SimulatorCommand;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.log.Log;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;

@Name("simulator")
@Scope(ScopeType.APPLICATION)
public class Simulator {
	@Logger
	private Log log;
	
	ProcessDefinition procDef1 = ProcessDefinition
			.parseXmlString(
					"<?xml version='1.0' encoding='UTF-8'?>"
					+ "<process-definition xmlns='urn:jbpm.org:jpdl-3.2' name='process-1'>"
					+ "	<start-state name='start'>" 
					+ "		<transition to='state-01' />"
					+ "	</start-state>" 
					+ "	<state name='state-01'>"
					+ "		<transition to='state-02' />" 
					+ "	</state>"			
					+ "	<state name='state-02'>"
					+ "		<transition to='end' />" 
					+ "	</state>"
					+ "	<end-state name='end' />" 
					+ "</process-definition>");
	
	ProcessDefinition procDef2 = ProcessDefinition
			.parseXmlString(
					"<?xml version='1.0' encoding='UTF-8'?>"
					+ "<process-definition xmlns='urn:jbpm.org:jpdl-3.2' name='process-2'>"
					+ "	<start-state name='start'>" 
					+ "		<transition to='state-01' />"
					+ "	</start-state>" 
					+ "	<state name='state-01'>"
					+ "		<transition to='state-02' />" 
					+ "	</state>"			
					+ "	<state name='state-02'>"
					+ "		<transition to='end' />" 
					+ "	</state>"
					+ "	<end-state name='end' />" 
					+ "</process-definition>");
	
	ProcessDefinition procDef3 = ProcessDefinition
			.parseXmlString(
					"<?xml version='1.0' encoding='UTF-8'?>"
					+ "<process-definition xmlns='urn:jbpm.org:jpdl-3.2' name='process-3'>"
					+ "	<start-state name='start'>" 
					+ "		<transition to='state-01' />"
					+ "	</start-state>" 
					+ "	<state name='state-01'>"
					+ "		<transition to='state-02' />" 
					+ "	</state>"			
					+ "	<state name='state-02'>"
					+ "		<transition to='end' />" 
					+ "	</state>"
					+ "	<end-state name='end' />" 
					+ "</process-definition>");
	
	private void deployPocess() {
		JbpmContext ctx = ManagedJbpmContext.instance();
		DeployProcessCommand deployProcessCmd1 = new DeployProcessCommand(procDef1);
		DeployProcessCommand deployProcessCmd2 = new DeployProcessCommand(procDef2);
		DeployProcessCommand deployProcessCmd3 = new DeployProcessCommand(procDef3);
		deployProcessCmd1.execute(ctx);
		deployProcessCmd2.execute(ctx);
		deployProcessCmd3.execute(ctx);
	}
	
	private void simulateData() {
		log.debug("Simulation started at: " + Calendar.getInstance().getTime());
		JbpmContext ctx = ManagedJbpmContext.instance();
		SimulatorCommand cmd1 = new SimulatorCommand(procDef1, new Random().nextInt(10));
		SimulatorCommand cmd2 = new SimulatorCommand(procDef2, new Random().nextInt(10));
		SimulatorCommand cmd3 = new SimulatorCommand(procDef3, new Random().nextInt(10));
		cmd1.execute(ctx);
		cmd2.execute(ctx);
		cmd3.execute(ctx);
		log.debug("Simulation completed on: " + Calendar.getInstance().getTime());
	}
	
	public void simulate() {
		this.deployPocess();
		this.simulateData();
	}
}
