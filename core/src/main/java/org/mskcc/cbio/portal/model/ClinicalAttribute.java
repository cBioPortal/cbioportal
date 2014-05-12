/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

public class ClinicalAttribute  {

	// some defined statics
	public static final String NA = "NA";
	public static final String MISSING = "MISSING";
	public static final String OS_STATUS = "OS_STATUS";
	public static final String OS_MONTHS = "OS_MONTHS";
	public static final String DFS_STATUS = "DFS_STATUS";
	public static final String DFS_MONTHS = "DFS_MONTHS";
	public static final String AGE_AT_DIAGNOSIS = "AGE";
	public static final List<String> survivalAttributes = initializeSurvivalAttributeList();
	private static List<String> initializeSurvivalAttributeList() {
		return Arrays.asList(AGE_AT_DIAGNOSIS, OS_STATUS, OS_MONTHS, DFS_STATUS, DFS_MONTHS);
	}

    private String attributeId;
    private String displayName;
    private String description;
    private String datatype;

    public ClinicalAttribute(String attributeId, String displayName, String description, String datatype) {
        this.attributeId = attributeId;
		this.displayName = displayName;
		this.description = description;
        this.datatype = datatype;
    }

    @Override
    public String toString() {
        return "ClinicalAttribute[" +
			attributeId + "," +
			displayName + "," +
			description + "," +
			datatype + "]";
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
}
