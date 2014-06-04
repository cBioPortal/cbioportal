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
package org.mskcc.cbio.importer.internal;

// imports
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.Importer;
import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.DatabaseUtils;
import org.mskcc.cbio.importer.util.ClassLoader;
import org.mskcc.cbio.importer.model.CaseListMetadata;
import org.mskcc.cbio.importer.model.PortalMetadata;
import org.mskcc.cbio.importer.model.DatatypeMetadata;
import org.mskcc.cbio.importer.model.TumorTypeMetadata;
import org.mskcc.cbio.importer.model.ReferenceMetadata;
import org.mskcc.cbio.importer.model.CancerStudyMetadata;
import org.mskcc.cbio.importer.model.DataSourcesMetadata;
import org.mskcc.cbio.importer.util.Shell;
import org.mskcc.cbio.importer.util.MetadataUtils;
import org.mskcc.cbio.importer.util.MutationFileUtil;

import org.mskcc.cbio.portal.scripts.ImportCaseList;
import org.mskcc.cbio.portal.scripts.ImportCancerStudy;
import org.mskcc.cbio.portal.scripts.ImportTypesOfCancers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.*;
import java.lang.reflect.Method;

/**
 * Class which implements the Importer interface.
 */
class ImporterImpl implements Importer {

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
	 * @param initPortalDatabase Boolean
	 * @param initTumorTypes Boolean
	 * @param importReferenceData Boolean
	 * @throws Exception
	 */
    @Override
	public void importData(String portal, Boolean initPortalDatabase, Boolean initTumorTypes, Boolean importReferenceData) throws Exception {

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

		// init portal db if desired
		if (initPortalDatabase) {
			if (LOG.isInfoEnabled()) {
				LOG.info("importData(), clobbering existing database...");
			}
			databaseUtils.createDatabase(databaseUtils.getPortalDatabaseName(), false);
			if (databaseUtils.executeScript(databaseUtils.getPortalDatabaseName(),
											databaseUtils.getPortalDatabaseSchema(),
											databaseUtils.getDatabaseUser(),
											databaseUtils.getDatabasePassword())) {
				if (LOG.isInfoEnabled()) {
					LOG.info("create schema is complete.");
				}
			}
			else if (LOG.isInfoEnabled()) {
				LOG.info("error creating schema, aborting...");
				return;
			}
		}

		// import tumor types
		if (initTumorTypes) {
			if (LOG.isInfoEnabled()) {
				LOG.info("importData(), importing tumor types...");
			}
			importTypesOfCancer();
		}

		// import reference data if desired
		if (importReferenceData) {
			if (LOG.isInfoEnabled()) {
				LOG.info("importData(), importing reference data...");
			}
			importAllReferenceData();
		}

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

		String importerName = referenceMetadata.getImporterName();

		if (LOG.isInfoEnabled()) {
			LOG.info("importReferenceData(), importerName: " + importerName);
		}

		if (importByImporter(referenceMetadata)) {
			// if imported by Importer
			return;
                }

		Object[] args = { config, fileUtils, databaseUtils };
		if (Shell.exec(referenceMetadata, this, args, ".")) {
			if (LOG.isInfoEnabled()) {
				LOG.info("importReferenceData(), successfully executed importer.");
			}
		}
		else if (LOG.isInfoEnabled()) {
			LOG.info("importReferenceData(), failure executing importer.");
		}
	}

    /**
     * Imports all cancer studies found within the given directory.
     * If force is set, user will not be prompted to override existing cancer study.
     *
     * @param cancerStudyDirectoryName
     * @param skip
     * @param force
     */
    @Override
    public void importCancerStudy(String cancerStudyDirectoryName, boolean skip, boolean force) throws Exception
    {
		throw new UnsupportedOperationException();
    }
        
    private boolean importByImporter(ReferenceMetadata referenceMetadata) throws Exception {
		// we may be dealing with a class that implements the importer interface
		String importerName = referenceMetadata.getImporterName();
		try {
			Class<?> clazz = Class.forName(importerName);
			if (Class.forName("org.mskcc.cbio.importer.Importer").isAssignableFrom(clazz)) {
				Object[] importerArgs = { config, fileUtils, databaseUtils };
				Importer importer = (Importer)ClassLoader.getInstance(importerName, importerArgs);
				importer.importReferenceData(referenceMetadata);
                if (LOG.isInfoEnabled()) {
					LOG.info("importReferenceData(), successfully executed " + clazz + ".");
				}
				return true;
			}
                
			return false;
		} catch (java.lang.ClassNotFoundException ex) {
			return false;
		}
	}

	/**
	 * Helper function to import tumor type metadata.
	 */
        @Override
	public void importTypesOfCancer() throws Exception {
		// tumor types
		StringBuilder cancerFileContents = new StringBuilder();
		for (TumorTypeMetadata tumorType : config.getTumorTypeMetadata(Config.ALL)) {
			cancerFileContents.append(tumorType.getType());
			cancerFileContents.append(TumorTypeMetadata.TUMOR_TYPE_META_FILE_DELIMITER);
			cancerFileContents.append(tumorType.getName());
            cancerFileContents.append(TumorTypeMetadata.TUMOR_TYPE_META_FILE_DELIMITER);
            cancerFileContents.append(tumorType.getClinicalTrialKeywords());
            cancerFileContents.append(TumorTypeMetadata.TUMOR_TYPE_META_FILE_DELIMITER);
            cancerFileContents.append(tumorType.getDedicatedColor());
            cancerFileContents.append(TumorTypeMetadata.TUMOR_TYPE_META_FILE_DELIMITER);
            cancerFileContents.append(tumorType.getShortName());
            cancerFileContents.append("\n");
		}
		File cancerFile = fileUtils.createTmpFileWithContents(TumorTypeMetadata.TUMOR_TYPE_META_FILE_NAME,
															  cancerFileContents.toString());
		String[] importCancerTypesArgs = { cancerFile.getCanonicalPath() };
		ImportTypesOfCancers.main(importCancerTypesArgs);
		cancerFile.delete();
	}

	/**
	 * Helper function to import case lists.
	 */
        @Override
        public void importCaseLists(String portal) throws Exception {
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
            
            // iterate over all cancer studies
            for (CancerStudyMetadata cancerStudyMetadata : config.getCancerStudyMetadata(portalMetadata.getName())) {
                importCaseLists(portalMetadata, cancerStudyMetadata);
            }
        }
        
	private void importCaseLists(PortalMetadata portalMetadata, CancerStudyMetadata cancerStudyMetadata) throws Exception {
		Collection<DataSourcesMetadata> dataSourcesMetadata = config.getDataSourcesMetadata(Config.ALL);
                String rootDirectory = MetadataUtils.getCancerStudyRootDirectory(portalMetadata, dataSourcesMetadata, cancerStudyMetadata);
                // create missing case lists
                List<String> missingCaseListFilenames = fileUtils.getMissingCaseListFilenames(rootDirectory, cancerStudyMetadata);

                if (!missingCaseListFilenames.isEmpty()) {
                        if (LOG.isInfoEnabled()) {
                                LOG.info("loadStagingFile(), the following case lists are missing and if data files are available will be generated: " + missingCaseListFilenames);
                        }
                        // create missing caselists
                        fileUtils.generateCaseLists(false, true, rootDirectory, cancerStudyMetadata);
                }

                // process case lists
                String caseListDirectory = (rootDirectory + File.separator + cancerStudyMetadata.getStudyPath() + File.separator + "case_lists");
                String[] args = new String[] { caseListDirectory };
                if (LOG.isInfoEnabled()) {
                        LOG.info("loadStagingFile(), ImportCaseList:main(), with args: " + Arrays.asList(args));
                }
                ImportCaseList.main(args);

                if (!missingCaseListFilenames.isEmpty()) {
                        if (LOG.isInfoEnabled()) {
                                LOG.info("loadStagingFile(), deleting auto-generated case list files...");
                        }
                        // remove missing caselists that were just created
                        for (String missingCaseListFilename : missingCaseListFilenames) {
                                fileUtils.deleteFile(new File(missingCaseListFilename));
                        }
                        File caseListDir = new File(caseListDirectory);
                        if (fileUtils.directoryIsEmpty(caseListDir)) fileUtils.deleteDirectory(caseListDir);
                }
	}

	/**
	 * Helper function to import all reference data.
	 */
	private void importAllReferenceData() throws Exception {
		// iterate over all other reference data types
		for (ReferenceMetadata referenceData : config.getReferenceMetadata(Config.ALL)) {
			if (referenceData.getImport()) {
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
			String rootDirectory = MetadataUtils.getCancerStudyRootDirectory(portalMetadata, dataSourcesMetadata, cancerStudyMetadata);

			if (rootDirectory == null) {
				if (LOG.isInfoEnabled()) {
					LOG.info("loadStagingFiles(), cannot find root directory for study: " + cancerStudyMetadata + " skipping...");
				}
				continue;
			}

			// import cancer name / metadata
			boolean createdCancerStudyMetadataFile = false;
			String cancerStudyMetadataFile = (rootDirectory + File.separator +
											  cancerStudyMetadata.getStudyPath() + File.separator +
											  cancerStudyMetadata.getCancerStudyMetadataFilename());
			if (!(new File(cancerStudyMetadataFile)).exists()) {
				if (LOG.isInfoEnabled()) {
					LOG.info("loadStagingFile(), cannot find cancer study metadata file: " + cancerStudyMetadataFile + ", creating...");
				}
				fileUtils.writeCancerStudyMetadataFile(rootDirectory, cancerStudyMetadata, -1);
				createdCancerStudyMetadataFile = true;
			}
            else {
                if (cancerStudyMetadataNeedsUpdating(cancerStudyMetadataFile, cancerStudyMetadata)) {
                    fileUtils.writeCancerStudyMetadataFile(rootDirectory, cancerStudyMetadata, -1);
                }
            }
			String[] args = { cancerStudyMetadataFile };
			if (LOG.isInfoEnabled()) {
				LOG.info("loadStagingFiles(), Importing cancer study metafile: " + cancerStudyMetadataFile);
			}
			ImportCancerStudy.main(args);

			// iterate over all datatypes
			for (DatatypeMetadata datatypeMetadata : config.getDatatypeMetadata(portalMetadata, cancerStudyMetadata)) {

				// get the metafile/staging file for this cancer_study / datatype
				String stagingFilename = getImportFilename(rootDirectory, cancerStudyMetadata, datatypeMetadata.getStagingFilename());

				// datatype might not exists for cancer study
                boolean createdZScoreFile = false;
				if (!(new File(stagingFilename)).exists()) {
                    if (isZScoreFile(stagingFilename, datatypeMetadata) &&
                        canCreateZScoreFile(rootDirectory, cancerStudyMetadata, datatypeMetadata)) {
                        if (createZScoreFile(rootDirectory, cancerStudyMetadata, datatypeMetadata)) {
                            createdZScoreFile = true;
                        }
                        else {
                            continue;
                        }
                    }
                    else {
                        if (LOG.isInfoEnabled()) {
                            LOG.info("loadStagingFile(), cannot find staging file: " + stagingFilename + ", skipping...");
                        }
                        continue;
                    }
				}
				// if MAF, oncotate
				if (stagingFilename.endsWith(DatatypeMetadata.MUTATIONS_STAGING_FILENAME)) {
					stagingFilename = getOncotatedFile(stagingFilename);
				}
				if (datatypeMetadata.requiresMetafile()) {
					String metaFilename = getImportFilename(rootDirectory, cancerStudyMetadata, datatypeMetadata.getMetaFilename());
					args = new String[] { "--data", stagingFilename, "--meta", metaFilename, "--loadMode", "bulkLoad" };
				}
				else {
					args = new String[] { stagingFilename, cancerStudyMetadata.toString() };
				}
				if (LOG.isInfoEnabled()) {
					LOG.info("loadStagingFile(), attempting to run: " + datatypeMetadata.getImporterClassName() +
							 ":main(), with args: " + Arrays.asList(args));
				}
				Method mainMethod = ClassLoader.getMethod(datatypeMetadata.getImporterClassName(), "main");
				mainMethod.invoke(null, (Object)args);

				// clean up
				if (!stagingFilename.equals(getImportFilename(rootDirectory, cancerStudyMetadata, datatypeMetadata.getStagingFilename()))) {
					fileUtils.deleteFile(new File(stagingFilename));
				}
                if (createdZScoreFile) {
					fileUtils.deleteFile(new File(stagingFilename));
                    fileUtils.deleteFile(new File(stagingFilename.replace("data_", "meta_")));
                }
			}

			importCaseLists(portalMetadata, cancerStudyMetadata);
                        
                        if (createdCancerStudyMetadataFile) fileUtils.deleteFile(new File(cancerStudyMetadataFile));
		}
	}

	/**
	 * Helper function to determine the proper staging or meta file to import.
	 *
	 * @param rootDirectory String
	 * @param cancerStudyMetadata CancerStudyMetadata
	 * @param filename String
	 * @return String
	 * @throws Exception
	 */
	private String getImportFilename(String rootDirectory, CancerStudyMetadata cancerStudyMetadata, String filename) throws Exception {
		String stagingFilename = (rootDirectory + File.separator + cancerStudyMetadata.getStudyPath() + File.separator + filename);
		stagingFilename = stagingFilename.replaceAll(DatatypeMetadata.CANCER_STUDY_TAG, cancerStudyMetadata.toString());
		return stagingFilename;
	}

	private String getOncotatedFile(String stagingFilename) throws Exception
	{
		if (MutationFileUtil.isOncotated(stagingFilename)) {
			return stagingFilename;
		}
		File stagingFile = new File(stagingFilename);
		File tmpMAF = org.apache.commons.io.FileUtils.getFile(org.apache.commons.io.FileUtils.getTempDirectory(),
															  ""+System.currentTimeMillis()+".tmpMAF");

		// oncotate the MAF (input is tmp maf, output is original maf)
		fileUtils.oncotateMAF(FileUtils.FILE_URL_PREFIX + stagingFile.getCanonicalPath(),
							  FileUtils.FILE_URL_PREFIX + tmpMAF.getCanonicalPath());
		return tmpMAF.getCanonicalPath();
	}

    private boolean cancerStudyMetadataNeedsUpdating(String cancerStudyMetadataFilename, CancerStudyMetadata cancerStudyMetadata) throws Exception
    {
        Properties properties = getProperties(cancerStudyMetadataFilename);
        return (propertyNeedsUpdating(properties.getProperty("name"), cancerStudyMetadata.getName()) ||
                propertyNeedsUpdating(properties.getProperty("description"), cancerStudyMetadata.getDescription()) ||
                propertyNeedsUpdating(properties.getProperty("citation"), cancerStudyMetadata.getCitation()) ||
                propertyNeedsUpdating(properties.getProperty("pmid"), cancerStudyMetadata.getPMID()) ||
                propertyNeedsUpdating(properties.getProperty("groups"), cancerStudyMetadata.getGroups()) ||
                propertyNeedsUpdating(properties.getProperty("short_name"), cancerStudyMetadata.getShortName()));
    }

    private Properties getProperties(String cancerStudyMetadataFilename) throws Exception
    {
        Properties properties = new Properties();
        File metaStudyFile = new File(cancerStudyMetadataFilename);
        properties.load(new FileInputStream(metaStudyFile));
        return properties;
    }

    private boolean propertyNeedsUpdating(String metaStudyFileProperty, String cancerStudyMetadataProperty)
    {
        if (!cancerStudyMetadataProperty.isEmpty() &&
            (metaStudyFileProperty == null || metaStudyFileProperty.isEmpty())) {
            return true;
        }
        if (!cancerStudyMetadataProperty.isEmpty() &&
            metaStudyFileProperty != null &&
            !cancerStudyMetadataProperty.equals(metaStudyFileProperty.trim())) {
            return true;
        }
        if (cancerStudyMetadataProperty.isEmpty() &&
            metaStudyFileProperty != null) {
            return true;
        }
        return false;
    }

    private boolean isZScoreFile(String stagingFilename, DatatypeMetadata datatypeMetadata)
    {
        return (stagingFilename.endsWith(datatypeMetadata.ZSCORE_STAGING_FILENAME_SUFFIX));
    }
    
    private boolean canCreateZScoreFile(String rootDirectory, CancerStudyMetadata cancerStudyMetadata, DatatypeMetadata datatypeMetadata) throws Exception
    {
        boolean canCreateZScoreFile = true;
        for (String dependency : datatypeMetadata.getDependencies()) {
            if (dependency.isEmpty()) {
                canCreateZScoreFile = false;
                break;
            }
            DatatypeMetadata dependencyDatatypeMetadata = config.getDatatypeMetadata(dependency).iterator().next();
            String dependencyStagingFilename = getImportFilename(rootDirectory, cancerStudyMetadata, dependencyDatatypeMetadata.getStagingFilename());
            if (!(new File(dependencyStagingFilename)).exists()) {
                canCreateZScoreFile = false;
                break;
            }   
        }
        return canCreateZScoreFile;
    }

    private boolean createZScoreFile(String rootDirectory, CancerStudyMetadata cancerStudyMetadata, DatatypeMetadata datatypeMetadata) throws Exception
    {
        ArrayList<DatatypeMetadata> dependencies = new ArrayList<DatatypeMetadata>();
        for (String dependency : datatypeMetadata.getDependencies()) {
            dependencies.add(config.getDatatypeMetadata(dependency).iterator().next());
        }
        boolean fileCreated = fileUtils.writeZScoresStagingFile(rootDirectory, cancerStudyMetadata, datatypeMetadata,
                                                 dependencies.toArray(new DatatypeMetadata[dependencies.size()]));

		if (fileCreated && datatypeMetadata.requiresMetafile()){
			fileUtils.writeMetadataFile(rootDirectory, cancerStudyMetadata, datatypeMetadata, null);
		}	

        return fileCreated;
    }
}
