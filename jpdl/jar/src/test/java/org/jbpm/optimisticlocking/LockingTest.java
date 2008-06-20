package org.jbpm.optimisticlocking;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.db.AbstractDbTestCase;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.Comment;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.persistence.db.DbPersistenceServiceFactory;

public class LockingTest extends TestCase {

  static JbpmConfiguration jbpmConfiguration = AbstractDbTestCase.getDbTestJbpmConfiguration();
  
  static int nbrOfThreads = 5;
  static int nbrOfIterations = 20;

  private void deployProcess() {
    // the process will be executed in 2 separete transactions:
    // Transaction 1 will create the process instance and position 
    // the root token in the start state
    // Transaction 2 will signal the process instance while it is in the
    // start state, and that signal will bring the process to it's end state.
    
    // It's the second transaction for which we'll set up multiple competing threads
    ProcessDefinition processDefinition = ProcessDefinition.parseXmlString(
      "<process-definition name='lockprocess'>" +
      "  <start-state name='start'>" +
      "    <transition to='end'/>" +
      "  </start-state>" +
      "  <end-state name='end' />" +
      "</process-definition>"
    );

    // deploy the process
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      jbpmContext.deployProcessDefinition(processDefinition);
    } finally {
      jbpmContext.close();
    }
  }

  public void testLocking() {
    createSchema();
    deployProcess();
    
    for(int i=0; i<nbrOfIterations; i++) {
      long processInstanceId = launchProcessInstance();
      
      // create a bunch of threads that will all wait on the 
      // semaphore before they will try to signal the same process instance
      Object semaphore = new Object();
      List threads = startThreads(semaphore, processInstanceId);
      
      // release all the threads
      synchronized(semaphore) {
        semaphore.notifyAll();
      }
      
      // wait for all threads to finish
      joinAllThreads(threads);
      
      // check that only 1 of those threads committed
      JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
      try {
        Session session = jbpmContext.getSession();
        Query query = session.createQuery("select c from org.jbpm.graph.exe.Comment as c");
        List results = query.list();
        System.out.println("iteration "+i+": "+results);
        assertEquals(results.toString(), 1, results.size());

        // delete the comment
        session.delete(results.get(0));
        
      } finally {
        jbpmContext.close();
      }

      // check that the process instance has ended
      jbpmContext = jbpmConfiguration.createJbpmContext();
      try {
        ProcessInstance processInstance = jbpmContext.loadProcessInstance(processInstanceId);
        assertTrue(processInstance.hasEnded());
        
      } finally {
        jbpmContext.close();
      }
    }
  }
  
  private long launchProcessInstance() {
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      return jbpmContext.newProcessInstance("lockprocess").getId();
    } finally {
      jbpmContext.close();
    }
  }
  
  private List startThreads(Object semaphore, long processInstanceId) {
    List threads = new ArrayList();
    for (int i=0; i<nbrOfThreads; i++) {
      Thread thread = new LockThread(semaphore, processInstanceId);
      thread.start();
      threads.add(thread);
    }
    
    try {
      // giving the threads the opportunity to start and arrive in the wait
      Thread.sleep(200);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    return threads;
  }
  
  public static class LockThread extends Thread {
    Object semaphore;
    long processInstanceId;
    public LockThread(Object semaphore, long processInstanceId) {
      this.semaphore = semaphore;
      this.processInstanceId = processInstanceId;
    }
    public void run() {
      try {
        // first wait until the all threads are released at once in the 
        // method testLocking
        synchronized(semaphore) {
          semaphore.wait();
        }
        
        // after a thread is released (=notified), it will try to load the process instance,
        // signal it and then commit the transaction
        JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
        try {
          ProcessInstance processInstance = jbpmContext.loadProcessInstance(processInstanceId);
          processInstance.signal();
          jbpmContext.save(processInstance);
          
          // add a comment in the same transaction so that we can see which thread won
          Comment comment = new Comment(getName()+" committed");
          jbpmContext.getSession().save(comment);

        } catch (Exception e) {
          jbpmContext.setRollbackOnly();
        } finally {
          jbpmContext.close();
        }
        
      } catch (InterruptedException e) {
        e.printStackTrace();
        fail("semaphore waiting got interrupted");
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }

  private void joinAllThreads(List threads) {
    Iterator iter = threads.iterator();
    while (iter.hasNext()) {
      Thread thread = (Thread) iter.next();
      try {
        thread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
        fail("join interrupted");
      }
    }
  }

  private void createSchema() {
    // create the jbpm schema
    DbPersistenceServiceFactory persistenceServiceFactory = (DbPersistenceServiceFactory) jbpmConfiguration.getServiceFactory("persistence");
    persistenceServiceFactory.createSchema();
  }
  
}
