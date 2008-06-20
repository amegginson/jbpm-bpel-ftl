package org.jbpm.sim.jpdl;

import org.dom4j.Element;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.jpdl.xml.JpdlXmlReader;

/**
 * The SimAction class adds behavior to check, if the action should be executed in a simulation run.
 * 
 * Default behavior is: not execute.
 * 
 * To execute an action during a simulation run, you have to configure either
 * the attribute <b>simulation</b> with the value 'execute' or to add a 
 * <simulation-class> element, which defines a special handler for the simulation.
 * 
 *  Another way is to define not a <action>, but a <simulation-action>, which is only
 *  executed in simulation runs
 * 
 * @author bernd.ruecker@camunda.com
 */
public class SimAction extends Action {

  private static final long serialVersionUID = 1L;
  
  private boolean simulate = false;
  
  private boolean simulationHandlerChecked = false;

  public void read(Element actionElement, JpdlXmlReader jpdlReader) {
    super.read(actionElement, jpdlReader);

    String simulation = actionElement.attributeValue("simulation");
    if ( "execute".equals(simulation)
         || "simulation-action".equals(actionElement.getQName().getName())
       ) {
      simulate = true;
    }
    
    String simulationClass = actionElement.attributeValue("simulation-class");
    if (simulationClass!=null) {
      simulate = true;
      actionDelegation.setClassName(simulationClass);
      // don't use same configuration in this case
      // if the user wants the same configurations, he has to use a own 
      // SimulationAction
      // TODO: Maybe make some configuration property for this?
      actionDelegation.setConfiguration(null);
    }
  }

  public void execute(ExecutionContext executionContext) throws Exception {
    // check if the delegation class implements one of the simulation interfaces
    if (!simulationHandlerChecked) {
      Object delegation = actionDelegation.getInstance();

      // if the SimulationHandler is implemented, change the action to call the simExecute instead
      if (SimulationHandler.class.isAssignableFrom( delegation.getClass() )) {
        simulate = true;
        referencedAction = new ActionHandlerDelegation( (SimulationHandler)delegation );
      }
      // if the SimulationNoop is implemented skip execution completely
      else if (SimulationNoop.class.isAssignableFrom( delegation.getClass() ))
        simulate = false;
      simulationHandlerChecked = true;
    }

    if (simulate) {
      super.execute(executionContext);
    }
  }
  
  private class ActionHandlerDelegation extends Action {
    
    private SimulationHandler delegate;
    
    public ActionHandlerDelegation(SimulationHandler delegate) {
      this.delegate = delegate;
    }
    public void execute(ExecutionContext executionContext) throws Exception {
      delegate.simExecute(executionContext);
    }    
  }
}
