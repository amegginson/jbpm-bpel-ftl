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
package org.jbpm.persistence.jta;

import org.jbpm.persistence.db.DbPersistenceServiceFactory;
import org.jbpm.svc.Service;

/**
 * The JTA persistence service enables jBPM to participate in JTA transactions.
 * If an existing transaction is underway, {@link JtaDbPersistenceService} 
 * clings to it; otherwise it starts a new transaction.
 * 
 * <h3>Configuration</h3>
 * 
 * The JTA persistence service factory has the configurable fields described
 * below.
 * 
 * <ul>
 * <li><code>isCurrentSessionEnabled</code></li>
 * <li><code>isTransactionEnabled</code></li>
 * </ul>
 * 
 * Refer to the jBPM manual for details.
 * 
 * @author Tom Baeyens
 */
public class JtaDbPersistenceServiceFactory extends DbPersistenceServiceFactory {

  private static final long serialVersionUID = 1L;

  public JtaDbPersistenceServiceFactory() {
    setCurrentSessionEnabled(true);
    setTransactionEnabled(false);
  }
  
  public Service openService() {
    return new JtaDbPersistenceService(this);
  }
}
