package org.jboss.bam.action;

import java.util.Arrays;
import java.util.List;

import org.jboss.bam.report.Report;
import org.jboss.bam.util.UIUtils;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

@Name("reportList")
public class ReportList extends EntityQuery<Report> {

	private static final long serialVersionUID = 1L;

	private static final String[] RESTRICTIONS = {
			"lower(report.name) like concat('%',lower(#{reportList.report.name}),'%')",
			"lower(report.description) like concat('%',lower(#{reportList.report.description}),'%')",
			"lower(report.summary) like concat('%',lower(#{reportList.report.summary}),'%')",
			"report.reportType like concat(#{reportList.report.reportType})", };

	private Report report = new Report();

	@Override
	public String getEjbql() {
		return "select report from Report report";
	}

	@Override
	public Integer getMaxResults() {
		return UIUtils.MAX_RECORDS_PER_PAGE;
	}

	public Report getReport() {
		return report;
	}

	@Override
	public List<String> getRestrictions() {
		return Arrays.asList(RESTRICTIONS);
	}

}
