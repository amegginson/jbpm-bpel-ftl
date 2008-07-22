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
package org.jbpm.logging.log;

import org.jbpm.util.DateDbTestUtil;

import java.util.Date;
import java.util.Iterator;

import org.jbpm.db.AbstractDbTestCase;
import org.jbpm.graph.exe.Token;

public class ProcessLogDbTest extends AbstractDbTestCase {

	public void testMessageLogMessage() {
		MessageLog messageLog = new MessageLog("piece of cake");
		messageLog = (MessageLog) saveAndReload(messageLog);
		assertEquals("piece of cake", messageLog.getMessage());
	}

	public void testProcessLogDate() {
		Date now = new Date();
		ProcessLog processLog = new MessageLog();
		processLog.setDate(now);
		processLog = saveAndReload(processLog);
		// assertEquals(now, processLog.getDate());
		// assertEquals(now.getTime(), processLog.getDate().getTime());
		assertEquals(DateDbTestUtil.getInstance().convertDateToSeconds(now),
				DateDbTestUtil.getInstance().convertDateToSeconds(processLog.getDate()));

	}

	public void testProcessLogToken() {
		Token token = new Token();
		session.save(token);
		ProcessLog processLog = new MessageLog();
		processLog.setToken(token);
		processLog = saveAndReload(processLog);
		assertNotNull(processLog.getToken());
	}

	public void testParentChildRelation() {
		CompositeLog compositeLog = new CompositeLog();
		ProcessLog processLog = new MessageLog("one");
		session.save(processLog);
		compositeLog.addChild(processLog);
		processLog = new MessageLog("two");
		session.save(processLog);
		compositeLog.addChild(processLog);
		processLog = new MessageLog("three");
		session.save(processLog);
		compositeLog.addChild(processLog);

		compositeLog = (CompositeLog) saveAndReload(compositeLog);
		assertEquals(3, compositeLog.getChildren().size());

		Iterator iter = compositeLog.getChildren().iterator();
		while(iter.hasNext()) {
			ProcessLog childLog = (ProcessLog) iter.next();
			assertSame(compositeLog, childLog.getParent());
		}
	}

}
