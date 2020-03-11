package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

import java.io.*;
import joptsimple.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

import org.apache.commons.collections.map.MultiKeyMap;

public class ImportResourceData extends ConsoleRunnable {

    public static final String DELIMITER = "\t";
    public static final String METADATA_PREFIX = "#";
    public static final String SAMPLE_ID_COLUMN_NAME = "SAMPLE_ID";
    public static final String PATIENT_ID_COLUMN_NAME = "PATIENT_ID";
    public static final String RESOURCE_ID_COLUMN_NAME = "RESOURCE_ID";
    public static final String URL_COLUMN_NAME = "URL";
    public static final String SAMPLE_TYPE_COLUMN_NAME = "SAMPLE_TYPE";
    private int numSampleSpecificResourcesAdded = 0;
    private int numPatientSpecificResourcesAdded = 0;
    private int numStudySpecificResourcesAdded = 0;
    private int numEmptyResourcesSkipped = 0;
    private int numSamplesProcessed = 0;

    private static Properties properties;

    private File resourceDataFile;
    private CancerStudy cancerStudy;
    private ResourceTypes resourceType;
    private boolean relaxed;
    private Set<String> patientIds = new HashSet<String>();

    public static enum MissingResourceValues {
        NOT_APPLICABLE("Not Applicable"), NOT_AVAILABLE("Not Available"), PENDING("Pending"),
        DISCREPANCY("Discrepancy"), COMPLETED("Completed"), NULL("null"), MISSING(""), NA("NA");

        private String propertyName;

        MissingResourceValues(String propertyName) {
            this.propertyName = propertyName;
        }

        public String toString() {
            return propertyName;
        }

        static public boolean has(String value) {
            if (value == null)
                return false;
            if (value.trim().equals(""))
                return true;
            try {
                value = value.replaceAll("[\\[|\\]]", "");
                value = value.replaceAll(" ", "_");
                return valueOf(value.toUpperCase()) != null;
            } catch (IllegalArgumentException x) {
                return false;
            }
        }

        static public String getNotAvailable() {
            return "[" + NOT_AVAILABLE.toString() + "]";
        }
    }

    public static enum ResourceTypes {
        PATIENT("PATIENT"), SAMPLE("SAMPLE"), STUDY("STUDY");

        private String resourceTypes;

        ResourceTypes(String resourceTypes) {
            this.resourceTypes = resourceTypes;
        }

        public String toString() {
            return resourceTypes;
        }
    }

    public void setFile(CancerStudy cancerStudy, File resourceDataFile, String resourceType, boolean relaxed) {
        this.cancerStudy = cancerStudy;
        this.resourceDataFile = resourceDataFile;
        this.resourceType = ResourceTypes.valueOf(resourceType);
        this.relaxed = relaxed;
    }

    public void importData() throws Exception {
        // if bulkLoading is ever turned off,
        // code has to be added to check whether
        // a resource data update should be
        // perform instead of an insert
        MySQLbulkLoader.bulkLoadOn();

        if (relaxed) {
            MySQLbulkLoader.relaxedModeOn();
        }

        FileReader reader = new FileReader(resourceDataFile);
        BufferedReader buff = new BufferedReader(reader);
        List<ResourceDefinition> resources = grabResources(buff);
        String currentLine = buff.readLine();
        String[] headerNames = currentLine.split("\t");
        
        int patientIdIndex = findPatientIdColumn(headerNames);
        int sampleIdIndex = findSampleIdColumn(headerNames);
        int resourceIdIndex = findResourceIdColumn(headerNames);
        int urlIndex = findURLColumn(headerNames);

        // validate required columns:
        if (resourceIdIndex < 0) {
            throw new RuntimeException("Aborting owing to failure to find " + RESOURCE_ID_COLUMN_NAME
                    + " in file. Please check your file format and try again.");
        }
        if (urlIndex < 0) {
            throw new RuntimeException("Aborting owing to failure to find " + URL_COLUMN_NAME
                    + " in file. Please check your file format and try again.");
        }
        if ((resourceType.toString().equalsIgnoreCase("sample") || resourceType.toString().equalsIgnoreCase("patient")) && patientIdIndex < 0) {
            // PATIENT_ID is required in both patient and sample file types:
            throw new RuntimeException("Aborting owing to failure to find " + PATIENT_ID_COLUMN_NAME
                    + " in file. Please check your file format and try again.");
        }
        if (resourceType.toString().equalsIgnoreCase("sample") && sampleIdIndex < 0) {
            // SAMPLE_ID is required in SAMPLE file type:
            throw new RuntimeException("Aborting owing to failure to find " + SAMPLE_ID_COLUMN_NAME
                    + " in file. Please check your file format and try again.");
        }
        importData(buff, resources, headerNames);

        if (MySQLbulkLoader.isBulkLoad()) {
            MySQLbulkLoader.flushAll();
            MySQLbulkLoader.relaxedModeOff();
        }
    }

    private List<ResourceDefinition> grabResources(BufferedReader buff) throws DaoException, IOException {
        List<ResourceDefinition> resources = new ArrayList<ResourceDefinition>();
        resources = DaoResourceDefinition.getDatumByStudy(cancerStudy.getInternalId());
        return resources;
    }

    private String[] splitFields(String line) throws IOException {
        line = line.replaceAll("^" + METADATA_PREFIX + "+", "");
        String[] fields = line.split(DELIMITER, -1);

        return fields;
    }

    private void importData(BufferedReader buff, List<ResourceDefinition> resources, String[] headerNames) throws Exception {
        String line;
        MultiKeyMap resourceMap = new MultiKeyMap();
        while ((line = buff.readLine()) != null) {
            if (skipLine(line.trim())) {
                continue;
            }

            String[] fieldValues = getFieldValues(line, headerNames);
            addDatum(fieldValues, resources, resourceMap, headerNames);
        }
    }

    private boolean skipLine(String line) {
        return (line.isEmpty() || line.substring(0, 1).equals(METADATA_PREFIX));
    }

    /**
     * Takes in the given line and returns the list of field values by splitting the
     * line on DELIMITER.
     * 
     * @param line
     * @param headerNames
     * @return the list of values, one for each column. Value will be "" for empty
     *         columns.
     */
    private String[] getFieldValues(String line, String[] headerNames) {
        // split on delimiter:
        String[] fieldValues = line.split(DELIMITER, -1);

        // validate: if number of fields is incorrect, give exception
        if (fieldValues.length != headerNames.length) {
            throw new IllegalArgumentException("Number of columns in line is not as expected. Expected: "
                    + headerNames.length + " columns, found: " + fieldValues.length + ", for line: " + line);
        }

        // now iterate over lines and trim each value:
        for (int i = 0; i < fieldValues.length; i++) {
            fieldValues[i] = fieldValues[i].trim();
        }
        return fieldValues;
    }

    private boolean addDatum(String[] fields, List<ResourceDefinition> resources, MultiKeyMap resourceMap, String[] headerNames)
            throws Exception {
        int sampleIdIndex = findSampleIdColumn(headerNames);
        String stableSampleId = (sampleIdIndex >= 0) ? fields[sampleIdIndex] : "";
        stableSampleId = StableIdUtil.getSampleId(stableSampleId);
        int patientIdIndex = findPatientIdColumn(headerNames);
        String stablePatientId = (patientIdIndex >= 0) ? fields[patientIdIndex] : "";
        stablePatientId = StableIdUtil.getPatientId(stablePatientId);
        int resourceIdIndex = findResourceIdColumn(headerNames);
        int urlIndex = findURLColumn(headerNames);
        int internalSampleId = -1;
        int internalPatientId = -1;

        // create resource_id set
        Set<String> patientResourceIdSet = resources.stream().filter(resource -> resource.getResourceType().equals(ResourceDefinition.PATIENT_RESOURCE_TYPE)).map(resource -> resource.getResourceId()).collect(Collectors.toSet());
        Set<String> sampleResourceIdSet = resources.stream().filter(resource -> resource.getResourceType().equals(ResourceDefinition.SAMPLE_RESOURCE_TYPE)).map(resource -> resource.getResourceId()).collect(Collectors.toSet());
        Set<String> studyResourceIdSet = resources.stream().filter(resource -> resource.getResourceType().equals(ResourceDefinition.STUDY_RESOURCE_TYPE)).map(resource -> resource.getResourceId()).collect(Collectors.toSet());

        // check if sample is not already added:
        Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudy.getInternalId(), stableSampleId, false);
        if (sample != null) {
            // get internal sample id if sample exists
            internalSampleId = sample.getInternalId();
        } else {
            Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(),
                    stablePatientId);
            if (patient != null) {
                // patient exists, get internal id:
                internalPatientId = patient.getInternalId();
            } else {
                // add patient:
                internalPatientId = (patientIdIndex >= 0) ? addPatientToDatabase(fields[patientIdIndex]) : -1;
            }
            // sample is new, so attempt to add to DB
            internalSampleId = (stableSampleId.length() > 0) ? addSampleToDatabase(stableSampleId, fields, headerNames)
                    : -1;
        }

        // validate and count:
        if (internalSampleId != -1) {
            // some minimal validation/fail safe for now: only continue if patientId is same
            // as patient id in
            // existing sample (can occur in case of this.isSupplementalData or in case of
            // parsing bug in addSampleToDatabase):
            internalPatientId = DaoPatient
                    .getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(), stablePatientId).getInternalId();
            if (internalPatientId != DaoSample.getSampleById(internalSampleId).getInternalPatientId()) {
                throw new RuntimeException("Error: Sample " + stableSampleId
                        + " was previously linked to another patient, and not to " + stablePatientId);
            }
            numSamplesProcessed++;
        }

        // if the resource id or url matches one of the missing values, skip this resource:
        if ((resourceIdIndex != -1 && MissingResourceValues.has(fields[resourceIdIndex])) || (urlIndex != -1 && MissingResourceValues.has(fields[urlIndex]))) {
            numEmptyResourcesSkipped++;
        } else {
            if (getResourceType() == ImportResourceData.ResourceTypes.PATIENT && internalPatientId != -1) {
                if (!patientResourceIdSet.contains(fields[resourceIdIndex])) {
                    throw new RuntimeException("Error: patient " + stablePatientId
                    + " with resource " + fields[resourceIdIndex]
                    + " does not have matching resources information in database, please make sure to include resource definition in the resource definition file");
                }
                // The resourceMap keeps track what patient/resource/url to value pairs are being
                // added to the DB. If there are duplicates,
                if (!resourceMap.containsKey(internalPatientId, fields[resourceIdIndex], fields[urlIndex])) {
                    addDatum(internalPatientId, fields[resourceIdIndex], fields[urlIndex],
                            ResourceDefinition.PATIENT_RESOURCE_TYPE);
                    resourceMap.put(internalPatientId, fields[resourceIdIndex], fields[urlIndex]);
                } else if (!relaxed) {
                    throw new RuntimeException("Error: Duplicated patient resource in file");
                }
                // if the "relaxed" flag was given, and the new record e.g. tries to override a
                // previously set resource
                else if (!resourceMap.get(internalPatientId, fields[resourceIdIndex], fields[urlIndex]).equals(fields[resourceIdIndex])) {
                    ProgressMonitor.logWarning("Error: Duplicated patient " + stablePatientId
                            + " with different values for patient resource " + fields[resourceIdIndex]
                            + "\n\tValues: " + resourceMap.get(internalPatientId, fields[resourceIdIndex], fields[urlIndex])
                            + " " + fields[resourceIdIndex] + " " + fields[urlIndex]);
                }
            } else if (getResourceType() == ImportResourceData.ResourceTypes.SAMPLE && internalSampleId != -1) {
                if (!sampleResourceIdSet.contains(fields[resourceIdIndex])) {
                    throw new RuntimeException("Error: sample " + internalSampleId
                    + " with resource " + fields[resourceIdIndex]
                    + " does not have matching resources information in database, please make sure to include resource definition in the resource definition file");
                }
                // The resourceMap keeps track what sample/resource/url to value pairs are being
                // added to the DB. If there are duplicates,
                if (!resourceMap.containsKey(internalSampleId, fields[resourceIdIndex], fields[urlIndex])) {
                    addDatum(internalSampleId, fields[resourceIdIndex], fields[urlIndex],
                            ResourceDefinition.SAMPLE_RESOURCE_TYPE);
                    resourceMap.put(internalSampleId, fields[resourceIdIndex], fields[urlIndex]);
                } else if (!relaxed) {
                    throw new RuntimeException("Error: Duplicated sample resource in file");
                }
                else if (!resourceMap.get(internalSampleId, fields[resourceIdIndex], fields[urlIndex]).equals(fields[resourceIdIndex])) {
                    ProgressMonitor.logWarning("Error: Duplicated sample " + stablePatientId
                            + " with different values for sample resource " + fields[resourceIdIndex]
                            + "\n\tValues: " + resourceMap.get(internalSampleId, fields[resourceIdIndex], fields[urlIndex])
                            + " " + fields[resourceIdIndex] + " " + fields[urlIndex]);
                }
            } else if (getResourceType() == ImportResourceData.ResourceTypes.STUDY) {
                if (!studyResourceIdSet.contains(fields[resourceIdIndex])) {
                    throw new RuntimeException("Error: study " + cancerStudy.getCancerStudyStableId()
                    + " with resource " + fields[resourceIdIndex]
                    + " does not have matching resources information in database, please make sure to include resource definition in the resource definition file");
                }
                // The resourceMap keeps track what study/resource/url to value pairs are being
                // added to the DB. If there are duplicates,
                if (!resourceMap.containsKey(cancerStudy.getInternalId(), fields[resourceIdIndex], fields[urlIndex])) {
                    addDatum(cancerStudy.getInternalId(), fields[resourceIdIndex], fields[urlIndex],
                            ResourceDefinition.STUDY_RESOURCE_TYPE);
                    resourceMap.put(cancerStudy.getInternalId(), fields[resourceIdIndex], fields[urlIndex]);
                } else if (!relaxed) {
                    throw new RuntimeException("Error: Duplicated study resource in file");
                } else if (!resourceMap.get(cancerStudy.getInternalId(), fields[resourceIdIndex], fields[urlIndex]).equals(fields[resourceIdIndex])) {
                    ProgressMonitor.logWarning("Error: Duplicated study " + cancerStudy.getCancerStudyStableId()
                            + " with different values for study resource " + fields[resourceIdIndex]
                            + "\n\tValues: " + resourceMap.get(cancerStudy.getInternalId(), fields[resourceIdIndex], fields[urlIndex])
                            + " " + fields[resourceIdIndex] + " " + fields[urlIndex]);
                }
            }
        }
        return true;
    }

    private int findPatientIdColumn(String[] headerNames) {
        return findColumnIndexInHeaders(PATIENT_ID_COLUMN_NAME, headerNames);
    }

    private int findSampleIdColumn(String[] headerNames) {
        return findColumnIndexInHeaders(SAMPLE_ID_COLUMN_NAME, headerNames);
    }

    private int findResourceIdColumn(String[] headerNames) {
        return findColumnIndexInHeaders(RESOURCE_ID_COLUMN_NAME, headerNames);
    }

    private int findURLColumn(String[] headerNames) {
        return findColumnIndexInHeaders(URL_COLUMN_NAME, headerNames);
    }

    private int findSampleTypeColumn(String[] headerNames) {
        return findColumnIndexInHeaders(SAMPLE_TYPE_COLUMN_NAME, headerNames);
    }

    private int findColumnIndexInHeaders(String columnHeader, String[] headerNames) {
        for (int lc = 0; lc < headerNames.length; lc++) {
            if (headerNames[lc].equals(columnHeader)) {
                return lc;
            }
        }
        return -1;
    }

    private int addPatientToDatabase(String patientId) throws Exception {
        int internalPatientId = -1;
        if (validPatientId(patientId)) {
            Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(), patientId);
            // other validations:
            // in case of PATIENT data import, there are some special checks:
            if (getResourceType() == ImportResourceData.ResourceTypes.PATIENT) {
                // if resource data is already there, then something has gone wrong (e.g.
                // patient is duplicated in file), abort:
                if (patient != null
                        && DaoResourceData.getDataByPatientId(cancerStudy.getInternalId(), patientId).size() > 0) {
                    throw new RuntimeException(
                            "Something has gone wrong. Patient " + patientId + " already has resource data loaded.");
                }
                // if patient is duplicated, abort as well in this case:
                if (!patientIds.add(patientId)) {
                    throw new RuntimeException("Error. Patient " + patientId + " found to be duplicated in your file.");
                }
            }

            if (patient != null) {
                // in all cases (SAMPLE, PATIENT, or STUDY) this can be expected, so
                // just fetch it:
                internalPatientId = patient.getInternalId();
            } else {
                // in case of PATIENT data import and patient == null :
                if (getResourceType() == ImportResourceData.ResourceTypes.PATIENT) {
                    // not finding the patient it unexpected (as SAMPLE data import should always
                    // precede it), but
                    // can happen when this patient does not have any samples for example. In any
                    // case, warn about it:
                    ProgressMonitor.logWarning("Patient " + patientId
                            + " being added for the first time. Apparently this patient was not in the samples file, or the samples file is not yet loaded (should be loaded before this one)");
                }

                patient = new Patient(cancerStudy, patientId);
                internalPatientId = DaoPatient.addPatient(patient);
            }
        }
        return internalPatientId;
    }

    private int addSampleToDatabase(String sampleId, String[] fields, String[] headerNames)
            throws Exception {
        int sampleTypeIndex = findSampleTypeColumn(headerNames);
        String sampleTypeStr = (sampleTypeIndex != -1) ? fields[sampleTypeIndex] : null;
        if (sampleTypeStr != null) {
            // want to match Sample.Type enum names
            sampleTypeStr = sampleTypeStr.trim().toUpperCase().replaceAll(" ", "_");
        }
        Sample.Type sampleType = Sample.Type.has(sampleTypeStr) ? Sample.Type.valueOf(sampleTypeStr) : null;

        int internalSampleId = -1;
        if (validSampleId(sampleId) && !StableIdUtil.isNormal(sampleId)) {
            // want to try and capture normal sample types based on value for SAMPLE_TYPE
            // if present in resource data
            if (sampleType != null && sampleType.isNormal()) {
                return internalSampleId;
            }
            String stablePatientId = getStablePatientId(sampleId, fields, headerNames);
            if (validPatientId(stablePatientId)) {
                Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(),
                        stablePatientId);
                if (patient == null) {
                    addPatientToDatabase(stablePatientId);
                    patient = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(),
                            stablePatientId);
                }
                sampleId = StableIdUtil.getSampleId(sampleId);
                internalSampleId = DaoSample.addSample(
                        new Sample(sampleId, patient.getInternalId(), cancerStudy.getTypeOfCancerId(), sampleTypeStr));
            }
        }

        return internalSampleId;
    }

    private String getStablePatientId(String sampleId, String[] fields, String[] headerNames) {
        Matcher tcgaSampleBarcodeMatcher = StableIdUtil.TCGA_PATIENT_BARCODE_FROM_SAMPLE_REGEX.matcher(sampleId);
        if (tcgaSampleBarcodeMatcher.find()) {
            return tcgaSampleBarcodeMatcher.group(1);
        } else {
            // internal studies should have a patient id column
            int patientIdIndex = findPatientIdColumn(headerNames);
            if (patientIdIndex >= 0) {
                return fields[patientIdIndex];
            }
            // sample and patient id are the same
            else {
                return sampleId;
            }
        }
    }

    private boolean validPatientId(String patientId) {
        return (patientId != null && !patientId.isEmpty());
    }

    private boolean validSampleId(String sampleId) {
        return (sampleId != null && !sampleId.isEmpty());
    }

    // add datum for patient, sample and study resources
    private void addDatum(int internalId, String resourceId, String resourceURL, String resourceType) throws Exception {
        // if bulk loading is ever turned off, we need to check if
        // resource value exists and if so, perfom an update
        if (resourceType.equals(ResourceDefinition.PATIENT_RESOURCE_TYPE)) {
            numPatientSpecificResourcesAdded++;
            DaoResourceData.addPatientDatum(internalId, resourceId, resourceURL);
        } else if (resourceType.equals(ResourceDefinition.SAMPLE_RESOURCE_TYPE)) {
            numSampleSpecificResourcesAdded++;
            DaoResourceData.addSampleDatum(internalId, resourceId, resourceURL);
        } else {
            numStudySpecificResourcesAdded++;
            DaoResourceData.addStudyDatum(internalId, resourceId, resourceURL);
        }
    }

    public int getNumSampleSpecificResourcesAdded() {
        return numSampleSpecificResourcesAdded;
    }

    public int getNumPatientSpecificResourcesAdded() {
        return numPatientSpecificResourcesAdded;
    }

    public int getNumStudySpecificResourcesAdded() {
        return numStudySpecificResourcesAdded;
    }

    public int getNumEmptyResourcesSkipped() {
        return numEmptyResourcesSkipped;
    }

    public int getNumSamplesProcessed() {
        return numSamplesProcessed;
    }

    /**
     * The type of resource found in the file. Basically the type of import
     * running for this instance. Can be one of ResourceTypes.
     * 
     * @return
     */
    public ResourceTypes getResourceType() {
        return resourceType;
    }

    /**
     * Imports resource defination and resource data (from the worksheet)
     */
    public void run() {
        try {
            String progName = "importResourceData";
            String description = "Import resource files.";
            // usage: --data <data_file.txt> --meta <meta_file.txt> --loadMode
            // [directLoad|bulkLoad (default)] [--noprogress]

            OptionParser parser = new OptionParser();
            OptionSpec<String> data = parser.accepts("data", "profile data file").withRequiredArg()
                    .describedAs("data_file.txt").ofType(String.class);
            OptionSpec<String> meta = parser.accepts("meta", "meta (description) file").withOptionalArg()
                    .describedAs("meta_file.txt").ofType(String.class);
            OptionSpec<String> study = parser.accepts("study", "cancer study id").withOptionalArg().describedAs("study")
                    .ofType(String.class);
            OptionSpec<String> relaxedFlag = parser.accepts("r",
                    "(not recommended) Flag for relaxed mode, determining how to handle detected data harmonization problems in the same study")
                    .withOptionalArg().describedAs("r").ofType(String.class);
            parser.accepts("loadMode", "direct (per record) or bulk load of data").withOptionalArg()
                    .describedAs("[directLoad|bulkLoad (default)]").ofType(String.class);
            parser.accepts("noprogress",
                    "this option can be given to avoid the messages regarding memory usage and % complete");

            OptionSet options = null;
            try {
                options = parser.parse(args);
            } catch (OptionException e) {
                throw new UsageException(progName, description, parser, e.getMessage());
            }
            File resourceFile = null;
            if (options.has(data)) {
                resourceFile = new File(options.valueOf(data));
            } else {
                throw new UsageException(progName, description, parser, "'data' argument required.");
            }
            String resourceType = null;
            boolean relaxed = false;
            String cancerStudyStableId = null;
            if (options.has(study)) {
                cancerStudyStableId = options.valueOf(study);
            }
            if (options.has(meta)) {
                properties = new TrimmedProperties();
                properties.load(new FileInputStream(options.valueOf(meta)));
                resourceType = properties.getProperty("resource_type");
                cancerStudyStableId = properties.getProperty("cancer_study_identifier");
            }
            if (options.has(relaxedFlag)) {
                relaxed = true;

            }
            SpringUtil.initDataSource();
            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyStableId);
            if (cancerStudy == null) {
                throw new IllegalArgumentException("Unknown cancer study: " + cancerStudyStableId);
            }
            ProgressMonitor.setCurrentMessage("Reading data from:  " + resourceFile.getAbsolutePath());
            int numLines = FileUtil.getNumLines(resourceFile);
            ProgressMonitor.setCurrentMessage(" --> total number of lines:  " + numLines);
            ProgressMonitor.setMaxValue(numLines);

            setFile(cancerStudy, resourceFile, resourceType, relaxed);
            importData();

            if (getResourceType() == ImportResourceData.ResourceTypes.PATIENT) {
                ProgressMonitor.setCurrentMessage("Total number of patient specific resources added:  "
                        + getNumPatientSpecificResourcesAdded());
            }
            if (getResourceType() == ImportResourceData.ResourceTypes.SAMPLE) {
                ProgressMonitor.setCurrentMessage("Total number of sample specific resources added:  "
                        + getNumSampleSpecificResourcesAdded());
                ProgressMonitor.setCurrentMessage("Total number of samples processed:  " + getNumSamplesProcessed());
            }
            ProgressMonitor.setCurrentMessage("Total number of resource values skipped because of empty value:  "
                    + getNumEmptyResourcesSkipped());
            if (getResourceType() == ImportResourceData.ResourceTypes.SAMPLE
                    && (getNumSampleSpecificResourcesAdded() + getNumSamplesProcessed()) == 0) {
                // should not occur:
                throw new RuntimeException("No sample resources data was added.  " + "Please check your file format and try again.");
            }
            if (getResourceType() == ImportResourceData.ResourceTypes.PATIENT
                    && getNumPatientSpecificResourcesAdded() == 0) {
                // could occur if patient resource file is given with only PATIENT_ID column:
                throw new RuntimeException("No patient resources data was added.  "
                        + "Please check your file format and try again. If you only have sample resources data, then a patients file with only PATIENT_ID column is not required.");
            }
            if (getResourceType() == ImportResourceData.ResourceTypes.STUDY
                    && getNumStudySpecificResourcesAdded() == 0) {
                throw new RuntimeException("No study resource data was added.  "
                        + "Please check your file format and try again.");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Makes an instance to run with the given command line arguments.
     *
     * @param args the command line arguments to be used
     */
    public ImportResourceData(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportResourceData(args);
        runner.runInConsole();
    }
}
