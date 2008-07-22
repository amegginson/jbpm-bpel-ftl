package org.jboss.bam.action;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.persistence.Query;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;

import org.jboss.bam.report.BirtReport;
import org.jboss.bam.report.JasperReport;
import org.jboss.bam.report.Report;
import org.jboss.bam.report.SubJasperReport;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.log.Log;

/**
 * Persistence handler for the report.
 * 
 * @author Fady Matar
 * 
 */
@Name("reportHome")
public class ReportHome extends EntityHome<Report> {
	@Logger
	Log log;

	private static final long serialVersionUID = 1L;

	private byte[] subReportFile;

	private String subReportName;

	public void setSubReportFile(byte[] subReportFile) {
		this.subReportFile = subReportFile;
	}

	public byte[] getSubReportFile() {
		return this.subReportFile;
	}

	public void setSubReportName(String subReportName) {
		this.subReportName = subReportName;
	}

	public String getSubReportName() {
		return this.subReportName;
	}

	public void setReportId(Long id) {
		setId(id);
	}

	public Long getReportId() {
		return (Long) getId();
	}

	@Override
	protected Report createInstance() {
		Report report = new Report();
		return report;
	}

	public void wire() {
	}

	public boolean isWired() {
		return true;
	}

	public Report getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	@Override
	public String persist() {
		if (checkUniqueName() && checkReport()) {
			if (this.getInstance().getReportType().equalsIgnoreCase("jasper")) {
				JasperReport report = new JasperReport();
				report.setId(this.getInstance().getId());
				report.setName(this.getInstance().getName());
				report.setSummary(this.getInstance().getSummary());
				report.setDescription(this.getInstance().getDescription());
				report.setReportFile(this.getInstance().getReportFile());
				this.setInstance(report);
			} else {
				BirtReport report = new BirtReport();
				report.setId(this.getInstance().getId());
				report.setName(this.getInstance().getName());
				report.setSummary(this.getInstance().getSummary());
				report.setDescription(this.getInstance().getDescription());
				report.setReportFile(this.getInstance().getReportFile());
				this.setInstance(report);
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
		if (checkUniqueName()) {
			if (this.getInstance().getReportFile().length == 0) {
				byte[] reportFile = getReportFile();
				this.getInstance().setReportFile(reportFile);
				return super.update();
			} else {
				if (checkReport()) {
					return super.update();
				} else {
					return "failed";
				}
			}
		} else {
			FacesMessages.instance().add(
			    new FacesMessage("Please correct the report information."));
			return "failed";
		}
	}

	public void addSubReport() {
		SubJasperReport subReport = new SubJasperReport();
		subReport.setSubReportFile(this.getSubReportFile());
		if (checkSubReport(subReportFile)) {
			JasperReport parentReport = this.getEntityManager().find(
			    JasperReport.class, this.getInstance().getId());
			subReport.setReport(parentReport);
			subReport.setName(this.getSubReportName());
			this.getEntityManager().persist(subReport);
		} else {
			FacesMessages.instance().add(
			    new FacesMessage("Please correct the sub report information."));
		}
	}

	public boolean checkUniqueName() {
		String queryName = this.getInstance().getName();
		String query = "from Report obj where lower(obj.name) = :reportName";
		try {
			Query uniqueObjectQuery = this.getPersistenceContext().createQuery(query);
			uniqueObjectQuery.setParameter("reportName", queryName.toLowerCase());
			Report existingReport = (Report) uniqueObjectQuery.getSingleResult();
			if (existingReport.getId() != this.getInstance().getId()) {
				FacesMessages.instance().addToControl("name",
				    new FacesMessage("Another report with the same name exists."));
				return false;
			}
		} catch (Exception ex) {
			log.debug("Report name is unique");
		}
		return true;
	}

	public byte[] getReportFile() {
		try {
			String queryStr = "select report.reportFile from Report report where report.id = :reportId";
			Query query = this.getPersistenceContext().createQuery(queryStr);
			query.setParameter("reportId", this.getId());
			byte[] result = (byte[]) query.getSingleResult();
			return result;
		} catch (Exception ex) {
			return null;
		}
	}

	public boolean checkReport() {
		// Start with compiling the sub-reports then compile the parent report
		JasperReport jsReport = (JasperReport) this.getInstance();
		ArrayList<SubJasperReport> subReports = new ArrayList<SubJasperReport>(
		    jsReport.getSubReports());
		for (SubJasperReport subJasperReport : subReports) {
			if (!checkSubReport(subJasperReport.getSubReportFile())) {
				FacesMessages.instance().add(
				    new FacesMessage("Error compiling the report"));
				return false;
			}
		}
		try {
			byte[] report = this.getInstance().getReportFile();
			ByteArrayInputStream reportInputStream = new ByteArrayInputStream(report);
			JasperCompileManager.compileReport(reportInputStream);
			return true;
		} catch (JRException ex) {
			ex.printStackTrace();
			FacesMessages.instance().add(
			    new FacesMessage("Error compiling the report"));
			return false;
		}
	}

	public boolean checkSubReport(byte[] subReportByte) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(subReportByte);
			JasperCompileManager.compileReport(bais);
			return true;
		} catch (JRException ex) {
			return false;
		}
	}

	public List<SubJasperReport> getJasperReports() {
		return getInstance() == null ? null : this.getEntityManager().find(
		    JasperReport.class, this.getInstance().getId()).getSubReports();
	}
}
