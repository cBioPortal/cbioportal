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
import org.mskcc.cbio.importer.util.ClassLoader;

import java.util.Set;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.lang.reflect.Method;

/**
 * Class which contains datatype metadata.
 */
public class DatatypeMetadata {

	public static final String NUM_CASES_TAG = "<NUM_CASES>";
	public static final String NUM_GENES_TAG = "<NUM_GENES>";
	public static final String TUMOR_TYPE_TAG = "<TUMOR_TYPE>";
	public static final String CANCER_STUDY_TAG = "<CANCER_STUDY>";
	
	// delimiter when specifying datatypes on worksheet
    public static final String DATATYPES_DELIMITER = ":"; 

	// MAF file extension - used (at least) by FileUtils.oncotateAllMAFs
	public static final String MAF_FILE_EXT = ".maf.annotated";

	/*
	 * The following is an example of a downloadArchive string which the following 
	 * static delimiters are meant to address:
	 *
	 * CopyNumber_Gistic2.Level_4:amp_genes.conf_99.txt;CopyNumber_Gistic2.Level_4:table_amp.conf_99.txt
	 */

	// delimiter between download archive pairs
	private static final String DOWNLOAD_ARCHIVE_DELIMITER = ";";
 
	// delimiter between archive and filename pair
	private static final String ARCHIVE_FILENAME_PAIR_DELIMITER = ":";

	// delimiter between dependencies
	private static final String DEPENDENCIES_DELIMITER = ":";

	// bean properties
	private String datatype;
	private Boolean download;
	private String[] dependencies;
	// tcgadownloadArchive is parsed in constructor
	private LinkedHashSet<String> tcgaArchives;
	// key is archive file name, values is ARCHIVE_FILENAME_PAIR_DELIMITER filenames
	private HashMap<String, String> tcgaArchivedFiles;
    private String stagingFilename;
    private String converterClassName;
    private String importerClassName;

	private Boolean requiresMetafile;
    private String metaFilename;
    private String metaStableID;
    private String metaGeneticAlterationType;
    private Boolean metaShowProfileInAnalysisTab;
    private String metaProfileName;
	private String metaProfileDescription;

    /**
     * Create a DatatypeMetadata instance with properties in given array.
	 * Its assumed order of properties is that from google worksheet.
     *
	 * @param properties String[]
     */
    public DatatypeMetadata(String[] properties) {

		if (properties.length < 14) {
            throw new IllegalArgumentException("corrupt properties array passed to contructor");
		}

		this.datatype = properties[0].trim();
		this.download = new Boolean(properties[1].trim());
		this.dependencies = (properties[2].trim() != null) ?
			this.dependencies = properties[2].trim().split(DEPENDENCIES_DELIMITER) : new String[0];
		tcgaArchives = new LinkedHashSet<String>();
		tcgaArchivedFiles = new HashMap<String, String>();
		if (properties[3] != null && properties[3].length() > 0) {
			for (String archivePair : properties[3].trim().split(DOWNLOAD_ARCHIVE_DELIMITER)) {
				String[] parts = archivePair.split(ARCHIVE_FILENAME_PAIR_DELIMITER);
				String archive = parts[0].trim();
				String archivedFile = parts[1].trim();
				tcgaArchives.add(archive);
				if (tcgaArchivedFiles.containsKey(archive)) {
					tcgaArchivedFiles.put(archive, (tcgaArchivedFiles.get(archive) +
												ARCHIVE_FILENAME_PAIR_DELIMITER +
												archivedFile));
				}
				else {
					tcgaArchivedFiles.put(archive, archivedFile);
				}
			}
		}
		this.stagingFilename = properties[4].trim();
		this.converterClassName = properties[5].trim();
		this.importerClassName = properties[6].trim();
		this.requiresMetafile = new Boolean(properties[7].trim());
		this.metaFilename = properties[8].trim();
		this.metaStableID = properties[9].trim();
		this.metaGeneticAlterationType = properties[10];
		this.metaShowProfileInAnalysisTab = new Boolean(properties[11].trim());
		this.metaProfileName = properties[12].trim();
		this.metaProfileDescription = properties[13].trim();
	}

	public String getDatatype() { return datatype; }
	public Boolean isDownloaded() { return download; }
	public String[] getDependencies() { return dependencies; }
	public Set<String> getTCGADownloadArchives() { return tcgaArchives; }
	public Set<String> getTCGAArchivedFiles(String archive) {
		if (tcgaArchivedFiles.containsKey(archive)) {
			return new LinkedHashSet<String>(Arrays.asList(tcgaArchivedFiles.get(archive).split(ARCHIVE_FILENAME_PAIR_DELIMITER)));
		}
		else {
			// we do the following juggling because archive may be:
			// gdac.broadinstitute.org_BRCA.Merge_transcriptome__agilentg4502a_07_3__unc_edu__Level_3__unc_lowess_normalization_gene_level__data.Level_3.2012080400.0.0.tar.gz
			// but keys could be of the form:
			// Merge_transcriptome__agilentg4502a_07_3__unc_edu__Level_3__unc_lowess_normalization_gene_level__data.Level_3
			for (String possibleArchive : getTCGADownloadArchives()) {
				if (archive.contains(possibleArchive)) {
					return new LinkedHashSet<String>(Arrays.asList(tcgaArchivedFiles.get(possibleArchive).split(ARCHIVE_FILENAME_PAIR_DELIMITER)));
				}
			}
		}
		// should not get here
		return new LinkedHashSet<String>();
	}
	public String getStagingFilename() { return stagingFilename; }
	public String getConverterClassName() { return converterClassName; }
	public String getImporterClassName() { return importerClassName; }

	public Boolean requiresMetafile() { return requiresMetafile; }
	public String getMetaFilename() { return metaFilename; }
	public String getMetaStableID() { return metaStableID; }
	public String getMetaGeneticAlterationType() { return metaGeneticAlterationType; }
	public Boolean getMetaShowProfileInAnalysisTab() { return metaShowProfileInAnalysisTab; }
	public String getMetaProfileName() { return metaProfileName; }
	public String getMetaProfileDescription() { return metaProfileDescription; }

	/**
	 * Function used to get the appropriate download archive method.
	 *
	 * @param dataSourceName String
	 * @return Method
	 */
	public Method getDownloadArchivesMethod(String dataSourceName) {

		// we need to determine correct download archive method on the DatatypeMetadata object
		String downloadArchivesMethodName = ("get" +
											 dataSourceName.split(DataSourcesMetadata.DATA_SOURCE_NAME_DELIMITER)[0].toUpperCase() +
											 "DownloadArchives");
		return ClassLoader.getMethod(this.getClass().getName(), downloadArchivesMethodName);
	}

	/**
	 * Function used to get the appropriate archived files method.
	 *
	 * @param dataSourceName String
	 * @return Method
	 */
	public Method getArchivedFilesMethod(String dataSourceName) {

		// we need to determine correct download archive method on the DatatypeMetadata object
		String archivedFilesMethodName = ("get" +
										  dataSourceName.split(DataSourcesMetadata.DATA_SOURCE_NAME_DELIMITER)[0].toUpperCase() +
										  "ArchivedFiles");
		return ClassLoader.getMethod(this.getClass().getName(), archivedFilesMethodName);
	}
}
