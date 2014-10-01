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

public class ClinicalAttributesMetadata
{
	public static final String WORKSHEET_UPDATE_COLUMN_KEY = "NORMALIZEDCOLUMNHEADER";

	private String normalizedColumnHeader;
    private String displayName;
    private String description;
    private String datatype;
    private String attributeType;
    private String priority;

    public ClinicalAttributesMetadata(String[] properties) {

		if (properties.length < 6) {
            throw new IllegalArgumentException("corrupt properties array passed to contructor");
		}

        this.normalizedColumnHeader = properties[0].trim();
        this.displayName = properties[1].trim();
        this.description = properties[2].trim();
		this.datatype = properties[3].trim();
		this.attributeType = properties[4].trim();
		String priority = properties[5].trim();
		this.priority = (priority.isEmpty()) ? "1" : priority;
	}

	public String getNormalizedColumnHeader() { return normalizedColumnHeader; }
	public String getDisplayName() { return displayName; }
	public String getDescription() { return description; }
	public String getDatatype() { return datatype; }
	public String getAttributeType() { return attributeType; }
	public String getPriority() { return priority; }

	public boolean missingAttributes()
	{
		return (displayName.isEmpty() ||
		        description.isEmpty() ||
		        datatype.isEmpty() ||
		        attributeType.isEmpty());
	}
}
