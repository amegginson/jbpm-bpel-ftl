/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the JBPM BPEL PUBLIC LICENSE AGREEMENT as
 * published by JBoss Inc.; either version 1.0 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.jbpm.bpel.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.bpel.integration.IntegrationService;
import org.jbpm.svc.Services;

/**
 * Loads and closes the {@linkplain JbpmConfiguration jBPM configuration} when
 * the servlet context is initialized and destroyed, respectively.
 * <h3>Configuration</h3>
 * Servlet context init parameters 
 * <table border="1">
 * <tr>
 * <th>Name</th>
 * <th>Description</th>
 * <th>Default value</th>
 * </tr>
 * <tr>
 * <td>jbpm.configuration.resource</td>
 * <td>name of the resource that defines the jBPM configuration</td>
 * <td>jbpm.cfg.xml</td>
 * </tr>
 * </table>
 * @author Alejandro Guizar
 */
public class JbpmConfigurationLoader implements ServletContextListener {

  public void contextInitialized(ServletContextEvent event) {
    String resource = event.getServletContext().getInitParameter("jbpm.configuration.resource");
    JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance(resource);
    // force services to initialize here
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      Services services = jbpmContext.getServices();
      services.getPersistenceService();
      services.getMessageService();
      services.getSchedulerService();
      services.getService(IntegrationService.SERVICE_NAME);
    }
    finally {
      jbpmContext.close();
    }
  }

  public void contextDestroyed(ServletContextEvent event) {
    String resource = event.getServletContext().getInitParameter("jbpm.configuration.resource");
    JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance(resource);
    jbpmConfiguration.close();
  }

}
