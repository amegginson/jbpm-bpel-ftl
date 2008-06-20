package org.jbpm.command;

import java.util.Date;
import java.util.Iterator;

import org.hibernate.Query;
import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;

/**
 * 
 * @author Bernd Ruecker (bernd.ruecker@camunda.com)
 * 
 */
public class CancelProcessInstanceCommand extends AbstractCancelCommand implements Command {

	private static final long serialVersionUID = 7145293049356621597L;

	private long processId;

	/**
	 * recursivly cancel subprocesses?
	 */
	boolean cancelSubProcesses = false;

	public CancelProcessInstanceCommand() {		
	}

	public CancelProcessInstanceCommand(long processId) {
		this.processId = processId;
	}

	public Object execute(JbpmContext jbpmContext) throws Exception {
		this.jbpmContext = jbpmContext;
		cancelProcess(processId);
		this.jbpmContext = null;
		return null;
	}
	
	protected void cancelProcess(long processIdToCancel) {
		ProcessInstance pi = jbpmContext.getGraphSession().loadProcessInstance(
				processIdToCancel);
	
		log.info("cancel process instance " + pi.getId());
	
		// Record a standardized variable that we can use to determine that this
		// process has been 'cancelled' and not just ended.
		pi.getContextInstance().createVariable("cancelled", new Date());
	
		try {
			// End the process instance and any open tokens
			// TODO: better implementation (also cancel sub processes etc.)
			// see http://intranet.computation.de/bugs/view_bug.php?bug_id=295
	
			cancelToken(pi.getRootToken());
			cancelTokens(pi.getRootToken().getChildren().values());
	
            pi.end();

            if (cancelSubProcesses)
				cancelSubProcesses(processIdToCancel);

			log.info("finished process cancellation");
		} catch (RuntimeException ex) {
			log.error("problems while cancel process", ex);
			throw ex;
		}
	}
	
	/**
	 * cancel all sub processes of the process with the given id calls
	 * "cancelProcess" recursivly until all processes are ended
	 * 
	 * @param processId
	 */
	protected void cancelSubProcesses(long processId) {
		Query q = jbpmContext.getSession().getNamedQuery(
				"GraphSession.findSubProcessInstances");
		q.setLong("instanceId", processId);
		Iterator iter = q.list().iterator();
		while (iter.hasNext()) {
			ProcessInstance pi = (ProcessInstance) iter.next();
			log.info("cancel sub process instance #" + pi.getId());
			cancelProcess(pi.getId());
		}
	}	

	public boolean isCancelSubProcesses() {
		return cancelSubProcesses;
	}

	public void setCancelSubProcesses(boolean cancelSubProcesses) {
		this.cancelSubProcesses = cancelSubProcesses;
	}

	public long getProcessId() {
		return processId;
	}

	public void setProcessId(long processId) {
		this.processId = processId;
	}

}
