package org.jboss.bam.report;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.validator.NotNull;

@Entity
@Table(name = "jasper_sub_reports")
public class SubJasperReport {
	private long id;

	private String name;

	private byte[] subReportFile;

	private JasperReport report;

	public void setId(long id) {
		this.id = id;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	@NotNull
	public long getId() {
		return this.id;
	}

	public void setSubReportFile(byte[] subReportFile) {
		this.subReportFile = subReportFile;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "name")
	@NotNull
	public String getName() {
		return this.name;
	}

	@Lob
	@Column(name = "sub_report_file")
	@NotNull
	public byte[] getSubReportFile() {
		return this.subReportFile;
	}

	public void setReport(JasperReport report) {
		this.report = report;
	}

	@ManyToOne
	@JoinColumn(name = "report_id")
	public JasperReport getReport() {
		return this.report;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		SubJasperReport result = (SubJasperReport) obj;
		return this.getSubReportFile().equals(result.getSubReportFile())
		    && this.getReport().equals(result.getReport());
	}
}
