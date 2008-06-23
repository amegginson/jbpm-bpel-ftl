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
package org.jbpm;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.configuration.ObjectFactory;
import org.jbpm.configuration.ObjectFactoryImpl;
import org.jbpm.configuration.ObjectFactoryParser;
import org.jbpm.configuration.ObjectInfo;
import org.jbpm.configuration.ValueInfo;
import org.jbpm.job.executor.JobExecutor;
import org.jbpm.persistence.db.DbPersistenceServiceFactory;
import org.jbpm.persistence.db.StaleObjectLogConfigurer;
import org.jbpm.svc.ServiceFactory;
import org.jbpm.svc.Services;
import org.jbpm.util.ClassLoaderUtil;

/**
 * configuration of one jBPM instance.
 * 
 * <p>During process execution, jBPM might need to use some services.  
 * A JbpmConfiguration contains the knowledge on how to create those services.
 * </p>
 * 
 * <p>A JbpmConfiguration is a thread safe object and serves as a factory for 
 * {@link org.jbpm.JbpmContext}s, which means one JbpmConfiguration 
 * can be used to create {@link org.jbpm.JbpmContext}s for all threads. 
 * The single JbpmConfiguration can be maintained in a static member or 
 * in the JNDI tree if that is available.
 * </p>
 * 
 * <p>A JbpmConfiguration can be obtained in following ways:
 * <ul>
 *   <li>from a resource (by default <code>jbpm.cfg.xml</code> is used):
 * <pre> JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance();
 * </pre>
 * or
 * <pre> String myXmlResource = "...";
 * JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance(myXmlResource);</pre> 
 *   </li>
 *   <li>from an XML string:
 * <pre> JbpmConfiguration jbpmConfiguration = JbpmConfiguration.parseXmlString(
 *   "<jbpm-configuration>" +
 *   ...
 *   "</jbpm-configuration>"
 * );
 * </pre>
 *   </li>
 *   <li>By specifying a custom implementation of an object factory.  This can be 
 *   used to specify a JbpmConfiguration in other bean-style notations such as 
 *   used by JBoss Microcontainer or Spring.
 * <pre> ObjectFactory of = new <i>MyCustomObjectFactory</i>();
 * JbpmConfiguration.Configs.setDefaultObjectFactory(of);
 * JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance();
 * </pre>
 *   </li>
 * </ul>
 * </p>
 * 
 * <p>JbpmConfigurations can be configured using a spring-like XML notation
 * (in relax ng compact notation):
 * </p>
 * 
 * <pre>
 * datatypes xs = "http://www.w3.org/2001/XMLSchema-datatypes"
 * 
 * start = element beans { element object* }
 * 
 * object = {
 *   jbpm-context |
 *   bean |
 *   ref |
 *   map |
 *   list |
 *   string |
 *   int |
 *   long |
 *   float |
 *   double |
 *   char |
 *   bool |
 *   true |
 *   false |
 *   null
 * }
 * 
 * jbpm-context = element jbpm-context {
 *   ( attribute name {xsd:string},
 *     service*,
 *     save-operations? 
 *   )
 * }
 * 
 * service = element service {
 *   ( attribute name {xsd:string},
 *     ( attribute factory {xsd:string} ) |
 *     ( factory )
 *   )
 * }
 * 
 * factory = element factory {
 *   ( bean |
 *     ref
 *   )
 * }
 * 
 * save-operations = element save-operations {
 *   ( save-operation* )
 * }
 * 
 * save-operation = element save-operation {
 *   ( ( attribute class {xsd:string} ) |
 *     ( bean |
 *       ref
 *     ) 
 *   )
 * }
 * 
 * bean = element bean {
 *   ( attribute ref-name {xsd:string} ) |
 *   ( attribute name {xsd:string}?,
 *     attribute class {xsd:string}?,
 *     attribute singleton { "true" | "false" }?,
 *     constructor*,
 *     field*,
 *     property*
 *   )
 * }
 * 
 * ref = element ref {
 *   ( attribute bean (xsd:string) )
 * }
 * 
 * constructor = element constructor {
 *   attribute class {xsd:string}?,
 *   ( attribute factory {xsd:string}, 
 *     attribute method {xsd:string}
 *   )?,
 *   parameter*
 * }
 * 
 * parameter = element parameter {
 *   attribute class {xsd:string},
 *   object
 * }
 * 
 * field = element field {
 *   attribute name {xsd:string},
 *   object
 * }
 * 
 * property = element property {
 *   ( attribute name {xsd:string} |
 *     attribute setter {xsd:string}
 *   ),
 *   object
 * }
 * 
 * map = element map {
 *   entry*
 * }
 * 
 * entry = element entry { 
 *   key, 
 *   value 
 * }
 * 
 * key = element key {
 *   object
 * }
 * 
 * value = element value {
 *   object
 * }
 * 
 * list = element list {
 *   object*
 * }
 * 
 * string = element string {xsd:string}
 * int    = element integer {xsd:integer}
 * long   = element long {xsd:long}
 * float  = element float {xsd:string}
 * double = element string {xsd:double}
 * char   = element char {xsd:character}
 * bool   = element bool { "true" | "false" }
 * true   = element true {}
 * false  = element false {}
 * null   = element null {}
 * </pre>
 * </p>
 * 
 * <p>
 * Other configuration properties
 * <table>
 *   <tr>
 *     <td>jbpm.files.dir</td><td></td>
 *   </tr>
 *   <tr>
 *     <td>jbpm.types</td><td></td>
 *   </tr>
 * </table>
 * </p>
 */
public class JbpmConfiguration implements Serializable {
  
  private static final long serialVersionUID = 1L;

  static ObjectFactory defaultObjectFactory = null;
  static Map instances = new HashMap();
  
  /**
   * resets static members for test isolation.
   */
  static void reset() {
    defaultObjectFactory = null;
    instances = new HashMap();
  }

  ObjectFactory objectFactory = null;
  static ThreadLocal jbpmConfigurationsStacks = new ThreadLocal();
  ThreadLocal jbpmContextStacks = new ThreadLocal();
  JobExecutor jobExecutor = null; 

  public JbpmConfiguration(ObjectFactory objectFactory) {
    this.objectFactory = objectFactory;
  }
  public static JbpmConfiguration getInstance() {
    return getInstance(null);
  }
  public static JbpmConfiguration getInstance(String resource) {
    
    JbpmConfiguration instance = null;
    
    synchronized(instances) {
      if (resource==null) {
        resource = "jbpm.cfg.xml";
      }

      instance = (JbpmConfiguration) instances.get(resource);
      if (instance==null) {

        if (defaultObjectFactory!=null) {
          log.debug("creating jbpm configuration from given default object factory '"+defaultObjectFactory+"'");
          instance = new JbpmConfiguration(defaultObjectFactory);

        } else {
          
          try {
            log.info("using jbpm configuration resource '"+resource+"'");
            InputStream jbpmCfgXmlStream = ClassLoaderUtil.getStream(resource);

            // if a resource SHOULD BE used, but is not found in the classpath
            // throw exception (otherwise, the user wants to load own stuff
            // but is confused, if it is not found and not loaded, without
            // any notice)
            if (jbpmCfgXmlStream==null)
              throw new JbpmException("jbpm configuration resource '"+resource+"' is not available");

            ObjectFactory objectFactory = parseObjectFactory(jbpmCfgXmlStream);
            instance = createJbpmConfiguration(objectFactory);
            
          } catch (RuntimeException e) {
            throw new JbpmException("couldn't parse jbpm configuration from resource '"+resource+"'", e);
          }
        }

        instances.put(resource, instance);
      }
    }

    return instance;
  }

  public static boolean hasInstance(String resource) {
    boolean hasInstance = false;
    if (resource==null) {
      resource = "jbpm.cfg.xml";
    }
    if ( (instances!=null)
         && (instances.containsKey(resource))
       ) {
      hasInstance = true;
    }
    return hasInstance;
  }

  protected static ObjectFactory parseObjectFactory(InputStream inputStream) {
    log.debug("loading defaults in jbpm configuration");
    ObjectFactoryParser objectFactoryParser = new ObjectFactoryParser();
    ObjectFactoryImpl objectFactoryImpl = new ObjectFactoryImpl();
    objectFactoryParser.parseElementsFromResource("org/jbpm/default.jbpm.cfg.xml", objectFactoryImpl);

    if (inputStream!=null) {
      log.debug("loading specific configuration...");
      objectFactoryParser.parseElementsStream(inputStream, objectFactoryImpl);
    }

    return objectFactoryImpl;
  }

  /**
   * create an ObjectFacotory from an XML string.
   */
  public static JbpmConfiguration parseXmlString(String xml) {
    log.debug("creating jbpm configuration from xml string");
    InputStream inputStream = null;
    if (xml!=null) {
      inputStream = new ByteArrayInputStream(xml.getBytes());
    }
    ObjectFactory objectFactory = parseObjectFactory(inputStream);
    return createJbpmConfiguration(objectFactory);
  }
  
  protected static JbpmConfiguration createJbpmConfiguration(ObjectFactory objectFactory) {
    JbpmConfiguration jbpmConfiguration = new JbpmConfiguration(objectFactory);

    // now we make the bean jbpm.configuration always availble 
    if (objectFactory instanceof ObjectFactoryImpl) {
      ObjectFactoryImpl objectFactoryImpl = (ObjectFactoryImpl)objectFactory;
      ObjectInfo jbpmConfigurationInfo = new ValueInfo("jbpmConfiguration", jbpmConfiguration);
      objectFactoryImpl.addObjectInfo(jbpmConfigurationInfo);
      
      if (mustStaleObjectExceptionsBeHidden(objectFactory)) {
        StaleObjectLogConfigurer.hideStaleObjectExceptions();
      }
    }

    return jbpmConfiguration;
  }

  private static boolean mustStaleObjectExceptionsBeHidden(ObjectFactory objectFactory) {
    if (!objectFactory.hasObject("jbpm.hide.stale.object.exceptions")) {
      return true;
    }
    Object o = (Boolean) objectFactory.createObject("jbpm.hide.stale.object.exceptions");
    if ( (o instanceof Boolean)
         && (((Boolean)o).booleanValue()==false)
       ) {
      return false;
    }
    return true;
  }
  
  public static JbpmConfiguration parseInputStream(InputStream inputStream) {
    ObjectFactory objectFactory = parseObjectFactory(inputStream); 
    log.debug("creating jbpm configuration from input stream");
    return createJbpmConfiguration(objectFactory);
  }

  public static JbpmConfiguration parseResource(String resource) {
    InputStream inputStream = null;
    log.debug("creating jbpm configuration from resource '"+resource+"'");
    if (resource!=null) {
      inputStream = ClassLoaderUtil.getStream(resource);
    }
    ObjectFactory objectFactory = parseObjectFactory(inputStream);
    return createJbpmConfiguration(objectFactory);
  }

  public JbpmContext createJbpmContext() {
    return createJbpmContext(JbpmContext.DEFAULT_JBPM_CONTEXT_NAME);
  }

  public JbpmContext createJbpmContext(String name) {
    JbpmContext jbpmContext = (JbpmContext) objectFactory.createObject(name);
    jbpmContext.jbpmConfiguration = this;
    jbpmContextCreated(jbpmContext);
    return jbpmContext;
  }
  
  public ServiceFactory getServiceFactory(String serviceName) {
    return getServiceFactory(serviceName, JbpmContext.DEFAULT_JBPM_CONTEXT_NAME);
  }

  public ServiceFactory getServiceFactory(String serviceName, String jbpmContextName) {
    ServiceFactory serviceFactory = null;
    JbpmContext jbpmContext = createJbpmContext(jbpmContextName);
    try {
      serviceFactory = jbpmContext.getServices().getServiceFactory(serviceName);
    } finally {
      jbpmContext.close();
    }
    return serviceFactory;
  }
  
  /**
   * gives the jbpm domain model access to configuration information via the current JbpmContext.
   */
  public abstract static class Configs {
    public static ObjectFactory getObjectFactory() {
      ObjectFactory objectFactory = null;
      JbpmContext jbpmContext = JbpmContext.getCurrentJbpmContext();
      if (jbpmContext!=null) {
        objectFactory = jbpmContext.objectFactory;
      } else {
        objectFactory = getInstance().objectFactory;
      }
      return objectFactory;
    }
    public static void setDefaultObjectFactory(ObjectFactory objectFactory){
      defaultObjectFactory = objectFactory;
    }
    public static boolean hasObject(String name) {
      ObjectFactory objectFactory = getObjectFactory();
      return objectFactory.hasObject(name);
    }
    public static synchronized Object getObject(String name) {
      ObjectFactory objectFactory = getObjectFactory();
      return objectFactory.createObject(name);
    }
    public static String getString(String name) {
      return (String) getObject(name);
    }

    public static long getLong(String name) {
      return ((Long)getObject(name)).longValue();
    }
    
    public static int getInt(String name) {
      return ((Integer)getObject(name)).intValue();
    }
    
    public static boolean getBoolean(String name) {
      return ((Boolean)getObject(name)).booleanValue();
    }
  }

  public void createSchema() {
    createSchema(JbpmContext.DEFAULT_JBPM_CONTEXT_NAME);
  }
  
  public void createSchema(String jbpmContextName) {
    JbpmContext jbpmContext = createJbpmContext(jbpmContextName);
    try {
      Services services = jbpmContext.getServices();
      DbPersistenceServiceFactory persistenceServiceFactory = (DbPersistenceServiceFactory) services.getServiceFactory(Services.SERVICENAME_PERSISTENCE);
      persistenceServiceFactory.createSchema();
    } finally {
      jbpmContext.close();
    }
  }
  
  public void dropSchema() {
    dropSchema(JbpmContext.DEFAULT_JBPM_CONTEXT_NAME);
  }
  
  public void dropSchema(String jbpmContextName) {
    JbpmContext jbpmContext = createJbpmContext(jbpmContextName);
    try {
      Services services = jbpmContext.getServices();
      DbPersistenceServiceFactory persistenceServiceFactory = (DbPersistenceServiceFactory) services.getServiceFactory(Services.SERVICENAME_PERSISTENCE);
      persistenceServiceFactory.dropSchema();
    } finally {
      jbpmContext.close();
    }
  }

  public void close() {
    close(JbpmContext.DEFAULT_JBPM_CONTEXT_NAME);
  }

  public void close(String jbpmContextName) {
    JbpmContext jbpmContext = createJbpmContext(jbpmContextName);
    try {
      
      synchronized (instances) {
        Iterator iter = instances.keySet().iterator();
        while(iter.hasNext()) {
          String resource = (String) iter.next();
          if (this==instances.get(resource)) {
            instances.remove(resource);
            break;
          }
        }
      }
      
      if (jobExecutor!=null) {
        jobExecutor.stop();
      }
      
      Map serviceFactories = jbpmContext.getServices().getServiceFactories();
      if (serviceFactories!=null) {
        Iterator iter = serviceFactories.values().iterator();
        while (iter.hasNext()) {
          ServiceFactory serviceFactory = (ServiceFactory) iter.next();
          serviceFactory.close();
        }
      }
    } finally {
      jbpmContext.close();
    }
  }
  
  static JbpmConfiguration getCurrentJbpmConfiguration() {
    JbpmConfiguration currentJbpmConfiguration = null;
    Stack stack = getJbpmConfigurationStack();
    if (! stack.isEmpty()) {
      currentJbpmConfiguration = (JbpmConfiguration) stack.peek();
    }
    return currentJbpmConfiguration;
  }

  static synchronized Stack getJbpmConfigurationStack() {
    Stack stack = (Stack) jbpmConfigurationsStacks.get();
    if (stack==null) {
      stack = new Stack();
      jbpmConfigurationsStacks.set(stack);
    }
    return stack;
  }
  
  synchronized void pushJbpmConfiguration() {
    getJbpmConfigurationStack().push(this);
  }

  synchronized void popJbpmConfiguration() {
    getJbpmConfigurationStack().remove(this);
  }


  public JbpmContext getCurrentJbpmContext() {
    JbpmContext currentJbpmContext = null;
    Stack stack = getJbpmContextStack();
    if (! stack.isEmpty()) {
      currentJbpmContext = (JbpmContext) stack.peek();
    }
    return currentJbpmContext;
  }

  
  Stack getJbpmContextStack() {
    Stack stack = (Stack) jbpmContextStacks.get();
    if (stack==null) {
      stack = new Stack();
      jbpmContextStacks.set(stack);
    }
    return stack;
  }

  void pushJbpmContext(JbpmContext jbpmContext) {
    getJbpmContextStack().push(jbpmContext);
  }

  void popJbpmContext(JbpmContext jbpmContext) {
    Stack stack = getJbpmContextStack();
    if (stack.isEmpty()) {
      throw new JbpmException("closed JbpmContext more then once... check your try-finally's around JbpmContexts blocks");
    }
    JbpmContext popped = (JbpmContext) stack.pop();
    if (jbpmContext!=popped) {
      throw new JbpmException("closed JbpmContext in different order then they were created... check your try-finally's around JbpmContexts blocks");
    }
  }


  void jbpmContextCreated(JbpmContext jbpmContext) {
    pushJbpmConfiguration();
    pushJbpmContext(jbpmContext);
  }
  
  void jbpmContextClosed(JbpmContext jbpmContext) {
    popJbpmConfiguration();
    popJbpmContext(jbpmContext);
  }
  
  public void startJobExecutor() {
    getJobExecutor().start();
  }

  public synchronized JobExecutor getJobExecutor() {
    if (jobExecutor==null) {
      try {
        jobExecutor = (JobExecutor) this.objectFactory.createObject("jbpm.job.executor"); 
      } catch (ClassCastException e) {
        throw new JbpmException("jbpm configuration object under key 'jbpm.job.executor' is not a "+JobExecutor.class.getName(), e);
      }
    }
    return jobExecutor;
  }

  private static Log log = LogFactory.getLog(JbpmConfiguration.class);
}
