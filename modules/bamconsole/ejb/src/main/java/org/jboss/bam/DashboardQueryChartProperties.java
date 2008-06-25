package org.jboss.bam;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.validator.NotNull;

@Embeddable
public class DashboardQueryChartProperties implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String chartType;
	private boolean threeDimensional;
	private long width = 320;
	private long height = 240;
	private String domainLabel = "";
	private String rangeLabel = "";
	
	public void setChartType(String chartType) {
		this.chartType = chartType;
	}
	
	@Column(name = "chart_type")
	@NotNull
	public String getChartType() {
		return this.chartType;
	}
	
	public void setThreeDimensional(boolean threeDimensional) {
		this.threeDimensional = threeDimensional;
	}
	
	@Column(name = "three_dimensional")
	public boolean getThreeDimensional() {
		return this.threeDimensional;
	}
	
	public void setWidth(long width) {
		this.width = width;
	}
	
	@Column(name = "chart_width")
	public long getWidth() {
		return this.width;
	}
	
	public void setHeight(long height) {
		this.height = height;
	}
	
	@Column(name = "chart_height")
	public long getHeight() {
		return this.height;
	}
	
	public void setDomainLabel(String domainLabel) {
		this.domainLabel = domainLabel;
	}
	
	@Column(name="domain_label")
	@NotNull
	public String getDomainLabel() {
		return this.domainLabel;
	}
	
	public void setRangeLabel(String rangeLabel) {
		this.rangeLabel = rangeLabel;
	}
	
	@Column(name="range_label")
	@NotNull
	public String getRangeLabel() {
		return this.rangeLabel;
	}
}
