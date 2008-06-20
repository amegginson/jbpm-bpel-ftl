package org.jboss.bam.action;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.persistence.EntityManager;

import org.jboss.bam.AbstractDashboardQuery;
import org.jboss.bam.dashboard.ui.Data;
import org.jboss.bam.dashboard.ui.DynamicChart;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jbpm.JbpmContext;

@Name("ProcessStatisticsHandler")
@Scope(ScopeType.PAGE)
public class ProcessStatisticsQueryHandler {
	
	@Logger
	private Log log;
	
	@In("entityManager")
	EntityManager em;
	
	@In(required = false)
	@Out(required = false)
	private Date startDate;
	
	@In(required = false)
	@Out(required = false)
	private Date endDate;
	
	@In(required = false)
	@Out(required = false)
	private String processName = "";
	
	@Out(required = false)
	private List<?> resultsList;
	
	@Out(required = false)
	private DynamicChart chart;
	
	@Out(required = false)
	private String result;
	
	public DynamicChart getChart() {
		return this.chart;
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public Date getStartDate() {
		if (startDate == null) {
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.add(Calendar.DATE, -14);
			startDate = calendar.getTime();
		}
		return this.startDate;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public Date getEndDate() {
		if (endDate == null) {
			endDate = Calendar.getInstance().getTime();
		}
		return this.endDate;
	}
	
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	
	public String getProcessName() {
		return this.processName;
	}
	
	public List<?> getResultsList() {
		return this.resultsList;
	}
	
	private void setResultsList(List<?> resultsList) {
		this.resultsList = resultsList;
	}
	
	public String getResult() {
		return this.result;
	}
	
	public void setResult(String result) {
		this.result = result;
	}
	
	public void renderTable(String queryName) {
		this.reset();
		if (this.getStartDate().compareTo(this.getEndDate()) > 0) {
			FacesMessages
					.instance()
					.addToControl(
							"endDate",
							new FacesMessage(
									"Start date cannot occur after End date, please correct the date parameters"));
			return;
		} else {
			AbstractDashboardQuery dashboardQuery = loadQueryByName(queryName);
			if (dashboardQuery != null) {
				JbpmContext ctx = ManagedJbpmContext.instance();
				org.hibernate.Query query = ctx.getSession().createQuery(
						dashboardQuery.getQuery());
				query.setParameter("processName", this.getProcessName());
				query.setParameter("startDate", this.getStartDate());
				query.setParameter("endDate", this.getEndDate());
				this.setResultsList(query.list());
			} else {
				FacesMessages.instance().add(
						"The system cannot find the query [" + queryName
								+ "]. Make sure it exists");
			}
		}
	}
	
	public void renderChart(String queryName) {
		this.reset();
		if (this.getStartDate().compareTo(this.getEndDate()) > 0) {
			FacesMessages
					.instance()
					.addToControl(
							"endDate",
							new FacesMessage(
									"Start date cannot occur after End date, please correct the date parameters"));
			return;
		} else {
			AbstractDashboardQuery dashboardQuery = loadQueryByName(queryName);
			if (dashboardQuery != null) {
				JbpmContext ctx = ManagedJbpmContext.instance();
				org.hibernate.Query query = ctx.getSession().createQuery(
						dashboardQuery.getQuery());
				query.setParameter("processName", this.getProcessName());
				query.setParameter("startDate", this.getStartDate());
				query.setParameter("endDate", this.getEndDate());
				this.setResultsList(query.list());
				
				chart = new DynamicChart();
				chart.setTitle(dashboardQuery.getName());
				chart.setIs3d(dashboardQuery.getChart().getThreeDimensional());
				chart.setChartType(dashboardQuery.getChart().getChartType());
				chart.setWidth(dashboardQuery.getChart().getWidth());
				chart.setHeight(dashboardQuery.getChart().getHeight());
				chart.setDomainAxisLabel(dashboardQuery.getChart().getDomainLabel());
				chart.setRangeAxisLabel(dashboardQuery.getChart().getRangeLabel());
				chart.setDescription(dashboardQuery.getDescription());
				chart.getCategories().add(dashboardQuery.getName());
				
				Iterator<?> iterator = resultsList.iterator();
				
				while (iterator.hasNext()) {
					Object[] row = (Object[]) iterator.next();
					String label = (String) row[0];
					Number value = (Number) row[1];
					addSeries(chart, label, value);
				}
			} else {
				FacesMessages.instance().add(
						"The system cannot find the query [" + queryName
								+ "]. Make sure it exists");
			}
		}
	}
	
	public String renderResult(String queryName) {
		AbstractDashboardQuery dashboardQuery = loadQueryByName(queryName);
		if (dashboardQuery != null) {
			JbpmContext ctx = ManagedJbpmContext.instance();
			org.hibernate.Query query = ctx.getSession().createQuery(
					dashboardQuery.getQuery());
			if (dashboardQuery.getQuery().contains(":processName")) {
				query.setParameter("processName", this.getProcessName());
			}
			if (dashboardQuery.getQuery().contains(":startDate")) {
				query.setParameter("startDate", this.getStartDate());
			}
			if (dashboardQuery.getQuery().contains(":endDate")) {
				query.setParameter("endDate", this.getEndDate());
			}
			return String.valueOf(query.uniqueResult());
			
		} else {
			FacesMessages.instance().add(
					"The system cannot find the query [" + queryName
							+ "]. Make sure it exists");
			return "";
		}
	}
	
	private AbstractDashboardQuery loadQueryByName(String queryName) {
		try {
			javax.persistence.Query q = em
					.createQuery("from AbstractDashboardQuery query where lower(query.name) = :queryName ");
			q.setParameter("queryName", queryName.toLowerCase());
			return (AbstractDashboardQuery) q.getSingleResult();
		} catch (Exception ex) {
			log.debug("query not found :" + queryName);
			ex.printStackTrace();
			return null;
		}
	}
	
	private void addSeries(DynamicChart chart, String seriesId,
			Number categoryValue) {
		Data set = new Data();
		set.setId(seriesId);
		for (String category : chart.getCategories()) {
			set.addValue(category, categoryValue);
		}
		chart.getData().add(set);
	}
	
	private void resetChart() {
		this.chart = null;
	}
	
	private void resetResultsList() {
		this.resultsList = null;
	}
	
	public void reset() {
		this.resetChart();
		this.resetResultsList();
	}
	
	public void populateData() {
	}
	
}