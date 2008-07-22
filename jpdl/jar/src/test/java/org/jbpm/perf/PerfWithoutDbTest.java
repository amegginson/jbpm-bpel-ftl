package org.jbpm.perf;

import java.util.Collection;

import junit.framework.TestCase;

import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.taskmgmt.exe.TaskInstance;

public class PerfWithoutDbTest extends TestCase {

  JbpmConfiguration jbpmConfiguration = JbpmConfiguration.parseXmlString(
      "<jbpm-configuration><jbpm-context /></jbpm-configuration>"
  );

  ProcessDefinition processDefinition = parseWebSale(); 

  /* this test tests/shows the memory consumption
  
  public void testLoop() throws Exception {
    PrintWriter out = new PrintWriter(new FileWriter("mem.txt"));
    int i = 1;
    long total = 0;
    while (true) {
      long start = System.currentTimeMillis();
      executeWebSale();
      long stop = System.currentTimeMillis();
      long freeMemory = Runtime.getRuntime().freeMemory();

      long millis = stop-start;

      String avg;
      if (i>20) {
        total += millis;
        avg = Float.toString((float)total/(float)(i-20));
      } else {
        avg = "";
      }
      
      String msg = i+" "+millis+" "+freeMemory+" "+avg;
      System.out.println(msg);
      out.println(msg);
      out.flush();

      System.gc();
      i++;
    }
  }
  */

  /** this test shows the performance */
  public void testLoop() throws Exception {
    
    int nbrOfExecutions = 1000;
    int i = 0;
    long start = System.currentTimeMillis();
    while (true) {
      executeWebSale();
      if (i>nbrOfExecutions) {
        long elapsed = System.currentTimeMillis() - start;
        System.out.println(Float.toString((float)nbrOfExecutions/(float)elapsed));
        i = 0;
        start = System.currentTimeMillis();
      }
      i++;
    }
  }

  void executeWebSale() {
    ProcessInstance processInstance;
    
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      processInstance = new ProcessInstance(processDefinition);
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
      Token token = processInstance.getRootToken();
      Collection taskInstances = processInstance.getTaskMgmtInstance().getUnfinishedTasks(token);
      TaskInstance taskInstance = (TaskInstance) taskInstances.iterator().next();
      taskInstance.end("OK");
    } finally {
      jbpmContext.close();
    }
    
    jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      Token token = processInstance.getRootToken();
      Token shipping = token.getChild("payment");
      Collection taskInstances = processInstance.getTaskMgmtInstance().getUnfinishedTasks(shipping);
      TaskInstance taskInstance = (TaskInstance) taskInstances.iterator().next();
      taskInstance.end();
    } finally {
      jbpmContext.close();
    }
  }

  
  ProcessDefinition parseWebSale() {
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      ProcessDefinition processDefinition = 
        ProcessDefinition.parseXmlResource("org/jbpm/perf/memleaktest.processdefinition.xml");
      
      return processDefinition;
    } finally {
      jbpmContext.close();
    }
  }
}
