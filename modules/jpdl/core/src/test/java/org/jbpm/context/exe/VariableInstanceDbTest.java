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
package org.jbpm.context.exe;

import java.io.*;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmException;
import org.jbpm.context.def.*;
import org.jbpm.db.*;
import org.jbpm.graph.def.*;
import org.jbpm.graph.exe.*;
import org.jbpm.util.DateDbTestUtil;

public class VariableInstanceDbTest extends AbstractDbTestCase {

	ProcessInstance processInstance;
	ContextInstance contextInstance;

	public void setUp() throws Exception {
		super.setUp();

		ProcessDefinition processDefinition = new ProcessDefinition();
		processDefinition.addDefinition(new ContextDefinition());
		graphSession.saveProcessDefinition(processDefinition);

		processInstance = new ProcessInstance(processDefinition);
		contextInstance = processInstance.getContextInstance();
	}

	public void testVariableInstanceString() {
		contextInstance.setVariable("comment",
				"it's not the size that matters, it's how you use it.");

		processInstance = saveAndReload(processInstance);
		contextInstance = processInstance.getContextInstance();

		assertEquals("it's not the size that matters, it's how you use it.",
				contextInstance.getVariable("comment"));
	}

	public void testVariableInstanceLong() {
		contextInstance.setVariable("new salary", new Long(500000));

		processInstance = saveAndReload(processInstance);
		contextInstance = processInstance.getContextInstance();

		assertEquals(new Long(500000), contextInstance.getVariable("new salary"));
	}

	public void testVariableInstanceByteArray() {
		String text = "oh, what a wonderfull world";
		for(int i = 0; i < 10; i++)
			text += text;
		byte[] bytes = text.getBytes();
		assertEquals(text, new String(bytes));
		contextInstance.setVariable("a lot of bytes", bytes);

		processInstance = saveAndReload(processInstance);

		contextInstance = processInstance.getContextInstance();
		bytes = (byte[]) contextInstance.getVariable("a lot of bytes");
		assertEquals(text, new String(bytes));
	}

	public void testString() {
		contextInstance.setVariable("a", new String("3"));
		processInstance = saveAndReload(processInstance);
		contextInstance = processInstance.getContextInstance();
		assertEquals("3", contextInstance.getVariable("a"));
	}

	public void testBoolean() {
		contextInstance.setVariable("a", Boolean.TRUE);
		processInstance = saveAndReload(processInstance);
		contextInstance = processInstance.getContextInstance();
		assertEquals(Boolean.TRUE, contextInstance.getVariable("a"));
	}

	public void testCharacter() {
		contextInstance.setVariable("a", new Character('c'));
		processInstance = saveAndReload(processInstance);
		contextInstance = processInstance.getContextInstance();
		assertEquals(new Character('c'), contextInstance.getVariable("a"));
	}

	public void testFloat() {
		contextInstance.setVariable("a", new Float(3.3));
		processInstance = saveAndReload(processInstance);
		contextInstance = processInstance.getContextInstance();
		assertEquals(new Float(3.3), contextInstance.getVariable("a"));
	}

	public void testDouble() {
		contextInstance.setVariable("a", new Double(3.3));
		processInstance = saveAndReload(processInstance);
		contextInstance = processInstance.getContextInstance();
		assertEquals(new Double(3.3), contextInstance.getVariable("a"));
	}

	public static class MySerializableClass implements Serializable {
		private static final long serialVersionUID = 1L;
		int member;

		MySerializableClass(int member) {
			this.member = member;
		}

		public boolean equals(Object o) {
			if(!(o instanceof MySerializableClass))
				return false;
			return(member == ((MySerializableClass) o).member);
		}
	}

	public void testCustomTypeSerializable() {
		contextInstance.setVariable("a", new MySerializableClass(4));
		processInstance = saveAndReload(processInstance);
		contextInstance = processInstance.getContextInstance();
		assertEquals(new MySerializableClass(4), contextInstance.getVariable("a"));
	}

	public void testLong() {
		contextInstance.setVariable("a", new Long(3));
		processInstance = saveAndReload(processInstance);
		contextInstance = processInstance.getContextInstance();
		assertEquals(new Long(3), contextInstance.getVariable("a"));
	}

	public void testByte() {
		contextInstance.setVariable("a", new Byte("3"));
		processInstance = saveAndReload(processInstance);
		contextInstance = processInstance.getContextInstance();
		assertEquals(new Byte("3"), contextInstance.getVariable("a"));
	}

	public void testShort() {
		contextInstance.setVariable("a", new Short("3"));
		processInstance = saveAndReload(processInstance);
		contextInstance = processInstance.getContextInstance();
		assertEquals(new Short("3"), contextInstance.getVariable("a"));
	}

	public void testInteger() {
		contextInstance.setVariable("a", new Integer(3));
		processInstance = saveAndReload(processInstance);
		contextInstance = processInstance.getContextInstance();
		assertEquals(new Integer(3), contextInstance.getVariable("a"));
	}

	public void testDate() {
		final Date now = Calendar.getInstance().getTime();
		contextInstance.setVariable("a", now);
		processInstance = saveAndReload(processInstance);
		contextInstance = processInstance.getContextInstance();
		final Date result = (Date) contextInstance.getVariable("a");
		// assertEquals(expected, result);
		log.info("Now: " + now + ", " + now.getTime() + ", "
				+ DateDbTestUtil.getInstance().convertDateToSeconds(now));
		log.info("Now: " + result + ", " + result.getTime() + ", "
				+ DateDbTestUtil.getInstance().convertDateToSeconds(result));
		assertEquals(DateDbTestUtil.getInstance().convertDateToSeconds(now),
				DateDbTestUtil.getInstance().convertDateToSeconds(result));
		assertEquals(DateDbTestUtil.getInstance().convertDateToSeconds(now),
				DateDbTestUtil.getInstance().convertDateToSeconds(result));

	}

	public void testNullUpdate() {
		contextInstance.setVariable("a", "blablabla");
		processInstance = saveAndReload(processInstance);
		contextInstance = processInstance.getContextInstance();
		contextInstance.setVariable("a", null);
		processInstance = saveAndReload(processInstance);
		contextInstance = processInstance.getContextInstance();
		assertNull(contextInstance.getVariable("a"));
	}

	public void testChangeTypeWithDeleteIsAllowed() {
		contextInstance.setVariable("a", new String("3"));
		processInstance = saveAndReload(processInstance);
		contextInstance = processInstance.getContextInstance();
		contextInstance.deleteVariable("a");
		processInstance = saveAndReload(processInstance);
		contextInstance = processInstance.getContextInstance();
		contextInstance.setVariable("a", new Integer(3));
	}

	public void testSerializableCollection() {
		List l = new ArrayList();
		l.add("one");
		l.add("two");
		l.add("three");
		contextInstance.setVariable("l", l);
		processInstance = saveAndReload(processInstance);
		contextInstance = processInstance.getContextInstance();
		assertEquals(l, contextInstance.getVariable("l"));
	}

	public void testNonStorableType() {
		contextInstance.setVariable("t", new Thread());
		try {
			jbpmContext.save(processInstance);
			fail("expected exception");
		}
		catch(JbpmException e) {
			// OK
			// let's make sure the auto flushing of hibernate doesn't explode
			contextInstance.deleteVariable("t");
		}
	}

	private static Log log = LogFactory.getLog(VariableInstanceDbTest.class);
}
