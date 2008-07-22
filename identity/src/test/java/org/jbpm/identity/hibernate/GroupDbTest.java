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

import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jbpm.identity.*;

public class GroupDbTest extends IdentityDbTestCase {

  public void testGroup() {
    Group group = new Group("people with read hair");
    group = saveAndReload(group);
    assertEquals("people with read hair", group.getName());
  }

  public void testGroupMemberships() {
    Group chicagoBulls = new Group("chicago bulls");
    User tyson = new User("tyson");
    User eddy = new User("eddy");
    User antonio = new User("antonio");
    User chris = new User("chris");
    
    Membership.create(tyson, "F-C", chicagoBulls);
    Membership.create(eddy, "C-F", chicagoBulls);
    Membership.create(antonio, "F-C", chicagoBulls);
    Membership.create(chris, "G", chicagoBulls);

    chicagoBulls = saveAndReload(chicagoBulls);
    assertEquals(4, chicagoBulls.getUsers().size());
  }

  public void testGroupPermissions() {
    Group chicagoBulls = new Group("chicago bulls");
    chicagoBulls.addPermission(new SocketPermission("basket", "connect"));
    chicagoBulls.addPermission(new FilePermission("ticket", "write"));

    chicagoBulls = saveAndReload(chicagoBulls);
    
    assertEquals(2, chicagoBulls.getPermissions().size());
  }

  public void testGroups() {
    Group clowns = new Group("clowns");
    identitySession.saveGroup(clowns);
    
    newTransaction();
    
    clowns = identitySession.loadGroup(clowns.getId());
    assertEquals("clowns", clowns.getName());
  }

  public void testGroupChildren() {
    Group clowns = new Group("clowns");
    clowns.addChild(new Group("cowboys"));
    clowns.addChild(new Group("indians"));
    identitySession.saveGroup(clowns);

    newTransaction();
    
    clowns = identitySession.loadGroup(clowns.getId());
    assertEquals("clowns", clowns.getName());
    Set children = clowns.getChildren();
    assertNotNull(children);
    assertEquals(2, children.size());
    
    Set childNames = new HashSet();
    Iterator iter = children.iterator();
    Group child = (Group)iter.next();
    assertEquals("clowns", child.getParent().getName());
    childNames.add(child.getName());
    child = (Group)iter.next();
    assertEquals("clowns", child.getParent().getName());
    childNames.add(child.getName());

    Set expectedChildNames = new HashSet();
    expectedChildNames.add("cowboys");
    expectedChildNames.add("indians");
    
    assertEquals(expectedChildNames, childNames);
  }
}
