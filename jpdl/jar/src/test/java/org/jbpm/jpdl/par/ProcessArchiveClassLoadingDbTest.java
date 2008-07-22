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
package org.jbpm.jpdl.par;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.jbpm.db.AbstractDbTestCase;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.util.ClassLoaderUtil;
import org.jbpm.util.IoUtil;

public class ProcessArchiveClassLoadingDbTest extends AbstractDbTestCase {
  
  public static boolean isLoadedActionHandlerExecuted = false;

  String getTestClassesDir() {
    return ProcessArchiveDeploymentDbTest.class.getProtectionDomain().getCodeSource().getLocation().getFile();
  }

  public void testProcessClassLoading() throws Exception {
    // first we read the processLoadedActionHandlerClassBytes from the file system 
    InputStream inputStream = ClassLoaderUtil.getStream("org/jbpm/jpdl/par/ProcessLoadedActionHandler.class");
    byte[] processLoadedActionHandlerClassBytes = IoUtil.readBytes(inputStream);
    inputStream.close();
    
    // then, we delete the ProcessLoadedActionHandler from the 
    // test classes dir, thereby removing it from this class's classloader
    String classFileName = getTestClassesDir()+"/org/jbpm/jpdl/par/ProcessLoadedActionHandler.class";
    if (! new File(classFileName).delete()) {
      fail("couldn't delete "+classFileName);
    }
    
    try {
      try {
        // now, we gonna check if the ProcessArchiveDeployerDbTest is in the classpath of 
        // this test
        ProcessArchiveClassLoadingDbTest.class.getClassLoader().loadClass("org.jbpm.jpdl.par.ProcessLoadedActionHandler");
        fail("expected exception");
      } catch (ClassNotFoundException e)  {
        // OK
      }
      
      // next we create a process archive that includes the class file we just 
      // deleted from the file system.
      String fileName = getTestClassesDir()+"/testarchive3.par";
      FileOutputStream fileOutputStream = new FileOutputStream(fileName);
      ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
      addEntry(zipOutputStream, "processdefinition.xml", "org/jbpm/jpdl/par/classloadingprocess.xml");
      addEntry(zipOutputStream, "classes/org/jbpm/jpdl/par/ProcessLoadedActionHandler.class", processLoadedActionHandlerClassBytes);
      zipOutputStream.close();
      
      // and then, the process archive is deployed in the jbpm database
      ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(fileName));
      ProcessDefinition processDefinition = ProcessDefinition.parseParZipInputStream(zipInputStream);
      jbpmContext.deployProcessDefinition(processDefinition);
      
      newTransaction();
      
      List allProcessDefinitions = graphSession.findAllProcessDefinitions();
      assertEquals(1, allProcessDefinitions.size());
      processDefinition = (ProcessDefinition) allProcessDefinitions.get(0);
      ProcessInstance processInstance = new ProcessInstance(processDefinition);
      processInstance.signal();
      
      assertTrue(isLoadedActionHandlerExecuted);

    } finally {
      FileOutputStream fileOutputStream = new FileOutputStream(classFileName);
      fileOutputStream.write(processLoadedActionHandlerClassBytes);
      fileOutputStream.flush();
      fileOutputStream.close();
    }
  }
  
  private static void addEntry(ZipOutputStream zipOutputStream, String entryName, String resource) throws IOException {
    InputStream inputStream = ClassLoaderUtil.getStream(resource);
    byte[] bytes = IoUtil.readBytes(inputStream);
    addEntry(zipOutputStream, entryName, bytes);
  }
  
  private static void addEntry(ZipOutputStream zipOutputStream, String entryName, byte[] content) throws IOException {
    ZipEntry zipEntry = new ZipEntry(entryName);
    zipOutputStream.putNextEntry(zipEntry);
    zipOutputStream.write(content);
  }

}
