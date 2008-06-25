package org.jboss.bam;

import java.util.Iterator;
import java.util.Set;

import org.jboss.bam.command.DeployProcessCommand;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.identity.Membership;
import org.jbpm.identity.User;
import org.jbpm.identity.hibernate.IdentitySession;

@Name("authenticator")
public class Authenticator {
	@Logger
	Log log;
	
	@In
	Identity identity;
	
	public boolean authenticate() {
//		log.info("authenticating #0", identity.getUsername());
//		identity.addRole("admin");
//		return true;
		
		return this.jBPMIdentityAuthenticate();
	}
	
	/**
	 * Authenticate against the jBPM identity component.
	 * 
	 * @return
	 */
	private boolean jBPMIdentityAuthenticate() {
		boolean status = false;
		log.info("authenticating #0", identity.getUsername());
		JbpmContext ctx = ManagedJbpmContext.instance();
		IdentitySession identitySession = new IdentitySession(ctx.getSession());
		User user = null;
		try {
			user = identitySession.getUserByName(identity.getUsername());
			try {
				Set<Membership> membershipList = user.getMemberships();
				Iterator<Membership> i = membershipList.iterator();
				while (i.hasNext()) {
					Membership m = i.next();
					identity.addRole(m.getRole());
				}
			} catch (Exception ex) {
				log.info("Authentication failed");
				return false;
			}
		} catch (Exception ex) {
			log.debug("username [" + identity.getUsername() + "] does not exist");
			try {
				throw ex;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!user.getPassword().equals(identity.getPassword())) {
			status = false;
		} else {
			status = true;
		}
		return status;
	}
	
	// Demo on how to use the command pattern framework from within the
	// application
	private void testDeployCommand() {
		JbpmContext ctx = ManagedJbpmContext.instance();
		ProcessDefinition procDef = ProcessDefinition
				.parseXmlString("<process-definition name='simple'> "
						+ "<start-state name='start'>" + "<transition to='s' /> "
						+ "</start-state>" + "<state name='s'> "
						+ "<transition to='end' />" + "</state>"
						+ "<end-state name='end' />" + "</process-definition>");
		DeployProcessCommand deployProcessCmd = new DeployProcessCommand(procDef);
		deployProcessCmd.execute(ctx);
	}
}
