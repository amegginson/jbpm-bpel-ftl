package org.jboss.bam.action.identity.cmd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jboss.bam.util.UIUtils;
import org.jbpm.JbpmContext;
import org.jbpm.command.Command;
import org.jbpm.identity.Group;
import org.jbpm.identity.hibernate.IdentitySession;

/**
 * Command to return the list of all the groups available in the system
 * 
 * @author Fady Matar
 * 
 */

public class GroupsListCommand implements Command {

	private final Log log = LogFactory.getLog(this.getClass());

	private static final long serialVersionUID = 1L;

	private String name;

	private String type;

	private Integer firstResult;

	private String order;

	public GroupsListCommand() {
	}

	public GroupsListCommand(String name, String type) {
		this.setName(name);
		this.setType(type);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
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
		Criteria criteria = identitySession.getSession()
		    .createCriteria(Group.class);
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
		if (this.getName() != null) {
			criteria.add(Restrictions.like("name", "%" + this.getName() + "%"));
		}
		if (this.getType() != null && !this.getType().equals(" ")) {
			criteria.add(Restrictions.like("type", "%" + this.getType() + "%"));
		}
		criteria.setMaxResults(UIUtils.MAX_RECORDS_PER_PAGE);
		return criteria.list();
	}
}
