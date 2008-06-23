package org.jbpm.perf;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.db.AbstractDbTestCase;
import org.jbpm.taskmgmt.exe.TaskInstance;

public class TasklistEagerLoadingTest extends AbstractDbTestCase {

  public void testTasklistEagerLoading() {
    for (int i=0; i<20; i++) {
      TaskInstance taskInstance = new TaskInstance("task "+i);
      taskInstance.setActorId("johndoe");
      session.save(taskInstance);
    }
    newTransaction();
    
    assertEquals(20, jbpmContext.getTaskList("johndoe").size());
  }

  public void testPooledTasklistEagerLoading() {
    for (int i=0; i<20; i++) {
      TaskInstance taskInstance = new TaskInstance("group task "+i);
      taskInstance.setPooledActors(new String[]{"group"+i});
      session.save(taskInstance);
    }
    for (int i=0; i<20; i++) {
      TaskInstance taskInstance = new TaskInstance("task "+i);
      taskInstance.setPooledActors(new String[]{"johndoe", "bachelors", "partyanimals", "wildwomen"});
      session.save(taskInstance);
    }
    newTransaction();
    
    List actorIds = new ArrayList();
    actorIds.add("johndoe");
    assertEquals(20, jbpmContext.getGroupTaskList(actorIds).size());
  }
}
