package org.jboss.bam;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

@Entity
@Table(name = "dashboard_queries")
@DiscriminatorColumn(name = "query_type", discriminatorType = DiscriminatorType.STRING)
public class AbstractDashboardQuery implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	private long id;
	private int version;
	private String name;
	private String description;
	private String queryType;
	private String query;
	private DashboardQueryChartProperties chart = new DashboardQueryChartProperties();
	
	public AbstractDashboardQuery() {
	}
	
	public AbstractDashboardQuery(long id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public AbstractDashboardQuery(long id, String name, String description,
			String query) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.query = query;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	@NotNull
	public long getId() {
		return this.id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	@Version
	@Column(name = "version")
	@NotNull
	public int getVersion() {
		return this.version;
	}
	
	public void setVersion(int version) {
		this.version = version;
	}
	
	@Column(name = "name", unique = true, nullable = false, length = 128)
	@NotNull
	@Length(min = 2, max = 128)
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Column(name = "description", length = 65535)
	@Length(max = 65535)
	public String getDescription() {
		return this.description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Column(name = "query_type", nullable = false, length = 32, insertable = false, updatable = false)
	public String getQueryType() {
		return this.queryType;
	}
	
	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}
	
	@Column(name = "query", length = 65535)
	@Length(max = 65535)
	public String getQuery() {
		return this.query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public void setChart(DashboardQueryChartProperties chart) {
		this.chart = chart;
	}
	
	@Embedded
	public DashboardQueryChartProperties getChart() {
		return this.chart;
	}
	
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		AbstractDashboardQuery result = (AbstractDashboardQuery) obj;
		return this.getName().equals(result.getName())
				&& this.getDescription().equals(result.getDescription())
				&& this.getQuery().equals(result.getQuery());
	}
}
