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
package org.jbpm.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jbpm.JbpmException;

public abstract class IoUtil {

  public static final int BUFFERSIZE = 4096;

  public static byte[] readBytes(InputStream inputStream) throws IOException {
    byte[] bytes = null;
    if (inputStream==null) {
      throw new JbpmException("inputStream is null");
    }
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    transfer(inputStream, outputStream);
    bytes = outputStream.toByteArray();
    outputStream.close();
    return bytes;
  }
  
  public static int transfer(InputStream in, OutputStream out) throws IOException {
    int total = 0;
    byte[] buffer = new byte[BUFFERSIZE];
    int bytesRead = in.read( buffer );
    while ( bytesRead != -1 ) {
      out.write( buffer, 0, bytesRead );
      total += bytesRead;
      bytesRead = in.read( buffer );
    }
    return total;
  }
}
