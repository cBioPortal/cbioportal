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
import org.mskcc.cbio.importer.util.MetadataUtils;

import java.util.List;
import java.util.ArrayList;

/**
 * Class which contains reference metadata.
 */
public class ReferenceMetadata {

    // delimiter between tumor type and center (used for find the path)
	public static final String REFERENCE_DATA_ARGS_DELIMITER = ":";

	// bean properties
	private String referenceType;
	private Boolean fetchData; // fetch?
	private Boolean importData; // import?
	private String fetcherName;
	private List<String> fetcherArgs;
	private String importerName;
	private List<String> importerArgs;

    /**
     * Create a ReferenceMetadata instance with properties in given array.
	 * Its assumed order of properties is that from google worksheet.
     *
	 * @param properties String[]
     */
    public ReferenceMetadata(String[] properties) {

		if (properties.length < 6) {
            throw new IllegalArgumentException("corrupt properties array passed to contructor");
		}

		this.referenceType = properties[0].trim();
		this.fetchData = new Boolean(properties[1].trim());
		this.importData = new Boolean(properties[2].trim());
		this.fetcherName = MetadataUtils.getCanonicalPath(properties[3].trim());
		this.fetcherArgs = new ArrayList<String>();
		for (String fetcherArg : properties[4].trim().split(REFERENCE_DATA_ARGS_DELIMITER)) {
			this.fetcherArgs.add(MetadataUtils.getCanonicalPath(fetcherArg));
		}
		this.importerName = MetadataUtils.getCanonicalPath(properties[5].trim());
		this.importerArgs = new ArrayList<String>();
		for (String importerArg : properties[6].trim().split(REFERENCE_DATA_ARGS_DELIMITER)) {
			this.importerArgs.add(MetadataUtils.getCanonicalPath(importerArg));
		}
	}

	public String getReferenceType() { return referenceType; }
	public Boolean getFetch() { return fetchData; }
	public Boolean getImport() { return importData; }
	public String getFetcherName() { return fetcherName; }
	public List<String> getFetcherArgs() { return fetcherArgs; }
	public String getImporterName() { return importerName; }
	public List<String> getImporterArgs() { return importerArgs; }
}
