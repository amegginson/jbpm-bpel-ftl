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
package org.jbpm.db;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.jbpm.AbstractJbpmTestCase;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.job.Job;
import org.jbpm.job.executor.JobExecutor;
import org.jbpm.logging.log.ProcessLog;
import org.jbpm.taskmgmt.exe.TaskInstance;

public abstract class AbstractDbTestCase extends AbstractJbpmTestCase {

	protected static JbpmConfiguration jbpmConfiguration = getDbTestJbpmConfiguration();

	protected JbpmContext jbpmContext = null;
	protected SchemaExport schemaExport = null;

	protected Session session = null;
	protected GraphSession graphSession = null;
	protected TaskMgmtSession taskMgmtSession = null;
	protected ContextSession contextSession = null;
	protected JobSession jobSession = null;
	protected LoggingSession loggingSession = null;

	protected JobExecutor jobExecutor;

	public static JbpmConfiguration getDbTestJbpmConfiguration() {
	  String configurationResource = AbstractDbTestCase.class.getClassLoader().getResource("hibernate.properties") != null
	      ? "org/jbpm/db/jbpm.db.test.cfg.xml" : "org/jbpm/jbpm.test.cfg.xml";
		return JbpmConfiguration.getInstance(configurationResource);
	}

	public void setUp() throws Exception {
		super.setUp();
		createSchema();
		createJbpmContext();
		initializeMembers();

		log.debug("");
		log.debug("### starting " + this.getClass().getCanonicalName() + "."
				+ getName() + " ####################################################");
	}

	public void tearDown() throws Exception {
		log.debug("### " + this.getClass().getCanonicalName() + "." + getName()
				+ " done ####################################################");
		resetMembers();
		closeJbpmContext();
		dropSchema();
		super.tearDown();
	}

	public void beginSessionTransaction() {
		createJbpmContext();
		initializeMembers();
	}

	public void commitAndCloseSession() {
		closeJbpmContext();
		resetMembers();
	}

	protected void newTransaction() {
		try {
			commitAndCloseSession();
			beginSessionTransaction();
		}
		catch(Throwable t) {
			throw new RuntimeException("couldn't commit and start new transaction", t);
		}
	}

	public ProcessInstance saveAndReload(ProcessInstance pi) {
		jbpmContext.save(pi);
		newTransaction();
		return graphSession.loadProcessInstance(pi.getId());
	}

	public TaskInstance saveAndReload(TaskInstance taskInstance) {
		jbpmContext.save(taskInstance);
		newTransaction();
		return (TaskInstance) session.load(TaskInstance.class, new Long(
				taskInstance.getId()));
	}

	public ProcessDefinition saveAndReload(ProcessDefinition pd) {
		graphSession.saveProcessDefinition(pd);
		newTransaction();
		return graphSession.loadProcessDefinition(pd.getId());
	}

	public ProcessLog saveAndReload(ProcessLog processLog) {
		loggingSession.saveProcessLog(processLog);
		newTransaction();
		return loggingSession.loadProcessLog(processLog.getId());
	}

	protected void createSchema() {
		getJbpmConfiguration().createSchema();
	}

	protected JbpmConfiguration getJbpmConfiguration() {
		return jbpmConfiguration;
	}

	protected void dropSchema() {
		getJbpmConfiguration().dropSchema();
	}

	protected void createJbpmContext() {
		jbpmContext = getJbpmConfiguration().createJbpmContext();
	}

	protected void closeJbpmContext() {
		jbpmContext.close();
	}

	protected void startJobExecutor() {
		jobExecutor = getJbpmConfiguration().getJobExecutor();
		jobExecutor.start();
	}

	private void processAllJobs(final long maxWait, int maxJobs) {
		boolean jobsAvailable = true;

		// install a timer that will interrupt if it takes too long
		// if that happens, it will lead to an interrupted exception and the test
		// will fail
		TimerTask interruptTask = new TimerTask() {
			Thread testThread = Thread.currentThread();

			public void run() {
				log
						.debug("test " + getName()
								+ " took too long. going to interrupt...");
				testThread.interrupt();
			}
		};
		Timer timer = new Timer();
		timer.schedule(interruptTask, maxWait);

		try {
			while(jobsAvailable) {
				log
						.debug("going to sleep for 200 millis, waiting for the job executor to process more jobs");
				Thread.sleep(200);
				jobsAvailable = (getNbrOfJobsAvailable() > maxJobs);
			}
			jobExecutor.stopAndJoin();

		}
		catch(InterruptedException e) {
			fail("test execution exceeded treshold of " + maxWait + " milliseconds");
		}
		finally {
			timer.cancel();
		}
	}

	protected int getNbrOfJobsAvailable() {
		if(session != null) {
			return getNbrOfJobsAvailable(session);
		}
		else {
			beginSessionTransaction();
			try {
				return getNbrOfJobsAvailable(session);
			}
			finally {
				commitAndCloseSession();
			}
		}
	}

	private int getNbrOfJobsAvailable(Session session) {
		int nbrOfJobsAvailable = 0;
		Number jobs = (Number) session.createQuery(
				"select count(*) from org.jbpm.job.Job").uniqueResult();
		log.debug("there are '" + jobs + "' jobs currently in the job table");
		if(jobs != null) {
			nbrOfJobsAvailable = jobs.intValue();
		}
		return nbrOfJobsAvailable;
	}

	protected boolean areJobsAvailable() {
		return(getNbrOfJobsAvailable() > 0);
	}

	protected Job getJob() {
		return (Job) session.createQuery("from org.jbpm.job.Job").uniqueResult();
	}

	protected void processJobs(long maxWait) {
		processJobs(maxWait, 0);
	}

	protected void processJobs(long maxWait, int maxJobs) {
		commitAndCloseSession();
		try {
			Thread.sleep(300);
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}
		startJobExecutor();
		try {
			processAllJobs(maxWait, maxJobs);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		finally {
			stopJobExecutor();
			beginSessionTransaction();
		}
	}

	protected void stopJobExecutor() {
		if(jobExecutor != null) {
			try {
				jobExecutor.stopAndJoin();
			}
			catch(InterruptedException e) {
				throw new RuntimeException(
						"waiting for job executor to stop and join got interrupted", e);
			}
		}
	}

	protected void initializeMembers() {
		session = jbpmContext.getSession();
		graphSession = jbpmContext.getGraphSession();
		taskMgmtSession = jbpmContext.getTaskMgmtSession();
		loggingSession = jbpmContext.getLoggingSession();
		jobSession = jbpmContext.getJobSession();
		contextSession = jbpmContext.getContextSession();
	}

	protected void resetMembers() {
		session = null;
		graphSession = null;
		taskMgmtSession = null;
		loggingSession = null;
		jobSession = null;
		contextSession = null;
	}

	private static Log log = LogFactory.getLog(AbstractDbTestCase.class);
}
