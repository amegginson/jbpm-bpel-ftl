package org.jboss.bam.action;

import org.jboss.bam.AbstractDashboardQuery;
import org.jboss.bam.util.UIUtils;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.List;
import java.util.Arrays;

@Name("dashboardQueryList")
public class DashboardQueryList extends EntityQuery<AbstractDashboardQuery> {
	
	private static final long serialVersionUID = 1L;
	
	private static final String[] RESTRICTIONS = {
			"lower(dashboardQuery.name) like concat('%',lower(#{dashboardQueryList.dashboardQuery.name}),'%')",
			"lower(dashboardQuery.description) like concat('%',lower(#{dashboardQueryList.dashboardQuery.description}),'%')",
			"dashboardQuery.queryType like concat(#{dashboardQueryList.dashboardQuery.queryType})", };
	
	private AbstractDashboardQuery dashboardQuery = new AbstractDashboardQuery();
	
	@Override
	public String getEjbql() {
		return "select dashboardQuery from AbstractDashboardQuery dashboardQuery";
	}
	
	@Override
	public Integer getMaxResults() {
		return UIUtils.MAX_RECORDS_PER_PAGE;
	}
	
	public AbstractDashboardQuery getDashboardQuery() {
		return dashboardQuery;
	}
	
	@Override
	public List<String> getRestrictions() {
		return Arrays.asList(RESTRICTIONS);
	}
	
}
