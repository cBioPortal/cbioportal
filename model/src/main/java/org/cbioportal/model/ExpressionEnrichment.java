package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import javax.validation.constraints.NotNull;

public class ExpressionEnrichment implements Serializable {

	@NotNull
	private List<GroupStatistics> groupsStatistics;
	@NotNull
	private BigDecimal pValue;

	public List<GroupStatistics> getGroupsStatistics() {
		return groupsStatistics;
	}

	public void setGroupsStatistics(List<GroupStatistics> groupsStatistics) {
		this.groupsStatistics = groupsStatistics;
	}

	public BigDecimal getpValue() {
		return pValue;
	}

	public void setpValue(BigDecimal pValue) {
		this.pValue = pValue;
	}
}
