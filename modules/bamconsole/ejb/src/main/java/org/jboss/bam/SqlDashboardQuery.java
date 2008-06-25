package org.jboss.bam;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("SQL")
public class SqlDashboardQuery extends AbstractDashboardQuery {
	
	private static final long serialVersionUID = 1L;
	
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		SqlDashboardQuery result = (SqlDashboardQuery) obj;
		return this.getName().equals(result.getName())
				&& this.getDescription().equals(result.getDescription())
				&& this.getQuery().equals(result.getQuery());
	}
}
