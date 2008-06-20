package org.jboss.bam.action;

import javax.faces.application.FacesMessage;
import javax.persistence.Query;

import org.hibernate.Session;
import org.jboss.bam.AbstractDashboardQuery;
import org.jboss.bam.HqlDashboardQuery;
import org.jboss.bam.SqlDashboardQuery;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.log.Log;

/**
 * Persistence handler for the dashboard queries.
 * 
 * @author fmatar
 * 
 */
@Name("dashboardQueryHome")
public class DashboardQueryHome extends EntityHome<AbstractDashboardQuery> {
	@Logger
	Log log;
	
	private static final long serialVersionUID = 1L;
	
	public void setDashboardQueryId(Long id) {
		setId(id);
	}
	
	public Long getDashboardQueryId() {
		return (Long) getId();
	}
	
	@Override
	protected AbstractDashboardQuery createInstance() {
		AbstractDashboardQuery dashboardQuery = new AbstractDashboardQuery();
		return dashboardQuery;
	}
	
	public void wire() {
	}
	
	public boolean isWired() {
		return true;
	}
	
	public AbstractDashboardQuery getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}
	
	@Override
	public String persist() {
		if (checkUniqueName() && validateQl()) {
			if (this.getInstance().getQueryType().equalsIgnoreCase("sql")) {
				SqlDashboardQuery sqlQuery = new SqlDashboardQuery();
				sqlQuery.setId(this.getInstance().getId());
				sqlQuery.setName(this.getInstance().getName());
				sqlQuery.setQuery(this.getInstance().getQuery());
				sqlQuery.setDescription(this.getInstance().getDescription());
				sqlQuery.setChart(this.getInstance().getChart());
				this.setInstance(sqlQuery);
			} else {
				HqlDashboardQuery hqlQuery = new HqlDashboardQuery();
				hqlQuery.setId(this.getInstance().getId());
				hqlQuery.setName(this.getInstance().getName());
				hqlQuery.setQuery(this.getInstance().getQuery());
				hqlQuery.setDescription(this.getInstance().getDescription());
				hqlQuery.setChart(this.getInstance().getChart());
				this.setInstance(hqlQuery);
			}
			return super.persist();
		} else {
			FacesMessages.instance().add(
					new FacesMessage("Please correct the query information."));
			return "failed";
		}
	}
	
	@Override
	public String update() {
		if (checkUniqueName() && validateQl()) {
			return super.update();
		} else {
			FacesMessages.instance().add(
					new FacesMessage("Please correct the query information."));
			return "failed";
		}
	}
	
	public boolean validateQl() {
		Session session = ManagedJbpmContext.instance().getSession();
		try {
			if (getInstance().getQueryType().equalsIgnoreCase("hql")) {
				session.createQuery(this.getInstance().getQuery());
			} else {
				session.createSQLQuery(this.getInstance().getQuery()).list();
			}
			return true;
		} catch (Exception ex) {
			FacesMessages.instance().addToControl(
					"query",
					new FacesMessage("Your [" + this.getInstance().getQueryType()
							+ "] query is invalid. Please check your syntax."
							+ ex.getMessage()));
			ex.printStackTrace();
			return false;
		}
	}
	
	public boolean checkUniqueName() {
		String queryName = this.getInstance().getName();
		String query = "from AbstractDashboardQuery obj where lower(obj.name) = :queryName";
		try {
			Query uniqueObjectQuery = this.getPersistenceContext().createQuery(query);
			uniqueObjectQuery.setParameter("queryName", queryName.toLowerCase());
			AbstractDashboardQuery existingQuery = (AbstractDashboardQuery) uniqueObjectQuery
					.getSingleResult();
			if (existingQuery.getId() != this.getInstance().getId()) {
				FacesMessages.instance().addToControl("name",
						new FacesMessage("Another query with the same name exists."));
				return false;
			}
		} catch (Exception ex) {
			log.debug(ex.getCause() + " - " + ex.getMessage());
		}
		return true;
	}
	
	public void validate() {
		if(validateQl()) {
			FacesMessages.instance().add(new FacesMessage("Your query is valid"));
		} else {
			FacesMessages.instance().add(new FacesMessage("Your query contains error, please fix it"));
		}
	}
}
