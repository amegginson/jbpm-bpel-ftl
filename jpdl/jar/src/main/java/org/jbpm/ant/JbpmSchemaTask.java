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

import java.net.URL;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.db.JbpmSchema;
import org.jbpm.persistence.db.DbPersistenceServiceFactory;
import org.jbpm.svc.Services;

public class JbpmSchemaTask extends Task {

  String jbpmCfg = null;
  String hibernateCfg = null;
  String hibernateProperties = null;

  boolean quiet = false;
  boolean text = false;
  String output = null;
  String delimiter = null;

  String actions = null;

  public void execute() throws BuildException {
    if (actions==null) {
      // default action is create
      actions = "create";
    }
    
    // we need a configuration
    Configuration configuration = null; 

    // if there is no jbpm nor hibernate configuration specified
    if ( (jbpmCfg==null)
         && (hibernateCfg==null) 
       ) {
      // search for the default jbpm.cfg.xml
      URL defaultJbpmCfgUrl = getClass().getClassLoader().getResource("jbpm.cfg.xml");
      if (defaultJbpmCfgUrl!=null) {
        jbpmCfg = "jbpm.cfg.xml";
      // if still not found, search for the default hibernate.cfg.xml
      } else {
        URL defaultHibernateCfgUrl = getClass().getClassLoader().getResource("hibernate.cfg.xml");
        if (defaultHibernateCfgUrl!=null) {
          hibernateCfg = "hibernate.cfg.xml";
        }
      }
    }
    
    // first see if the jbpm cfg is specified cause that implies a hibernate configuration
    if (jbpmCfg!=null) {
      log("using jbpm configuration "+jbpmCfg);
      JbpmConfiguration jbpmConfiguration = AntHelper.getJbpmConfiguration(jbpmCfg);
      JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
      try {
        DbPersistenceServiceFactory dbPersistenceServiceFactory = (DbPersistenceServiceFactory) jbpmConfiguration.getServiceFactory(Services.SERVICENAME_PERSISTENCE);
        configuration = dbPersistenceServiceFactory.getConfiguration();
      } finally {
        jbpmContext.close();
      }
      
    // if there is no jbpm.cfg.xml specified, check if there is a hibernate.cfg.xml specified
    } else if (hibernateCfg!=null) {
      log("using hibernate configuration "+hibernateCfg);
      configuration = AntHelper.getConfiguration(hibernateCfg, hibernateProperties);

    // no hibernate configuration specified
    } else {
      throw new BuildException("couldn't create schema.  no jbpm nor hibernate configuration specified.");
    }

    JbpmSchema jbpmSchema = new JbpmSchema(configuration);

    SchemaExport schemaExport = new SchemaExport(configuration);
    if (output!=null) schemaExport.setOutputFile(output);
    if (delimiter!=null) schemaExport.setDelimiter(delimiter);

    StringTokenizer tokenizer = new StringTokenizer(actions, ",");
    while (tokenizer.hasMoreTokens()) {
      String action = tokenizer.nextToken();

      if ("drop".equalsIgnoreCase(action)) {
        schemaExport.drop(!quiet, !text);

      } else if ("create".equalsIgnoreCase(action)) {
        schemaExport.create(!quiet, !text);
        
      } else if ("clean".equalsIgnoreCase(action)) {
        jbpmSchema.cleanSchema();
      }
    }
  }
  
  public void setActions(String actions) {
    this.actions = actions;
  }
  public void setJbpmCfg(String jbpmCfg) {
    this.jbpmCfg = jbpmCfg;
  }
  public void setHibernateCfg(String hibernateCfg) {
    this.hibernateCfg = hibernateCfg;
  }
  public void setHibernateProperties(String hibernateProperties) {
    this.hibernateProperties = hibernateProperties;
  }
  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }
  public void setOutput(String output) {
    this.output = output;
  }
  public void setQuiet(boolean quiet) {
    this.quiet = quiet;
  }
  public void setText(boolean text) {
    this.text = text;
  }
}
