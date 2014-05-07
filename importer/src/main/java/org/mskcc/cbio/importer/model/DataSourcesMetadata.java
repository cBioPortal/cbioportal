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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class which contains datasource metadata.
 */
public class DataSourcesMetadata {

	// delimiter between datasource names (like tcga-stddata)
	public static final String DATA_SOURCE_NAME_DELIMITER = "-";

	// bean properties
	private String dataSource;
    private String downloadDirectory;
	private Boolean additionalStudiesSource;
    private String fetcherBeanID;

    /**
     * Create a DataSourcesMetadata instance with properties in given array.
	 * Its assumed order of properties is that from google worksheet.
     *
	 * @param properties String[]
     */
    public DataSourcesMetadata(String[] properties) {

		if (properties.length < 4) {
            throw new IllegalArgumentException("corrupt properties array passed to contructor");
		}

		this.dataSource = properties[0].trim();
		this.downloadDirectory = MetadataUtils.getCanonicalPath(properties[1].trim());
		this.additionalStudiesSource = new Boolean(properties[2].trim());
		this.fetcherBeanID = properties[3].trim();
	}

	public String getDataSource() { return dataSource; }
	public String getDownloadDirectory() { return downloadDirectory; }
	public Boolean isAdditionalStudiesSource() { return additionalStudiesSource; }
	public String getFetcherBeanID() { return fetcherBeanID; }
}
