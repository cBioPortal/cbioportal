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
package org.mskcc.cbio.importer.internal;

import org.mskcc.cbio.importer.model.*;
import org.mskcc.cbio.importer.Importer;
import org.mskcc.cbio.importer.util.ClassLoader;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.util.CancerStudyReader;
import org.mskcc.cbio.portal.scripts.ImportCaseList;
import org.mskcc.cbio.portal.scripts.ImportTypesOfCancers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.io.*;
import org.apache.commons.io.filefilter.*;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

class CancerStudyImporterImpl implements Importer {

    private static final String DATA_FILE_PREFIX = "data_";
    private static final String META_FILE_PREFIX = "meta_";
    private static final String CASE_LIST_DIRECTORY_NAME = "case_lists";
    private static final String CANCER_STUDY_FILENAME = "meta_study.txt";
    private static final String CANCER_TYPE_FILENAME = "cancer_type.txt";
    private static final String GENETIC_ALTERATION_TYPE_PROP = "genetic_alteration_type";
	private static final Log LOG = LogFactory.getLog(CancerStudyImporterImpl.class);

    private class CancerStudyData
    {
        private CancerStudy cancerStudy;
        private String importerClassName;
        private String stagingFilename;
        private boolean requiresMetadataFile;
        private String metadataFilename;
        
        public CancerStudyData(CancerStudy cancerStudy, String importerClassName, String requiresMetadataFile, String stagingFilename, String metadataFilename)
        {
            this.cancerStudy = cancerStudy;
            this.importerClassName = importerClassName;
            this.requiresMetadataFile = Boolean.parseBoolean(requiresMetadataFile);
            this.stagingFilename = stagingFilename;
            this.metadataFilename = metadataFilename;
        }

        public String getImporterClassName() { return importerClassName; }

        public String[] getImporterClassArgs()
        {
            return ((requiresMetadataFile) ?
                    new String[] { "--returnFromMain", "--data", stagingFilename, "--meta", metadataFilename, "--loadMode", "bulkLoad" } :
                    new String[] { stagingFilename, cancerStudy.getCancerStudyStableId() });
        }
    }

    @Autowired
    @Qualifier("importerClassMap")
    private HashMap<String,String> importerClassMap;

    @Autowired
    @Qualifier("importerClassArgsMap")
    private HashMap<String,String> importerClassArgsMap;
   
    @Override
	public void importData(String portal, Boolean initPortalDatabase, Boolean initTumorTypes, Boolean importReferenceData) throws Exception
    {
		throw new UnsupportedOperationException();
    }

	@Override
	public void importReferenceData(ReferenceMetadata referenceMetadata) throws Exception
    {
		throw new UnsupportedOperationException();
    }

    @Override
    public void importCancerStudy(String cancerStudyDirectoryName, boolean echo, boolean force) throws Exception
    {
        for (File metaStudyFile : listFiles(cancerStudyDirectoryName, FileFilterUtils.nameFileFilter(CANCER_STUDY_FILENAME))) {
            logMessage("importCancerStudy(), found study file: " + metaStudyFile.getCanonicalPath() + ", processing...");
            try {
                processCancerStudy(metaStudyFile, echo, force);
            }
            catch (Exception e) {
                if (e.getMessage() != null) {
                    logMessage(e.getMessage());
                }
            }
        }
    }

    private void processCancerStudy(File metaStudyFile, boolean echo, boolean force) throws Exception
    {
        CancerStudy cancerStudy = CancerStudyReader.loadCancerStudy(metaStudyFile, false, false);
        if (continueIfStudyExists(cancerStudy, force)) {
            String cancerStudyDirectoryName = metaStudyFile.getParent();
            importCancerStudy(cancerStudy, cancerStudyDirectoryName, echo);  // meta file, cancer type
            importCancerStudyData(cancerStudy, cancerStudyDirectoryName, echo);
            importCancerStudyCaseLists(cancerStudyDirectoryName, echo);
        }
    }

    private boolean continueIfStudyExists(CancerStudy cancerStudy, boolean force) throws Exception
    {
        if (DaoCancerStudy.doesCancerStudyExistByStableId(cancerStudy.getCancerStudyStableId())) {
            // study exists, ok to force?
            return (force || getForceFromUser(cancerStudy));
        }
        return true;
    }

    private boolean getForceFromUser(CancerStudy cancerStudy)
    {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Cancer study with stable id: '" +
                         cancerStudy.getCancerStudyStableId() +
                         "' already exists in db, overwrite? [y/n]: ");
        return (scanner.next().equals("y"));
    }

    private void importCancerStudy(CancerStudy cancerStudy, String cancerStudyDirectoryName, boolean echo) throws Exception
    {
        processCancerType(cancerStudy, cancerStudyDirectoryName, echo);
        if (modifyDb(echo)) {
            DaoCancerStudy.addCancerStudy(cancerStudy, true);
        }
    }
    
    private void processCancerType(CancerStudy cancerStudy, String cancerStudyDirectoryName, boolean echo) throws Exception
    {
        String cancerType = cancerStudy.getTypeOfCancerId();
        logMessage("Checking for existing cancer type: " + cancerType);
        TypeOfCancer typeOfCancer = DaoTypeOfCancer.getTypeOfCancerById(cancerStudy.getTypeOfCancerId());
        if (typeOfCancer == null) {
            logMessage("Cancer type does not exist in database, attempting to import cancer type: " + cancerType);
            importCancerType(cancerStudyDirectoryName, echo);
        }
        else if (LOG.isInfoEnabled()) {
            LOG.info("Cancer type already exists in database");
        }
    }

    private void importCancerType(String cancerStudyDirectoryName, boolean echo) throws Exception
    {
        File cancerTypeFile = FileUtils.getFile(cancerStudyDirectoryName, CANCER_TYPE_FILENAME);
        if (!cancerTypeFile.exists()) {
            throw new IllegalArgumentException("Cannot find cancer type file: " + cancerTypeFile.getCanonicalPath() + " aborting!");
        }
        if (modifyDb(echo)) {
            ProgressMonitor pMonitor = new ProgressMonitor();
            pMonitor.setConsoleMode(true);
            ImportTypesOfCancers.load(pMonitor, cancerTypeFile, false);
        }
    }

    private void importCancerStudyData(CancerStudy cancerStudy, String cancerStudyDirectoryName, boolean echo) throws Exception
    {
        for (CancerStudyData cancerStudyData : getCancerStudyData(cancerStudy, cancerStudyDirectoryName)) {
            try {
                Method mainMethod = ClassLoader.getMethod(cancerStudyData.getImporterClassName(), "main");
                if (modifyDb(echo)) {
                    mainMethod.invoke(null, (Object)cancerStudyData.getImporterClassArgs());
                }
            }
            catch (Exception e) {
                String message = (e instanceof InvocationTargetException) ? 
                    ((InvocationTargetException)e).getTargetException().getMessage() : e.getMessage();
                if (message != null) {
                    logMessage(message);
                }
            }
        }
    }

    private Collection<CancerStudyData> getCancerStudyData(CancerStudy cancerStudy, String cancerStudyDirectoryName) throws Exception
    {
        ArrayList<CancerStudyData> cancerStudyDataList = new ArrayList<CancerStudyData>();

        for (File metadataFile : listFiles(cancerStudyDirectoryName, FileFilterUtils.prefixFileFilter(META_FILE_PREFIX))) {
            File dataFile = getDataFile(metadataFile.getCanonicalPath());
            if (dataFile == null) {
                logMessage("Cannot find matching data file, skipping import: " + metadataFile.getCanonicalPath());                 
            }
            else {
                CancerStudyData cancerStudyData = getCancerStudyData(cancerStudy, metadataFile, dataFile);
                if (cancerStudyData != null) {
                    cancerStudyDataList.add(cancerStudyData);
                }
            }
        }

        return cancerStudyDataList;
    }

    private File getDataFile(String metadataFilename)
    {
        File dataFile = FileUtils.getFile(metadataFilename.replace(META_FILE_PREFIX, DATA_FILE_PREFIX));
        return (dataFile.exists()) ? dataFile : null;
    }

    private CancerStudyData getCancerStudyData(CancerStudy cancerStudy, File metadataFile, File dataFile)
    {
        CancerStudyData cancerStudyData = null;

        try {
            String geneticAlterationType = getGeneticAlterationType(metadataFile);
            String importerClassName = getImporterClassName(geneticAlterationType);
            String requiresMetadataFile = getRequiresMetadataFile(importerClassName);
            cancerStudyData = new CancerStudyData(cancerStudy,
                                                  importerClassName,
                                                  requiresMetadataFile,
                                                  metadataFile.getCanonicalPath(),
                                                  dataFile.getCanonicalPath());
        }
        catch (Exception e) {
            logMessage(e.getMessage());
        }

        return cancerStudyData;
    }

    private String getGeneticAlterationType(File metadataFile) throws Exception
    {
        Properties properties = new Properties();
        FileInputStream fis = new FileInputStream(metadataFile);
        properties.load(fis);
        fis.close();
        String property = properties.getProperty(GENETIC_ALTERATION_TYPE_PROP);
        if (property == null) {
            throw new IllegalArgumentException("Cannot find " + GENETIC_ALTERATION_TYPE_PROP + 
                                               " in file, skipping import: " + metadataFile.getCanonicalPath());
        }

        return property;
    }

    private String getImporterClassName(String geneticAlterationType) throws Exception
    {
        if (importerClassMap.containsKey(geneticAlterationType)) {
            return importerClassMap.get(geneticAlterationType);
        }
        else {
            throw new IllegalArgumentException("Unknown " + GENETIC_ALTERATION_TYPE_PROP +
                                               ", skipping import: " + geneticAlterationType);
        }
    }

    private String getRequiresMetadataFile(String importerClassName)
    {
        if (importerClassArgsMap.containsKey(importerClassName)) {
            return importerClassArgsMap.get(importerClassName);
        }
        else {
            throw new IllegalArgumentException("Unknown importer class name" +
                                               ", skipping import: " + importerClassName);
        }
    }

    private void importCancerStudyCaseLists(String cancerStudyDirectoryName, boolean echo) throws Exception
    {
        File caseListDirectory = FileUtils.getFile(cancerStudyDirectoryName, CASE_LIST_DIRECTORY_NAME);
        if (caseListDirectory.exists()) {
            logMessage("Importing case lists found in directory: " + caseListDirectory.getCanonicalPath());
            if (modifyDb(echo)) {
                String[] args = new String[] { caseListDirectory.getCanonicalPath() };
                ImportCaseList.main(args);
            }
        }
        else {
            logMessage("Cannot find case list directory, skipping case list import: " + caseListDirectory.getCanonicalPath());
        }
    }

    private Collection<File> listFiles(String directoryName, IOFileFilter filter) throws Exception
    {
        File directory = new File(directoryName);
        return FileUtils.listFiles(directory, filter, TrueFileFilter.INSTANCE);
    }

    private boolean modifyDb(boolean echo)
    {
        return (!echo);
    }

    private void logMessage(String message)
    {
        if (LOG.isInfoEnabled()) {
            LOG.info(message);
        }
        System.err.println(message);
    }
}
