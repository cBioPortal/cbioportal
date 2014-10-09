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
import java.io.File;

/**
 * Class which contains cancer study metadata.
 */
public class CancerStudyMetadata {
	
    // delimiter between tumor type and center (used for find the path)
	public static final String CANCER_STUDY_DELIMITER = "/"; 

	// this is value in worsheet-matrix cell if cancer study is in a desired portal
	public static final String CANCER_STUDY_IN_PORTAL_INDICATOR = "x";

	// file/file extension of metadata file
	private static final String CANCER_STUDY_METADATA_FILE_EXT = ".txt";
	private static final String CANCER_STUDY_METADATA_FILE = "meta_study" + CANCER_STUDY_METADATA_FILE_EXT;

	// cancer study identifier delimiter (used in metadata files)
	private static final String CANCER_STUDY_IDENTIFIER_DELIMITER = "_";

	// these are the tags to replace in description
	public static final String NUM_CASES_TAG = "<NUM_CASES>";
	public static final String TUMOR_TYPE_TAG = "<TUMOR_TYPE>";
	public static final String TUMOR_TYPE_NAME_TAG = "<TUMOR_TYPE_NAME>";

	// these suffix identifies a published study
	public static final String PUBLISHED_TCGA_STUDY_SUFFIX = "tcga_pub";

	// bean properties
	private String name;
	private String tumorType;
	private TumorTypeMetadata tumorTypeMetadata;
	private String description;
	private String citation;
	private String pmid;
	private String studyPath;
	private String stableId;
        private String center;
	private String groups;
        private String shortName;
        private boolean convert;

    /**
     * Create a CancerStudyMetadata instance with properties in given array.
	 * ITs assumed order of properties is that from google worksheet.
	 * cancerStudyPath is of the form brca/tcga/pub that you would find 
	 * on the google spreadsheet cancer_studies worksheet.
	 *
	 * All portal columns are ignored (anything > 1)
     *
	 * @param properties String[]
     */
    public CancerStudyMetadata(String[] properties) {

		if (properties.length < 5) {
            throw new IllegalArgumentException("corrupt properties array passed to contructor");
		}
                
                this.studyPath = properties[0].trim();
                String[] parts = properties[0].trim().split(CANCER_STUDY_DELIMITER);
		if (parts.length < 2) {
			throw new IllegalArgumentException("cancerStudyPath is missing tumor type and or center");
                }
                this.center = parts[1];
		this.tumorType = properties[1].trim();
                this.stableId = properties[2].trim();
		this.name = properties[3].trim();
		this.description = properties[4].trim();
		this.citation = properties[5].trim();
		this.pmid = properties[6].trim();
		this.groups = properties[7].trim();
                this.shortName = properties[8].trim();
                this.convert = Boolean.parseBoolean(properties[9].trim());
	}

	public String getName() { return name; }
	public String getTumorType() { return tumorType; }
	public String getStableId() { return stableId; }
	public TumorTypeMetadata getTumorTypeMetadata() { return tumorTypeMetadata; }
	public void setTumorTypeMetadata(TumorTypeMetadata tumorTypeMetadata) { this.tumorTypeMetadata = tumorTypeMetadata; }
	public String getStudyPath() { return studyPath; }
        public String getCenter() { return center; }
	public String getDescription() { return description; }
	public String getCitation() { return citation; }
	public String getPMID() { return pmid; }
	public String getGroups() { return groups; }
    public String getShortName() { return shortName; }
	public Boolean isConverted() { return convert; }

	public String getCancerStudyMetadataFilename() {
		//return getStudyPath() + File.separator + toString() + CANCER_STUDY_METADATA_FILE_EXT;
		return CANCER_STUDY_METADATA_FILE;
	}

	public String toString() {
		return stableId;
	}
}
