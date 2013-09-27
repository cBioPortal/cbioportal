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

import java.util.*;
import java.io.File;
import java.lang.reflect.*;

class CancerStudyImporterImpl implements Importer {

    private static final String CASE_LIST_DIRECTORY = "case_lists";
    private static final String CANCER_STUDY_FILENAME = "meta_study.txt";
    private static final String CANCER_TYPE_FILENAME = "cancer_type.txt";
	private static final Log LOG = LogFactory.getLog(CancerStudyImporterImpl.class);
    private static final String IMPORT_SCRIPT_MAP_DELIMITER = ":";

    private class ImporterClass
    {
        private String className;
        private String stagingFilename;
        private boolean requiresMetadata;
        private String metadataFilename;
        
        public ImporterClass(String className, String stagingFilename, String requiresMetadata, String metadataFilename)
        {
            this.className = className;
            this.stagingFilename = stagingFilename;
            this.requiresMetadata = Boolean.parseBoolean(requiresMetadata);
            this.metadataFilename = metadataFilename;
        }

        public boolean requiresMetadata() { return requiresMetadata; }
        public String getImporterClassName() { return className; }
        public String getStagingFilename() { return stagingFilename; }
        public String getMetadataFilename() { return metadataFilename; }

        public String[] getImporterClassArgs(CancerStudy cancerStudy)
        {
            return ((requiresMetadata) ?
                    new String[] { "--returnFromMain", "--data", stagingFilename, "--meta", metadataFilename, "--loadMode", "bulkLoad" } :
                    new String[] { stagingFilename, cancerStudy.getCancerStudyStableId() });
        }
    }

    @Autowired
    @Qualifier("importerClassMap")
    private HashMap<String,String> importerClassMap;
   
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
        File cancerStudyDirectory = new File(cancerStudyDirectoryName);
        for (File metaStudyFile : listFiles(cancerStudyDirectory, CANCER_STUDY_FILENAME)) {
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
            String cancerStudyDirectory = metaStudyFile.getParent();
            importCancerStudy(cancerStudyDirectory, cancerStudy, echo);  // meta file, cancer type
            importCancerStudyData(cancerStudyDirectory, cancerStudy, echo);
            importCancerStudyCaseLists(cancerStudyDirectory, echo);
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

    private void importCancerStudy(String cancerStudyDirectory, CancerStudy cancerStudy, boolean echo) throws Exception
    {
        processCancerType(cancerStudyDirectory, cancerStudy, echo);
        if (modifyDb(echo)) {
            DaoCancerStudy.addCancerStudy(cancerStudy, true);
        }
    }
    
    private void processCancerType(String cancerStudyDirectory, CancerStudy cancerStudy, boolean echo) throws Exception
    {
        String cancerType = cancerStudy.getTypeOfCancerId();
        logMessage("Checking for existing cancer type: " + cancerType);
        TypeOfCancer typeOfCancer = DaoTypeOfCancer.getTypeOfCancerById(cancerStudy.getTypeOfCancerId());
        if (typeOfCancer == null) {
            logMessage("Cancer type does not exist in database, attempting to import cancer type: " + cancerType);
            importCancerType(cancerStudyDirectory, echo);
        }
        else if (LOG.isInfoEnabled()) {
            LOG.info("Cancer type already exists in database");
        }
    }

    private void importCancerType(String cancerStudyDirectory, boolean echo) throws Exception
    {
        File cancerTypeFile = FileUtils.getFile(cancerStudyDirectory, CANCER_TYPE_FILENAME);
        if (!cancerTypeFile.exists()) {
            throw new IllegalArgumentException("Cannot find cancer type file: " + cancerTypeFile.getCanonicalPath() + " aborting!");
        }
        if (modifyDb(echo)) {
            ProgressMonitor pMonitor = new ProgressMonitor();
            pMonitor.setConsoleMode(true);
            ImportTypesOfCancers.load(pMonitor, cancerTypeFile, false);
        }
    }

    private void importCancerStudyData(String cancerStudyDirectory, CancerStudy cancerStudy, boolean echo) throws Exception
    {
        for (File dataFile : getFilesToProcess(cancerStudyDirectory)) {
            try {
                ImporterClass importerClass = getImporterClass(cancerStudyDirectory, dataFile);
                checkForMetadataFile(importerClass);
                Method mainMethod = ClassLoader.getMethod(importerClass.getImporterClassName(), "main");
                if (modifyDb(echo)) {
                    mainMethod.invoke(null, (Object)importerClass.getImporterClassArgs(cancerStudy));
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

    private void importCancerStudyCaseLists(String cancerStudyDirectory, boolean echo) throws Exception
    {
        File caseListDirectory = FileUtils.getFile(cancerStudyDirectory, CASE_LIST_DIRECTORY);
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

    private Collection<File> getFilesToProcess(String cancerStudyDirectory) throws Exception
    {
        ArrayList<File> filesToProcess = new ArrayList<File>();
        for (String dataFilename : importerClassMap.keySet()) {
            File dataFile = FileUtils.getFile(cancerStudyDirectory, dataFilename);
            if (dataFile.exists()) {
                filesToProcess.add(dataFile);
            }
        }
        return filesToProcess;
    }

    private ImporterClass getImporterClass(String cancerStudyDirectory, File dataFile) throws Exception
    {
        // see importScriptMap definition within applicationContext-portalImporterTool.xml for explanation
        String[] importerClassParts = importerClassMap.get(dataFile.getName()).split(IMPORT_SCRIPT_MAP_DELIMITER);
        String metaFilename = (importerClassParts.length == 3) ?
            FileUtils.getFile(cancerStudyDirectory, importerClassParts[2]).getCanonicalPath() : "";
        return new ImporterClass(importerClassParts[0], dataFile.getCanonicalPath(),
                                 importerClassParts[1], metaFilename);
    }

    private Collection<File> listFiles(File directory, String filter) throws Exception
    {
        return FileUtils.listFiles(directory, FileFilterUtils.nameFileFilter(filter), TrueFileFilter.INSTANCE);
    }

    private boolean modifyDb(boolean echo)
    {
        return (!echo);
    }

    private void checkForMetadataFile(ImporterClass importerClass) throws Exception
    {
        if (importerClass.requiresMetadata()) {
            File metadataFile = FileUtils.getFile(importerClass.getMetadataFilename());
            if (!metadataFile.exists()) {
                throw new IllegalArgumentException("Cannot find required metadata file, skipping import: " + metadataFile.getCanonicalPath()); 
            }
        }
    }

    private void logMessage(String message)
    {
        if (LOG.isInfoEnabled()) {
            LOG.info(message);
        }
        System.err.println(message);
    }
}
