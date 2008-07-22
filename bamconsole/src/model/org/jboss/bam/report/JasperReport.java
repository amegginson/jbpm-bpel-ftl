package org.jboss.bam.report;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
@DiscriminatorValue("JASPER")
public class JasperReport extends Report {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		JasperReport result = (JasperReport) obj;
		return this.getName().equals(result.getName())
		    && this.getDescription().equals(result.getDescription())
		    && this.getSummary().equals(result.getSummary())
		    && this.getReportFile().equals(result.getReportFile());
	}

	private List<SubJasperReport> subReports = new ArrayList<SubJasperReport>();

	public void setSubReports(List<SubJasperReport> subReports) {
		this.subReports = subReports;
	}

	@OneToMany(mappedBy = "report", cascade = CascadeType.ALL)
	public List<SubJasperReport> getSubReports() {
		return this.subReports;
	}
}
