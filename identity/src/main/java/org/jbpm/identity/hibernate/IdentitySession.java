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
package org.jbpm.identity.hibernate;

import java.util.*;

import org.apache.commons.logging.*;
import org.hibernate.*;
import org.jbpm.db.JbpmSession;
import org.jbpm.identity.*;
import org.jbpm.identity.assignment.*;
import org.jbpm.identity.security.*;

public class IdentitySession implements IdentityService, ExpressionSession {
  
  Session session = null;
  private Transaction transaction;

  public IdentitySession(Session session) {
    this.session = session;
  }

  public IdentitySession() {
    JbpmSession currentJbpmSession = JbpmSession.getCurrentJbpmSession();
    if ( (currentJbpmSession==null)
         || (currentJbpmSession.getSession()==null)
         || (! currentJbpmSession.getSession().isOpen()) 
       ) {
      throw new RuntimeException("no active JbpmSession to create an identity session");
    }
    session = currentJbpmSession.getSession();
  }

  // IdentityService methods //////////////////////////////////////////////////
  
  public Object verify(String userName, String pwd) {
    Object userId = null;
    Query query = session.createQuery(
        "select user.id " +
        "from org.jbpm.identity.User as user " +
        "where user.name = :userName " +
        "  and user.password = :password"); 
    query.setString("userName", userName);
    query.setString("password", pwd);
    userId = (Long) query.uniqueResult();
    return userId;
  }

  public User getUserById(Object userId) {
    return (User) session.load(User.class, (Long) userId);
  }
  
  // transaction convenience methods //////////////////////////////////////////

  public Session getSession() {
    return session;
  }

  public Transaction getTransaction() {
    return transaction;
  }

  public void beginTransaction() {
    try {
      transaction = session.beginTransaction();
    } catch (HibernateException e) {
      log.error( e );
      throw new RuntimeException( "couldn't begin a transaction", e );
    }
  }

  public void commitTransaction() {
    if ( transaction == null ) {
      throw new RuntimeException("can't commit : no transaction started" );
    }
    try {
      session.flush();
      transaction.commit();
    } catch (HibernateException e) {
      log.error( e );
      throw new RuntimeException( "couldn't commit transaction", e );
    }
    transaction = null;
  }

  public void rollbackTransaction() {
    if ( transaction == null ) {
      throw new RuntimeException("can't rollback : no transaction started" );
    }
    try {
      transaction.rollback();
    } catch (HibernateException e) {
      log.error( e );
      throw new RuntimeException( "couldn't rollback transaction", e );
    }
    transaction = null;
  }
  
  public void commitTransactionAndClose() {
    commitTransaction();
    close();
  }
  
  public void rollbackTransactionAndClose() {
    rollbackTransaction();
    close();
  }

  public void close() {
    try {
      session.close();
    } catch (HibernateException e) {
      log.error( e );
      throw new RuntimeException( "couldn't close the hibernate connection", e );
    }
  }
  
  // identity methods /////////////////////////////////////////////////////////

  public void saveUser(User user) {
    session.save(user);
  }
  public void saveGroup(Group group) {
    session.save(group);
  }
  public void saveEntity(Entity entity) {
    session.save(entity);
  }

  public void saveMembership(Membership membership) {
    session.save(membership);
  }

  public User loadUser(long userId) {
    return (User) session.load(User.class, new Long(userId));
  }

  public Group loadGroup(long groupId) {
    return (Group) session.load(Group.class, new Long(groupId));
  }

  public User getUserByName(String userName) {
    User user = null;
    Query query = session.createQuery(
      "select u " +
      "from org.jbpm.identity.User as u " +
      "where u.name = :userName"
    );
    query.setString("userName", userName);
    List users = query.list();
    if ( (users!=null)
         && (users.size()>0) ) {
      user = (User) users.get(0);
    }
    return user;
  }

  public Group getGroupByName(String groupName) {
    Group group = null;
    Query query = session.createQuery(
      "select g " +
      "from org.jbpm.identity.Group as g " +
      "where g.name = :groupName"
    );
    query.setString("groupName", groupName);
    List groups = query.list();
    if ( (groups!=null)
         && (groups.size()>0) ) {
      group = (Group) groups.get(0);
    }
    return group;
  }
  
  public List getUsers() {
    Query query = session.createQuery(
      "select u " +
      "from org.jbpm.identity.User as u"
    );
    return query.list();
  }

  public List getGroupNamesByUserAndGroupType(String userName, String groupType) {
    Query query = session.createQuery(
      "select membership.group.name " +
      "from org.jbpm.identity.Membership as membership " +
      "where membership.user.name = :userName " +
      "  and membership.group.type = :groupType"
    );
    query.setString("userName", userName);
    query.setString("groupType", groupType);
    return query.list();
  }

  public User getUserByGroupAndRole(String groupName, String role) {
    User user = null;
    Query query = session.createQuery(
      "select m.user " +
      "from org.jbpm.identity.Membership as m " +
      "where m.group.name = :groupName " +
      "  and m.role = :role"
    );
    query.setString("groupName", groupName);
    query.setString("role", role);
    user = (User) query.uniqueResult();
    return user;
  }

  private static final Log log = LogFactory.getLog(IdentitySession.class);
}
