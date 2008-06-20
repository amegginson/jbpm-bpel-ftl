package org.jboss.bam.report;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("BIRT")
public class BirtReport extends Report {

	private static final long serialVersionUID = 1L;

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		BirtReport result = (BirtReport) obj;
		return this.getName().equals(result.getName())
		    && this.getDescription().equals(result.getDescription())
		    && this.getSummary().equals(result.getSummary())
		    && this.getReportFile().equals(result.getReportFile());
	}
}
