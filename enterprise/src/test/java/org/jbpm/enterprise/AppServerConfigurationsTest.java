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
package org.jbpm.enterprise;

import org.apache.cactus.ServletTestCase;
import org.jbpm.JbpmConfiguration;
import org.jbpm.msg.jms.JmsMessageServiceFactoryImpl;
import org.jbpm.persistence.jta.JtaDbPersistenceServiceFactory;
import org.jbpm.scheduler.ejbtimer.EntitySchedulerServiceFactory;
import org.jbpm.svc.Services;

public class AppServerConfigurationsTest extends ServletTestCase {

  JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance();

  public void testUnavailabilityOfTheJobExecutor() {
    assertNull(jbpmConfiguration.getJobExecutor());
  }

  public void testJtaDbPersistenceFactoryConfiguration() {
    assertSame(JtaDbPersistenceServiceFactory.class, jbpmConfiguration.getServiceFactory(Services.SERVICENAME_PERSISTENCE).getClass());
    JtaDbPersistenceServiceFactory persistenceServiceFactory = (JtaDbPersistenceServiceFactory) jbpmConfiguration.getServiceFactory(Services.SERVICENAME_PERSISTENCE);
    assertFalse(persistenceServiceFactory.isTransactionEnabled());
    assertTrue(persistenceServiceFactory.isCurrentSessionEnabled());
  }

  public void testJmsMessageServiceFactoryConfiguration() {
    assertSame(JmsMessageServiceFactoryImpl.class, jbpmConfiguration.getServiceFactory(Services.SERVICENAME_MESSAGE).getClass());
  }

  public void testEjbSchedulerServiceFactoryConfiguration() {
    assertSame(EntitySchedulerServiceFactory.class, jbpmConfiguration.getServiceFactory(Services.SERVICENAME_SCHEDULER).getClass());
  }
}
