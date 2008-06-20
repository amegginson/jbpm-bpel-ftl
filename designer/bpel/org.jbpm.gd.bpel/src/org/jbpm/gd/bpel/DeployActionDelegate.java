package org.jbpm.gd.bpel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.jbpm.bpel.graph.def.BpelProcessDefinition;
import org.jbpm.bpel.tools.FileUtil;
import org.jbpm.bpel.tools.ModuleDeployHelper;
import org.jbpm.bpel.tools.ProcessDeployer;
import org.jbpm.bpel.tools.WebModuleGenerator;
import org.jbpm.bpel.xml.ProblemCollector;
import org.jbpm.bpel.xml.ProblemHandler;
import org.jbpm.jpdl.par.ProcessArchive;
import org.jbpm.jpdl.xml.Problem;

public class DeployActionDelegate implements IObjectActionDelegate {

	private IFile file;

	private IContainer container;

	IViewPart targetPart;

	private static final File tmpDir = new File(System
			.getProperty("java.io.tmpdir"), "DeployActionDelegate");

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		if (targetPart instanceof IViewPart) {
			this.targetPart = (IViewPart) targetPart;
		} else {
			this.targetPart = null;
		}
	}

	private void setStatusMessage(final String string) {
		if (targetPart != null) {
			targetPart.getViewSite().getShell().getDisplay().asyncExec(
					new Runnable() {
						public void run() {
							getStatusLineManager().setMessage(string);
						}
					});
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection != null && !(selection instanceof IStructuredSelection))
			return;
		IStructuredSelection structuredSel = (IStructuredSelection) selection;
		Object object = structuredSel.getFirstElement();
		if (object != null && !(object instanceof IFile))
			return;
		file = (IFile) object;
	}
	
	public void run(IAction action) {
		Job job = new Job("Deploying " + file.getName() + "...") {
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Deploying " + file.getName(), IProgressMonitor.UNKNOWN);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
				runJob();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	public void runJob() {
		PrintStream saved = System.out;
		MessageConsoleStream out = findConsole().newMessageStream();
		System.setOut(new PrintStream(out));
		System.out.println("Deploying " + file.getName() + "...");
		if (tmpDir.exists())
			FileUtil.recursiveDelete(tmpDir);
		tmpDir.mkdir();
		ProblemCollector problemCollector = new ProblemCollector();
		try {
			assembleProcessArchive();

			generateWebModule(problemCollector);
			if (problemCollector.getProblemCount() > 0)
				return;

			deployProcessArchive(problemCollector);
			if (problemCollector.getProblemCount() > 0)
				return;

			deployWebModule();
		} catch (CoreException e) {
			// TODO this is unrelated to deployment, report it as a problem?
			problemCollector.add(new Problem(Problem.LEVEL_ERROR, e
					.getMessage(), e));
		} catch (Exception e) {
			problemCollector.add(new Problem(Problem.LEVEL_ERROR, e
					.getMessage(), e));
		}
		// TODO show collected problems in the eclipse problems view :-)
		if (problemCollector.getProblemCount() == 0) {
			System.out.println(file.getName() + "was deployed successfully.");
			setStatusMessage(file.getName() + "was deployed successfully.");
		} else {
			System.out.println("Unexpected error while deploying " + file.getName());
			setStatusMessage("Unexpected error while deploying " + file.getName());
		}
		System.setOut(saved);
	}
	
	protected void assembleProcessArchive() throws IOException, CoreException {
		ZipOutputStream processOutput = new ZipOutputStream(
				new FileOutputStream(getProcessFile()));
		try {
			container = file.getParent();
			System.out.println(container.getName());

			IResource[] resources = container.members();
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				String fileName = resource.getName();
				String fileExtension = resource.getFileExtension();

				if ("bpel".equals(fileExtension))
					addFile(processOutput, resource, fileName);
				else if ("wsdl".equals(fileExtension))
					addFile(processOutput, resource, fileName);
				else if (".web.xml".equals(fileName))
					addFile(processOutput, resource, "web.xml");
				else if (".webservices.xml".equals(fileName))
					addFile(processOutput, resource, "webservices.xml");
				else if (".bpel-application.xml".equals(fileName))
					addFile(processOutput, resource, "bpel-application.xml");
			}
		} finally {
			processOutput.close();
		}
	}

	private String getModuleName() {
		String fileName = file.getName();
		return fileName.substring(0, fileName.lastIndexOf('.'));
	}

	private File getProcessFile() {
		return new File(tmpDir, getModuleName() + "-process.zip");
	}

	private void addFile(ZipOutputStream zipOutput, IResource resource,
			String zipFileName) throws CoreException, IOException {
		if (!resource.exists())
			return;
		if (resource.getType() != IResource.FILE)
			return;

		InputStream resourceInput = ((IFile) resource).getContents();
		try {
			zipOutput.putNextEntry(new ZipEntry(zipFileName));

			byte[] buff = new byte[256];
			for (int read; (read = resourceInput.read(buff)) != -1;)
				zipOutput.write(buff, 0, read);
		} finally {
			resourceInput.close();
		}
	}

	protected void deployProcessArchive(ProblemHandler problemHandler) {
		ProcessDeployer processDeployer = new ProcessDeployer();
		processDeployer.setProblemHandler(problemHandler);
		processDeployer.deployProcess(getProcessFile());
	}

	protected void generateWebModule(ProblemHandler problemHandler)
			throws IOException {
		WebModuleGenerator moduleGenerator = new WebModuleGenerator();
		moduleGenerator.setOutputDirectory(tmpDir);
		moduleGenerator.setProblemHandler(problemHandler);
		moduleGenerator.setModuleName(getModuleName());
		moduleGenerator.generateWebModule(parseProcessArchive());
	}

	private BpelProcessDefinition parseProcessArchive() throws IOException {
		ZipInputStream processInput = new ZipInputStream(new FileInputStream(
				getProcessFile()));
		try {
			ProcessArchive processArchive = new ProcessArchive(processInput);
			return (BpelProcessDefinition) processArchive
					.parseProcessDefinition();
		} finally {
			processInput.close();
		}
	}

	protected void deployWebModule() throws Exception {
		ModuleDeployHelper moduleDeployHelper = new ModuleDeployHelper();
		moduleDeployHelper.deploy(new File(tmpDir, getModuleName() + ".war")
				.getAbsolutePath());
	}

	private IStatusLineManager getStatusLineManager() {
		return targetPart.getViewSite().getActionBars().getStatusLineManager();
	}

	private MessageConsole findConsole() {
		String consoleName = "org.jbpm.gd.bpel";
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (consoleName.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		MessageConsole myConsole = new MessageConsole(consoleName, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

}
