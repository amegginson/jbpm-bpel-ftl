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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jbpm.JbpmConfiguration;

/**
 * @author Alejandro Guizar
 * @version $Revision$ $Date: 2007/10/22 04:47:44 $
 */
public class AdministrationServlet extends HttpServlet {

  private JbpmConfiguration jbpmConfiguration;

  /** Request parameter: administrative operation */
  public static final String PARAM_OPERATION = "operation";

  /** Administrative operation: create schema */
  public static final String OP_CREATE_SCHEMA = "create_schema";
  /** Administrative operation: drop schema */
  public static final String OP_DROP_SCHEMA = "drop_schema";

  private static final long serialVersionUID = 1L;

  public void init() throws ServletException {
    String configResource = getServletContext().getInitParameter(WebConstants.PARAM_CONFIG_RESOURCE);
    jbpmConfiguration = JbpmConfiguration.getInstance(configResource);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String operation = request.getParameter(PARAM_OPERATION);
    if (OP_CREATE_SCHEMA.equals(operation)) {
      jbpmConfiguration.createSchema();
      log("created schema");
    }
    else if (OP_DROP_SCHEMA.equals(operation)) {
      jbpmConfiguration.dropSchema();
      log("dropped schema");
    }
    else {
      throw new ServletException("value '"
          + operation
          + "' is not valid for parameter '"
          + PARAM_OPERATION
          + "'");
    }
    response.sendRedirect("database.jsp");
  }
}
