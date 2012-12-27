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
package org.mskcc.cbio.importer.internal;

// imports
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.Importer;
import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.DatabaseUtils;
import org.mskcc.cbio.importer.util.ClassLoader;
import org.mskcc.cbio.importer.model.PortalMetadata;
import org.mskcc.cbio.importer.model.DatatypeMetadata;
import org.mskcc.cbio.importer.model.TumorTypeMetadata;
import org.mskcc.cbio.importer.model.ReferenceMetadata;
import org.mskcc.cbio.importer.model.CancerStudyMetadata;
import org.mskcc.cbio.importer.model.DataSourcesMetadata;
import org.mskcc.cbio.importer.util.Shell;

import org.mskcc.cbio.cgds.scripts.ImportCaseList;
import org.mskcc.cbio.cgds.scripts.ImportCancerStudy;
import org.mskcc.cbio.cgds.scripts.ImportTypesOfCancers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.lang.reflect.Method;

/**
 * Class which implements the Importer interface.
 */
class ImporterImpl implements Importer {

	// our logger
	private static Log LOG = LogFactory.getLog(ImporterImpl.class);

	// ref to configuration
	private Config config;

	// ref to file utils
	private FileUtils fileUtils;

	// ref to database utils
	private DatabaseUtils databaseUtils;

	/**
	 * Constructor.
     *
     * @param config Config
	 * @param fileUtils FileUtils
	 * @param databaseUtils DatabaseUtils
	 */
	public ImporterImpl(Config config, FileUtils fileUtils, DatabaseUtils databaseUtils) {

		// set members
		this.config = config;
        this.fileUtils = fileUtils;
		this.databaseUtils = databaseUtils;
	}

	/**
	 * Imports data for use in the given portal.
	 *
     * @param portal String
	 * @throws Exception
	 */
    @Override
	public void importData(String portal) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("importData()");
		}

        // check args
        if (portal == null) {
            throw new IllegalArgumentException("portal must not be null");
		}

        // get portal metadata
        PortalMetadata portalMetadata = config.getPortalMetadata(portal).iterator().next();
        if (portalMetadata == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("importData(), cannot find PortalMetadata, returning");
            }
            return;
        }

		// clobber db
		if (LOG.isInfoEnabled()) {
			LOG.info("importData(), clobbering existing database...");
		}
		databaseUtils.createDatabase(databaseUtils.getPortalDatabaseName(), true);

		// use mysql to create new schema
		String[] command = new String[] {"mysql",
										 "--user=" + databaseUtils.getDatabaseUser(),
										 "--password=" + databaseUtils.getDatabasePassword(),
										 databaseUtils.getPortalDatabaseName(),
										 "-e",
										 "source " + databaseUtils.getDatabaseSchemaCanonicalPath()};
		if (LOG.isInfoEnabled()) {
			LOG.info("executing: " + Arrays.asList(command));
		}
		if (Shell.exec(Arrays.asList(command), ".")) {
			if (LOG.isInfoEnabled()) {
				LOG.info("create schema is complete.");
			}
		}

		// import reference data
		if (LOG.isInfoEnabled()) {
			LOG.info("importData(), importing reference data...");
		}
		importAllReferenceData();

		// load staging files
		if (LOG.isInfoEnabled()) {
			LOG.info("importData(), loading staging files...");
		}
		loadStagingFiles(portalMetadata);

		if (LOG.isInfoEnabled()) {
			LOG.info("importData(), complete!, exiting...");
		}
	}

	/**
	 * Imports the given reference data.
	 *
     * @param referenceMetadata ReferenceMetadata
	 * @throws Exception
	 */
	@Override
	public void importReferenceData(ReferenceMetadata referenceMetadata) throws Exception {

		// we are either going to use a cgds package importer which has a main method
		// or one of our own classes which implements the Importer interface.
		// Check for a main method, if found, use it, otherwise assume we have a class
		// that implements the Importer interface.

		Method mainMethod = ClassLoader.getMethod(referenceMetadata.getImporterClassName(), "main");
		if (mainMethod != null) {
			String [] args = referenceMetadata.getReferenceFile().split(ReferenceMetadata.REFERENCE_FILE_DELIMITER);
			mainMethod.invoke(null, (Object)args);
		}
		else {
			Object[] args = { config, fileUtils, databaseUtils };
			Importer importer = (Importer)ClassLoader.getInstance(referenceMetadata.getImporterClassName(), args);
			importer.importReferenceData(referenceMetadata);
		}
	}

	/**
	 * Helper function to import all reference data.
	 */
	private void importAllReferenceData() throws Exception {

		// tumor types
		StringBuilder cancerFileContents = new StringBuilder();
		for (TumorTypeMetadata tumorType : config.getTumorTypeMetadata(Config.ALL)) {
			cancerFileContents.append(tumorType.getType());
			cancerFileContents.append(TumorTypeMetadata.TUMOR_TYPE_META_FILE_DELIMITER);
			cancerFileContents.append(tumorType.getName());
			cancerFileContents.append("\n");
		}
		File cancerFile = fileUtils.createTmpFileWithContents(TumorTypeMetadata.TUMOR_TYPE_META_FILE_NAME,
															  cancerFileContents.toString());
		String[] importCancerTypesArgs = { cancerFile.getCanonicalPath() };
		ImportTypesOfCancers.main(importCancerTypesArgs);
		cancerFile.delete();
		
		// iterate over all other reference data types
		for (ReferenceMetadata referenceData : config.getReferenceMetadata(Config.ALL)) {
			if (referenceData.importIntoPortal()) {
				importReferenceData(referenceData);
			}
		}
	}

	/**
	 * Helper function to import all staging data.
	 *
	 * @param portalMetadata PortalMetadata
	 */
	private void loadStagingFiles(PortalMetadata portalMetadata) throws Exception {

		Collection<DatatypeMetadata> datatypeMetadatas = config.getDatatypeMetadata(Config.ALL);
		Collection<DataSourcesMetadata> dataSourcesMetadata = config.getDataSourcesMetadata(Config.ALL);

		// iterate over all cancer studies
		for (CancerStudyMetadata cancerStudyMetadata : config.getCancerStudyMetadata(portalMetadata.getName())) {

			// lets determine if cancer study is in staging directory or studies directory
			String rootDirectory = getCancerStudyRootDirectory(portalMetadata, dataSourcesMetadata, cancerStudyMetadata);

			if (rootDirectory == null) {
				if (LOG.isInfoEnabled()) {
					LOG.info("loadStagingFiles(), cannot find root directory for study: " + cancerStudyMetadata + " skipping...");
				}
				continue;
			}

			// import cancer name / metadata
			String[] args = { cancerStudyMetadata.toString(),
							  (rootDirectory +
							   cancerStudyMetadata.getStudyPath() +
							   File.separator + cancerStudyMetadata.toString() +
							   CancerStudyMetadata.CANCER_STUDY_METADATA_FILE_EXT) };
			ImportCancerStudy.main(args);

			// iterate over all datatypes
			for (DatatypeMetadata datatypeMetadata : config.getDatatypeMetadata(portalMetadata, cancerStudyMetadata)) {

				// get the metafile/staging file for this cancer_study / datatype
				String stagingFilename =  (rootDirectory +
										   cancerStudyMetadata.getStudyPath() +
										   File.separator + datatypeMetadata.getStagingFilename());
				stagingFilename = stagingFilename.replaceAll(DatatypeMetadata.CANCER_STUDY_TAG, cancerStudyMetadata.toString());
				if (datatypeMetadata.requiresMetafile()) {
					String metaFilename = (rootDirectory +
										   cancerStudyMetadata.getStudyPath() +
										   File.separator + datatypeMetadata.getMetaFilename());
					args = new String[] { "--data", stagingFilename, "--meta", metaFilename, "--loadMode", "bulkLoad" };
				}
				else {
					args = new String[] { stagingFilename, cancerStudyMetadata.toString() };
				}
				Method mainMethod = ClassLoader.getMethod(datatypeMetadata.getImporterClassName(), "main");
				mainMethod.invoke(null, (Object)args);
			}

			// process case lists
			args = new String[] { (rootDirectory + File.separator +
								   cancerStudyMetadata.getStudyPath() + File.separator + "case_lists") };
			ImportCaseList.main(args);
		}
	}

	/**
	 * Helper function to determine root directory for cancer study to install.
	 *
	 * @param portalMetadata PortalMetadata
	 * @param dataSourcesMetadata Collection<DataSourcesMetadata>
	 * @param cancerStudyMetadata CancerStudyMetadata
	 * @return String
	 */
	private String getCancerStudyRootDirectory(PortalMetadata portalMetadata,
											   Collection<DataSourcesMetadata> dataSourcesMetadata,
											   CancerStudyMetadata cancerStudyMetadata) {

		// check portal staging area - should work for all tcga
		File cancerStudyDirectory =
			new File(portalMetadata.getStagingDirectory() + File.separator + cancerStudyMetadata.getStudyPath());
		if (cancerStudyDirectory.exists()) {
			return portalMetadata.getStagingDirectory();
		}

		// made it here, check other datasources 
		for (DataSourcesMetadata dataSourceMetadata : dataSourcesMetadata) {
			cancerStudyDirectory =
				new File(dataSourceMetadata.getDownloadDirectory() + File.separator + cancerStudyMetadata.getStudyPath());
			if (cancerStudyDirectory.exists()) {
				return dataSourceMetadata.getDownloadDirectory();
			}
		}

		// outta here
		return null;
	}
}
