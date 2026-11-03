package org.mskcc.cbio.portal.scripts;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.mskcc.cbio.portal.dao.ClickHouseBulkDeleter;
import org.mskcc.cbio.portal.dao.ClickHouseBulkLoader;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoPatient;
import org.mskcc.cbio.portal.dao.DaoResourceData;
import org.mskcc.cbio.portal.dao.DaoResourceDefinition;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.Patient;
import org.mskcc.cbio.portal.model.ResourceDefinition;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.model.shared.ResourceType;
import org.mskcc.cbio.portal.util.FileUtil;
import org.mskcc.cbio.portal.util.MissingValues;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.util.StableIdUtil;

public class ImportResourceData extends ConsoleRunnable {

    public static final String DELIMITER = "\t";
    public static final String METADATA_PREFIX = "#";
    public static final String SAMPLE_ID_COLUMN_NAME = "SAMPLE_ID";
    public static final String PATIENT_ID_COLUMN_NAME = "PATIENT_ID";
    public static final String RESOURCE_ID_COLUMN_NAME = "RESOURCE_ID";
    public static final String URL_COLUMN_NAME = "URL";
    public static final String SAMPLE_TYPE_COLUMN_NAME = "SAMPLE_TYPE";
    // Optional per-row columns (override resource-definition-level values)
    public static final String DISPLAY_NAME_COLUMN_NAME = "DISPLAY_NAME";
    public static final String METADATA_COLUMN_NAME = "METADATA";
    private int numSampleSpecificResourcesAdded = 0;
    private int numPatientSpecificResourcesAdded = 0;
    private int numStudySpecificResourcesAdded = 0;
    private int numEmptyResourcesSkipped = 0;
    private int numSamplesProcessed = 0;

    private static Properties properties;

    private File resourceDataFile;
    private CancerStudy cancerStudy;
    private ResourceType resourceType;
    private boolean relaxed;
    private Set<String> patientIds = new HashSet<String>();
    // Resource IDs encountered while reading the file, used to delete stale existing rows for
    // this study/resource-ID combination before the new rows are flushed (see importData()).
    // The resources and the entities of the rows this file actually loaded. Filled where a row
    // is accepted, not where it is read: a row rejected for a missing URL or an unknown sample
    // loads nothing, so its resource must not be included in what the re-import replaces.
    private final Set<String> importedResourceIds = new HashSet<>();
    private final Set<String> importedEntityStableIds = new HashSet<>();

    public void setFile(CancerStudy cancerStudy, File resourceDataFile, String resourceType, boolean relaxed) {
        this.cancerStudy = cancerStudy;
        this.resourceDataFile = resourceDataFile;
        this.resourceType = ResourceType.valueOf(resourceType);
        this.relaxed = relaxed;
    }

    public void importData() throws Exception {
        // Resource data is delete-then-insert on re-import (see DaoResourceData javadoc):
        // ClickHouse bulk-loads inserts, so this must run before the buffered inserts below are
        // flushed.
        ClickHouseBulkLoader.bulkLoadOn();

        if (relaxed) {
            ClickHouseBulkLoader.relaxedModeOn();
        }

        FileReader reader = new FileReader(resourceDataFile);
        BufferedReader buff = new BufferedReader(reader);
        List<ResourceDefinition> resources = DaoResourceDefinition.getDatumByStudy(cancerStudy.getInternalId());
        String currentLine = buff.readLine();
        String[] headerNames = currentLine.split("\t");
        Map<String, Integer> headerIndexMap = makeHeaderIndexMap(headerNames);
        
        int patientIdIndex = findPatientIdColumn(headerIndexMap);
        int sampleIdIndex = findSampleIdColumn(headerIndexMap);
        int resourceIdIndex = findResourceIdColumn(headerIndexMap);
        int urlIndex = findURLColumn(headerIndexMap);

        // validate required columns:
        if (resourceIdIndex < 0) {
            throw new RuntimeException("Aborting owing to failure to find " + RESOURCE_ID_COLUMN_NAME
                    + " in file. Please check your file format and try again.");
        }
        if (urlIndex < 0) {
            throw new RuntimeException("Aborting owing to failure to find " + URL_COLUMN_NAME
                    + " in file. Please check your file format and try again.");
        }
        if ((resourceType.equals(ResourceType.SAMPLE) || resourceType.equals(ResourceType.PATIENT)) && patientIdIndex < 0) {
            // PATIENT_ID is required in both patient and sample file types:
            throw new RuntimeException("Aborting owing to failure to find " + PATIENT_ID_COLUMN_NAME
                    + " in file. Please check your file format and try again.");
        }
        if (resourceType.equals(ResourceType.SAMPLE) && sampleIdIndex < 0) {
            // SAMPLE_ID is required in SAMPLE file type:
            throw new RuntimeException("Aborting owing to failure to find " + SAMPLE_ID_COLUMN_NAME
                    + " in file. Please check your file format and try again.");
        }
        importData(buff, resources, headerIndexMap);
        buff.close();

        // Delete the rows this file replaces before the buffered inserts below are flushed, so a
        // re-import of a corrected file replaces its rows instead of adding to them. Restricted
        // to the entities the file names: a whole-study import removes the study first, but
        // import-study-data loads a single file into a study that is still there.
        // Safe to compute here because ClickHouseBulkLoader holds the inserts in memory until
        // flushAll() below, so the lookup sees only the rows that were already in the table.
        DaoResourceData.deleteResourceDataForReimport(
            cancerStudy.getInternalId(), getResourceType().name(),
            importedResourceIds, importedEntityStableIds);

        if (ClickHouseBulkLoader.isBulkLoad()) {
            // The deleter has to be flushed here, not left to a later stage: importing a resource
            // file is the whole job when it is run on its own, so nothing afterwards would execute
            // the queued deletions and the re-imported rows would be added to the stale ones
            // rather than replacing them. ClickHouseBulkLoader.flushAll() below flushes inserts
            // only; it is a different queue.
            ClickHouseBulkDeleter.flushAll();
            ClickHouseBulkLoader.flushAll();
            ClickHouseBulkLoader.relaxedModeOff();
        }
    }

    private void importData(BufferedReader buff, List<ResourceDefinition> resources, Map<String, Integer> headerIndexMap) throws Exception {
        String line;
        MultiKeyMap resourceMap = new MultiKeyMap();
        // create resource_id set
        Set<String> patientResourceIdSet = resources
                .stream()
                .filter(resource -> resource.getResourceType().equals(ResourceType.PATIENT))
                .map(resource -> resource.getResourceId())
                .collect(Collectors.toSet());
        Set<String> sampleResourceIdSet = resources
                .stream()
                .filter(resource -> resource.getResourceType().equals(ResourceType.SAMPLE))
                .map(resource -> resource.getResourceId())
                .collect(Collectors.toSet());
        Set<String> studyResourceIdSet = resources
                .stream()
                .filter(resource -> resource.getResourceType().equals(ResourceType.STUDY))
                .map(resource -> resource.getResourceId())
                .collect(Collectors.toSet());

        int resourceIdIndex = findResourceIdColumn(headerIndexMap);

        while ((line = buff.readLine()) != null) {
            if (skipLine(line.trim())) {
                continue;
            }

            String[] fieldValues = getFieldValues(line, headerIndexMap);
            addDatum(fieldValues, resources, resourceMap, headerIndexMap, patientResourceIdSet, sampleResourceIdSet, studyResourceIdSet);
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
     * @param headerIndexMap
     * @return the list of values, one for each column. Value will be "" for empty
     *         columns.
     */
    private String[] getFieldValues(String line, Map<String, Integer> headerIndexMap) {
        // split on delimiter:
        String[] fieldValues = line.split(DELIMITER, -1);

        // validate: if number of fields is incorrect, give exception
        if (fieldValues.length != headerIndexMap.size()) {
            throw new IllegalArgumentException("Number of columns in line is not as expected. Expected: "
                    + headerIndexMap.size() + " columns, found: " + fieldValues.length + ", for line: " + line);
        }

        // now iterate over lines and trim each value:
        for (int i = 0; i < fieldValues.length; i++) {
            fieldValues[i] = fieldValues[i].trim();
        }
        return fieldValues;
    }

    private boolean addDatum(String[] fields, List<ResourceDefinition> resources, MultiKeyMap resourceMap, Map<String, Integer> headerIndexMap, Set<String> patientResourceIdSet, Set<String> sampleResourceIdSet, Set<String> studyResourceIdSet)
            throws Exception {
        int sampleIdIndex = findSampleIdColumn(headerIndexMap);
        String stableSampleId = (sampleIdIndex >= 0) ? fields[sampleIdIndex] : "";
        stableSampleId = StableIdUtil.getSampleId(stableSampleId);
        int patientIdIndex = findPatientIdColumn(headerIndexMap);
        String stablePatientId = (patientIdIndex >= 0) ? fields[patientIdIndex] : "";
        stablePatientId = StableIdUtil.getPatientId(stablePatientId);
        int resourceIdIndex = findResourceIdColumn(headerIndexMap);
        int urlIndex = findURLColumn(headerIndexMap);
        int internalSampleId = -1;
        int internalPatientId = -1;

        // validate patient and sample for patient and sample attibutes
        if (resourceType.equals(ResourceType.PATIENT) || resourceType.equals(ResourceType.SAMPLE)) {
            Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(), stablePatientId);
            if (patient != null) {
                // patient exists, get internal id:
                internalPatientId = patient.getInternalId();
            } else {
                // add patient: if patient do not exist and resource type is sample
                internalPatientId = (stablePatientId.length() > 0) ? addPatientToDatabase(stablePatientId) : -1;
            }

            if (resourceType.equals(ResourceType.SAMPLE)) {
                // check if sample is not already added:
                Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudy.getInternalId(), stableSampleId, false);
                if (sample != null) {
                    // get internal sample id if sample exists
                    internalSampleId = sample.getInternalId();
                } else {
                    // sample is new, so attempt to add to DB
                    internalSampleId = (stableSampleId.length() > 0) ? addSampleToDatabase(stableSampleId, fields, headerIndexMap, internalPatientId)
                            : -1;
                }
    
                // validate and count:
                if (internalSampleId != -1) {
                    // some minimal validation/fail safe for now: only continue if patientId is same
                    // as patient id in
                    // existing sample (can occur in case of this.isSupplementalData or in case of
                    // parsing bug in addSampleToDatabase):
                    if (internalPatientId != DaoSample.getSampleById(internalSampleId).getInternalPatientId()) {
                        throw new RuntimeException("Error: Sample " + stableSampleId
                                + " was previously linked to another patient, and not to " + stablePatientId);
                    }
                    numSamplesProcessed++;
                }
            }
        }

        // if the resource id or url matches one of the missing values, skip this resource:
        if ((resourceIdIndex != -1 && MissingValues.has(fields[resourceIdIndex])) || (urlIndex != -1 && MissingValues.has(fields[urlIndex]))) {
            numEmptyResourcesSkipped++;
        } else {
            // if patient_id column exists and resource type is patient
            if (getResourceType() == ResourceType.PATIENT && internalPatientId != -1) {
                validateAddDatum(internalPatientId, stablePatientId, fields[resourceIdIndex], fields[urlIndex], 
                        ResourceType.PATIENT, patientResourceIdSet, resourceMap, fields, headerIndexMap, stablePatientId);
            } 
            // if sample_id column exists and resource type is sample
            else if (getResourceType() == ResourceType.SAMPLE && internalSampleId != -1) {
                validateAddDatum(internalSampleId, stableSampleId, fields[resourceIdIndex], fields[urlIndex], 
                        ResourceType.SAMPLE, sampleResourceIdSet, resourceMap, fields, headerIndexMap, stablePatientId);
            }
            // if resource type is study
            else if (getResourceType() == ResourceType.STUDY) {
                validateAddDatum(cancerStudy.getInternalId(), cancerStudy.getCancerStudyStableId(), fields[resourceIdIndex], fields[urlIndex], 
                        ResourceType.STUDY, studyResourceIdSet, resourceMap, fields, headerIndexMap, null);
            }
        }
        return true;
    }

    private Map<String, Integer> makeHeaderIndexMap(String[] headerNames) {
        Map<String, Integer> headerIndexMap = new HashMap<String, Integer>();
        for (int i= 0; i < headerNames.length; i++) {
            headerIndexMap.put(headerNames[i], i);
        }
        return headerIndexMap;
    }

    private int findPatientIdColumn(Map<String, Integer> headerIndexMap) {
        return findColumnIndexInHeaders(PATIENT_ID_COLUMN_NAME, headerIndexMap);
    }

    private int findSampleIdColumn(Map<String, Integer> headerIndexMap) {
        return findColumnIndexInHeaders(SAMPLE_ID_COLUMN_NAME, headerIndexMap);
    }

    private int findResourceIdColumn(Map<String, Integer> headerIndexMap) {
        return findColumnIndexInHeaders(RESOURCE_ID_COLUMN_NAME, headerIndexMap);
    }

    private int findURLColumn(Map<String, Integer> headerIndexMap) {
        return findColumnIndexInHeaders(URL_COLUMN_NAME, headerIndexMap);
    }

    private int findSampleTypeColumn(Map<String, Integer> headerIndexMap) {
        return findColumnIndexInHeaders(SAMPLE_TYPE_COLUMN_NAME, headerIndexMap);
    }

    private int findColumnIndexInHeaders(String columnHeader, Map<String, Integer> headerIndexMap) {
        return headerIndexMap.getOrDefault(columnHeader, -1);
    }

    private int addPatientToDatabase(String patientId) throws Exception {
        int internalPatientId = -1;

        if (validPatientId(patientId)) {
            // in case of PATIENT data import and patient == null :
            if (getResourceType() == ResourceType.PATIENT) {
                // not finding the patient it unexpected (as SAMPLE data import should always
                // precede it), but
                // can happen when this patient does not have any samples for example. In any
                // case, warn about it:
                ProgressMonitor.logWarning("Patient " + patientId
                        + " being added for the first time. Apparently this patient was not in the samples file, or the samples file is not yet loaded (should be loaded before this one)");
            }
            internalPatientId = DaoPatient.addPatient(new Patient(cancerStudy, patientId));
        }
        return internalPatientId;
    }

    private int addSampleToDatabase(String sampleId, String[] fields, Map<String, Integer> headerIndexMap, int internalPatientId)
            throws Exception {
        int sampleTypeIndex = findSampleTypeColumn(headerIndexMap);
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
            sampleId = StableIdUtil.getSampleId(sampleId);
            if (internalPatientId != -1) {
                internalSampleId = DaoSample.addSample(
                    new Sample(sampleId, internalPatientId, sampleTypeStr));
            }
        }

        return internalSampleId;
    }

    private boolean validPatientId(String patientId) {
        return (patientId != null && !patientId.isEmpty());
    }

    private boolean validSampleId(String sampleId) {
        return (sampleId != null && !sampleId.isEmpty());
    }

    private void validateAddDatum(int internalId, String stableId, String resourceId, String resourceURL, ResourceType resourceType, Set<String> resourceSet, MultiKeyMap resourceMap, String[] fields, Map<String, Integer> headerIndexMap, String stablePatientId) throws Exception {
        // throw exception if resource definition is not exist in the database
        if (!resourceSet.contains(resourceId)) {
            throw new RuntimeException("Error: " + resourceType.toString().toLowerCase() + " " + stableId
            + " with resource " + resourceId
            + " does not have matching resources information in database, please make sure to include resource definition in the resource definition file");
        }
        // The resourceMap makes sure a pair of (internalId/resource_id/url) is unique
        // added to the DB if there are no duplicates,
        if (!resourceMap.containsKey(internalId, resourceId, resourceURL)) {
            addDatum(internalId, stableId, resourceId, resourceURL, resourceType, fields, headerIndexMap, stablePatientId);
            resourceMap.put(internalId, resourceId, resourceURL, resourceURL);
            importedResourceIds.add(resourceId);
            if (resourceType != ResourceType.STUDY) {
                importedEntityStableIds.add(stableId);
            }
        }
        // handle duplicates
        // if the "relaxed" flag was given, and the new record e.g. tries to ignore a duplicated resources and log a warning
        else if (!relaxed) {
            throw new RuntimeException("Error: Duplicated " + resourceType.toString().toLowerCase() + " resource in file");
        }
        // log a warning
        else if (!resourceMap.get(internalId, resourceId, resourceURL).equals(resourceURL)) {
            ProgressMonitor.logWarning("Error: Duplicated "  + resourceType.toString().toLowerCase() + " " + stableId
                    + " with different values for "  + resourceType.toString().toLowerCase() + " resource " + resourceId
                    + "\n\tValues: " + resourceId + " " + resourceURL);
        }
    }

    private String getOptionalField(String[] fields, Map<String, Integer> headerIndexMap, String columnName) {
        Integer idx = headerIndexMap.get(columnName);
        if (idx == null || idx < 0 || idx >= fields.length) return null;
        String val = fields[idx].trim();
        return (val.isEmpty() || MissingValues.has(val)) ? null : val;
    }

    // add datum for patient, sample and study resources (writes only to resource)
    private void addDatum(int internalId, String stableId, String resourceId, String resourceURL, ResourceType resourceType, String[] fields, Map<String, Integer> headerIndexMap, String stablePatientId) throws Exception {
        String displayName = getOptionalField(fields, headerIndexMap, DISPLAY_NAME_COLUMN_NAME);
        String metadata = getOptionalField(fields, headerIndexMap, METADATA_COLUMN_NAME);

        if (resourceType.equals(ResourceType.PATIENT)) {
            numPatientSpecificResourcesAdded++;
            DaoResourceData.addResourceDatum(
                cancerStudy.getInternalId(), resourceId, "PATIENT",
                stableId, null,
                resourceURL, displayName, metadata);
        } else if (resourceType.equals(ResourceType.SAMPLE)) {
            numSampleSpecificResourcesAdded++;
            // The patient id comes from the row's own PATIENT_ID column, which importData()
            // requires for sample files and which addDatum has already checked against the
            // sample's linkage. Re-deriving it from the sample caches instead left the column
            // NULL whenever a cache missed, and a SAMPLE row with no patient_id is skipped by
            // the resource -> patient key, so nothing downstream would report it.
            DaoResourceData.addResourceDatum(
                cancerStudy.getInternalId(), resourceId, "SAMPLE",
                stablePatientId, stableId,
                resourceURL, displayName, metadata);
        } else {
            numStudySpecificResourcesAdded++;
            DaoResourceData.addResourceDatum(
                cancerStudy.getInternalId(), resourceId, "STUDY",
                null, null,
                resourceURL, displayName, metadata);
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
     * running for this instance. Can be one of ResourceType.
     * 
     * @return
     */
    public ResourceType getResourceType() {
        return resourceType;
    }

    /**
     * Imports resource definition and resource data (from the worksheet)
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

            if (getResourceType() == ResourceType.PATIENT) {
                ProgressMonitor.setCurrentMessage("Total number of patient specific resources added:  "
                        + getNumPatientSpecificResourcesAdded());
            }
            if (getResourceType() == ResourceType.SAMPLE) {
                ProgressMonitor.setCurrentMessage("Total number of sample specific resources added:  "
                        + getNumSampleSpecificResourcesAdded());
                ProgressMonitor.setCurrentMessage("Total number of samples processed:  " + getNumSamplesProcessed());
            }
            ProgressMonitor.setCurrentMessage("Total number of resource values skipped because of empty value:  "
                    + getNumEmptyResourcesSkipped());
            if (getResourceType() == ResourceType.SAMPLE
                    && (getNumSampleSpecificResourcesAdded() + getNumSamplesProcessed()) == 0) {
                // should not occur:
                throw new RuntimeException("No sample resources data was added.  " + "Please check your file format and try again.");
            }
            if (getResourceType() == ResourceType.PATIENT
                    && getNumPatientSpecificResourcesAdded() == 0) {
                // could occur if patient resource file is given with only PATIENT_ID column:
                throw new RuntimeException("No patient resources data was added.  "
                        + "Please check your file format and try again. If you only have sample resources data, then a patients file with only PATIENT_ID column is not required.");
            }
            if (getResourceType() == ResourceType.STUDY
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
