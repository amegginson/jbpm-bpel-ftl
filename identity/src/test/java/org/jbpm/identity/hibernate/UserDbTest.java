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

import java.net.*;
import java.security.*;
import java.util.*;

import org.jbpm.identity.*;

public class UserDbTest extends IdentityDbTestCase {

  public void testUser() {
    User user = new User("johndoe");
    user = saveAndReload(user);
    assertEquals("johndoe", user.getName());
  }
  
  public void testUserMemberships() {
    User john = new User("johndoe");
    Group qaTeam = new Group("qa team", "hierarchy");
    Group marketingTeam = new Group("marketing team", "hierarchy");
    Membership.create(john, "mgr", qaTeam);
    Membership.create(john, "mgr", marketingTeam);
    
    john = saveAndReload(john);
    
    Set groups = john.getGroupsForGroupType("hierarchy");
    
    assertEquals(2, groups.size());
    assertEquals(2, groups.size());
    assertTrue(containsGroup(groups, "qa team"));
    assertTrue(containsGroup(groups, "marketing team"));
  }
  
  public void testUserPermissions() {
    User user = new User("johndoe");
    user.addPermission(new NetPermission("connect", "9001"));
    user.addPermission(new AllPermission("all", "everything"));
    
    user = saveAndReload(user);
    
    Set permissions = user.getPermissions();
    assertNotNull(permissions);
    assertEquals(2, permissions.size());
  }
  
  public void testVerifyWrongUser() {
    assertNull(identitySession.verify("unexisting-user", "wrong password"));
  }

  public void testVerifyValidPwd() {
    User johnDoe = new User("johndoe");
    johnDoe.setPassword("johnspwd");
    identitySession.saveUser(johnDoe);
    newTransaction();

    assertEquals(new Long(1), identitySession.verify("johndoe", "johnspwd"));
  }

  public boolean containsGroup(Set groups, String groupName) {
    Iterator iter = groups.iterator();
    while (iter.hasNext()) {
      if( groupName.equals(((Group)iter.next()).getName())) {
        return true;
      }
    }
    return false;
  }
}
