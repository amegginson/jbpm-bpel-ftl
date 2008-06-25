package org.jbpm.ejb;

import javax.ejb.EJBLocalObject;

import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;

public interface LocalTimerEntity extends EJBLocalObject {

	public void cancelTimer();

	public void createTimer(org.jbpm.job.Timer timer);

	public void cancelTimersByName(String timerName, Token token);

	public void deleteTimersForProcessInstance(ProcessInstance processInstance);

}