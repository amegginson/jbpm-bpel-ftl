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
package org.jbpm.context.exe.converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jbpm.JbpmException;
import org.jbpm.bytes.ByteArray;
import org.jbpm.context.exe.Converter;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.util.ClassLoaderUtil;
import org.jbpm.util.CustomLoaderObjectInputStream;

public class SerializableToByteArrayConverter implements Converter {

	private static final long serialVersionUID = 1L;

	public boolean supports(Object value) {
		if(value == null)
			return true;
		return Serializable.class.isAssignableFrom(value.getClass());
	}

	public Object convert(Object o) {
		byte[] bytes = null;
		try {
			ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
			ObjectOutputStream objectStream = new ObjectOutputStream(memoryStream);
			objectStream.writeObject(o);
			objectStream.flush();
			bytes = memoryStream.toByteArray();
		}
		catch(IOException e) {
			throw new JbpmException("couldn't serialize '" + o + "'", e);
		}
		return new ByteArray(bytes);
	}

	public Object revert(Object o) {
		ByteArray byteArray = (ByteArray) o;
		InputStream memoryStream = new ByteArrayInputStream(byteArray.getBytes());
		try {
			ObjectInputStream objectStream = new ObjectInputStream(memoryStream);
			return objectStream.readObject();
		}
		catch(IOException ex) {
			throw new JbpmException("failed to read object", ex);
		}
		catch(ClassNotFoundException ex) {
			throw new JbpmException("serialized object class not found", ex);
		}
	}

	public Object revert(Object o, ProcessDefinition processDefinition) {
		ByteArray byteArray = (ByteArray) o;
		InputStream memoryStream = new ByteArrayInputStream(byteArray.getBytes());
		try {
		  ObjectInputStream objectStream = new CustomLoaderObjectInputStream(memoryStream, ClassLoaderUtil.getProcessClassLoader(processDefinition));
			return objectStream.readObject();
		}
		catch(IOException ex) {
			throw new JbpmException("failed to read object", ex);
		}
		catch(ClassNotFoundException ex) {
			throw new JbpmException("serialized object class not found", ex);
		}
	}
}
