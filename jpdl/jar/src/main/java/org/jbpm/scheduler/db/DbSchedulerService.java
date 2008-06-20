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
package org.jbpm.scheduler.db;

import org.jbpm.JbpmContext;
import org.jbpm.JbpmException;
import org.jbpm.db.JobSession;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.job.Timer;
import org.jbpm.scheduler.SchedulerService;

public class DbSchedulerService implements SchedulerService {

  private static final long serialVersionUID = 1L;
  
  JobSession jobSession = null;
  
  public DbSchedulerService() {
    this.jobSession = JbpmContext.getCurrentJbpmContext().getJobSession();
  }
  
  public void createTimer(Timer timerJob) {
    jobSession.saveJob(timerJob);
  }

  public void deleteTimersByName(String timerName, Token token) {
    jobSession.cancelTimersByName(timerName, token);
  }

  public void deleteTimersByProcessInstance(ProcessInstance processInstance) {
    if (processInstance==null) {
      throw new JbpmException("couldn't cancel timers for null process instance");
    }
    jobSession.deleteJobsForProcessInstance(processInstance);
  }

  public void close() {
  }
}
