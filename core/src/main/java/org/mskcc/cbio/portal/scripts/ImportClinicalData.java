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

public class ImportClinicalData {

    public static final String DELIMITER = "\t";
    public static final String METADATA_PREFIX = "#";
    public static final String SAMPLE_ID_COLUMN_NAME = "SAMPLE_ID";
    public static final String PATIENT_ID_COLUMN_NAME = "PATIENT_ID";
    public static final String SAMPLE_TYPE_COLUMN_NAME = "SAMPLE_TYPE";
    private int numSampleSpecificClinicalAttributesAdded = 0;
    private int numPatientSpecificClinicalAttributesAdded = 0;
    private int numEmptyClinicalAttributesSkipped = 0;
    private int numSamplesAdded = 0;
    
    private static Properties properties;

    private File clinicalDataFile;
    private CancerStudy cancerStudy;
    private AttributeTypes attributesType;
    private Set<String> patientIds = new HashSet<String>();

    public static enum MissingAttributeValues
    {
        NOT_APPLICABLE("Not Applicable"),
        NOT_AVAILABLE("Not Available"),
        PENDING("Pending"),
        DISCREPANCY("Discrepancy"),
        COMPLETED("Completed"),
        NULL("null"),
        MISSING("");

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

    public ImportClinicalData(CancerStudy cancerStudy, File clinicalDataFile, String attributesDatatype)
    {
        this.cancerStudy = cancerStudy;
        this.clinicalDataFile = clinicalDataFile;
        this.attributesType = AttributeTypes.valueOf(attributesDatatype);
    }

    public void importData() throws Exception
    {
        // if bulkLoading is ever turned off,
        // code has to be added to check whether
        // a clinical attribute update should be
        // perform instead of an insert
        MySQLbulkLoader.bulkLoadOn();

        FileReader reader =  new FileReader(clinicalDataFile);
        BufferedReader buff = new BufferedReader(reader);
        List<ClinicalAttribute> columnAttrs = grabAttrs(buff);
        
        int patientIdIndex = findPatientIdColumn(columnAttrs);
        int sampleIdIndex = findSampleIdColumn(columnAttrs);

        //validate required columns:
        if (patientIdIndex < 0) { //TODO - for backwards compatibility maybe add and !attributesType.toString().equals("MIXED")? See next TODO in addDatum()
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
        
        if (MySQLbulkLoader.isBulkLoad()) {
            MySQLbulkLoader.flushAll();
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
                    break;
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
                                      priorities[i]);
            attrs.add(attr);
            //skip PATIENT_ID / SAMPLE_ID columns, i.e. these are not clinical attributes but relational columns:
            if (attr.getAttrId().equals(PATIENT_ID_COLUMN_NAME) ||
            	attr.getAttrId().equals(SAMPLE_ID_COLUMN_NAME)) {
	            continue;
            }
        	ClinicalAttribute attrInDb = DaoClinicalAttribute.getDatum(attr.getAttrId());
            if (null==attrInDb) {
                DaoClinicalAttribute.addDatum(attr);
            }
            else if (attrInDb.isPatientAttribute() != attr.isPatientAttribute()) {
            	throw new DaoException("Illegal change in attribute type[SAMPLE/PATIENT] for attribute " + attr.getAttrId() + 
            			". An attribute cannot change from SAMPLE type to PATIENT type (or vice-versa) during import. This should " + 
            			"be changed manually first in DB.");
            }
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
        while ((line = buff.readLine()) != null) {

            line = line.trim();
            if (skipLine(line)) {
                continue;
            }

            String[] fields = getFields(line, columnAttrs);
            addDatum(fields, columnAttrs);
        }
    }

    private boolean skipLine(String line)
    {
        return (line.isEmpty() || line.substring(0,1).equals(METADATA_PREFIX));
    }

    private String[] getFields(String line, List<ClinicalAttribute> columnAttrs)
    {
        String[] fields = line.split(DELIMITER, -1);
        if (fields.length < columnAttrs.size()) {
            int origFieldsLen = fields.length;
            fields = Arrays.copyOf(fields, columnAttrs.size());
            Arrays.fill(fields, origFieldsLen, columnAttrs.size(), "");
        }
        return fields; 
    }

    private void addDatum(String[] fields, List<ClinicalAttribute> columnAttrs) throws Exception
    {
        int sampleIdIndex = findSampleIdColumn(columnAttrs);
        String stableSampleId = (sampleIdIndex >= 0) ? fields[sampleIdIndex] : "";
        //check if sample is not already added:
        Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudy.getInternalId(), stableSampleId, false);
        if (sample != null) {
        	//this should be a WARNING in case of TCGA studies (see https://github.com/cBioPortal/cbioportal/issues/839#issuecomment-203452415)
        	//and an ERROR in other studies. I.e. a sample should occur only once in clinical file!
        	if (stableSampleId.startsWith("TCGA-")) {
        		ProgressMonitor.logWarning("Sample " + stableSampleId + " found to be duplicated in your file. Only data of the first sample will be processed.");
        	}
        	else {
        		throw new RuntimeException("Error: Sample " + stableSampleId + " found to be duplicated in your file.");
        	}
        }
        else {
        	// sample is new/unique, go ahead and add its attributes,
        	// so attempt to add both a patient and sample to database
            int patientIdIndex = findPatientIdColumn(columnAttrs);
            int internalPatientId = (patientIdIndex >= 0) ?
                addPatientToDatabase(fields[patientIdIndex]) : -1; 
	        int internalSampleId = (stableSampleId.length() > 0) ?
	            addSampleToDatabase(stableSampleId, fields, columnAttrs) : -1;
	            
	        //count:
	        if (internalSampleId != -1) {
	        	numSamplesAdded++;
	        }
	
	        // this will happen when clinical file contains sample id, but not patient id
	        //TODO - this part, and the dummy patient added in addSampleToDatabase, can be removed as the field PATIENT_ID is now
	        //always required (as validated at start of importData() ). Probably kept here for "old" studies, but Ben's tests did not find anything...
	        // --> alternative would be to be less strict in validation at importData() and allow for missing PATIENT_ID when type is MIXED... 
	        if (internalPatientId == -1 && internalSampleId != -1) {
	            sample = DaoSample.getSampleById(internalSampleId);
	            internalPatientId = sample.getInternalPatientId();
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
	                addDatum(internalPatientId, columnAttrs.get(lc).getAttrId(), fields[lc],
	                         ClinicalAttribute.PATIENT_ATTRIBUTE);
	            }
	            else if (internalSampleId != -1) {
	                addDatum(internalSampleId, columnAttrs.get(lc).getAttrId(), fields[lc],
	                         ClinicalAttribute.SAMPLE_ATTRIBUTE);
	            }
	        }
        }
    }

    private int findPatientIdColumn(List<ClinicalAttribute> attrs)
    {
        return findAttributeColumnIndex(PATIENT_ID_COLUMN_NAME, attrs);
    }

    private int findSampleIdColumn(List<ClinicalAttribute> attrs)
    {
        return findAttributeColumnIndex(SAMPLE_ID_COLUMN_NAME, attrs);
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
        int internalSampleId = -1;
        if (validSampleId(sampleId) && !StableIdUtil.isNormal(sampleId)) {
            String stablePatientId = getStablePatientId(sampleId, fields, columnAttrs);
            if (validPatientId(stablePatientId)) {
                Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(), stablePatientId);
                if (patient == null) {
                    addPatientToDatabase(stablePatientId);
                    patient = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(), stablePatientId);
                }
                sampleId = StableIdUtil.getSampleId(sampleId);
               	internalSampleId = DaoSample.addSample(new Sample(sampleId,
                                                               patient.getInternalId(),
                                                               cancerStudy.getTypeOfCancerId()));
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
    
    public int getNumSamplesAdded() {
    	return numSamplesAdded;
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
     *
     * Imports clinical data and clinical attributes (from the worksheet)
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        try {
	        ProgressMonitor.setConsoleModeAndParseShowProgress(args);

	        String usageLine = "Import clinical files.\n" +
	                   "command line usage for importClinicalData:";
	         /*
	          * usage:
	          * --data <data_file.txt> --meta <meta_file.txt> --loadMode [directLoad|bulkLoad (default)] [--noprogress]
	          */
	
	        OptionParser parser = new OptionParser();
	        OptionSpec<String> data = parser.accepts( "data",
	               "profile data file" ).withRequiredArg().describedAs( "data_file.txt" ).ofType( String.class );
	        OptionSpec<String> meta = parser.accepts( "meta",
	               "meta (description) file" ).withOptionalArg().describedAs( "meta_file.txt" ).ofType( String.class );
	        OptionSpec<String> study = parser.accepts("study",
	                "cancer study id").withOptionalArg().describedAs("study").ofType(String.class);
	        OptionSpec<String> attributeFlag = parser.accepts("a",
	                "Flag for using MIXED_ATTRIBUTES (deprecated)").withOptionalArg().describedAs("a").ofType(String.class);
	        parser.accepts( "loadMode", "direct (per record) or bulk load of data" )
	          .withOptionalArg().describedAs( "[directLoad|bulkLoad (default)]" ).ofType( String.class );
	        parser.accepts("noprogress", "this option can be given to avoid the messages regarding memory usage and % complete");
	        
	        OptionSet options = null;
	        try {
	            options = parser.parse( args );
	        } catch (OptionException e) {
	        	ConsoleUtil.quitWithUsageLine(e.getMessage(), usageLine, parser);
	        }
	        File clinical_f = null;
	        if( options.has( data ) ){
	            clinical_f = new File( options.valueOf( data ) );
	        }else{
	        	ConsoleUtil.quitWithUsageLine("'data argument required.", usageLine, parser);
	        }
	        String attributesDatatype = null;
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

            SpringUtil.initDataSource();
            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyStableId);
            if (cancerStudy == null) {
                throw new IllegalArgumentException("Unknown cancer study: " + cancerStudyStableId);
            }
            else {
                ProgressMonitor.setCurrentMessage("Reading data from:  " + clinical_f.getAbsolutePath());
                int numLines = FileUtil.getNumLines(clinical_f);
                ProgressMonitor.setCurrentMessage(" --> total number of lines:  " + numLines);
                ProgressMonitor.setMaxValue(numLines);

                ImportClinicalData importClinicalData = new ImportClinicalData(cancerStudy, clinical_f, attributesDatatype);
                importClinicalData.importData();

                if (importClinicalData.getAttributesType() == ImportClinicalData.AttributeTypes.PATIENT_ATTRIBUTES ||
                		importClinicalData.getAttributesType() == ImportClinicalData.AttributeTypes.MIXED_ATTRIBUTES) { 
                	ProgressMonitor.setCurrentMessage("Total number of patient specific clinical attributes added:  "
                        + importClinicalData.getNumPatientSpecificClinicalAttributesAdded());
                }
                if (importClinicalData.getAttributesType() == ImportClinicalData.AttributeTypes.SAMPLE_ATTRIBUTES ||
                		importClinicalData.getAttributesType() == ImportClinicalData.AttributeTypes.MIXED_ATTRIBUTES) { 
                	ProgressMonitor.setCurrentMessage("Total number of sample specific clinical attributes added:  "
                        + importClinicalData.getNumSampleSpecificClinicalAttributesAdded());
                	ProgressMonitor.setCurrentMessage("Total number of samples added:  "
                        + importClinicalData.getNumSamplesAdded());
                }
                ProgressMonitor.setCurrentMessage("Total number of attribute values skipped because of empty value:  "
                        + importClinicalData.getNumEmptyClinicalAttributesSkipped());
                if (importClinicalData.getAttributesType() != ImportClinicalData.AttributeTypes.PATIENT_ATTRIBUTES &&
                	(importClinicalData.getNumSampleSpecificClinicalAttributesAdded() + importClinicalData.getNumSamplesAdded()) == 0) {
                	//should not occur: 
                	throw new RuntimeException("No data was added.  " +
                            "Please check your file format and try again.");
                }
                if (importClinicalData.getAttributesType() == ImportClinicalData.AttributeTypes.PATIENT_ATTRIBUTES &&
                    importClinicalData.getNumPatientSpecificClinicalAttributesAdded() == 0) {
                	//could occur if patient clinical file is given with only PATIENT_ID column:
                    throw new RuntimeException("No data was added.  " +
                            "Please check your file format and try again. If you only have sample clinical data, then a patients file with only PATIENT_ID column is not required.");
                }
                ProgressMonitor.setCurrentMessage("Done.");

            }
            ConsoleUtil.showMessages();
        } catch (Exception e) {
            ConsoleUtil.showWarnings();
            //exit with error status:
        	System.err.println ("\nABORTED! Error:  " + e.getMessage());
        	if (e.getMessage() == null)
	        	e.printStackTrace();
            System.exit(1);
        }
    }
}
