package org.jboss.bam.action.identity.cmd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmContext;
import org.jbpm.command.Command;
import org.jbpm.identity.Group;
import org.jbpm.identity.hibernate.IdentitySession;

/**
 * Command to return the list of all the users available in the system
 * 
 * @author Fady Matar
 * 
 */

public class LoadGroupByIdCommand implements Command {

	private final Log log = LogFactory.getLog(this.getClass());

	private static final long serialVersionUID = 1L;

	private long groupId;

	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}

	public long getGroupId() {
		return this.groupId;
	}

	public Object execute(JbpmContext jbpmCtx) throws Exception {
		IdentitySession identitySession = new IdentitySession(jbpmCtx.getSession());
		try {
			return identitySession.getSession().load(Group.class, this.getGroupId());
		} catch (Exception ex) {
			log.error("Error retrieving group of group id -" + this.getGroupId());
			ex.printStackTrace();
			return null;
		}
	}
}
