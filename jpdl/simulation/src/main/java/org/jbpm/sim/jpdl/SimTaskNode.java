package org.jbpm.sim.jpdl;

import org.dom4j.Element;
import org.jbpm.graph.node.TaskNode;
import org.jbpm.jpdl.xml.JpdlXmlReader;

public class SimTaskNode extends TaskNode {

  public void read(Element element, JpdlXmlReader jpdlReader) {
    super.read(element, jpdlReader);
  }

}
