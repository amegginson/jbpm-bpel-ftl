package org.jboss.bam.action.identity.cmd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmContext;
import org.jbpm.command.Command;
import org.jbpm.identity.User;
import org.jbpm.identity.hibernate.IdentitySession;

/**
 * Command to persist a user into the system
 * 
 * @author Fady Matar
 * 
 */

public class UserPersistCommand implements Command {

	private final Log log = LogFactory.getLog(this.getClass());

	private static final long serialVersionUID = 1L;

	private String name;

	private String email;

	private String password;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return this.email;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return this.password;
	}

	public Object execute(JbpmContext jbpmCtx) throws Exception {
		log.debug("Adding user");
		IdentitySession identitySession = new IdentitySession(jbpmCtx.getSession());
		User user = new User(this.getName());
		user.setEmail(this.getEmail());
		user.setPassword(this.getPassword());
		identitySession.saveUser(user);
		return user;
	}
}
