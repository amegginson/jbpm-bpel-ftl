package org.jboss.bam.report;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

@Entity
@Table(name = "reports")
@DiscriminatorColumn(name = "report_type", discriminatorType = DiscriminatorType.STRING)
public class Report implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
	private long id;
	
	private int version;
	
	private String name;
	
	private String summary;
	
	private String description;
	
	private byte[] reportFile;
	
	private String reportType;
	
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
	
	public void setVersion(int version) {
		this.version = version;
	}
	
	@Version
	@Column(name = "version")
	public int getVersion() {
		return this.version;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Column(name = "name", unique = true, nullable = false, length = 128)
	@NotNull
	public String getName() {
		return this.name;
	}
	
	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	public String getSummary() {
		return this.summary;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Column(name = "description", length = 65535)
	@Length(max = 65535)
	public String getDescription() {
		return this.description;
	}
	
	public void setReportFile(byte[] reportFile) {
		this.reportFile = reportFile;
	}
	
	@Lob
	@Column(name = "report_file")
	@NotNull
	public byte[] getReportFile() {
		return this.reportFile;
	}
	
	public void setReportType(String reportType) {
		this.reportType = reportType;
	}
	
	@Column(name = "report_type", nullable = false, length = 32, insertable = false, updatable = false)
	public String getReportType() {
		return this.reportType;
	}
	
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		Report result = (Report) obj;
		return this.getName().equals(result.getName())
				&& this.getDescription().equals(result.getDescription())
				&& this.getSummary().equals(result.getSummary())
				&& this.getReportFile().equals(result.getReportFile());
	}
}
