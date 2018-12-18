/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

import java.io.*;
import joptsimple.*;
import java.util.*;
import java.util.regex.*;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang.StringUtils;

public class ImportClinicalData extends ConsoleRunnable {

    public static final String DELIMITER = "\t";
    public static final String METADATA_PREFIX = "#";
    public static final String SAMPLE_ID_COLUMN_NAME = "SAMPLE_ID";
    public static final String PATIENT_ID_COLUMN_NAME = "PATIENT_ID";
    public static final String SAMPLE_TYPE_COLUMN_NAME = "SAMPLE_TYPE";
    private int numSampleSpecificClinicalAttributesAdded = 0;
    private int numPatientSpecificClinicalAttributesAdded = 0;
    private int numEmptyClinicalAttributesSkipped = 0;
    private int numSamplesProcessed = 0;
    
    private static Properties properties;

    private File clinicalDataFile;
    private CancerStudy cancerStudy;
    private AttributeTypes attributesType;
    private boolean relaxed;
    private Set<String> patientIds = new HashSet<String>();

    public static enum MissingAttributeValues
    {
        NOT_APPLICABLE("Not Applicable"),
        NOT_AVAILABLE("Not Available"),
        PENDING("Pending"),
        DISCREPANCY("Discrepancy"),
        COMPLETED("Completed"),
        NULL("null"),
        MISSING(""),
        NA("NA");

        private String propertyName;
        
        MissingAttributeValues(String propertyName) { this.propertyName = propertyName; }
        public String toString() { return propertyName; }

        static public boolean has(String value) {
            if (value == null) return false;
            if (value.trim().equals("")) return true;
            try { 
                value = value.replaceAll("[\\[|\\]]", "");
                value = value.replaceAll(" ", "_");
                return valueOf(value.toUpperCase()) != null; 
            }
            catch (IllegalArgumentException x) { 
                return false;
            }
        }

        static public String getNotAvailable()
        {
            return "[" + NOT_AVAILABLE.toString() + "]";
        }
    }
    
    public static enum AttributeTypes 
    {
        PATIENT_ATTRIBUTES("PATIENT"),
        SAMPLE_ATTRIBUTES("SAMPLE"),
        MIXED_ATTRIBUTES("MIXED");
        
        private String attributeType;
        
        AttributeTypes(String attributeType) {this.attributeType = attributeType;}
        
        public String toString() {return attributeType;}
    }
    
    public static enum DataTypes 
    {
        STRING,
        NUMBER,
        BOOLEAN;
    	
    	static public boolean has(String value) {
    		try { 
                return valueOf(value.toUpperCase()) != null; 
            }
            catch (IllegalArgumentException x) { 
                return false;
            }
    	}
    }

    public void setFile(CancerStudy cancerStudy, File clinicalDataFile, String attributesDatatype, boolean relaxed)
    {
        this.cancerStudy = cancerStudy;
        this.clinicalDataFile = clinicalDataFile;
        this.attributesType = AttributeTypes.valueOf(attributesDatatype);
        this.relaxed = relaxed;
    }

    public void importData() throws Exception
    {
        // if bulkLoading is ever turned off,
        // code has to be added to check whether
        // a clinical attribute update should be
        // perform instead of an insert
        MySQLbulkLoader.bulkLoadOn();
        
        if(relaxed) {
            MySQLbulkLoader.relaxedModeOn();
        }    
        
        FileReader reader =  new FileReader(clinicalDataFile);
        BufferedReader buff = new BufferedReader(reader);
        List<ClinicalAttribute> columnAttrs = grabAttrs(buff);
        
        int patientIdIndex = findPatientIdColumn(columnAttrs);
        int sampleIdIndex = findSampleIdColumn(columnAttrs);

        //validate required columns:
        if (patientIdIndex < 0) {
        	//PATIENT_ID is required in both file types:
        	throw new RuntimeException("Aborting owing to failure to find " +
                    PATIENT_ID_COLUMN_NAME + 
                    " in file. Please check your file format and try again.");
        }
        if (attributesType.toString().equals("SAMPLE") && sampleIdIndex < 0) {
        	//SAMPLE_ID is required in SAMPLE file type:
            throw new RuntimeException("Aborting owing to failure to find " +
                    SAMPLE_ID_COLUMN_NAME +
                    " in file. Please check your file format and try again.");
        }
        importData(buff, columnAttrs);
        
        if (getAttributesType() == ImportClinicalData.AttributeTypes.SAMPLE_ATTRIBUTES ||
            getAttributesType() == ImportClinicalData.AttributeTypes.MIXED_ATTRIBUTES) {
            DaoPatient.createSampleCountClinicalData(cancerStudy.getInternalId());
        }
        
        if (MySQLbulkLoader.isBulkLoad()) {
            MySQLbulkLoader.flushAll();
            MySQLbulkLoader.relaxedModeOff();
        }
    }

    private List<ClinicalAttribute> grabAttrs(BufferedReader buff) throws DaoException, IOException {
        List<ClinicalAttribute> attrs = new ArrayList<ClinicalAttribute>();

        String line = buff.readLine();
        String[] displayNames = splitFields(line);
        String[] descriptions, datatypes, attributeTypes = {}, priorities, colnames;
        if (line.startsWith(METADATA_PREFIX)) {
            // contains meta data about the attributes
            descriptions = splitFields(buff.readLine());
            datatypes = splitFields(buff.readLine());
            
            switch(this.attributesType)
            {
                case PATIENT_ATTRIBUTES:
                case SAMPLE_ATTRIBUTES:
                    attributeTypes = new String[displayNames.length];
                    Arrays.fill(attributeTypes, this.attributesType.toString());   
                    break;
                case MIXED_ATTRIBUTES:
                    attributeTypes = splitFields(buff.readLine());
                    //quick validation: attributeTypes values should be either PATIENT or SAMPLE
                    for (String attributeTypeVal : attributeTypes) {
                    	if (!attributeTypeVal.equalsIgnoreCase(AttributeTypes.PATIENT_ATTRIBUTES.toString()) && 
                    			!attributeTypeVal.equalsIgnoreCase(AttributeTypes.SAMPLE_ATTRIBUTES.toString())) {
                    		throw new RuntimeException("Invalid value for attributeType: " + attributeTypeVal + ". Check the header rows of your data file."); 
                    	}	
                    }
                    break;
            }
            
            //quick validation: datatypes values should be one of the valid types
            for (String datatype : datatypes) {
            	if (!DataTypes.has(datatype)) {
            		throw new RuntimeException("Invalid value for datatype: " + datatype + ". Check the header rows of your data file."); 
            	}	
            }
                     
            priorities = splitFields(buff.readLine());
            colnames = splitFields(buff.readLine());

            if (displayNames.length != colnames.length
                ||  descriptions.length != colnames.length
                ||  datatypes.length != colnames.length
                ||  attributeTypes.length != colnames.length
                ||  priorities.length != colnames.length) {
                throw new DaoException("attribute and metadata mismatch in clinical staging file. All lines in header and data rows should have the same number of columns.");
            }
        } else {
            // attribute Id header only
            colnames = displayNames;
            descriptions = new String[colnames.length];
            Arrays.fill(descriptions, ClinicalAttribute.MISSING);
            datatypes = new String[colnames.length];
            Arrays.fill(datatypes, ClinicalAttribute.DEFAULT_DATATYPE);
            attributeTypes = new String[colnames.length];
            Arrays.fill(attributeTypes, ClinicalAttribute.SAMPLE_ATTRIBUTE);
            priorities = new String[colnames.length];
            Arrays.fill(priorities, "1");
            displayNames = new String[colnames.length];
            Arrays.fill(displayNames, ClinicalAttribute.MISSING);
        }

        for (int i = 0; i < colnames.length; i+=1) {
            ClinicalAttribute attr =
                new ClinicalAttribute(colnames[i].trim().toUpperCase(), displayNames[i],
                                      descriptions[i], datatypes[i],
                                      attributeTypes[i].equals(ClinicalAttribute.PATIENT_ATTRIBUTE),
                                      priorities[i],
                                      cancerStudy.getInternalId());
            attrs.add(attr);
            //skip PATIENT_ID / SAMPLE_ID columns, i.e. these are not clinical attributes but relational columns:
            if (attr.getAttrId().equals(PATIENT_ID_COLUMN_NAME) ||
            	attr.getAttrId().equals(SAMPLE_ID_COLUMN_NAME)) {
	            continue;
            }
            ClinicalAttribute attrInDb = DaoClinicalAttributeMeta.getDatum(attr.getAttrId(), cancerStudy.getInternalId());
            if (attrInDb != null) {
                ProgressMonitor.logWarning("Attribute " + attrInDb.getAttrId() + " found twice in your study!");
                continue;
            }
            DaoClinicalAttributeMeta.addDatum(attr);
        }
        return attrs;
    }
   
    private String[] splitFields(String line) throws IOException {
        line = line.replaceAll("^"+METADATA_PREFIX+"+", "");
        String[] fields = line.split(DELIMITER, -1);

        return fields;
    }

    private void importData(BufferedReader buff, List<ClinicalAttribute> columnAttrs) throws Exception
    {
        String line;
        MultiKeyMap attributeMap = new MultiKeyMap();
        while ((line = buff.readLine()) != null) {
            if (skipLine(line.trim())) {
                continue;
            }

            String[] fieldValues = getFieldValues(line, columnAttrs);
            addDatum(fieldValues, columnAttrs, attributeMap);
        }
    }

    private boolean skipLine(String line)
    {
        return (line.isEmpty() || line.substring(0,1).equals(METADATA_PREFIX));
    }

    /**
     * Takes in the given line and returns the list of field values by 
     * splitting the line on DELIMITER.
     *  
     * @param line
     * @param columnAttrs
     * @return the list of values, one for each column. Value will be "" for empty columns.
     */
    private String[] getFieldValues(String line, List<ClinicalAttribute> columnAttrs)
    {
    	// split on delimiter:
        String[] fieldValues = line.split(DELIMITER, -1);
        
        // validate: if number of fields is incorrect, give exception
        if (fieldValues.length != columnAttrs.size()) {
        	throw new IllegalArgumentException("Number of columns in line is not as expected. Expected: " + 
        								columnAttrs.size() + " columns, found: " + fieldValues.length + ", for line: " + line);
        }
        
        // now iterate over lines and trim each value:
        for (int i = 0; i < fieldValues.length; i++)
        {
        	fieldValues[i] = fieldValues[i].trim();
        }
        return fieldValues;
    }

    private boolean addDatum(String[] fields, List<ClinicalAttribute> columnAttrs, MultiKeyMap attributeMap) throws Exception
    {
        int sampleIdIndex = findSampleIdColumn(columnAttrs);
        String stableSampleId = (sampleIdIndex >= 0) ? fields[sampleIdIndex] : "";
        stableSampleId = StableIdUtil.getSampleId(stableSampleId);
        int patientIdIndex = findPatientIdColumn(columnAttrs);
        String stablePatientId = (patientIdIndex >= 0) ? fields[patientIdIndex] : "";
        stablePatientId = StableIdUtil.getPatientId(stablePatientId);
        int internalSampleId = -1;
        int internalPatientId = -1;
        
        //check if sample is not already added:
        Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudy.getInternalId(), stableSampleId, false);
        if (sample != null) {
        	//this should be a WARNING in case of TCGA studies (see https://github.com/cBioPortal/cbioportal/issues/839#issuecomment-203452415)
        	//and an ERROR in other studies. I.e. a sample should occur only once in clinical file!
        	if (stableSampleId.startsWith("TCGA-")) {
        		ProgressMonitor.logWarning("Sample " + stableSampleId + " found to be duplicated in your file. Only data of the first sample will be processed.");
        		return false;
        	}
        	//give error or warning if sample is already in DB and this is NOT expected (i.e. not supplemental data):
        	if (!this.isSupplementalData()) {
	        	throw new RuntimeException("Error: Sample " + stableSampleId + " found to be duplicated in your file.");
        	}
        	else {
        		internalSampleId = sample.getInternalId();
        	}
        }
        else {
        	Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(), stablePatientId);
        	if (patient != null) {
        		//patient exists, get internal id:
        		internalPatientId = patient.getInternalId();
        	}
        	else {
        		//add patient:
	            internalPatientId = (patientIdIndex >= 0) ?
	                addPatientToDatabase(fields[patientIdIndex]) : -1;
        	}
        	// sample is new, so attempt to add to DB
	        internalSampleId = (stableSampleId.length() > 0) ?
	            addSampleToDatabase(stableSampleId, fields, columnAttrs) : -1;
	        
        }    
		
    	//validate and count:
        if (internalSampleId != -1) {
        	//some minimal validation/fail safe for now: only continue if patientId is same as patient id in 
            //existing sample (can occur in case of this.isSupplementalData or in case of parsing bug in addSampleToDatabase):
    		internalPatientId = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(), stablePatientId).getInternalId();
    		if (internalPatientId != DaoSample.getSampleById(internalSampleId).getInternalPatientId()) {
    			throw new RuntimeException("Error: Sample " + stableSampleId + " was previously linked to another patient, and not to " + stablePatientId);
    		}
        	numSamplesProcessed++;
        }

        for (int lc = 0; lc < fields.length; lc++) {
            //if lc is sampleIdIndex or patientIdIndex, skip as well since these are the relational fields:
            if (lc == sampleIdIndex || lc == patientIdIndex) {
            	continue;
        	}
        	//if the value matches one of the missing values, skip this attribute:
            if (MissingAttributeValues.has(fields[lc])) {
            	numEmptyClinicalAttributesSkipped++;
                continue;
            }
            boolean isPatientAttribute = columnAttrs.get(lc).isPatientAttribute(); 
            if (isPatientAttribute && internalPatientId != -1) {
                // The attributeMap keeps track what  patient/attribute to value pairs are being added to the DB. If there are duplicates,
                // (which can happen in a MIXED_ATTRIBUTES type clinical file), we need to make sure that the value for the same
                // attributes are consistent. This prevents duplicate entries in the temp file that the MySqlBulkLoader uses.
                if(!attributeMap.containsKey(internalPatientId, columnAttrs.get(lc).getAttrId())) {
                    addDatum(internalPatientId, columnAttrs.get(lc).getAttrId(), fields[lc],
                        ClinicalAttribute.PATIENT_ATTRIBUTE);
                    attributeMap.put(internalPatientId, columnAttrs.get(lc).getAttrId(), fields[lc]);
                }
                else if (!relaxed) {
                    throw new RuntimeException("Error: Duplicated patient in file");
                }
                // if the "relaxed" flag was given, and the new record e.g. tries to override a previously set attribute for 
                // an existing patient (e.g. set AGE from 2 to 10...in same study, or GENDER from M to F), then the system will keep 
                // the previous value and give a warning. NB: this is a kind of "random" harmonization strategy and is 
                // NOT recommended! TODO - change this to an Exception instead of just a warning. 
                else if (!attributeMap.get(internalPatientId, columnAttrs.get(lc).getAttrId()).equals(fields[lc])) {
                    ProgressMonitor.logWarning("Error: Duplicated patient " + stablePatientId + " with different values for patient attribute " + columnAttrs.get(lc).getAttrId() + 
                        "\n\tValues: " + attributeMap.get(internalPatientId, columnAttrs.get(lc).getAttrId()) + " " + fields[lc]);
                }
            }
            else if (internalSampleId != -1) {
                if(!attributeMap.containsKey(internalSampleId, columnAttrs.get(lc).getAttrId())) {
                    addDatum(internalSampleId, columnAttrs.get(lc).getAttrId(), fields[lc],
                        ClinicalAttribute.SAMPLE_ATTRIBUTE);
                    attributeMap.put(internalSampleId, columnAttrs.get(lc).getAttrId(), fields[lc]);
                }
                else if (!relaxed) {
                    throw new RuntimeException("Error: Duplicated sample in file");
                }
                else if (!attributeMap.get(internalSampleId, columnAttrs.get(lc).getAttrId()).equals(fields[lc])) {
                    ProgressMonitor.logWarning("Error: Duplicated sample " + stableSampleId + " with different values for sample attribute " + columnAttrs.get(lc).getAttrId() + 
                        "\n\tValues: " + attributeMap.get(internalSampleId, columnAttrs.get(lc).getAttrId()) + " " + fields[lc]);
                }
            }
        }
        return true;
    }

    private boolean isSupplementalData() {
    	//TODO : for now this is only true in MIXED_ATTRIBUTES type. We could add an extra flag "SUPPLEMENTAL_DATA" to make this more explicit:
    	return this.getAttributesType() == ImportClinicalData.AttributeTypes.MIXED_ATTRIBUTES;
	}

	private int findPatientIdColumn(List<ClinicalAttribute> attrs)
    {
        return findAttributeColumnIndex(PATIENT_ID_COLUMN_NAME, attrs);
    }

    private int findSampleIdColumn(List<ClinicalAttribute> attrs)
    {
        return findAttributeColumnIndex(SAMPLE_ID_COLUMN_NAME, attrs);
    }
    
    private int findSampleTypeColumn(List<ClinicalAttribute> attrs) {
        return findAttributeColumnIndex(SAMPLE_TYPE_COLUMN_NAME, attrs);
    }

    private int findAttributeColumnIndex(String columnHeader, List<ClinicalAttribute> attrs)
    {
        for (int lc = 0; lc < attrs.size(); lc++) {
            if (attrs.get(lc).getAttrId().equals(columnHeader)) {
                return lc;
            }
        }
        return -1;
    }

    private int addPatientToDatabase(String patientId) throws Exception
    {
        int internalPatientId = -1;
        if (validPatientId(patientId)) {
        	Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(), patientId);
        	//other validations:
        	//in case of PATIENT data import, there are some special checks:
        	if (getAttributesType() == ImportClinicalData.AttributeTypes.PATIENT_ATTRIBUTES) {
        		//if clinical data is already there, then something has gone wrong (e.g. patient is duplicated in file), abort:
        		if (patient != null && DaoClinicalData.getDataByPatientId(cancerStudy.getInternalId(), patientId).size() > 0) {
        			throw new RuntimeException("Something has gone wrong. Patient " + patientId + " already has clinical data loaded.");
        		}
        		//if patient is duplicated, abort as well in this case:
        		if (!patientIds.add(patientId)) {
        			throw new RuntimeException("Error. Patient " + patientId + " found to be duplicated in your file.");
        		}
        	}
        	
            if (patient != null) {
            	//in all cases (SAMPLE, PATIENT, or MIXED data import) this can be expected, so just fetch it:
                internalPatientId = patient.getInternalId();
            }
            else {            	
            	//in case of PATIENT data import and patient == null :
            	if (getAttributesType() == ImportClinicalData.AttributeTypes.PATIENT_ATTRIBUTES) {
            		//not finding the patient it unexpected (as SAMPLE data import should always precede it), but 
                	//can happen when this patient does not have any samples for example. In any case, warn about it:
            		ProgressMonitor.logWarning("Patient " + patientId + " being added for the first time. Apparently this patient was not in the samples file, or the samples file is not yet loaded (should be loaded before this one)");
            	}
            	
                patient = new Patient(cancerStudy, patientId);
                internalPatientId = DaoPatient.addPatient(patient);
            }
        }
        return internalPatientId;
    }

    private int addSampleToDatabase(String sampleId, String[] fields, List<ClinicalAttribute> columnAttrs) throws Exception
    {
        int sampleTypeIndex = findSampleTypeColumn(columnAttrs);
        String sampleTypeStr = (sampleTypeIndex != -1) ? fields[sampleTypeIndex] : null;
        if (sampleTypeStr != null) {
            // want to match Sample.Type enum names
            sampleTypeStr = sampleTypeStr.trim().toUpperCase().replaceAll(" ", "_");
        }
        Sample.Type sampleType = Sample.Type.has(sampleTypeStr) ? Sample.Type.valueOf(sampleTypeStr) : null;
        
        int internalSampleId = -1;
        if (validSampleId(sampleId) && !StableIdUtil.isNormal(sampleId)) {
            // want to try and capture normal sample types based on value for SAMPLE_TYPE
            // if present in clinical data
            if (sampleType != null && sampleType.isNormal()) {
                return internalSampleId;
            }
            String stablePatientId = getStablePatientId(sampleId, fields, columnAttrs);
            if (validPatientId(stablePatientId)) {
                Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(), stablePatientId);
                if (patient == null) {
                    addPatientToDatabase(stablePatientId);
                    patient = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(), stablePatientId);
                }
                sampleId = StableIdUtil.getSampleId(sampleId);
               	internalSampleId = DaoSample.addSample(new Sample(sampleId, patient.getInternalId(),
                                                       cancerStudy.getTypeOfCancerId(),
                                                       sampleTypeStr));
            }
        }

        return internalSampleId;
    }

    private String getStablePatientId(String sampleId, String[] fields, List<ClinicalAttribute> columnAttrs)
    {
        Matcher tcgaSampleBarcodeMatcher = StableIdUtil.TCGA_PATIENT_BARCODE_FROM_SAMPLE_REGEX.matcher(sampleId);
        if (tcgaSampleBarcodeMatcher.find()) {
            return tcgaSampleBarcodeMatcher.group(1);
        }
        else {
            // internal studies should have a patient id column
            int patientIdIndex = findAttributeColumnIndex(PATIENT_ID_COLUMN_NAME, columnAttrs);
            if (patientIdIndex >= 0) {
                return fields[patientIdIndex];
            }
            // sample and patient id are the same
            else {
                return sampleId;
            }
        }
    }

    private boolean validPatientId(String patientId)
    {
        return (patientId != null && !patientId.isEmpty());
    }

    private boolean validSampleId(String sampleId)
    {
        return (sampleId != null && !sampleId.isEmpty());
    }

    private void addDatum(int internalId, String attrId, String attrVal, String attrType) throws Exception
    {
        // if bulk loading is ever turned off, we need to check if
        // attribute value exists and if so, perfom an update
        if (attrType.equals(ClinicalAttribute.PATIENT_ATTRIBUTE)) {
            numPatientSpecificClinicalAttributesAdded++;
            DaoClinicalData.addPatientDatum(internalId, attrId, attrVal.trim());
        }
        else {
            numSampleSpecificClinicalAttributesAdded++;
            DaoClinicalData.addSampleDatum(internalId, attrId, attrVal.trim());
        }
    }

    public int getNumSampleSpecificClinicalAttributesAdded() {
        return numSampleSpecificClinicalAttributesAdded;
    }

    public int getNumPatientSpecificClinicalAttributesAdded() {
        return numPatientSpecificClinicalAttributesAdded;
    }
    
    public int getNumEmptyClinicalAttributesSkipped() {
    	return numEmptyClinicalAttributesSkipped;
    }
    
    public int getNumSamplesProcessed() {
    	return numSamplesProcessed;
    }
    
    /**
     * The type of attributes found in the file. Basically the 
     * type of import running for this instance. Can be one of 
     * AttributeTypes.
     * 
     * @return
     */
    public AttributeTypes getAttributesType() {
    	return attributesType;
    }

    /**
     * Imports clinical data and clinical attributes (from the worksheet)
     */
    public void run() {
        try {
            String progName = "importClinicalData";
            String description = "Import clinical files.";
            // usage: --data <data_file.txt> --meta <meta_file.txt> --loadMode [directLoad|bulkLoad (default)] [--noprogress]
	
	        OptionParser parser = new OptionParser();
	        OptionSpec<String> data = parser.accepts( "data",
	               "profile data file" ).withRequiredArg().describedAs( "data_file.txt" ).ofType( String.class );
	        OptionSpec<String> meta = parser.accepts( "meta",
	               "meta (description) file" ).withOptionalArg().describedAs( "meta_file.txt" ).ofType( String.class );
	        OptionSpec<String> study = parser.accepts("study",
	                "cancer study id").withOptionalArg().describedAs("study").ofType(String.class);
	        OptionSpec<String> attributeFlag = parser.accepts("a",
	                "(deprecated) Flag for using MIXED_ATTRIBUTES").withOptionalArg().describedAs("a").ofType(String.class);
                	        OptionSpec<String> relaxedFlag = parser.accepts("r",
	                "(not recommended) Flag for relaxed mode, determining how to handle detected data harmonization problems in the same study").withOptionalArg().describedAs("r").ofType(String.class);
	        parser.accepts( "loadMode", "direct (per record) or bulk load of data" )
	          .withOptionalArg().describedAs( "[directLoad|bulkLoad (default)]" ).ofType( String.class );
	        parser.accepts("noprogress", "this option can be given to avoid the messages regarding memory usage and % complete");
	        
	        OptionSet options = null;
	        try {
	            options = parser.parse( args );
	        } catch (OptionException e) {
                throw new UsageException(
                        progName, description, parser,
                        e.getMessage());
	        }
	        File clinical_f = null;
	        if( options.has( data ) ){
	            clinical_f = new File( options.valueOf( data ) );
            } else {
                throw new UsageException(
                        progName, description, parser,
                        "'data' argument required.");
            }
	        String attributesDatatype = null;
                boolean relaxed = false;
	        String cancerStudyStableId = null;
	        if( options.has ( study ) )
	        {
	            cancerStudyStableId = options.valueOf(study);
	        }
	        if( options.has ( meta ) )
	        {
	            properties = new TrimmedProperties();
	            properties.load(new FileInputStream(options.valueOf(meta)));
	            attributesDatatype = properties.getProperty("datatype");
	            cancerStudyStableId = properties.getProperty("cancer_study_identifier");
	        }
                if( options.has ( attributeFlag ) )
                {
                    attributesDatatype = "MIXED_ATTRIBUTES";
                }
                if( options.has ( relaxedFlag ) )
                {
                    relaxed = true;

                }
            SpringUtil.initDataSource();
            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyStableId);
            if (cancerStudy == null) {
                throw new IllegalArgumentException("Unknown cancer study: " + cancerStudyStableId);
            }
            ProgressMonitor.setCurrentMessage("Reading data from:  " + clinical_f.getAbsolutePath());
            int numLines = FileUtil.getNumLines(clinical_f);
            ProgressMonitor.setCurrentMessage(" --> total number of lines:  " + numLines);
            ProgressMonitor.setMaxValue(numLines);

            setFile(cancerStudy, clinical_f, attributesDatatype, relaxed);
            importData();

            if (getAttributesType() == ImportClinicalData.AttributeTypes.PATIENT_ATTRIBUTES ||
                    getAttributesType() == ImportClinicalData.AttributeTypes.MIXED_ATTRIBUTES) { 
                ProgressMonitor.setCurrentMessage("Total number of patient specific clinical attributes added:  "
                    + getNumPatientSpecificClinicalAttributesAdded());
            }
            if (getAttributesType() == ImportClinicalData.AttributeTypes.SAMPLE_ATTRIBUTES ||
                    getAttributesType() == ImportClinicalData.AttributeTypes.MIXED_ATTRIBUTES) { 
                ProgressMonitor.setCurrentMessage("Total number of sample specific clinical attributes added:  "
                    + getNumSampleSpecificClinicalAttributesAdded());
                ProgressMonitor.setCurrentMessage("Total number of samples processed:  "
                    + getNumSamplesProcessed());
            }
            ProgressMonitor.setCurrentMessage("Total number of attribute values skipped because of empty value:  "
                    + getNumEmptyClinicalAttributesSkipped());
            if (getAttributesType() == ImportClinicalData.AttributeTypes.SAMPLE_ATTRIBUTES &&
                (getNumSampleSpecificClinicalAttributesAdded() + getNumSamplesProcessed()) == 0) {
                //should not occur: 
                throw new RuntimeException("No data was added.  " +
                        "Please check your file format and try again.");
            }
            if (getAttributesType() == ImportClinicalData.AttributeTypes.PATIENT_ATTRIBUTES &&
                getNumPatientSpecificClinicalAttributesAdded() == 0) {
                //could occur if patient clinical file is given with only PATIENT_ID column:
                throw new RuntimeException("No data was added.  " +
                        "Please check your file format and try again. If you only have sample clinical data, then a patients file with only PATIENT_ID column is not required.");
            }
            //backward compatible check (TODO - remove this later):
            if (getAttributesType() == ImportClinicalData.AttributeTypes.MIXED_ATTRIBUTES &&
                (getNumPatientSpecificClinicalAttributesAdded() + getNumSampleSpecificClinicalAttributesAdded()) == 0) {
                    //should not occur: 
                    throw new RuntimeException("No data was added.  " +
                            "Please check your data and try again.");
                }

            if (getAttributesType() == ImportClinicalData.AttributeTypes.PATIENT_ATTRIBUTES &&
                getNumPatientSpecificClinicalAttributesAdded() == 0) {
                //could occur if patient clinical file is given with only PATIENT_ID column:
                throw new RuntimeException("No data was added.  " +
                        "Please check your file format and try again. If you only have sample clinical data, then a patients file with only PATIENT_ID column is not required.");
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
     * @param args  the command line arguments to be used
     */
    public ImportClinicalData(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args  the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportClinicalData(args);
        runner.runInConsole();
    }
}
