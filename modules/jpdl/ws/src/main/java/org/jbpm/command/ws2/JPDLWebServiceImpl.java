/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jbpm.command.ws2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.JbpmException;
import org.jbpm.command.CancelProcessInstanceCommand;
import org.jbpm.command.Command;
import org.jbpm.command.GetProcessDefinitionsCommand;
import org.jbpm.command.GetProcessInstancesCommand;
import org.jbpm.command.NewProcessInstanceCommand;
import org.jbpm.command.SignalCommand;
import org.jbpm.command.StartProcessInstanceCommand;
import org.jbpm.command.VariablesCommand;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.w3c.dom.Element;


/**
 * Web service frontend to the command facade
 *
 * @author Heiko.Braun@jboss.com
 */
@WebService()
//@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public class JPDLWebServiceImpl implements JPDLWebServiceFacade
{
   private static Log log = LogFactory.getLog(JPDLWebServiceImpl.class);
  
   
   
   @WebMethod()
   public ProcessInstanceRef newProcessInstance(ProcessRequest processRequest)
   {
     log.debug("New process " + processRequest.getProcessDefinitionName());

     // we either start it immediatley or just create the process instance
     NewProcessInstanceCommand cmd = processRequest.isStart() ?
       new StartProcessInstanceCommand() : new NewProcessInstanceCommand();
     cmd.setProcessName( processRequest.getProcessDefinitionName() );
     //I must be able to add the version of the process definition that i want... Salaboy
     Object result = executeCommand(cmd);
     ProcessInstanceRef processRef=new ProcessInstanceRef((ProcessInstance)result);

     return processRef;
   }
   
   
   
   @WebMethod()
   public TokenRef signal(SignalRequest signalRequest)
   {
      log.debug("Signal token " + signalRequest.getTokenId());

      SignalCommand cmd = new SignalCommand(
        signalRequest.getTokenId(),
        signalRequest.getTransitionName()
      );

      // Associate process variables 
      addProcessVariables(cmd, signalRequest);
      
      Token token = (Token)executeCommand(cmd);

      return new TokenRef(token);

   }
   @SuppressWarnings("unchecked")
   @WebMethod()
   public List<ProcessDefinitionRef> getProcessDefinitions(boolean onlyLatest) {
	   GetProcessDefinitionsCommand cmd=new GetProcessDefinitionsCommand();
	   cmd.setOnlyLatest(onlyLatest);
	   
	   List<ProcessDefinition> processDefs=(List<ProcessDefinition>) executeCommand(cmd);
	   List<ProcessDefinitionRef> result=new ArrayList<ProcessDefinitionRef>();
	   for (ProcessDefinition processDefinition : processDefs) {
		   	result.add(new ProcessDefinitionRef(processDefinition.getName(),processDefinition.getVersion()));
	   }
	   return result;
	   
   }
   
   
   @SuppressWarnings("unchecked")
   @WebMethod()
   public List<ProcessInstanceRef> getProcessInstances(ProcessDefinitionRef processDefinitionRef) {
	   GetProcessInstancesCommand cmd=new GetProcessInstancesCommand();
	   cmd.setProcessName(processDefinitionRef.getProcessDefinitionName());
	   
	   
	   List<ProcessInstance> processInstances=(List<ProcessInstance>) executeCommand(cmd);
	   List<ProcessInstanceRef> result=new ArrayList<ProcessInstanceRef>();
	   for (ProcessInstance processInstance : processInstances) {
		   	result.add(new ProcessInstanceRef(processInstance.getId(),
		   			processInstance.getProcessDefinition().getName(),
		   			processInstance.getProcessDefinition().getVersion(),
		   			processInstance.getRootToken().getId(),
		    		  processInstance.getRootToken().getNode().getName()));
	   }
	   return result;
	   
   }
   
   
   @WebMethod()
   public boolean cancelProcessInstance(ProcessInstanceRef processInstanceRef){
	   CancelProcessInstanceCommand cmd=new CancelProcessInstanceCommand();
	   cmd.setProcessId(processInstanceRef.getProcessInstanceId());
	   
	   executeCommand(cmd);
	   
	   return true;
	
   }
   
   //REVIEW!!!!!! AND TEST!!!! NOT FINISHED!!!
   @WebMethod()
   public void setProcessVariable(ProcessInstanceRef processRef,String variableName,Object value){
	   VariablesCommand cmd=new VariablesCommand();
	   Map<String,Object> variables=new HashMap<String,Object>();
	   variables.put(variableName, value);
	   cmd.setVariables(variables);
	   executeCommand(cmd);
   }
   
   
   
   
   private void addProcessVariables(SignalCommand cmd, SignalRequest signalRequest)
   {
      if(signalRequest.getAny()!=null && !signalRequest.getAny().isEmpty())
      {
         Map vars = new HashMap( signalRequest.getAny().size() );
         cmd.setVariables(vars);
         
         for(Element el : signalRequest.getAny() )
         {
            String name = el.getNodeName();
            String value = el.getTextContent(); // TODO: type conversion
            
            cmd.getVariables().put( name, value );
         }
      }
   }

   private Object executeCommand(Command command)
   {
      JbpmConfiguration jbpmConfiguration = JbpmConfiguration.getInstance();
      Object result = null;
      JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
      try
      {
         log.debug("Executing " + command);
         result = command.execute(jbpmContext);
      }
      catch (Exception e)
      {
         throw new JbpmException(
           "Failed to execute '" + command + "': " + e.getMessage(),  e
         );
      }
      finally
      {
         jbpmContext.close();
      }

      return result;
   }
   
   
  

}
