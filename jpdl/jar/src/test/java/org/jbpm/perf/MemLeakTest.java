package org.jbpm.perf;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Collection;

import junit.framework.TestCase;

import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.identity.Entity;
import org.jbpm.identity.hibernate.IdentitySession;
import org.jbpm.identity.xml.IdentityXmlParser;
import org.jbpm.taskmgmt.exe.TaskInstance;

public class MemLeakTest extends TestCase {

  JbpmConfiguration jbpmConfiguration = createJbpmConfiguration();
  
  public void testLoop() throws Exception {
    PrintWriter out = new PrintWriter(new FileWriter("mem.txt"));
    deployWebSale();
    loadIdentities();
    int i = 0;
    while (true) {
      long start = System.currentTimeMillis();
      executeWebSale();
      long stop = System.currentTimeMillis();
      long freeMemory = Runtime.getRuntime().freeMemory();

      long millis = stop-start;
      
      String msg = i+" "+millis+" "+freeMemory;
      System.out.println(msg);
      out.println(msg);
      out.flush();

      cleanDb();
      System.gc();
      i++;
    }
  }

  void executeWebSale() {
    long processInstanceId;
    
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      ProcessInstance processInstance = jbpmContext.newProcessInstanceForUpdate("websale");
      processInstanceId = processInstance.getId();
      TaskInstance taskInstance = processInstance.getTaskMgmtInstance().createStartTaskInstance();
      taskInstance.setVariable("item", "cookies");
      taskInstance.setVariable("quantity", "lots of them");
      taskInstance.setVariable("address", "46 Main St.");
      taskInstance.end();
    } finally {
      jbpmContext.close();
    }

    jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      ProcessInstance processInstance = jbpmContext.loadProcessInstanceForUpdate(processInstanceId);
      Token token = processInstance.getRootToken();
      Collection taskInstances = processInstance.getTaskMgmtInstance().getUnfinishedTasks(token);
      TaskInstance taskInstance = (TaskInstance) taskInstances.iterator().next();
      taskInstance.end("OK");
    } finally {
      jbpmContext.close();
    }
    
    jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      ProcessInstance processInstance = jbpmContext.loadProcessInstanceForUpdate(processInstanceId);
      Token token = processInstance.getRootToken();
      Token shipping = token.getChild("payment");
      Collection taskInstances = processInstance.getTaskMgmtInstance().getUnfinishedTasks(shipping);
      TaskInstance taskInstance = (TaskInstance) taskInstances.iterator().next();
      taskInstance.end();
    } finally {
      jbpmContext.close();
    }
  }

  
  
  JbpmConfiguration createJbpmConfiguration() {
    JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance("org/jbpm/perf/memleaktest.jbpm.cfg.xml");
    try {
      JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
      Connection connection = null;
      try {
        connection = jbpmContext.getConnection();
        executeSql("org/jbpm/perf/memleaktest.jbpm.schema.sql", connection);
      } finally {
        jbpmContext.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return jbpmConfiguration;
  }

  void cleanDb() throws Exception {
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    Connection connection = null;
    try {
      connection = jbpmContext.getConnection();
      executeSql("org/jbpm/perf/memleaktest.clean.db.sql", connection);
    } finally {
      jbpmContext.close();
    }
  }

  private void executeSql(String resource, Connection connection) throws Exception {
    try {
      InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(resource);
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
      String sql = reader.readLine();
      while(sql!=null) {
        try {
          connection.prepareStatement(sql).executeUpdate();
        } catch (Exception e) {
          System.out.println("  --> "+e.toString());
        }
        sql = reader.readLine();
      }
      reader.close();
      inputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  void loadIdentities() {
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      Entity[] entities = IdentityXmlParser.parseEntitiesResource("org/jbpm/perf/memleaktest.identities.xml");
      IdentitySession identitySession = new IdentitySession(jbpmContext.getSession());
      for (int i=0; i<entities.length; i++) {
        identitySession.saveEntity(entities[i]);
      }
    } finally {
      jbpmContext.close();
    }
  }
  
  void deployWebSale() {
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      ProcessDefinition processDefinition = 
        ProcessDefinition.parseXmlResource("org/jbpm/perf/memleaktest.processdefinition.xml");
      jbpmContext.deployProcessDefinition(processDefinition);
    } finally {
      jbpmContext.close();
    }
  }
}
