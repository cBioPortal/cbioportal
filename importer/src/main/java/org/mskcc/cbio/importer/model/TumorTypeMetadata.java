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

/**
 * Class which contains tumor type  metadata.
 */
public class TumorTypeMetadata {

    // delimiter between tumor type & name within the reference file
	public static final String TUMOR_TYPE_META_FILE_DELIMITER = "\t";

    // name of reference file created to import into portal
	public static final String TUMOR_TYPE_META_FILE_NAME = "cancers.txt"; 

	// bean properties
	private String tumorType;
	private String tumorTypeName;
    private String clinicalTrialKeywords;
    private String tissue;
    private String category;
    private String dedicatedColor;
    private String shortName;
    private Boolean download; // download?

    /**
     * Create a TumorTypeMetadata instance with properties in given array.
	 * Its assumed order of properties is that from google worksheet.
     *
	 * @param properties String[]
     */
    public TumorTypeMetadata(String[] properties) {
		if (properties.length < 7) {
            throw new IllegalArgumentException("corrupt properties array passed to contructor");
		}

		this.download = Boolean.parseBoolean(properties[0].trim());
		this.tumorType = properties[1].trim();
		this.tumorTypeName = properties[2].trim();
        this.clinicalTrialKeywords = properties[3].trim();
        this.tissue = properties[4].trim();
        this.category = properties[5].trim();
        this.dedicatedColor = properties[6].trim();
        this.shortName = properties[7].trim();
	}

	public String getType() { return tumorType; }
	public String getName() { return tumorTypeName; }
	public Boolean getDownload() { return download; }
    public String getClinicalTrialKeywords() { return clinicalTrialKeywords; }
    public String getDedicatedColor() { return dedicatedColor; }
    public String getTissue() { return tissue; }
    public String getCategory() { return category; }
    public String getShortName() { return shortName; }
}
