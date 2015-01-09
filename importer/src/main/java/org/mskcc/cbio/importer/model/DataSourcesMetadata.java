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
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import org.mskcc.cbio.importer.config.internal.ImporterSpreadsheetService;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.util.MetadataUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class which contains datasource metadata.
 */
public class DataSourcesMetadata {

	// delimiter between datasource names (like tcga-stddata)
	public static final String CMO_PIPELINE_REPOS = "bic-mskcc";
	public static final String DATA_SOURCE_NAME_DELIMITER = "-";

	private static final String DEFAULT_STAGING_BASE = "/tmp";

	// bean properties
	private String dataSource;
    private String downloadDirectory;
	private Boolean additionalStudiesSource;
    private String fetcherBeanID;

	/*
	Constructor using Google worksheet paramteer
	 */

	public DataSourcesMetadata( Map<String,String> worksheetRowMap){
		this.dataSource = worksheetRowMap.get("datasource");
		this.downloadDirectory = worksheetRowMap.get("downloaddirectory");
		this.additionalStudiesSource = Boolean.parseBoolean(worksheetRowMap.get("addtionalstudiessource"));
		this.fetcherBeanID = worksheetRowMap.get("fetcherbeanid");
	}

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

	public Path resolveBaseStatgingPath() {
		if(!this.getDownloadDirectory().startsWith("$")){
			return Paths.get(this.getDownloadDirectory());
		}
		// the first portion of the download directory field is an environment variable
		List<String> dirList = StagingCommonNames.pathSplitter.splitToList(this.getDownloadDirectory());
		String envVar = System.getenv(dirList.get(0).replace("$", "")) ; // resolve the system environment variable
		String base;
		if(Strings.isNullOrEmpty(envVar)) {
			System.out.println("Unable to resolve system environment variable " + dirList.get(0)
			+" Setting to default " +DEFAULT_STAGING_BASE);
			base = DEFAULT_STAGING_BASE;
		}else {
			base = envVar;
		}
		return  Paths.get(base,StagingCommonNames.pathJoiner.join( dirList.subList(1, dirList.size())));
	}


	public static final  Optional<DataSourcesMetadata> findDataSourcesMetadatByDataSourceName(String name){
		final String dataSourcesWorksheet = "data_sources";
		final String dataSourceColumnName = "datasource";
		if(Strings.isNullOrEmpty(name)) {return Optional.absent();}
		Optional<Map<String,String >> rowOptional = ImporterSpreadsheetService.INSTANCE.getWorksheetRowByColumnValue(dataSourcesWorksheet, dataSourceColumnName,
				name);
		if (rowOptional.isPresent()) {
			return Optional.of(new DataSourcesMetadata(rowOptional.get()));
		}
		return Optional.absent();
	}

	// main method for testing
	public static void main (String...args){
		String dataSourceName = StagingCommonNames.DATA_SOURCE_DMP;
		Optional<DataSourcesMetadata> optMeta = DataSourcesMetadata.findDataSourcesMetadatByDataSourceName(dataSourceName);
		if(optMeta.isPresent()){
			System.out.println(System.getenv("PORTAL_DATA_HOME"));
			System.out.println(optMeta.get().getDataSource());
			System.out.println(optMeta.get().getDownloadDirectory());
			System.out.println( "Path = " +optMeta.get().resolveBaseStatgingPath().toString());
		} else {
			System.out.println("Unable to resolve data source for " +dataSourceName);
		}



	}

}
