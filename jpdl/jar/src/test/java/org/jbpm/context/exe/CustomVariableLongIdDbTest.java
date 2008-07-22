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

import org.hibernate.cfg.Configuration;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.context.def.ContextDefinition;
import org.jbpm.db.AbstractDbTestCase;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.persistence.db.DbPersistenceServiceFactory;
import org.jbpm.svc.Services;

public class CustomVariableLongIdDbTest extends AbstractDbTestCase {

  static JbpmConfiguration customLongJbpmConfiguration = createJbpmConfiguration();

  private static JbpmConfiguration createJbpmConfiguration() {
    // make custom jbpm configuration aware of hibernate.properties
    String configurationResource = CustomVariableLongIdDbTest.class.getClassLoader().getResource("hibernate.properties") != null
        ? "org/jbpm/db/jbpm.db.test.cfg.xml" : "org/jbpm/jbpm.test.cfg.xml";
    JbpmConfiguration jbpmConfiguration = JbpmConfiguration.parseResource(configurationResource);

    DbPersistenceServiceFactory dbPersistenceServiceFactory = (DbPersistenceServiceFactory) jbpmConfiguration.getServiceFactory(Services.SERVICENAME_PERSISTENCE);
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      /* getConfiguration() relies on JbpmConfiguration.Configs, which accesses either the current context
       * or the default jbpm configuration resource, but has no access to a custom jbpm configuration.
       * The context is created only to enforce the use of the custom jbpm configuration. */
      Configuration configuration = dbPersistenceServiceFactory.getConfiguration();
      configuration.addClass(CustomLongClass.class);
    }
    finally {
      jbpmContext.close();
    }
    return jbpmConfiguration;
  }

  protected JbpmConfiguration getJbpmConfiguration() {
    return customLongJbpmConfiguration;
  }

  public void testCustomVariableClassWithLongId() {
    // create and save the process definition
    ProcessDefinition processDefinition = new ProcessDefinition();
    processDefinition.addDefinition(new ContextDefinition());
    graphSession.saveProcessDefinition(processDefinition);

    // create the process instance
    ProcessInstance processInstance = new ProcessInstance(processDefinition);
    // set the jbpmSession in the context because its used to determine the
    // jbpm-type for the custom object.
    ContextInstance contextInstance = processInstance.getContextInstance();

    // create the custom object
    CustomLongClass customLongObject = new CustomLongClass("customname");
    contextInstance.setVariable("custom hibernate object", customLongObject);

    processInstance = saveAndReload(processInstance);
    contextInstance = processInstance.getContextInstance();

    // get the custom hibernatable object from the variableInstances
    customLongObject = (CustomLongClass) contextInstance.getVariable("custom hibernate object");
    assertNotNull(customLongObject);
    assertEquals("customname", customLongObject.getName());
  }
}
