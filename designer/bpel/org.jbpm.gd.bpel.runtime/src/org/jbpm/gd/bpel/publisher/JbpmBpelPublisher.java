/*******************************************************************************
 * Copyright (c) 2006 University College London Software Systems Engineering
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Bruno Wassermann - initial API, implementation, subsequent bug fixes
 *  Liang (Ben) Chen - BPEL 2.0 to 1.1 conversion code
 *******************************************************************************/
package org.jbpm.gd.bpel.publisher;

import org.eclipse.bpel.runtimes.publishers.GenericBPELPublisher;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModuleArtifact;

public class JbpmBpelPublisher extends GenericBPELPublisher {

	public IStatus[] unpublish(IProgressMonitor monitor) {
		return null;
	}

	public IStatus[] publish(IModuleArtifact[] resources, IProgressMonitor monitor) {

//		IModule[] modules = super.getModule();
//		resources = new IModuleArtifact[modules.length];
//		for (int i=0; i<modules.length; i++) {
//			IModule module = modules[i];
//			String moduleId = module.getId();
//			String bpelFileRelativePathString = moduleId.split(">>")[1];			
//			IPath path = new Path(bpelFileRelativePathString);
//			IFile bpelFile = module.getProject().getFile(path);
//			BPELModuleArtifact moduleArtifact = new BPELModuleArtifact(module, bpelFile);
//			resources[i] = moduleArtifact;
//		}
		
		System.out.println("publishing a bpel file");
		
		IStatus[] result = new Status[1];
		
//		// TODO maybe check that validation status of resource in editor is okay
//		// before attempting to publish
//		
//		try {
//			for (int i=0; i<resources.length; i++) {
//				BprGenerator bprGen = new BprGenerator(
//						super.getHost(), 
//						super.getHttpPort());
//				File bprFile = bprGen.generateBpr(resources[i]);				
//			}
//		} catch (IllegalArgumentException e) {

//			IStatus status = new Status(
//					IStatus.ERROR, 
//					ActiveBPELRuntimePlugin.PLUGIN_ID, 
//					0, 
//					"Error during deployment: " + e.getMessage(), 
//					e);
//			result[0] = status;
//			// TODO need to do some logging here!
//			Logger.getLogger().logError(e);
//			return result;
//			
//		} catch (Exception e) {
//			Logger.getLogger().logError(e);
//		}
		
		result[0] = new Status(
				IStatus.OK, 
				"org.jbpm.gd.bpel.runtime", 
				1, 
				"Deployment successful", 
				null);
		return result;

	} 

}
