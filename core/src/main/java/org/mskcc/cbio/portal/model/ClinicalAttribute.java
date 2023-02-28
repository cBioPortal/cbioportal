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

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

public class ClinicalAttribute  {

	// some defined statics
	public static final String NA = "NA";
    public static final String DEFAULT_DATATYPE = "STRING";
    public static final String PATIENT_ATTRIBUTE = "PATIENT";
    public static final String SAMPLE_ATTRIBUTE = "SAMPLE";
	public static final String MISSING = "MISSING";
	public static final String OS_STATUS = "OS_STATUS";
	public static final String OS_MONTHS = "OS_MONTHS";
	public static final String DFS_STATUS = "DFS_STATUS";
	public static final String DFS_MONTHS = "DFS_MONTHS";
	public static final String AGE_AT_DIAGNOSIS = "AGE";
    // two additional variables for multi-cancer
	public static final String CANCER_TYPE = "CANCER_TYPE"; // may have to change this to MAIN_CANCER_TYPE
    public static final String CANCER_TYPE_DETAILED = "CANCER_TYPE_DETAILED"; // may have to change this to CANCER_TYPE
    public static final String SAMPLE_COUNT = "SAMPLE_COUNT";
	
	public static final List<String> survivalAttributes = initializeSurvivalAttributeList();
	private static List<String> initializeSurvivalAttributeList() {
		return Arrays.asList(AGE_AT_DIAGNOSIS, OS_STATUS, OS_MONTHS, DFS_STATUS, DFS_MONTHS);
	}

    private String attributeId;
    private String displayName;
    private String description;
    private String datatype;
    private boolean patientAttribute;
    private String priority;
    private Integer cancerStudyId;

    public ClinicalAttribute(String attributeId, String displayName, String description,
                             String datatype, boolean patientAttribute, String priority, Integer cancerStudyId) {
        this.attributeId = attributeId;
		this.displayName = displayName;
		this.description = description;
        this.datatype = datatype;
        this.patientAttribute = patientAttribute;
        this.priority = priority;
        this.cancerStudyId = cancerStudyId;
    }

    @Override
    public String toString() {
        return "ClinicalAttribute[" +
            attributeId + "," +
            displayName + "," +
            description + "," +
            priority + "," +
            datatype + "," +
            String.valueOf(cancerStudyId) + "]";
    }

    public String getAttrId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

	public boolean isPatientAttribute() {
		return patientAttribute;
	}

	public void setPatientAttribute(boolean patientAttribute) {
		this.patientAttribute = patientAttribute;
	}

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
    public Integer getCancerStudyId() {
        return cancerStudyId;
    }
    public void setCancerStudyId(Integer cancerStudyId) {
        this.cancerStudyId = cancerStudyId;
    }
}
