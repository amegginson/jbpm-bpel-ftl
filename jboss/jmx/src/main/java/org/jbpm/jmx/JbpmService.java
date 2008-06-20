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
package org.jbpm.jmx;

import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;

import org.jboss.util.naming.NonSerializableFactory;
import org.jboss.system.ServiceMBeanSupport;
import org.jbpm.JbpmConfiguration;

public class JbpmService extends ServiceMBeanSupport implements JbpmServiceMBean {

  String jndiName = null;
  String jbpmCfgResource = null;
  String jbpmContextName = null;
  
  protected void startService() throws Exception {
    log.debug("starting jbpm service...");
    JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance(jbpmCfgResource);
    bind(jbpmConfiguration, jndiName);
  }

  protected void stopService() throws Exception {
    log.debug("stopping jbpm service...");
    unbind(jndiName);
  }

  private void bind(Object object, String jndiName) throws NamingException {
    InitialContext rootCtx = new InitialContext();
    Name fullName = rootCtx.getNameParser("").parse(jndiName);
    log.debug("binding '" +object+ "' to '"+jndiName+"'");
    NonSerializableFactory.rebind(fullName, object, true);
  }

  private void unbind(String jndiName) throws NamingException {
    InitialContext rootCtx = new InitialContext();
    log.debug("unbinding '"+jndiName+"'");
    rootCtx.unbind(jndiName);
    NonSerializableFactory.unbind(jndiName);
  }

  public String getJndiName() {
    return jndiName;
  }
  public void setJndiName(String jndiName) {
    this.jndiName = jndiName;
  }  
  public String getJbpmCfgResource() {
    return jbpmCfgResource;
  }
  public void setJbpmCfgResource(String jbpmCfgResource) {
    this.jbpmCfgResource = jbpmCfgResource;
  }
  public String getJbpmContextName() {
    return jbpmContextName;
  }
  public void setJbpmContextName(String jbpmContextName) {
    this.jbpmContextName = jbpmContextName;
  }
}
