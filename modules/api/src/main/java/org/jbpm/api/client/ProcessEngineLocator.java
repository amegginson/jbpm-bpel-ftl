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
package org.jbpm.api.client;

// $Id: $

import java.net.URL;

import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.bootstrap.basic.BasicBootstrap;
import org.jboss.kernel.plugins.deployment.xml.BasicXMLDeployer;
import org.jboss.kernel.plugins.util.KernelLocator;
import org.jboss.kernel.spi.registry.KernelRegistryEntry;
import org.jbpm.api.JBPMException;

/**
 * The ProcessEngineLocator is the main entry point for all client operations.
 * 
 * It locates the process engine instance shared by all clients.
 * 
 * @author thomas.diesler@jboss.com
 * @since 18-Jun-2008
 */
public abstract class ProcessEngineLocator {

  // Hide the constructor
  private ProcessEngineLocator() {
  }

  /**
   * Locate the ProcessEngine instance shared by all clients.
   * 
   * @return The configured instance of a process engine
   */
  public static ProcessEngine locateProcessEngine() {

    // Get or bootstrap the kernel
    Kernel kernel = KernelLocator.getKernel();
    if (kernel == null) {
      URL url = Thread.currentThread().getContextClassLoader().getResource("jbpm-beans.xml");
      if (url == null)
        throw new JBPMException("Cannot find resource: jbpm-beans.xml");

      EmbeddedBootstrap bootstrap = new EmbeddedBootstrap();
      bootstrap.bootstrap(url);
      kernel = bootstrap.getKernel();
    }

    KernelRegistryEntry entry = kernel.getRegistry().getEntry(ProcessEngine.BEAN_NAME_JBPMENGINE);
    ProcessEngine engine = (ProcessEngine) entry.getTarget();
    return engine;
  }

  static class EmbeddedBootstrap extends BasicBootstrap {

    public void bootstrap(URL url) {
      if (getKernel() == null) {
        try {
          super.bootstrap();
          
          BasicXMLDeployer deployer = new BasicXMLDeployer(getKernel());
          Runtime.getRuntime().addShutdownHook(new Shutdown(deployer));

          deployer.deploy(url);
          deployer.validate();
          
        } catch (Throwable e) {
          throw new JBPMException("Cannot boot from: " + url, e);
        }
      }
    }

    static class Shutdown extends Thread {
      private BasicXMLDeployer deployer;

      Shutdown(BasicXMLDeployer deployer) {
        this.deployer = deployer;
      }

      public void run() {
        deployer.shutdown();
      }
    }
  }
}
