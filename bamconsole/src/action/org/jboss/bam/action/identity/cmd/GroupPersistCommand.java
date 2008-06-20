package org.jboss.bam.action.identity.cmd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmContext;
import org.jbpm.command.Command;
import org.jbpm.identity.Group;
import org.jbpm.identity.hibernate.IdentitySession;

/**
 * Command to persist a group into the system
 * 
 * @author Fady Matar
 * 
 */

public class GroupPersistCommand implements Command {

	private final Log log = LogFactory.getLog(this.getClass());

	private static final long serialVersionUID = 1L;

	private Group group;

	public void setGroup(Group group) {
		this.group = group;
	}

	public Group getGroup() {
		return this.group;
	}

	public Object execute(JbpmContext jbpmCtx) throws Exception {
		log.debug("Adding group");
		IdentitySession identitySession = new IdentitySession(jbpmCtx.getSession());
		identitySession.saveGroup(this.getGroup());
		return group;
	}
}
