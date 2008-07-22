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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbpm.JbpmException;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.logging.log.ProcessLog;

public class LoggingSession {

  JbpmSession jbpmSession;
  Session session;
  
  public LoggingSession(JbpmSession jbpmSession) {
    this.jbpmSession = jbpmSession;
    this.session = jbpmSession.getSession();
  }
  
  public LoggingSession(Session session) {
    this.session = session;
    this.jbpmSession = new JbpmSession(session);
  }

  /**
   * returns a map that maps {@link Token}s to {@link List}s.  The lists contain the ordered
   * logs for the given token.  The lists are retrieved with {@link #findLogsByToken(long)}. 
   */
  public Map findLogsByProcessInstance(long processInstanceId) {
    Map tokenLogs = new HashMap();
    try {
      ProcessInstance processInstance = (ProcessInstance) session.load(ProcessInstance.class, new Long(processInstanceId));
      collectTokenLogs(tokenLogs, processInstance.getRootToken());
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new JbpmException("couldn't get logs for process instance '"+processInstanceId+"'", e);
    } 
    return tokenLogs;
  }

  private void collectTokenLogs(Map tokenLogs, Token token) {
    tokenLogs.put(token, findLogsByToken(token.getId()));
    Map children = token.getChildren();
    if ( (children!=null)
         && (!children.isEmpty()) 
       ) {
      Iterator iter = children.values().iterator();
      while (iter.hasNext()) {
        Token child = (Token) iter.next();
        collectTokenLogs(tokenLogs, child);
      }
    }
  }
  
  /**
   * collects the logs for a given token, ordered by creation time.
   */
  public List findLogsByToken(long tokenId) {
    List result = null;
    try {
      Token token = (Token) session.load(Token.class, new Long(tokenId));
      Query query = session.getNamedQuery("LoggingSession.findLogsByToken");
      query.setEntity("token", token);
      result = query.list();
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new JbpmException("couldn't get logs for token '"+tokenId+"'", e);
    } 
    return result;
  }
  
  /**
   * saves the given process log to the database.
   */
  public void saveProcessLog(ProcessLog processLog) {
    try {
      session.save(processLog);
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new JbpmException("couldn't save process log '"+processLog+"'", e);
    } 
  }
  
  /**
   * load the process log for a given id.
   */
  public ProcessLog loadProcessLog(long processLogId) {
    ProcessLog processLog = null;
    try {
      processLog = (ProcessLog) session.load(ProcessLog.class, new Long(processLogId));
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new JbpmException("couldn't load process log '"+processLogId+"'", e);
    } 
    return processLog;
  }
  
  /**
   * get the process log for a given id.
   */
  public ProcessLog getProcessLog(long processLogId) {
    ProcessLog processLog = null;
    try {
      processLog = (ProcessLog) session.get(ProcessLog.class, new Long(processLogId));
    } catch (Exception e) {
      log.error(e);
      jbpmSession.handleException();
      throw new JbpmException("couldn't get process log '"+processLogId+"'", e);
    } 
    return processLog;
  }
  
  private static final Log log = LogFactory.getLog(LoggingSession.class);
}
