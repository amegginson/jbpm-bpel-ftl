package org.jboss.bam.action.identity;

import java.util.List;

import org.hibernate.Criteria;
import org.jboss.bam.action.identity.cmd.GroupsListCommand;
import org.jboss.bam.util.UIUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.log.Log;
import org.jbpm.JbpmContext;
import org.jbpm.identity.Group;
import org.jbpm.identity.hibernate.IdentitySession;

@Name("groupsList")
public class GroupList {

	private static final long serialVersionUID = 1L;

	@Logger
	private Log log;

	@In(required = false, create = true)
	private String name = "";

	@In(required = false, create = true)
	private String type = "";

	private List<Group> resultsList;

	@In(required = false)
	private Integer firstResult;

	@In(required = false)
	private String order;

	public void setOrder(String order) {
		this.order = order;
	}

	public String getOrder() {
		return this.order;
	}

	public void setFirstResult(Integer firstResult) {
		this.firstResult = firstResult;
	}

	public Integer getFirstResult() {
		return this.firstResult;
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
	@SuppressWarnings("unchecked")
	public List<Group> getGroupsList() {
		GroupsListCommand cmd = new GroupsListCommand();
		if (this.getName() != null) {
			cmd.setName(this.getName());
		}
		if (this.getType() != null) {
			cmd.setType(this.getType());
		}
		cmd.setFirstResult(this.getFirstResult());
		cmd.setOrder(this.getOrder());
		JbpmContext jbpmCtx = ManagedJbpmContext.instance().getJbpmConfiguration()
				.getCurrentJbpmContext();
		try {
			resultsList = (List<Group>) cmd.execute(jbpmCtx);
			return resultsList;
		} catch (Exception ex) {
			log.error("Error retrieving users list");
			ex.printStackTrace();
			return null;
		}
	}

	public boolean isNextExists() {
		return (resultsList != null)
				&& (getRecordsCount() > UIUtils.MAX_RECORDS_PER_PAGE)
				&& (getRecordsCount() - getNextFirstResult() > 0);
	}

	public boolean isPreviousExists() {
		return getFirstResult() != null && getFirstResult() != 0;
	}

	public int getPreviousFirstResult() {
		Integer fr = getFirstResult();
		Integer mr = UIUtils.MAX_RECORDS_PER_PAGE;
		return mr >= (fr == null ? 0 : fr) ? 0 : fr - mr;
	}

	public int getNextFirstResult() {
		Integer fr = getFirstResult();
		return (fr == null ? 0 : fr) + UIUtils.MAX_RECORDS_PER_PAGE;
	}

	public Long getLastFirstResult() {
		Integer pc = getPageCount();
		return pc == null ? null : (pc.longValue() - 1)
				* UIUtils.MAX_RECORDS_PER_PAGE;
	}

	public Integer getPageCount() {
		int rc = getRecordsCount();
		int mr = UIUtils.MAX_RECORDS_PER_PAGE.intValue();
		int pages = rc / mr;
		return rc % mr == 0 ? pages : pages + 1;
	}

	public Integer getRecordsCount() {
		JbpmContext jbpmCtx = ManagedJbpmContext.instance().getJbpmConfiguration()
				.getCurrentJbpmContext();
		IdentitySession identitySession = new IdentitySession(jbpmCtx.getSession());
		Criteria criteria = identitySession.getSession().createCriteria(Group.class);
		return criteria.list().size();
	}
}
