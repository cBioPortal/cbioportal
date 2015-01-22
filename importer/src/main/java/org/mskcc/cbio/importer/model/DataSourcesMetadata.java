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

	/*
	Constructor using Google worksheet paramteer
	 */
	public DataSourcesMetadata( Map<String,String> worksheetRowMap){
		this.dataSource = worksheetRowMap.get("datasource");
		this.downloadDirectory = worksheetRowMap.get("downloaddirectory");
		this.additionalStudiesSource = Boolean.parseBoolean(worksheetRowMap.get("addtionalstudiessource"));
		this.fetcherBeanID = worksheetRowMap.get("fetcherbeanid");
	}

	public String getDataSource() { return dataSource; }
	public String getDownloadDirectory() {
		// resolve envroment variable portion of worksheet entry
		return this.resolveBaseStagingDirectory().toString();
		//return downloadDirectory;
	}
	public Boolean isAdditionalStudiesSource() { return additionalStudiesSource; }
	public String getFetcherBeanID() { return fetcherBeanID; }

	public Path resolveBaseStagingDirectory() {
		if(!this.downloadDirectory.startsWith("$")){
			return Paths.get(this.downloadDirectory);
		}
		// the first portion of the download directory field is an environment variable
		List<String> dirList = StagingCommonNames.pathSplitter.splitToList(this.downloadDirectory);
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


	/*
	public static method to instantiate a DataSourcesMetadata object based on a data source name
	the return object is encapsulated in an Optional to handle cases where a DataSourcesMetadata object
	wasn't found
	 */
	public static final  Optional<DataSourcesMetadata> findDataSourcesMetadataByDataSourceName(String name){
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
		//String dataSourceName = StagingCommonNames.DATA_SOURCE_DMP;
		String dataSourceName = "foundation-dev";
		Optional<DataSourcesMetadata> optMeta = DataSourcesMetadata.findDataSourcesMetadataByDataSourceName(dataSourceName);
		if(optMeta.isPresent()){
			System.out.println(System.getenv("PORTAL_DATA_HOME"));
			System.out.println(optMeta.get().getDataSource());
			System.out.println(optMeta.get().getDownloadDirectory());
			System.out.println( "Path = " +optMeta.get().resolveBaseStagingDirectory().toString());
		} else {
			System.out.println("Unable to resolve data source for " +dataSourceName);
		}



	}

}
