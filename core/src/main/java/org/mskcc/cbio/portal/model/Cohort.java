/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 *This represents a group of cancer studies, with a set of cases and other data.
 *
 * @author Karthik Kalletla
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Cohort implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JsonProperty
	private String studyName;
	@JsonProperty
	private String id;
	@JsonProperty
	private String description;
	
	private boolean isVirtualCohort;
	
	@JsonProperty("selectedCases")
	private List<CohortStudyCasesMap> cohortStudyCasesMap;
	
	public String getStudyName() {
		return studyName;
	}
	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<CohortStudyCasesMap> getCohortStudyCasesMap() {
		return cohortStudyCasesMap;
	}
	public void setCohortStudyCasesMap(List<CohortStudyCasesMap> cohortStudyCasesMap) {
		this.cohortStudyCasesMap = cohortStudyCasesMap;
	}
	public boolean isVirtualCohort() {
		return isVirtualCohort;
	}
	public void setVirtualCohort(boolean isVirtualCohort) {
		this.isVirtualCohort = isVirtualCohort;
	}
	@Override
	public String toString() {
		return "Cohort [studyName=" + studyName + ", id=" + id + ", description=" + description + ", isVirtualCohort="
				+ isVirtualCohort + ", cohortStudyCasesMap=" + cohortStudyCasesMap + "]";
	}

}
