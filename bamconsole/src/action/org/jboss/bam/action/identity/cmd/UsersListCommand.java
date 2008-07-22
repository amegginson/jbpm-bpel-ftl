package org.jboss.bam.action.identity.cmd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jboss.bam.util.UIUtils;
import org.jbpm.JbpmContext;
import org.jbpm.command.Command;
import org.jbpm.identity.User;
import org.jbpm.identity.hibernate.IdentitySession;

/**
 * Command to return the list of all the users available in the system
 * 
 * @author Fady Matar
 * 
 */

public class UsersListCommand implements Command {

	private final Log log = LogFactory.getLog(this.getClass());

	private static final long serialVersionUID = 1L;

	private String username;

	private String email;

	private Integer firstResult;

	private String order;

	public UsersListCommand() {
	}

	public UsersListCommand(String username, String email) {
		this.setUsername(username);
		this.setEmail(email);
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return this.username;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setFirstResult(Integer firstResult) {
		this.firstResult = firstResult;
	}

	public Integer getFirstResult() {
		return this.firstResult;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getOrder() {
		return this.order;
	}

	public Object execute(JbpmContext jbpmCtx) throws Exception {
		IdentitySession identitySession = new IdentitySession(jbpmCtx.getSession());
		Criteria criteria = identitySession.getSession().createCriteria(User.class);
		if (this.getFirstResult() != null) {
			criteria.setFirstResult(this.getFirstResult());
		}
		if (this.getOrder() != null) {
			String attribute = this.getOrder().trim().substring(0,
			    order.indexOf(" ", 0));
			if (this.getOrder().contains("asc")) {
				criteria.addOrder(Order.asc(attribute));
			}
			if (this.getOrder().contains("desc")) {
				criteria.addOrder(Order.desc(attribute));
			}
		}
		if (this.getUsername() != null) {
			criteria.add(Restrictions.like("name", "%" + this.getUsername() + "%"));
		}
		if (this.getEmail() != null) {
			criteria.add(Restrictions.like("email", "%" + this.getEmail() + "%"));
		}
		criteria.setMaxResults(UIUtils.MAX_RECORDS_PER_PAGE);
		return criteria.list();
	}
}
