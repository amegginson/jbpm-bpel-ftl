package org.jboss.bam.action.identity.cmd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmContext;
import org.jbpm.command.Command;
import org.jbpm.identity.User;
import org.jbpm.identity.hibernate.IdentitySession;

/**
 * Command to retrieve the user object given a user id.
 * 
 * @author Fady Matar
 * 
 */

public class LoadUserByIdCommand implements Command {

	private final Log log = LogFactory.getLog(this.getClass());

	private static final long serialVersionUID = 1L;

	private long userId;

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getUserId() {
		return this.userId;
	}

	public Object execute(JbpmContext jbpmCtx) throws Exception {
		log.debug("Retrieving user with id[" + this.getUserId() + "]");
		IdentitySession identitySession = new IdentitySession(jbpmCtx.getSession());
		return (User) identitySession.getUserById(this.getUserId());
	}
}
