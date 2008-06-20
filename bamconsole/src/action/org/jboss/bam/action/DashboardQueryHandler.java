package org.jboss.bam.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.bam.AbstractDashboardQuery;
import org.jboss.bam.HqlDashboardQuery;
import org.jboss.bam.SqlDashboardQuery;
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

@Name("DashboardQueryHandler")
@Scope(ScopeType.EVENT)
public class DashboardQueryHandler {
	
	@Logger
	private Log log;
	
	@In("entityManager")
	EntityManager em;
	
	@Out(required = false)
	DynamicChart chart;
	
	@Out(required = false)
	private List<DynamicChart> chartsList;
	
	@Out(required = false)
	private List<?> resultsTable;
	
	@Out(required = false)
	private List<AbstractDashboardQuery> queriesList;
	
	public void setChartsList(ArrayList<DynamicChart> chartsList) {
		this.chartsList = chartsList;
	}
	
	public List<DynamicChart> getChartsList() {
		return this.chartsList;
	}
	
	public List<AbstractDashboardQuery> getQueriesList() {
		return this.queriesList;
	}
	
	public DynamicChart getChart() {
		return this.chart;
	}
	
	public List<?> getResultsTable() {
		return this.resultsTable;
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
	
	// Categories is useful for grouping a number of charts together.
	public void addCategory(DynamicChart chart, String categoryId,
			Number categoryValue) {
		chart.getCategories().add(categoryId);
		for (Data set : chart.getData()) {
			set.addValue(categoryId, categoryValue);
		}
	}
	
	private AbstractDashboardQuery loadQueryByName(String queryName) {
		try {
			javax.persistence.Query q = em
					.createQuery("from AbstractDashboardQuery query where lower(query.name) = :queryName ");
			q.setParameter("queryName", queryName.toLowerCase().trim());
			return (AbstractDashboardQuery) q.getSingleResult();
		} catch (Exception ex) {
			log.debug("query not found :" + queryName);
			FacesMessages.instance().addToControl("name",
					new FacesMessage("Unable to render query"));
			return null;
		}
	}
	
	/**
	 * Render all the available queries into the dashboard
	 */
	@SuppressWarnings("unchecked")
	public void renderAllQueries() {
		chartsList = new ArrayList<DynamicChart>();
		Query allDashboardQueries = em.createQuery("from HqlDashboardQuery query");
		List<HqlDashboardQuery> allHqlQueries = allDashboardQueries.getResultList();
		this.queriesList = new ArrayList<AbstractDashboardQuery>();
		for (HqlDashboardQuery hqlDashboardQuery : allHqlQueries) {
			AbstractDashboardQuery query = this.loadQueryByName(hqlDashboardQuery.getName());
			if (!query.getQuery().contains(":")) {
				this.getQueriesList().add(query);
				JbpmContext ctx = ManagedJbpmContext.instance();
				Iterator<?> results = ctx.getSession().createQuery(query.getQuery())
						.list().iterator();
				DynamicChart currentChart = new DynamicChart();
				currentChart.setTitle(query.getName());
				currentChart.setIs3d(query.getChart().getThreeDimensional());
				currentChart.setChartType(query.getChart().getChartType());
				currentChart.setWidth(query.getChart().getWidth());
				currentChart.setHeight(query.getChart().getHeight());
				currentChart.setDomainAxisLabel(query.getChart().getDomainLabel());
				currentChart.setRangeAxisLabel(query.getChart().getRangeLabel());
				currentChart.setDescription(query.getDescription());
				currentChart.getCategories().add(query.getName());
				while (results.hasNext()) {
					Object[] row = (Object[]) results.next();
					String processName = (String) row[0];
					Number count = (Number) row[1];
					addSeries(currentChart, processName, count);
				}
				this.getChartsList().add(currentChart);
			}
		}
	}
	
	/**
	 * Render the dashboard query and returns the result rendered as a graph. Not
	 * the the query must return results of type <String, Number> i.e. The first
	 * value of each row is a string value, while the second object is numerical.
	 * 
	 * @param dashboardQuery
	 * @param queryParameters
	 * @return
	 */
	public void renderChart(AbstractDashboardQuery dashboardQuery, Map<String, String> queryParameters) {
		this.resetChart();
		if (dashboardQuery == null) {
			FacesMessages.instance().add(
					"The system cannot find the query specified. Make sure it exists");
		} else {
			org.hibernate.Query query = null;
			JbpmContext ctx = ManagedJbpmContext.instance();
			if (dashboardQuery instanceof HqlDashboardQuery) {
				query = ctx.getSession().createQuery(dashboardQuery.getQuery());
			}
			if (dashboardQuery instanceof SqlDashboardQuery) {
				query = ctx.getSession().createSQLQuery(dashboardQuery.getQuery());
			}
			Set<String> set = queryParameters.keySet();
			for (String key : set) {
				String value = queryParameters.get(key);
				query.setParameter(key, value);
			}
			List<?> resultsList = query.list();
			chart = new DynamicChart();
			chart.setTitle(dashboardQuery.getName());
			Iterator<?> iterator = resultsList.iterator();
			chart.getCategories().add(dashboardQuery.getName());
			while (iterator.hasNext()) {
				Object[] row = (Object[]) iterator.next();
				String label = (String) row[0];
				Number value = (Number) row[1];
				addSeries(chart, label, value);
			}
		}
	}
	
	public void renderChart(AbstractDashboardQuery dashboardQuery) {
		this.resetChart();
		if (dashboardQuery == null) {
			FacesMessages.instance().add(
					"The system cannot find the query specified. Make sure it exists");
		} else {
			org.hibernate.Query query = null;
			JbpmContext ctx = ManagedJbpmContext.instance();
			if (dashboardQuery instanceof HqlDashboardQuery) {
				query = ctx.getSession().createQuery(dashboardQuery.getQuery());
			}
			if (dashboardQuery instanceof SqlDashboardQuery) {
				query = ctx.getSession().createSQLQuery(dashboardQuery.getQuery());
			}
			List<?> resultsList = query.list();
			chart = new DynamicChart();
			chart.setTitle(dashboardQuery.getName());
			Iterator<?> iterator = resultsList.iterator();
			chart.getCategories().add(dashboardQuery.getName());
			while (iterator.hasNext()) {
				Object[] row = (Object[]) iterator.next();
				String label = (String) row[0];
				Number value = (Number) row[1];
				addSeries(chart, label, value);
			}
		}
	}
	
	public void renderChart(String queryName) {
		this.resetChart();
		AbstractDashboardQuery dashboardQuery = this.loadQueryByName(queryName);
		if (dashboardQuery != null) {
			this.renderChart(dashboardQuery);
		} else {
			FacesMessages.instance().add(
					"The system cannot find the query [" + queryName
							+ "]. Make sure it exists");
		}
		
	}
	
	public void renderChart(String queryName, Map<String, String> queryParameters) {
		this.resetChart();
		AbstractDashboardQuery dashboardQuery = this.loadQueryByName(queryName);
		if (dashboardQuery != null) {
			this.renderChart(dashboardQuery, queryParameters);
		} else {
			FacesMessages.instance().add(
					"The system cannot find the query [" + queryName
							+ "]. Make sure it exists");
		}
	}
	
	public void renderTable(AbstractDashboardQuery dashboardQuery) {
		this.resetResultsTable();
		org.hibernate.Query query = null;
		JbpmContext ctx = ManagedJbpmContext.instance();
		if (dashboardQuery instanceof HqlDashboardQuery) {
			query = ctx.getSession().createQuery(dashboardQuery.getQuery());
		}
		if (dashboardQuery instanceof SqlDashboardQuery) {
			query = ctx.getSession().createSQLQuery(dashboardQuery.getQuery());
		}
		resultsTable = query.list();
	}
	
	public void renderTable(AbstractDashboardQuery dashboardQuery,
			Map<String, String> queryParameters) {
		this.resetResultsTable();
		org.hibernate.Query query = null;
		JbpmContext ctx = ManagedJbpmContext.instance();
		if (dashboardQuery instanceof HqlDashboardQuery) {
			query = ctx.getSession().createQuery(dashboardQuery.getQuery());
		}
		if (dashboardQuery instanceof SqlDashboardQuery) {
			query = ctx.getSession().createSQLQuery(dashboardQuery.getQuery());
		}
		Set<String> set = queryParameters.keySet();
		for (String key : set) {
			String value = queryParameters.get(key);
			query.setParameter(key, value);
		}
		this.resultsTable = query.list();
	}
	
	public void renderTable(String queryName) {
		AbstractDashboardQuery dashboardQuery = this.loadQueryByName(queryName);
		this.renderTable(dashboardQuery);
	}
	
	public void renderTable(String queryName, Map<String, String> queryParameters) {
		AbstractDashboardQuery dashboardQuery = this.loadQueryByName(queryName);
		this.renderTable(dashboardQuery, queryParameters);
	}
	
	public void renderJbpmNamedQuery(String queryName) {
		JbpmContext ctx = ManagedJbpmContext.instance();
		org.hibernate.Query query = ctx.getSession().getNamedQuery(queryName);
		this.resultsTable = query.list();
	}
	
	public String renderSingleResultNamedJbpmNamedQuery(String queryName) {
		JbpmContext ctx = ManagedJbpmContext.instance();
		org.hibernate.Query query = ctx.getSession().getNamedQuery(queryName);
		return String.valueOf(query.uniqueResult());
	}
	
	private void resetChart() {
		this.chart = null;
	}
	
	public void resetResultsTable() {
		this.resultsTable = null;
	}
}