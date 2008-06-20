package org.jbpm.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.jbpm.JbpmConfiguration;

public class CloseJbpmConfigurationServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  String configurationName;
  
  public void init() throws ServletException {
    configurationName = getInitParameter("jbpm.configuration.resource", null);
  }
  
  public void destroy() {
    JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance(configurationName);
    jbpmConfiguration.close();
  }

  String getInitParameter(String name, String defaultValue) {
    String value = getInitParameter(name);
    if (value!=null) {
      return value;
    }
    return defaultValue;
  }
}
