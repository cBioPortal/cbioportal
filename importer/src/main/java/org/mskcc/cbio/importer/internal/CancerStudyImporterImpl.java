/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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
package org.mskcc.cbio.importer.internal;

import org.mskcc.cbio.importer.model.*;
import org.mskcc.cbio.importer.Importer;
import org.mskcc.cbio.importer.Validator;
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

class CancerStudyImporterImpl implements Importer, Validator {

    private static final String DATA_FILE_PREFIX = "data_";
    private static final String META_FILE_PREFIX = "meta_";
    private static final String CASE_LIST_DIRECTORY_NAME = "case_lists";
    private static final String CASE_LIST_WILDCARD = "*.txt";
    private static final String CANCER_STUDY_FILENAME = "meta_study.txt";
    private static final String CANCER_TYPE_FILENAME = "cancer_type.txt";
    private static final int NUM_FIELDS_CANCER_TYPE_RECORD = 5;
	private static final Log LOG = LogFactory.getLog(CancerStudyImporterImpl.class);

    private static enum MetadataProperties
    {
        CANCER_STUDY_IDENTIFIER("cancer_study_identifier"),
        GENETIC_ALTERATION_TYPE("genetic_alteration_type"),
        DATATYPE("datatype"),
        STABLE_ID("stable_id"),
        //SHOW_PROFILE_IN_ANALYSIS_TAB("show_profile_in_analysis_tab"),
        PROFILE_DESCRIPTION("profile_description"),
        PROFILE_NAME("profile_name");

        private String propertyName;
        
        MetadataProperties(String propertyName) { this.propertyName = propertyName; }
        public String toString() { return propertyName; }
    }

    private static enum CaseListProperties
    {
        CANCER_STUDY_IDENTIFIER("cancer_study_identifier"),
        STABLE_ID("stable_id"),
        CASE_LIST_NAME("case_list_name"),
        CASE_LIST_DESCRIPTION("case_list_description"),
        //CASE_LIST_CATEGORY("case_list_category"),
        CASE_LIST_IDS("case_list_ids");

        private String propertyName;
        
        CaseListProperties(String propertyName) { this.propertyName = propertyName; }
        public String toString() { return propertyName; }
    }

    private class CancerStudyData
    {
        private CancerStudy cancerStudy;
        private String importerClassName;
        private String stagingFilename;
        private boolean requiresMetadataFile;
        private String metadataFilename;
        
        public CancerStudyData(CancerStudy cancerStudy, String importerClassName, String requiresMetadataFile, String metadataFilename, String stagingFilename)
        {
            this.cancerStudy = cancerStudy;
            this.importerClassName = importerClassName;
            this.requiresMetadataFile = Boolean.parseBoolean(requiresMetadataFile);
            this.metadataFilename = metadataFilename;
            this.stagingFilename = stagingFilename;
        }

        public String getImporterClassName() { return importerClassName; }
        public String getMetadataFilename() { return metadataFilename; }
        public String getStagingFilename() { return stagingFilename; }

        public String[] getImporterClassArgs()
        {
            return ((requiresMetadataFile) ?
                    new String[] { "--data", stagingFilename, "--meta", metadataFilename, "--loadMode", "bulkLoad" } :
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
        public void importTypesOfCancer() throws Exception
    {
		throw new UnsupportedOperationException();
    }

    @Override
    public void importCaseLists(String portal) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void importCancerStudy(String cancerStudyDirectoryName, boolean skip, boolean force) throws Exception
    {
        for (File metaStudyFile : listFiles(cancerStudyDirectoryName, FileFilterUtils.nameFileFilter(CANCER_STUDY_FILENAME))) {
            logMessage("importCancerStudy(), found study file: " + metaStudyFile.getCanonicalPath() + ", processing...");
            try {
                processCancerStudy(metaStudyFile, skip, force);
            }
            catch (Exception e) {
                if (e.getMessage() != null) {
                    logMessage(e.getMessage());
                }
            }
        }
    }

    @Override
    public boolean validateCancerStudy(String cancerStudyDirectoryName) throws Exception
    {
        boolean status = true;

        Collection<File> metaStudyFiles = validateCancerStudyMetadata(cancerStudyDirectoryName);
        if (metaStudyFiles.size() > 0) {
            for (File metaStudyFile : metaStudyFiles) {
                logMessage("Validating cancer study found in: " + metaStudyFile.getParent());
                try {
                    CancerStudy cancerStudy = CancerStudyReader.loadCancerStudy(metaStudyFile, false, false); // validates metaStudyFile content
                    status = setStatus(status, validateCancerTypeMetadata(cancerStudy, metaStudyFile.getParent()));
                    status = setStatus(status, validateCancerStudyData(cancerStudy, metaStudyFile.getParent()));
                    status = setStatus(status, validateCaseListData(cancerStudy, metaStudyFile.getParent()));
                }
                catch (Exception e) {
                    logMessage(e.getMessage());
                    status = setStatus(status, false);
                }
            }
        }
        else {
            logMessage("No cancer study found in: " + cancerStudyDirectoryName);
            status = setStatus(status, false);
        }

        return status;
    }

    private void processCancerStudy(File metaStudyFile, boolean skip, boolean force) throws Exception
    {
        CancerStudy cancerStudy = CancerStudyReader.loadCancerStudy(metaStudyFile, false, false); // validates metaStudyFile content
        if (continueIfStudyExists(cancerStudy, skip, force)) {
            String cancerStudyDirectoryName = metaStudyFile.getParent();
            if (validateCancerStudy(cancerStudyDirectoryName)) {
                importCancerStudy(cancerStudy, cancerStudyDirectoryName);  // meta file, cancer type
                importCancerStudyData(cancerStudy, cancerStudyDirectoryName);
                importCancerStudyCaseLists(cancerStudyDirectoryName);
            }
            else {
                logMessage("Invalid cancer study found in directory: " + cancerStudyDirectoryName);
            }
        }
    }

    private boolean continueIfStudyExists(CancerStudy cancerStudy, boolean skip, boolean force) throws Exception
    {
        if (DaoCancerStudy.doesCancerStudyExistByStableId(cancerStudy.getCancerStudyStableId())) {
            if (skip) {
                logMessage("Cancer study exists, skip is set, skipping...");
                return false; // don't even ask me, just skip
            }
            if (force) { // don't ask
                logMessage("Cancer study exists, force is set, replacing study...");
                return true;
            }
            return (getForceFromUser(cancerStudy));  // ask
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

    private void importCancerStudy(CancerStudy cancerStudy, String cancerStudyDirectoryName) throws Exception
    {
        processCancerType(cancerStudy, cancerStudyDirectoryName);
        DaoCancerStudy.addCancerStudy(cancerStudy, true);
    }
    
    private void processCancerType(CancerStudy cancerStudy, String cancerStudyDirectoryName) throws Exception
    {
        logMessage("Checking for existing cancer type: " + cancerStudy.getTypeOfCancerId());
        TypeOfCancer typeOfCancer = getCancerTypeRecord(cancerStudy);
        if (typeOfCancer == null) {
            logMessage("Cancer type does not exist in database, attempting to import cancer type...");
            importCancerType(cancerStudyDirectoryName);
        }
        else if (LOG.isInfoEnabled()) {
            LOG.info("Cancer type already exists in database");
        }
    }

    private void importCancerType(String cancerStudyDirectoryName) throws Exception
    {
        File cancerTypeFile = FileUtils.getFile(cancerStudyDirectoryName, CANCER_TYPE_FILENAME);
        if (!cancerTypeFile.exists()) {
            throw new IllegalArgumentException("Cannot find cancer type file: " + cancerTypeFile.getCanonicalPath() + " aborting!");
        }
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);
        ImportTypesOfCancers.load(pMonitor, cancerTypeFile, false);
    }

    private void importCancerStudyData(CancerStudy cancerStudy, String cancerStudyDirectoryName) throws Exception
    {
        for (CancerStudyData cancerStudyData : getCancerStudyData(cancerStudy, cancerStudyDirectoryName)) {
            try {
                Method mainMethod = ClassLoader.getMethod(cancerStudyData.getImporterClassName(), "main");
                mainMethod.invoke(null, (Object)cancerStudyData.getImporterClassArgs());
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
            if (metadataFile.getName().equals(CANCER_STUDY_FILENAME)) continue;
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
        Properties properties = getProperties(metadataFile);
        return properties.getProperty(MetadataProperties.GENETIC_ALTERATION_TYPE.toString());
    }

    private String getImporterClassName(String geneticAlterationType)
    {
        return (importerClassMap.containsKey(geneticAlterationType)) ?
            importerClassMap.get(geneticAlterationType) : null;
    }

    private String getRequiresMetadataFile(String importerClassName)
    {
        return (importerClassArgsMap.containsKey(importerClassName)) ?
            importerClassArgsMap.get(importerClassName) : null;
    }

    private void importCancerStudyCaseLists(String cancerStudyDirectoryName) throws Exception
    {
        File caseListDirectory = FileUtils.getFile(cancerStudyDirectoryName, CASE_LIST_DIRECTORY_NAME);
        if (caseListDirectory.exists()) {
            logMessage("Importing case lists found in directory: " + caseListDirectory.getCanonicalPath());
            String[] args = new String[] { caseListDirectory.getCanonicalPath() };
            ImportCaseList.main(args);
        }
        else {
            logMessage("Cannot find case list directory, skipping case list import: " + caseListDirectory.getCanonicalPath());
        }
    }

    private Collection<File> validateCancerStudyMetadata(String cancerStudyDirectoryName) throws Exception
    {
        Collection<File> metaStudyFiles = listFiles(cancerStudyDirectoryName, FileFilterUtils.nameFileFilter(CANCER_STUDY_FILENAME));
        if (metaStudyFiles.size() == 0) {
            logMessage("No cancers study files (" + CANCER_STUDY_FILENAME + ") found: " + cancerStudyDirectoryName);
        }
        return metaStudyFiles;
    }

    private boolean validateCancerTypeMetadata(CancerStudy cancerStudy, String cancerStudyDirectoryName) throws Exception
    {
        boolean status = true;

        if (getCancerTypeRecord(cancerStudy) != null) return status;

        File cancerTypeFile = FileUtils.getFile(cancerStudyDirectoryName, CANCER_TYPE_FILENAME);
        if (cancerTypeFile.exists()) {
            String[] cancerTypeRecord = getCancerTypeRecord(cancerTypeFile);
            if (cancerTypeRecord == null || cancerTypeRecord.length != NUM_FIELDS_CANCER_TYPE_RECORD) {
                logMessage("Missing or corrupt record in " + CANCER_TYPE_FILENAME + ": " +
                           cancerTypeFile.getCanonicalPath());
                status = setStatus(status, false);
            }
            else {
                String typeOfCancerId = cancerTypeRecord[0].trim();
                if (!typeOfCancerId.equals(cancerStudy.getTypeOfCancerId())) {
                    File cancerStudyFile = FileUtils.getFile(cancerStudyDirectoryName, CANCER_STUDY_FILENAME);
                    logCancerTypeMismatch(cancerStudy, typeOfCancerId,
                                          cancerTypeFile.getCanonicalPath(), cancerStudyFile.getCanonicalPath());
                    status = setStatus(status, false);
                }
            }
        }
        else {
            logMessage("Unknown cancer type and a cancer type file cannot be found: " + cancerTypeFile.getCanonicalPath());
            status = setStatus(status, false);
        }

        return status;
    }

    private boolean validateCancerStudyData(CancerStudy cancerStudy, String cancerStudyDirectoryName) throws Exception
    {
        boolean status = true;

        HashMap<String, String> stableIds = new HashMap<String, String>();
        Collection<CancerStudyData> cancerStudyDataCol = getCancerStudyData(cancerStudy, cancerStudyDirectoryName);
        if (cancerStudyDataCol.size() > 0) {
            for (CancerStudyData cancerStudyData : cancerStudyDataCol) {
                try {
                    File metadataFile = FileUtils.getFile(cancerStudyData.getMetadataFilename());
                    logMessage("Validating cancer study metadata file: " + metadataFile.getCanonicalPath());
                    Properties properties = getProperties(metadataFile);
                    status = setStatus(status,
                                       validateMetadataProperties(cancerStudy, cancerStudyDirectoryName, properties, metadataFile.getCanonicalPath(), stableIds));
                    File stagingFile = FileUtils.getFile(cancerStudyData.getStagingFilename());
                    if (!stagingFile.exists()) {
                        logMessage("Cannot find data file: " + stagingFile.getCanonicalPath());
                        status = setStatus(status, false);
                    }
                }
                catch (Exception e) {
                    logMessage(e.getMessage());
                    status = setStatus(status, false);
                }
            }
        }
        else {
            logMessage("No cancer study data found in : " + cancerStudyDirectoryName);
            status = setStatus(status, false);
        }

        return status;
    }

    private boolean validateCaseListData(CancerStudy cancerStudy, String cancerStudyDirectoryName) throws Exception
    {
        boolean status = true;

        File caseListDirectory = FileUtils.getFile(cancerStudyDirectoryName, CASE_LIST_DIRECTORY_NAME);
        if (caseListDirectory.exists()) {
            logMessage("Validating case list files found in: " + caseListDirectory.getCanonicalPath());
            Collection<File> caseListFiles = listFiles(caseListDirectory.getCanonicalPath(), new WildcardFileFilter(CASE_LIST_WILDCARD));
            if (caseListFiles.isEmpty()) {
                logMessage("Caselist directory is empty: " + caseListDirectory.getCanonicalPath());
                status = setStatus(status, false);
            }
            else {
                HashMap<String, String> stableIds = new HashMap<String, String>();
                for (File caseListFile : caseListFiles) {
                    try {
                        logMessage("Validating case list file: " + caseListFile.getCanonicalPath());
                        Properties properties = getProperties(caseListFile);
                        status = setStatus(status,
                                           validateCaseListProperties(cancerStudy, cancerStudyDirectoryName, properties, caseListFile.getCanonicalPath(), stableIds));
                    }
                    catch (Exception e) {
                        logMessage(e.getMessage());
                        status = setStatus(status, false);
                    }
                }
            }
        }
        else {
            logMessage("Case list directory is not found: " + caseListDirectory.getCanonicalPath());
            status = setStatus(status, false);
        }
        
        return status;
    }

    private boolean validateMetadataProperties(CancerStudy cancerStudy, String cancerStudyDirectoryName,
                                               Properties properties, String metadataFilename,
                                               Map<String, String> stableIds) throws Exception
    {
        boolean status = true;

        for (MetadataProperties property : MetadataProperties.values()) {
            String propertyValue = properties.getProperty(property.toString());
            if (propertyValue == null || propertyValue.isEmpty()) {
                logMessage("Missing or empty property (property name, file): " +
                           property + ", " + metadataFilename);
                status = setStatus(status, false);
            }
            else if (property.equals(MetadataProperties.CANCER_STUDY_IDENTIFIER)) {
                status = setStatus(status,
                                   validateCancerStudyStableId(cancerStudy, cancerStudyDirectoryName,
                                                               propertyValue, metadataFilename));
            }
            else if (property.equals(MetadataProperties.GENETIC_ALTERATION_TYPE)) {
                status = setStatus(status, validateGeneticAlterationType(propertyValue));
            }
            else if (property.equals(MetadataProperties.STABLE_ID)) {
                status = setStatus(status, validateStableId(cancerStudy, propertyValue, stableIds));
            }
        }

        return status;
    }

    private boolean validateCaseListProperties(CancerStudy cancerStudy, String cancerStudyDirectoryName,
                                               Properties properties, String caseListFilename,
                                               Map<String, String> stableIds) throws Exception
    {
        boolean status = true;

        for (CaseListProperties property : CaseListProperties.values()) {
            String propertyValue = properties.getProperty(property.toString());
            if (propertyValue == null || propertyValue.isEmpty()) {
                logMessage("Missing or empty property (property name, file): " +
                           property + ", " + caseListFilename);
                status = setStatus(status, false);
            }
            else if (property.equals(CaseListProperties.CANCER_STUDY_IDENTIFIER)) {
                status = setStatus(status,
                                   validateCancerStudyStableId(cancerStudy, cancerStudyDirectoryName,
                                                               propertyValue, caseListFilename));
            }
            else if (property.equals(MetadataProperties.STABLE_ID)) {
                status = setStatus(status,
                                   validateStableId(cancerStudy, propertyValue, stableIds));
            }
        }

        return status;
    }

    private boolean validateCancerStudyStableId(CancerStudy cancerStudy, String cancerStudyDirectoryName,
                                             String cancerStudyStableIdFound, String metadataFilename) throws Exception
    {
        boolean status = true;

        String cancerStudyStableId = cancerStudy.getCancerStudyStableId();
        if (!cancerStudyStableIdFound.equals(cancerStudyStableId)) {
            File cancerStudyFile = FileUtils.getFile(cancerStudyDirectoryName, CANCER_STUDY_FILENAME);
            logCancerStudyStableIdMismatch(cancerStudy, cancerStudyStableIdFound,
                                           cancerStudyFile.getCanonicalPath(), metadataFilename);
            status = setStatus(status, false);
        }

        return status;
    }

    private boolean validateGeneticAlterationType(String geneticAlterationType)
    {
        boolean status = true;

        if (!importerClassMap.containsKey(geneticAlterationType)) {
            logMessage("Unknown genetic alteration type found: " + geneticAlterationType);
            status = setStatus(status, false);
        }

        return status;
    }

    private boolean validateStableId(CancerStudy cancerStudy, String stableIdFound, Map<String,String> stableIds)
    {
        boolean status = true;

        if (stableIds.containsKey(stableIdFound)) {
            logMessage("Duplicate stable id: " + stableIdFound);
            status = setStatus(status, false);
        }
        else {
            stableIds.put(stableIdFound, stableIdFound);
        }
        if (!stableIdFound.startsWith(cancerStudy.getCancerStudyStableId())) {
            logMessage("Stable Id should start with cancer study identifier: " + stableIdFound);
            status = setStatus(status, false);
        }

        return status;
    }
                                                
    private Properties getProperties(File file) throws Exception
    {
        Properties properties = new Properties();
        FileInputStream fis = new FileInputStream(file);
        properties.load(fis);
        fis.close();
        return properties;
    }

    TypeOfCancer getCancerTypeRecord(CancerStudy cancerStudy) throws Exception
    {
        return DaoTypeOfCancer.getTypeOfCancerById(cancerStudy.getTypeOfCancerId());
    }

    String[] getCancerTypeRecord(File cancerTypeFile) throws Exception
    {
        int lineCounter = 0;
        String[] tokens = null;
        Scanner scanner = new Scanner(cancerTypeFile);
        while (scanner.hasNextLine()) {
            if (++lineCounter == 1) {
                tokens = scanner.nextLine().split("\t", -1);
            }
            else {
                logMessage("Multiple records in " + cancerTypeFile.getCanonicalPath());
                break;
            }
        }

        return tokens;
    }

    private Collection<File> listFiles(String directoryName, IOFileFilter filter) throws Exception
    {
        File directory = new File(directoryName);
        return FileUtils.listFiles(directory, filter, TrueFileFilter.INSTANCE);
    }

    private void logCancerTypeMismatch(CancerStudy cancerStudy, String cancerTypeFound,
                                       String cancerStudyFilename, String cancerTypeFilename)
    {
        logMessage("Type of cancer id mismatch:\n" + 
                   "\t'" + cancerStudy.getTypeOfCancerId() +
                   "' found in: " + cancerStudyFilename + "\n" +
                   "\t'" + cancerTypeFound +
                   "' found in: " + cancerTypeFilename);
    }

    private boolean setStatus (boolean currentStatus, boolean newStatus)
    {
        if (newStatus == false) {
            currentStatus = newStatus;
        }
        return currentStatus;
    }

    private void logCancerStudyStableIdMismatch(CancerStudy cancerStudy, String cancerStudyStableIdFound,
                                                String cancerStudyFilename, String metadataFilename)
    {
        logMessage("Cancer study stable id mismatch:\n" + 
                   "\t'" + cancerStudy.getCancerStudyStableId() +
                   "' found in: " + cancerStudyFilename + "\n" +
                   "\t'" + cancerStudyStableIdFound +
                   "' found in: " + metadataFilename);
    }

    private void logMessage(String message)
    {
        if (LOG.isInfoEnabled()) {
            LOG.info(message);
        }
        System.err.println(message);
    }
}
