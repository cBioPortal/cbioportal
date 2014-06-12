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

// package
package org.mskcc.cbio.importer.model;

// imports
import java.util.Map;
import java.util.HashMap;
import java.text.SimpleDateFormat;

public class ClinicalAttributesNamespace
{
    public static final String CDE_TAG = "CDE_ID:";
    public static final String CDE_DELIM = ":";
	
    // worksheet column header that is used as key to find row to update
	public static final String WORKSHEET_UPDATE_COLUMN_KEY = "EXTERNALCOLUMNHEADER";

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

	// bean properties
    private String externalColumnHeader;
	private String normalizedColumnHeader;
    private String tumorType;
    private String cancerStudy;
    private String displayName;
    private String description;
    private String dateAdded;

    public ClinicalAttributesNamespace(String[] properties) {

		if (properties.length < 7) {
            throw new IllegalArgumentException("corrupt properties array passed to contructor");
		}

        this.externalColumnHeader = properties[0].trim();
        this.normalizedColumnHeader = properties[1].trim();
        this.tumorType = properties[2].trim();
        this.cancerStudy = properties[3].trim();
        this.displayName = properties[4].trim();
        this.description = properties[5].trim();
        this.dateAdded = properties[6].trim();
	}

	public String getExternalColumnHeader() { return externalColumnHeader; }
	public String getNormalizedColumnHeader() { return normalizedColumnHeader; }
	public String getTumorType() { return tumorType; }
	public String getCancerStudy() { return cancerStudy; }
	public String getDisplayName() { return displayName; }
	public String getDescription() { return description; }
    public String getDateAdded() { return dateAdded; }

    public static Map<String,String> getPropertiesMap(BCRDictEntry bcr, String dateAdded)
    {
        HashMap<String, String> toReturn = new HashMap<String,String>();
        toReturn.put("EXTERNALCOLUMNHEADER", bcr.id);
        toReturn.put("NORMALIZEDCOLUMNHEADER", "");
        toReturn.put("TUMORTYPE", bcr.tumorType);
        toReturn.put("CANCERSTUDY", bcr.cancerStudy);
        toReturn.put("DISPLAYNAME", bcr.displayName);
        toReturn.put("DESCRIPTION", bcr.description);
        toReturn.put("DATEADDED", dateAdded);

        return toReturn;
    }
}
