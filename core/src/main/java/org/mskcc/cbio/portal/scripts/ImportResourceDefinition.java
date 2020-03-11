package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.scripts.ImportResourceData.ResourceTypes;
import org.mskcc.cbio.portal.util.*;

import java.io.*;
import joptsimple.*;
import java.util.*;

public class ImportResourceDefinition extends ConsoleRunnable {

    public static final String DELIMITER = "\t";
    public static final String RESOURCE_ID_COLUMN_NAME = "RESOURCE_ID";
    public static final String DISPLAY_NAME_COLUMN_NAME = "DISPLAY_NAME";
    public static final String DESCRIPTION_COLUMN_NAME = "DESCRIPTION";
    public static final String RESOURCE_TYPE_COLUMN_NAME = "RESOURCE_TYPE";
    public static final String OPEN_BY_DEFAULT_COLUMN_NAME = "OPEN_BY_DEFAULT";
    public static final String PRIORITY_COLUMN_NAME = "PRIORITY";
    private int numResourceDefinitionsAdded = 0;

    private static Properties properties;

    private File resourceDataFile;
    private CancerStudy cancerStudy;
    private boolean relaxed;

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

    public void setFile(CancerStudy cancerStudy, File resourceDataFile, boolean relaxed) {
        this.cancerStudy = cancerStudy;
        this.resourceDataFile = resourceDataFile;
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

        String line = buff.readLine();
        String[] headerNames = splitFields(line);

        // validate columns and get index
        int resourceIdIndex = findAndValidateResourceIdColumn(headerNames);
        int displayNameIndex = findAndValidateDisplayNameColumn(headerNames);
        int descriptionIndex = findAndValidateDescriptionColumn(headerNames);
        int resourceTypeIndex = findAndValidateResourceTypeColumn(headerNames);
        int openByDefaultIndex = findAndValidateOpenByDefaultColumn(headerNames);
        int priorityIndex = findAndValidatePriorityColumn(headerNames);

        while ((line = buff.readLine()) != null) {
            if (skipLine(line.trim())) {
                continue;
            }

            String[] fieldValues = getFieldValues(line, headerNames);

            // set default value
            String resourceId = "";
            String displayName = "";
            String description = "";
            String resourceType = "";
            Boolean openByDefault = false;
            int priority = 1;
            // get resource definations from columns
            // get resourceId
            if (resourceIdIndex != -1 && isValueNotMissing(fieldValues[resourceIdIndex].toUpperCase())) {
                resourceId = fieldValues[resourceIdIndex].toUpperCase();
            } else {
                throw new RuntimeException(
                        "Please provide a valid resource id");
            }
            // get displayName
            if (resourceIdIndex != -1 && isValueNotMissing(fieldValues[displayNameIndex])) {
                displayName = fieldValues[displayNameIndex];
            }
            else {
                throw new RuntimeException(
                        "Please provide a valid display name");
            }
            // get description (optional)
            if (descriptionIndex != -1 && isValueNotMissing(fieldValues[descriptionIndex])) {
                description = fieldValues[descriptionIndex];
            }
            // get resourceType (must be value of ResourceTypes)
            if (resourceTypeIndex != -1 && isValidResourceType(fieldValues[resourceTypeIndex])) {
                resourceType = fieldValues[resourceTypeIndex];
            }
            // get openByDefault (optional)
            if (openByDefaultIndex != -1 && isValidOpenByDefault(fieldValues[openByDefaultIndex])) {
                openByDefault = fieldValues[openByDefaultIndex].equalsIgnoreCase("true");
            }
            // get priority (optional)
            try {
                priority = Integer.parseInt(fieldValues[priorityIndex]);
            } catch (NumberFormatException ex) {
                throw new DaoException(
                    "priority cannot be parsed as an integer, all priority should be an integer.");
            }

            // add resource definations into database
            ResourceDefinition resource = new ResourceDefinition(resourceId, displayName,
                    description, resourceType, openByDefault,
                    priority, cancerStudy.getInternalId());

            ResourceDefinition resourceInDb = DaoResourceDefinition.getDatum(resource.getResourceId(),
                    cancerStudy.getInternalId());
            if (resourceInDb != null) {
                ProgressMonitor.logWarning("Resource " + resourceInDb.getResourceId() + " found twice in your study!");
                continue;
            }
            if (DaoResourceDefinition.addDatum(resource) > 0) {
                numResourceDefinitionsAdded++;
            }
        }
        buff.close();

        if (MySQLbulkLoader.isBulkLoad()) {
            MySQLbulkLoader.flushAll();
            MySQLbulkLoader.relaxedModeOff();
        }
    }

    private String[] splitFields(String line) throws IOException {
        String[] fields = line.split(DELIMITER, -1);

        return fields;
    }

    private boolean skipLine(String line) {
        return line.isEmpty();
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

    private Boolean isValueNotMissing(String value) {
        if (MissingResourceValues.has(value)) {
            return false;
        }
        return true;
    }

    private Boolean isValidResourceType(String value) {
        try {
            ResourceTypes.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Resource_Type should be one of the following : 'SAMPLE', 'PATIENT' or 'STUDY'."
                    + "found: " + value);
        }
        return true;
    }

    private Boolean isValidOpenByDefault(String value) {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return true;
        }
        // open by default value not valid, will set as false by default
        ProgressMonitor.logWarning("OpenByDefault value is not true or false, set to false by default.");
        return false;
    }

    private int findAndValidateResourceIdColumn(String[] headerNames) {
        return findAndValidateColumnIndexInHeaders(RESOURCE_ID_COLUMN_NAME, headerNames);
    }

    private int findAndValidateDisplayNameColumn(String[] headerNames) {
        return findAndValidateColumnIndexInHeaders(DISPLAY_NAME_COLUMN_NAME, headerNames);
    }

    private int findAndValidateDescriptionColumn(String[] headerNames) {
        return findAndValidateColumnIndexInHeaders(DESCRIPTION_COLUMN_NAME, headerNames);
    }

    private int findAndValidateResourceTypeColumn(String[] headerNames) {
        return findAndValidateColumnIndexInHeaders(RESOURCE_TYPE_COLUMN_NAME, headerNames);
    }

    private int findAndValidateOpenByDefaultColumn(String[] headerNames) {
        return findAndValidateColumnIndexInHeaders(OPEN_BY_DEFAULT_COLUMN_NAME, headerNames);
    }

    private int findAndValidatePriorityColumn(String[] headerNames) {
        return findAndValidateColumnIndexInHeaders(PRIORITY_COLUMN_NAME, headerNames);
    }

    private int findAndValidateColumnIndexInHeaders(String columnHeader, String[] headerNames) {
        for (int lc = 0; lc < headerNames.length; lc++) {
            if (headerNames[lc].equals(columnHeader)) {
                return lc;
            }
        }
        throw new RuntimeException("Aborting owing to failure to find " + columnHeader + 
        " in file. Please check your file format and try again.");
    }

    public int getNumResourceDefinitionsAdded() {
        return numResourceDefinitionsAdded;
    }

    /**
     * Imports resource defination data (from the worksheet)
     */
    public void run() {
        try {
            String progName = "importResourceDefinition";
            String description = "Import resource definition file";
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
            boolean relaxed = false;
            String cancerStudyStableId = null;
            if (options.has(study)) {
                cancerStudyStableId = options.valueOf(study);
            }
            if (options.has(meta)) {
                properties = new TrimmedProperties();
                properties.load(new FileInputStream(options.valueOf(meta)));
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

            setFile(cancerStudy, resourceFile, relaxed);
            importData();

            // log import information
            ProgressMonitor.setCurrentMessage(
                    "Total number of resource definitions added:  " + getNumResourceDefinitionsAdded());
            if (getNumResourceDefinitionsAdded() == 0) {
                throw new RuntimeException(
                        "No resource definition was added.  " + "Please check your file format and try again.");
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
    public ImportResourceDefinition(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportResourceDefinition(args);
        runner.runInConsole();
    }
}
