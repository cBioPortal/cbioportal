/** Copyright (c) 2013 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/
package org.mskcc.cbio.portal.model;

import java.util.Map;
import org.apache.log4j.Logger;

/**
 * Encapsulates Patient Data.
 *
 * @author Benjamin Gross
 */
public class Patient {

    private String caseId;
	Map<String, ClinicalData> clinicalDataMap;
    private static final Logger logger = Logger.getLogger(Patient.class);

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
        if (data == null || data.getAttrVal().length() == 0 ||
                data.getAttrVal().equals(ClinicalAttribute.NA) ||
                data.getAttrVal().equals(ClinicalAttribute.MISSING)) {
            return null;
        }
        try {
            return Double.valueOf(data.getAttrVal());
        } catch (NumberFormatException e) {
            logger.warn("Can't handle clinical attribute of case: " + caseId);
            return null;
        }
	}

	private String getStringValue(String attribute) {
		ClinicalData data = clinicalDataMap.get(attribute);
		return (data == null || data.getAttrVal().length() == 0 ||
				data.getAttrVal().equals(ClinicalAttribute.NA) ||
				data.getAttrVal().equals(ClinicalAttribute.MISSING)) ? null : data.getAttrVal();
	}
}
