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

import java.util.Map;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 * Encapsulates Patient Data.
 *
 * @author Benjamin Gross
 */
public class Patient {

    private int internalId;
    private String stableId;
    private CancerStudy cancerStudy;

	private Map<String, ClinicalData> clinicalDataMap;
    private static final Logger logger = Logger.getLogger(Patient.class);

    public Patient(CancerStudy cancerStudy, String stableId)
    {
        this(cancerStudy, stableId, -1, new HashMap<String, ClinicalData>());
    }

    public Patient(CancerStudy cancerStudy, String stableId, int internalId)
    {
        this(cancerStudy, stableId, internalId, new HashMap<String, ClinicalData>());
    }

    public Patient(CancerStudy cancerStudy, String stableId, int internalId, Map<String, ClinicalData> clinicalDataMap)
    {
        this.cancerStudy = cancerStudy;
        this.stableId = stableId;
        this.internalId = internalId;
		this.clinicalDataMap = clinicalDataMap;
    }

    @Override
    public String toString()
    {
        return stableId;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Patient)) {
            return false;
        }
        
        Patient anotherPatient = (Patient)obj;
        if (this.internalId != anotherPatient.getInternalId()) {
            return false;
        }
        
        if (this.cancerStudy.getInternalId() != anotherPatient.getCancerStudy().getInternalId()) {
            return false;
        }
        
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + (this.stableId != null ? this.stableId.hashCode() : 0);
        hash = 41 * hash + this.cancerStudy.getInternalId();
        return hash;
    }

    public CancerStudy getCancerStudy()
    {
        return cancerStudy;
    }

    public int getInternalId()
    {
        return internalId;
    }

    public String getStableId()
    {
        return stableId;
    }

    public Double getOverallSurvivalMonths()
    { 
		return getDoubleValue(ClinicalAttribute.OS_MONTHS);
	}
    public String getOverallSurvivalStatus()
    {
		return getStringValue(ClinicalAttribute.OS_STATUS);
	}
    public Double getDiseaseFreeSurvivalMonths()
    {
		return getDoubleValue(ClinicalAttribute.DFS_MONTHS);
	}
    public String getDiseaseFreeSurvivalStatus()
    {
		return getStringValue(ClinicalAttribute.DFS_STATUS);
	}
    public Double getAgeAtDiagnosis()
    {
		return getDoubleValue(ClinicalAttribute.AGE_AT_DIAGNOSIS);
    }
    public Integer getSampleCount()
    {
		return getIntegerValue(ClinicalAttribute.SAMPLE_COUNT);
	}

	private Double getDoubleValue(String attribute)
    {
		ClinicalData data = clinicalDataMap.get(attribute);
        if (data == null || data.getAttrVal().length() == 0 ||
                data.getAttrVal().equals(ClinicalAttribute.NA) ||
                data.getAttrVal().equals(ClinicalAttribute.MISSING)) {
            return null;
        }
        try {
            return Double.valueOf(data.getAttrVal());
        } catch (NumberFormatException e) {
            logger.warn("Can't handle clinical attribute of patient: " + stableId);
            return null;
        }
    }
    
    private Integer getIntegerValue(String attribute)
    {
		ClinicalData data = clinicalDataMap.get(attribute);
        if (data == null || data.getAttrVal().length() == 0 ||
                data.getAttrVal().equals(ClinicalAttribute.NA) ||
                data.getAttrVal().equals(ClinicalAttribute.MISSING)) {
            return null;
        }
        try {
            return Integer.valueOf(data.getAttrVal());
        } catch (NumberFormatException e) {
            logger.warn("Can't handle clinical attribute of patient: " + stableId);
            return null;
        }
	}

	private String getStringValue(String attribute)
    {
		ClinicalData data = clinicalDataMap.get(attribute);
		return (data == null || data.getAttrVal().length() == 0 ||
				data.getAttrVal().equals(ClinicalAttribute.NA) ||
				data.getAttrVal().equals(ClinicalAttribute.MISSING)) ? null : data.getAttrVal();
	}
}
