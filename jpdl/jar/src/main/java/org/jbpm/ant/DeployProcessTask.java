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
package org.jbpm.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.FileSet;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;

/**
 * ant task for deploying process archives.
 */
public class DeployProcessTask extends MatchingTask {

  String jbpmCfg = null;
  File process = null;
  List fileSets = new ArrayList();

  public void execute() throws BuildException {
    try {
      // get the JbpmConfiguration
      JbpmConfiguration jbpmConfiguration = AntHelper.getJbpmConfiguration(jbpmCfg);
      
      // if attribute process is set, deploy that process file
      if (process!=null) {
        log( "deploying par "+process.getAbsolutePath()+" ..." );
        deploy(process, jbpmConfiguration);
      }
      
      // loop over all files that are specified in the filesets
      Iterator iter = fileSets.iterator();
      while (iter.hasNext()) {
        FileSet fileSet = (FileSet) iter.next();
        DirectoryScanner dirScanner = fileSet.getDirectoryScanner(getProject());
        String[] fileSetFiles = dirScanner.getIncludedFiles();

        for (int i = 0; i < fileSetFiles.length; i++) {
          String fileName = fileSetFiles[i];
          File file = new File(fileName);
          if ( !file.isFile() ) {
            file = new File( dirScanner.getBasedir(), fileName );
          }

          // deploy the file, specified in a fileset element
          log( "deploying process archive "+file+" ..." );
          deploy(file, jbpmConfiguration);
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new BuildException( "couldn't deploy process archives : " + e.getMessage() );
    }
  }

  void deploy(File file, JbpmConfiguration jbpmConfiguration) throws IOException, FileNotFoundException {
    
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));
      ProcessDefinition processDefinition = ProcessDefinition.parseParZipInputStream(zipInputStream);
      jbpmContext.deployProcessDefinition(processDefinition);
      
    } finally {
      jbpmContext.close();
    }
  }
  
  public void addFileset(FileSet fileSet) {
    this.fileSets.add(fileSet);
  }
  public void setJbpmCfg(String jbpmCfg) {
    this.jbpmCfg = jbpmCfg;
  }
  public void setProcess(File process) {
    this.process = process;
  }
}
