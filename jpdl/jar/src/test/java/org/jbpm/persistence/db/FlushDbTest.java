package org.jbpm.persistence.db;

import java.sql.Connection;
import java.sql.DriverManager;

import junit.framework.TestCase;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.svc.Services;

public class FlushDbTest extends TestCase {

  static JbpmConfiguration jbpmConfiguration = createJbpmConfiguration();

  private static JbpmConfiguration createJbpmConfiguration() {
    StringBuffer configurationText = new StringBuffer(
        "<jbpm-configuration>" +
        "  <jbpm-context> " +
        "    <service name='persistence'>" + 
        "      <factory> " +
        "        <bean class='org.jbpm.persistence.db.DbPersistenceServiceFactory'>" + 
        "          <field name='isTransactionEnabled'><boolean value='false'/></field> " +
        "        </bean> " +
        "      </factory> " +
        "    </service>" +
        "    <service name='tx' factory='org.jbpm.tx.TxServiceFactory' /> " +
        "    <service name='message' factory='org.jbpm.msg.db.DbMessageServiceFactory' />" + 
        "    <service name='scheduler' factory='org.jbpm.scheduler.db.DbSchedulerServiceFactory' />" + 
        "    <service name='logging' factory='org.jbpm.logging.db.DbLoggingServiceFactory' /> " +
        "    <service name='authentication' factory='org.jbpm.security.authentication.DefaultAuthenticationServiceFactory' />" +
        "  </jbpm-context> ");

    // make custom jbpm configuration aware of hibernate.properties
    if (FlushDbTest.class.getClassLoader().getResource("hibernate.properties") != null)
      configurationText.append("  <string name='resource.hibernate.properties' value='hibernate.properties' />");

    configurationText.append("</jbpm-configuration>");
    return JbpmConfiguration.parseXmlString(configurationText.toString());
  }

  // static DataSource dataSource = new Jdbc.MockDataSource();

  public void testProcessDeployment() throws Exception {
    jbpmConfiguration.createSchema();
    
    /* getConfiguration() relies on JbpmConfiguration.Configs, which accesses either the current context or
     * the default jbpm configuration resource, but has no access to a custom jbpm configuration.
     * Because createSchema() accesses the hibernate configuration under an open context, calling getConfiguration() 
     * afterwards results in obtaining a hibernate configuration aware of the custom jbpm configuration */
    DbPersistenceServiceFactory dbPersistenceServiceFactory = (DbPersistenceServiceFactory) jbpmConfiguration.getServiceFactory(Services.SERVICENAME_PERSISTENCE);
    Configuration configuration = dbPersistenceServiceFactory.getConfiguration();
    SessionFactory sessionFactory = dbPersistenceServiceFactory.getSessionFactory();
    
    try {
      String dbDriverClass = configuration.getProperty("hibernate.connection.driver_class");
      String dbUrl = configuration.getProperty("hibernate.connection.url");
      String dbUserName = configuration.getProperty("hibernate.connection.username");
      String dbPassword = configuration.getProperty("hibernate.connection.password");
      
      Class.forName(dbDriverClass);
      Connection connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword); 
      connection.setAutoCommit(false);
      JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
      jbpmContext.setConnection(connection);
      ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
        "<process-definition name='hello' />"
      );
      jbpmContext.deployProcessDefinition(processDefinition);
      jbpmContext.close();
      connection.commit();
      connection.close();

      connection = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
      connection.setAutoCommit(false);
      jbpmContext = jbpmConfiguration.createJbpmContext();
      jbpmContext.setConnection(connection);
      jbpmContext.newProcessInstanceForUpdate("hello");
      jbpmContext.close();
      connection.commit();
      connection.close();
      
    } finally {
      jbpmConfiguration.dropSchema();
      sessionFactory.close();
    }
  }
}