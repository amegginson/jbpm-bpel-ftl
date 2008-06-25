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
package org.jbpm.scheduler.ejbtimer;

import javax.naming.InitialContext;

import org.apache.cactus.ServletTestCase;
import org.jbpm.JbpmContext;
import org.jbpm.command.Command;
import org.jbpm.command.DeployProcessCommand;
import org.jbpm.command.StartProcessInstanceCommand;
import org.jbpm.ejb.LocalCommandService;
import org.jbpm.ejb.LocalCommandServiceHome;
import org.jbpm.enterprise.test.ActionRecorder;
import org.jbpm.enterprise.test.GetCurrentTime;
import org.jbpm.enterprise.test.Recorder;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;

public class EjbSchedulerTest extends ServletTestCase {
  
  private LocalCommandService commandService;

  private static LocalCommandServiceHome commandServiceHome;

  protected void setUp() throws Exception {
    if (commandServiceHome == null) {
      InitialContext initialContext = new InitialContext();
      commandServiceHome = (LocalCommandServiceHome) initialContext.lookup("java:comp/env/ejb/LocalCommandServiceBean");
    }
    commandService = commandServiceHome.create();
  }

  protected void tearDown() throws Exception {
    commandService = null;
  }

  public void testScheduleFuture() throws Exception {
    Recorder.resetCollections();
    deployProcess(
        "<process-definition name='timer included'>" +
        "  <start-state>" +
        "    <transition to='a' />" +
        "  </start-state>" +
        "  <state name='a'>" +
        "    <timer duedate='1 second'>" +
        "      <action class='" + ActionRecorder.class.getName() + "' />" +
        "    </timer>" +
        "  </state>" +
        "</process-definition>");
    launchProcess("timer included");
    assertEquals(0, Recorder.executions);
    Thread.sleep(500);
    assertEquals(0, Recorder.executions);
    Thread.sleep(1000);
    assertEquals(1, Recorder.executions);
  }

  public void testSchedulePast() throws Exception {
    Recorder.resetCollections();
    deployProcess(
        "<process-definition name='timer included'>" +
        "  <start-state>" +
        "    <transition to='a'>" +
        "      <action class='" + GetCurrentTime.class.getName() + "'>" +
        "        <variable>now</variable>" +
        "      </action>" +
        "    </transition>" +
        "  </start-state>" +
        "  <state name='a'>" +
        "    <timer duedate='#{now} - 1 second'>" +
        "      <action class='" + ActionRecorder.class.getName() + "' />" +
        "    </timer>" +
        "  </state>" +
        "</process-definition>");
    launchProcess("timer included");
    assertEquals(0, Recorder.executions);
    Thread.sleep(500);
    assertEquals(1, Recorder.executions);
  }

  public void testScheduleRepeat() throws Exception {
    Recorder.resetCollections();
    deployProcess(
        "<process-definition name='repeat included'>" +
        "  <start-state>" +
        "    <transition to='a' />" +
        "  </start-state>" +
        "  <state name='a'>" +
        "    <timer duedate='1 second' repeat='2 seconds'>" +
        "      <action class='" + ActionRecorder.class.getName() + "' />" +
        "    </timer>" +
        "  </state>" +
        "</process-definition>");
    long tokenId = launchProcess("repeat included").getRootToken().getId();
    assertEquals(0, Recorder.executions);
    Thread.sleep(500);
    assertEquals(0, Recorder.executions);
    Thread.sleep(1000);
    assertEquals(1, Recorder.executions);
    Thread.sleep(1000);
    assertEquals(1, Recorder.executions);
    Thread.sleep(1000);
    assertEquals(2, Recorder.executions);
    cancelTimer("a", tokenId);
  }

  public void testCancel() throws Exception {
    Recorder.resetCollections();
    deployProcess(
        "<process-definition name='repeat included'>" +
        "  <start-state>" +
        "    <transition to='a' />" +
        "  </start-state>" +
        "  <state name='a'>" +
        "    <timer duedate='1 second' repeat='1 second'>" +
        "      <action class='" + ActionRecorder.class.getName() + "' />" +
        "    </timer>" +
        "  </state>" +
        "</process-definition>");
    long tokenId = launchProcess("repeat included").getRootToken().getId();
    assertEquals(0, Recorder.executions);
    Thread.sleep(500);
    assertEquals(0, Recorder.executions);
    Thread.sleep(1000);
    assertEquals(1, Recorder.executions);
    cancelTimer("a", tokenId);
    Thread.sleep(1000);
    assertEquals(1, Recorder.executions);
    Thread.sleep(1000);
    assertEquals(1, Recorder.executions);
  }

  public void testScheduleMultiple() throws Exception {
    Recorder.resetCollections();
    deployProcess(
        "<process-definition name='timers included'>" +
        "  <start-state>" +
        "    <transition to='a' />" +
        "  </start-state>" +
        "  <state name='a'>" +
        "    <timer duedate='500 milliseconds' transition='next'>" +
        "      <action class='" + ActionRecorder.class.getName() + "' />" +
        "    </timer>" +
        "    <transition name='next' to='b' />" +
        "  </state>" +
        "  <state name='b'>" +
        "    <timer duedate='500 milliseconds' transition='next'>" +
        "      <action class='" + ActionRecorder.class.getName() + "' />" +
        "    </timer>" +
        "    <transition name='next' to='c' />" +
        "  </state>" +
        "  <state name='c'>" +
        "    <timer duedate='500 milliseconds' transition='next'>" +
        "      <action class='" + ActionRecorder.class.getName() + "' />" +
        "    </timer>" +
        "    <transition name='next' to='d' />" +
        "  </state>" +
        "  <state name='d'>" +
        "    <timer duedate='500 milliseconds' transition='next'>" +
        "      <action class='" + ActionRecorder.class.getName() + "' />" +
        "    </timer>" +
        "    <transition name='next' to='e' />" +
        "  </state>" +
        "  <state name='e'>" +
        "    <timer duedate='500 milliseconds'>" +
        "      <action class='" + ActionRecorder.class.getName() + "' />" +
        "    </timer>" +
        "  </state>" +
        "</process-definition>");
    launchProcess("timers included");
    assertEquals(0, Recorder.executions);
    Thread.sleep(250);
    assertEquals(0, Recorder.executions);
    for (int i = 1; i <= 5; i++) {
      Thread.sleep(500);
      assertEquals(i, Recorder.executions);
    }
  }

  public void testScheduleConcurrent() throws InterruptedException {
    Recorder.resetCollections();
    deployProcess(
        "<process-definition name='timers included'>" +
        "  <start-state>" +
        "    <transition to='f' />" +
        "  </start-state>" +
        "  <fork name='f'>" +
        "    <transition name='a' to='a' />" +
        "    <transition name='b' to='b' />" +
        "    <transition name='c' to='c' />" +
        "    <transition name='d' to='d' />" +
        "    <transition name='e' to='e' />" +
        "  </fork>" +
        "  <state name='a'>" +
        "    <timer duedate='1 second'>" +
        "      <action class='" + ActionRecorder.class.getName() + "' />" +
        "    </timer>" +
        "  </state>" +
        "  <state name='b'>" +
        "    <timer duedate='1 second'>" +
        "      <action class='" + ActionRecorder.class.getName() + "' />" +
        "    </timer>" +
        "  </state>" +
        "  <state name='c'>" +
        "    <timer duedate='1 second'>" +
        "      <action class='" + ActionRecorder.class.getName() + "' />" +
        "    </timer>" +
        "  </state>" +
        "  <state name='d'>" +
        "    <timer duedate='1 second'>" +
        "      <action class='" + ActionRecorder.class.getName() + "' />" +
        "    </timer>" +
        "  </state>" +
        "  <state name='e'>" +
        "    <timer duedate='1 second'>" +
        "      <action class='" + ActionRecorder.class.getName() + "' />" +
        "    </timer>" +
        "  </state>" +
        "</process-definition>");
    launchProcess("timers included");
    assertEquals(0, Recorder.executions);
    Thread.sleep(500);
    assertEquals(0, Recorder.executions);
    Thread.sleep(1000);
    assertEquals(5, Recorder.executions);
  }

  protected ProcessDefinition deployProcess(String xml) {
    return (ProcessDefinition) commandService.execute(new DeployProcessCommand(xml));
  }
  
  protected ProcessInstance launchProcess(final String processName) {
    StartProcessInstanceCommand command = new StartProcessInstanceCommand();
    command.setProcessName(processName);
    return (ProcessInstance) commandService.execute(command);
  }

  protected void cancelTimer(String timerName, long tokenId) {
    commandService.execute(new CancelTimerCommand(timerName, tokenId));
  }

  protected static class CancelTimerCommand implements Command {
    final String timerName;
    final long tokenId;
    private static final long serialVersionUID = 1L;
    public CancelTimerCommand(String timerName, long tokenId) {
      this.timerName = timerName;
      this.tokenId = tokenId;
    }
    public Object execute(JbpmContext jbpmContext) throws Exception {
      Token token = jbpmContext.loadToken(tokenId);
      jbpmContext.getJobSession().cancelTimersByName(timerName, token);
      return null;
    }
  }
}
