package org.jbpm.jpdl.par;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;

public class ResourceAction implements ActionHandler {

  private static final long serialVersionUID = 1L;

  public void execute(ExecutionContext executionContext) throws Exception {
    try {
      // class resources
      
      URL resource = getClass().getResource("classresource.txt");
      InputStream stream = resource.openStream();
      ProcessArchiveDeploymentDbTest.classResourceUrl = read(stream);

      stream = getClass().getResourceAsStream("classresource.txt");
      ProcessArchiveDeploymentDbTest.classResourceStream = read(stream);

      resource = ResourceAction.class.getClassLoader().getResource("org/jbpm/jpdl/par/classresource.txt");
      stream = resource.openStream();
      ProcessArchiveDeploymentDbTest.classLoaderResourceUrl = read(stream);

      stream = ResourceAction.class.getClassLoader().getResourceAsStream("org/jbpm/jpdl/par/classresource.txt");
      ProcessArchiveDeploymentDbTest.classLoaderResourceStream = read(stream);
      
      // archive resources

      resource = getClass().getResource("//archiveresource.txt");
      stream = resource.openStream();
      ProcessArchiveDeploymentDbTest.archiveResourceUrl = read(stream);

      stream = getClass().getResourceAsStream("//archiveresource.txt");
      ProcessArchiveDeploymentDbTest.archiveResourceStream = read(stream);

      resource = ResourceAction.class.getClassLoader().getResource("//archiveresource.txt");
      stream = resource.openStream();
      ProcessArchiveDeploymentDbTest.archiveClassLoaderResourceUrl = read(stream);

      stream = ResourceAction.class.getClassLoader().getResourceAsStream("//archiveresource.txt");
      ProcessArchiveDeploymentDbTest.archiveClassLoaderResourceStream = read(stream);
      
      // unexisting resources
      
      ProcessArchiveDeploymentDbTest.unexistingClassResourceStream = getClass().getResourceAsStream("unexistingresource.txt");
      ProcessArchiveDeploymentDbTest.unexistingClassLoaderResourceStream = ResourceAction.class.getClassLoader().getResourceAsStream("org/jbpm/jpdl/par/unexistingresource.txt");
      ProcessArchiveDeploymentDbTest.unexistingArchiveResourceStream = getClass().getResourceAsStream("//unexistingarchiveresource.txt");
      ProcessArchiveDeploymentDbTest.unexistingArchiveLoaderResourceStream = ResourceAction.class.getClassLoader().getResourceAsStream("//unexistingarchiveresource.txt");

    } catch (RuntimeException e) {
      e.printStackTrace();
    }
  }

  private String read(InputStream resourceAsStream) throws Exception {
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));
    StringBuffer buffer = new StringBuffer();
    String l;
    while ((l = bufferedReader.readLine()) != null) {
      buffer.append(l);
    }
    return buffer.toString();
  }
}
