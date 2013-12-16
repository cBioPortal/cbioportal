/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

// package
package org.mskcc.cbio.importer.model;

// imports
import java.util.Map;
import java.util.HashMap;

public class ClinicalAttributesNamespace
{
	// worksheet column header that is used as key to find row to update
	public static final String WORKSHEET_UPDATE_COLUMN_KEY = "EXTERNALCOLUMNHEADER";

	// bean properties
    private String externalColumnHeader;
	private String normalizedColumnHeader;
    private String tumorType;
    private String cancerStudy;
    private String displayName;
    private String description;
    private String datatype;

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
		this.datatype = properties[6].trim();
	}

	public String getExternalColumnHeader() { return externalColumnHeader; }
	public String getNormalizedColumnHeader() { return normalizedColumnHeader; }
	public String getTumorType() { return tumorType; }
	public String getCancerStudy() { return cancerStudy; }
	public String getDisplayName() { return displayName; }
	public String getDescription() { return description; }
	public String getDatatype() { return datatype; }

    public static Map<String,String> getPropertiesMap(BCRDictEntry bcr)
    {
        HashMap<String, String> toReturn = new HashMap<String,String>();
        toReturn.put("EXTERNALCOLUMNHEADER", bcr.id);
        toReturn.put("NORMALIZEDCOLUMNHEADER", "");
        toReturn.put("TUMORTYPE", bcr.tumorType.toLowerCase());
        toReturn.put("CANCERSTUDY", "");
        toReturn.put("DISPLAYNAME", bcr.displayName);
        toReturn.put("DESCRIPTION", bcr.description);
        toReturn.put("DATATYPE", "");

        return toReturn;
    }
}
