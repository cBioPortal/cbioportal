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
import org.mskcc.cbio.importer.util.Shell;
import org.mskcc.cbio.importer.util.DatatypeMetadataUtils;

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
final class ImporterImpl implements Importer {

	// our logger
	private static final Log LOG = LogFactory.getLog(ImporterImpl.class);

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
	public ImporterImpl(final Config config, final FileUtils fileUtils, final DatabaseUtils databaseUtils) {

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
	public void importData(final String portal) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("importData()");
		}

        // check args
        if (portal == null) {
            throw new IllegalArgumentException("portal must not be null");
		}

        // get portal metadata
        PortalMetadata portalMetadata = config.getPortalMetadata(portal);
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
		//importAllReferenceData();

		// move import overrides?

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
	public void importReferenceData(final ReferenceMetadata referenceMetadata) throws Exception {

		// we are either going to use a cgds package importer which has a main method
		// or one of our own classes which implements the Importer interface.
		// Check for a main method, if found, use it, otherwise assume we have a class
		// that implements the Importer interface.

		Method mainMethod = ClassLoader.getMainMethod(referenceMetadata.getImporterClassName());
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
		for (TumorTypeMetadata tumorType : config.getTumorTypeMetadata()) {
			cancerFileContents.append(tumorType.getTumorTypeID());
			cancerFileContents.append(TumorTypeMetadata.TUMOR_TYPE_DELIMITER);
			cancerFileContents.append(tumorType.getTumorTypeDescription());
			cancerFileContents.append("\n");
		}
		File cancerFile = fileUtils.createTmpFileWithContents(TumorTypeMetadata.TUMOR_TYPE_REFERENCE_FILE_NAME,
															  cancerFileContents.toString());
		String[] importCancerTypesArgs = { cancerFile.getCanonicalPath() };
		ImportTypesOfCancers.main(importCancerTypesArgs);
		cancerFile.delete();
		
		// iterate over all other reference data types
		for (ReferenceMetadata referenceData : config.getReferenceMetadata(Config.ALL_METADATA)) {
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
	private void loadStagingFiles(final PortalMetadata portalMetadata) throws Exception {

		Collection<DatatypeMetadata> datatypeMetadatas = config.getDatatypeMetadata();

		// iterate over all cancer studies
		for (String cancerStudy : portalMetadata.getCancerStudies()) {

			// import cancer name / metadata
			String[] args = { cancerStudy,
							  (portalMetadata.getStagingDirectory() +
							   File.separator + cancerStudy + ".txt") };
			ImportCancerStudy.main(args);

			// iterate over all datatypes
			for (String datatype : portalMetadata.getDatatypes()) {

				// get the DatatypeMetadata object
				DatatypeMetadata datatypeMetadata = DatatypeMetadataUtils.getDatatypeMetadata(datatype, datatypeMetadatas);
				if (datatypeMetadata == null) {
					if (LOG.isInfoEnabled()) {
						LOG.info("loadStagingFiles(), unrecognized datatype: " + datatype + ", skipping");
					}
					continue;
				}
				// get the metafile/staging file for this cancer_study / datatype
				String stagingFilename =  (portalMetadata.getStagingDirectory() +
										   File.separator + datatypeMetadata.getStagingFilename());
				stagingFilename = stagingFilename.replaceAll(DatatypeMetadata.CANCER_STUDY_TAG, cancerStudy);
				if (datatypeMetadata.requiresMetafile()) {
					String metaFilename = (portalMetadata.getStagingDirectory() +
										   File.separator + datatypeMetadata.getMetaFilename());
					args = new String[] { "--data", stagingFilename, "--meta", metaFilename, "--loadMode", "bulkLoad" };
				}
				else {
					args = new String[] { stagingFilename, cancerStudy };
				}
				Method mainMethod = ClassLoader.getMainMethod(datatypeMetadata.getImporterClassName());
				mainMethod.invoke(null, (Object)args);
			}

			// process case lists
			args = new String[] { (portalMetadata.getStagingDirectory() + File.separator +
								   cancerStudy + File.separator + "case_lists") };
			ImportCaseList.main(args);
		}
	}
}
