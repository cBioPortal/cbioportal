/** Copyright (c) 2013 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/
package org.mskcc.cbio.portal.model;

import java.util.Map;

/**
 * Encapsulates Patient Data.
 *
 * @author Benjamin Gross
 */
public class Patient {

    private String caseId;
	Map<String, ClinicalData> clinicalDataMap;

    /**
     * Constructor.
     *
     * @param caseId database id of the case/patient.
	 * @param clinicalData List of clinical data objects.
     */
    public Patient(String caseId, Map<String, ClinicalData> clinicalDataMap) {
        this.caseId = caseId;
		this.clinicalDataMap = clinicalDataMap;
    }

    public String getCaseId() { return caseId; }

    public Double getOverallSurvivalMonths() { 
		return getDoubleValue(ClinicalAttribute.OS_MONTHS);
	}
    public String getOverallSurvivalStatus() {
		return getStringValue(ClinicalAttribute.OS_STATUS);
	}
    public Double getDiseaseFreeSurvivalMonths() {
		return getDoubleValue(ClinicalAttribute.DFS_MONTHS);
	}
    public String getDiseaseFreeSurvivalStatus() {
		return getStringValue(ClinicalAttribute.DFS_STATUS);
	}
    public Double getAgeAtDiagnosis() {
		return getDoubleValue(ClinicalAttribute.AGE_AT_DIAGNOSIS);
	}

	private Double getDoubleValue(String attribute) {
		ClinicalData data = clinicalDataMap.get(attribute);
		return (data == null || data.getAttrVal().length() == 0 ||
				data.getAttrVal().equals(ClinicalAttribute.NA) ||
				data.getAttrVal().equals(ClinicalAttribute.MISSING)) ? null : Double.valueOf(data.getAttrVal());
	}

	private String getStringValue(String attribute) {
		ClinicalData data = clinicalDataMap.get(attribute);
		return (data == null || data.getAttrVal().length() == 0 ||
				data.getAttrVal().equals(ClinicalAttribute.NA) ||
				data.getAttrVal().equals(ClinicalAttribute.MISSING)) ? null : data.getAttrVal();
	}
}
