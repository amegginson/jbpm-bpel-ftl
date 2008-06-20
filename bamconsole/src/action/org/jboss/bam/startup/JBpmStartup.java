package org.jboss.bam.startup;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.bpm.Jbpm;
import org.jboss.seam.log.Log;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.ScopeType;

@Scope(ScopeType.APPLICATION)
@BypassInterceptors
@Startup
@Name("jBPMInitializer")
public class JBpmStartup extends Jbpm {
	
	@Logger
	Log log;
	
	@Create
	public void startup() {
		if (this.getProcessDefinitions() == null) {
			try {
				log.debug("deploying jBPM database");
				this.getJbpmConfiguration().createJbpmContext().getSession();
				log.debug("jBPM closing context");
			} catch (Exception ex) {
				log.error("Error deploying jBPM database");
				ex.printStackTrace();
			}
		}
	}
}
