package org.jbpm.assignment;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import org.drools.PackageIntegrationException;
import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.RuleIntegrationException;
import org.drools.StatefulSession;
import org.drools.WorkingMemory;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;
import org.drools.rule.InvalidPatternException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.exe.*;
import org.jbpm.taskmgmt.def.*;
import org.jbpm.taskmgmt.exe.Assignable;
import org.jbpm.identity.Entity;

public class RulesAssignmentHandler implements AssignmentHandler {

  private static final long serialVersionUID = 1L;

	public String group;
	public static String ruleFile;
	public List objectNames;


  public void assign(Assignable assignable, ExecutionContext executionContext) throws Exception {
      // load up the rulebase
      RuleBase ruleBase = readRule(ruleFile);
  	  StatefulSession workingMemory = ruleBase.newStatefulSession();
  	  
      // load the data
      Session s = executionContext.getJbpmContext().getSession();
      System.out.println("************** Session is :" + s.toString());

      assertObjects(getUsers(s), workingMemory);
      assertObjects(getGroupByName(s, group), workingMemory);
      assertObjects(getMemberships(s), workingMemory);
     
      Object object = null;
      System.out.println(objectNames.toString());
      Iterator iter = objectNames.iterator();
      String objectName = "";
      ContextInstance ci = executionContext.getContextInstance();
      while ( iter.hasNext() ) {
      	objectName = (String) iter.next();
        	object = ci.getVariable(objectName);
        	
      	System.out.println("object name is: " + objectName);
		// assert the object into the rules engine
      	workingMemory.insert( object );
      }

      // assert the assignable so that it may be used to set results
      System.out.println("assignable is: " + assignable);
      
      workingMemory.insert(assignable);
      System.out.println("fire all rules: " );
      workingMemory.fireAllRules();  
 	} 

  
 
  /**
	 * Please note that this is the "low level" rule assembly API.
	 */
  private static RuleBase readRule(String ruleFileName) throws IOException, 
  	DroolsParserException, 
  	RuleIntegrationException, 
  	PackageIntegrationException, 
  	InvalidPatternException,
  	Exception {
 
      PackageBuilder builder = new PackageBuilder();
      builder.addPackageFromDrl( new InputStreamReader( RulesAssignmentHandler.class.getResourceAsStream( ruleFileName ) ) );

      RuleBase ruleBase = RuleBaseFactory.newRuleBase();
      ruleBase.addPackage( builder.getPackage() );
      return ruleBase;
  }
  

  
  private List getUsers(Session session) {
	    Query query = session.createQuery(
	      "select u " +
	      "from org.jbpm.identity.User as u"
	    );
	    return query.list();
	  }

  
  private List getGroupByName(Session session, String groupName) {
	    Query query = session.createQuery(
	      "select g " +
	      "from org.jbpm.identity.Group as g " +
	      "where g.name = :groupName"
	    );
	    System.out.println("groupName is: " + groupName);
	    query.setString("groupName", groupName);
	    return query.list();
  }

  
  private List getMemberships(Session session) {
	    Query query = session.createQuery(
	      "select m " +
	      "from org.jbpm.identity.Membership as m"
	    );
	    return query.list();
	  }
  
  
  private void assertObjects(List objectList, WorkingMemory workingMemory) {
      Iterator iter = objectList.iterator();
      Entity entity = null;
      while ( iter.hasNext() ) {
      	entity = (Entity) iter.next();
      	System.out.println("object is: " + entity.getName());
	    workingMemory.insert( entity );
      }
    	
  }
 
}
