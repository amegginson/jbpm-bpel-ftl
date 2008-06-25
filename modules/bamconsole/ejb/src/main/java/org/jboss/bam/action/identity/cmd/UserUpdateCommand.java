package org.jboss.bam.action.identity.cmd;

import org.jbpm.JbpmContext;
import org.jbpm.command.Command;
import org.jbpm.identity.User;
import org.jbpm.identity.hibernate.IdentitySession;

public class UserUpdateCommand implements Command {

	private static final long serialVersionUID = 1L;

	private User user;

	public void setUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return this.user;
	}

	public Object execute(JbpmContext jbpmContext) throws Exception {
		IdentitySession identitySession = new IdentitySession(jbpmContext.getSession());
		identitySession.getSession().update(user);
		return null;
	}

}
