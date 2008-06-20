package org.jbpm.sim;

import java.util.Date;

import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.sim.def.JbpmSimulationExperiment;
import org.jbpm.sim.def.JbpmSimulationScenario;
import org.jbpm.sim.exe.ExperimentReader;
import org.jbpm.sim.kpi.BusinessFigureCalculator;
import org.jbpm.sim.report.ScenarioReport;
import org.jbpm.sim.report.dto.ValueStatisticResult;

import junit.framework.TestCase;

public class BusinessFiguresTest extends TestCase {
  
  private String testProcessXml =     
    "<process-definition name='test' start-distribution='startDist'>" +

    "  <start-state name='start'>" +
    "    <transition to='node1' />" +
    "  </start-state>" +
    
    "  <node name='node1'>" +
    "    <transition to='end'>" +
    "      <simulation-action class='org.jbpm.sim.kpi.BusinessFigureAction'>" +
    "         <name>testFigure</name> " +
    "      </simulation-action>" +
    "    </transition>" +
    "  </node>" +

    "  <end-state name='end'/>" +

    "</process-definition>" ;

  private String experimentConfiguration = 
    "<experiment name='MySimulationExperiment'" + 
    "          run-time='100'" + 
    "          real-start-time='01.01.1970 01:00:00:002'" +  
    "          time-unit='minute'>" + 
    "  <scenario name ='test'>" +
    "    <distribution name='startDist' sample-type='real' type='constant' value='20' /> " +
    "    <sim-process name='test' />" +
    "    <business-figure name='testFigure' type='costs' " +
    "      automatic-calculation='none'" + // | process - start | process -end" 
    "      handler='org.jbpm.sim.BusinessFiguresTest$BusinessFigureHandler' />" + 
    "  </scenario>" +  
    "</experiment>";
  
  public static class BusinessFigureHandler implements BusinessFigureCalculator{
    public Number calculate(ExecutionContext executionContext) {
      return Double.valueOf(10);
    }
  }
  
  public void testBusinessFigureConfiguredInProcess() {
    ExperimentReader reader = new ExperimentReader(experimentConfiguration);
    // inject process definition
    reader.addProcessDefinition("test", testProcessXml );
    
    JbpmSimulationExperiment experiment = reader.readExperiment();
    experiment.run();
    
    ScenarioReport report = experiment.getScenario("test").getScenarioReport();
    
    assertEquals(1, report.getBusinessFigureTypes().size());
    assertEquals("costs", report.getBusinessFigureTypes().iterator().next());
    assertEquals(
        40d, //
        report.getBusinessFigureValue("costs"), //
        0.1);
  }
  
  /**
   * and now the same with the usage of the figure configured in the 
   * experiment, no change in the process definition required
   */
  
  private String testProcessXml2 =     
    "<process-definition name='test' start-distribution='startDist'>" +

    "  <start-state name='start'>" +
    "    <transition to='node1' />" +
    "  </start-state>" +
    
    "  <node name='node1'>" +
    "    <transition to='end' />" +
    "  </node>" +

    "  <end-state name='end'/>" +

    "</process-definition>" ;

  private String experimentConfiguration2 = 
    "<experiment name='MySimulationExperiment'" + 
    "          run-time='100'" + 
    "          real-start-time='01.01.1970 01:00:00:002'" +  
    "          time-unit='minute'>" + 
    "  <scenario name ='test'>" +
    "    <distribution name='startDist' sample-type='real' type='constant' value='20' /> " +

    "    <business-figure name='testFigure' type='costs' " +
    "      automatic-calculation='none'" + // | process - start | process -end" 
    "      handler='org.jbpm.sim.BusinessFiguresTest$BusinessFigureHandler' />" + 

    "    <sim-process name='test'>" +
    "       <node-overwrite node-name='node1'>" + 
    "         <calculate-business-figure name='testFigure' /> " +    
    "       </node-overwrite> " +
    "    </sim-process>" +
    
    "  </scenario>" +  
    "</experiment>";
    
  public void testBusinessFigureConfiguredInExperiment() {
    ExperimentReader reader = new ExperimentReader(experimentConfiguration);
    // inject process definition
    reader.addProcessDefinition("test", testProcessXml );
    
    JbpmSimulationExperiment experiment = reader.readExperiment();
    experiment.run();
    
    ScenarioReport report = experiment.getScenario("test").getScenarioReport();
    
    assertEquals(1, report.getBusinessFigureTypes().size());
    assertEquals("costs", report.getBusinessFigureTypes().iterator().next());
    assertEquals(
        40d, //
        report.getBusinessFigureValue("costs"), //
        0.1);
  }
}
