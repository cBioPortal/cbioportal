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
package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class ImportClinicalData {

    public static final String DELIMITER = "\t";
    public static final String METADATA_PREFIX = "#";
    public static final String SAMPLE_ID_COLUMN_NAME = "SAMPLE_ID";
    public static final String PATIENT_ID_COLUMN_NAME = "PATIENT_ID";
    public static final String SAMPLE_TYPE_COLUMN_NAME = "SAMPLE_TYPE";

	private File clinicalDataFile;
	private CancerStudy cancerStudy;

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
            if (value.equals("")) return true;
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
	
    public ImportClinicalData(CancerStudy cancerStudy, File clinicalDataFile)
    {
        this.cancerStudy = cancerStudy;
        this.clinicalDataFile = clinicalDataFile;
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

        importData(buff, columnAttrs);
        
        if (MySQLbulkLoader.isBulkLoad()) {
            MySQLbulkLoader.flushAll();
        }
    }

    private List<ClinicalAttribute> grabAttrs(BufferedReader buff) throws DaoException, IOException {
        List<ClinicalAttribute> attrs = new ArrayList<ClinicalAttribute>();

        String line = buff.readLine();
        String[] displayNames = splitFields(line);
        String[] descriptions, datatypes, attributeTypes, priorities, colnames;
        if (line.startsWith(METADATA_PREFIX)) {
            // contains meta data about the attributes
            descriptions = splitFields(buff.readLine());
            datatypes = splitFields(buff.readLine());
            attributeTypes = splitFields(buff.readLine());
            priorities = splitFields(buff.readLine());
            colnames = splitFields(buff.readLine());

            if (displayNames.length != colnames.length
                ||  descriptions.length != colnames.length
                ||  datatypes.length != colnames.length
                ||  attributeTypes.length != colnames.length
                ||  priorities.length != colnames.length) {
                throw new DaoException("attribute and metadata mismatch in clinical staging file");
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
            if (null==DaoClinicalAttribute.getDatum(attr.getAttrId())) {
                DaoClinicalAttribute.addDatum(attr);
            }
            attrs.add(attr);
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
        // attempt to add both a patient and sample to database
        int patientIdIndex = findPatientIdColumn(columnAttrs); 
        int internalPatientId = (patientIdIndex >= 0) ?
            addPatientToDatabase(fields[patientIdIndex]) : -1; 
        int sampleIdIndex = findSampleIdColumn(columnAttrs);
        String stableSampleId = (sampleIdIndex >= 0) ? fields[sampleIdIndex] : "";
        int internalSampleId = (stableSampleId.length() > 0) ?
            addSampleToDatabase(stableSampleId, fields, columnAttrs) : -1;

        // this will happen when clinical file contains sample id, but not patient id
        if (internalPatientId == -1 && internalSampleId != -1) {
            Sample sample = DaoSample.getSampleById(internalSampleId);
            internalPatientId = sample.getInternalPatientId();
        }

        for (int lc = 0; lc < fields.length; lc++) {
            if (MissingAttributeValues.has(fields[lc])) {
                continue;
            }
            boolean isPatientAttribute = columnAttrs.get(lc).isPatientAttribute(); 
            int indexOfIdColumn = (isPatientAttribute) ? patientIdIndex : sampleIdIndex; 
            if (addAttributeToDatabase(lc, indexOfIdColumn, fields[lc])) {
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
            if (patient != null) {
                internalPatientId = patient.getInternalId();
            }
            else {
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
                if (patient != null) {
                    Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudy.getInternalId(), sampleId);
                    if (sample != null) {
                        internalSampleId = sample.getInternalId();
                    }
                    else {
                        internalSampleId = DaoSample.addSample(new Sample(sampleId,
                                                               patient.getInternalId(),
                                                               cancerStudy.getTypeOfCancerId()));
                    }
                }
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

    private boolean addAttributeToDatabase(int attributeIndex, int indexOfIdColumn, String attributeValue)
    {
        return (attributeIndex != indexOfIdColumn && !attributeValue.isEmpty());
    }

    private void addDatum(int internalId, String attrId, String attrVal, String attrType) throws Exception
    {
        // if bulk loading is ever turned off, we need to check if
        // attribute value exists and if so, perfom an update
        if (attrType.equals(ClinicalAttribute.PATIENT_ATTRIBUTE)) {
            DaoClinicalData.addPatientDatum(internalId, attrId, attrVal.trim());
        }
        else {
            DaoClinicalData.addSampleDatum(internalId, attrId, attrVal.trim());
        }
    }

    /**
     *
     * Imports clinical data and clinical attributes (from the worksheet)
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        if (args.length < 2) {
            System.out.println("command line usage:  importClinical <clinical.txt> <cancer_study_id> [is_sample_data]");
            return;
        }

		try {
			CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(args[1]);
			if (cancerStudy == null) {
				System.err.println("Unknown cancer study: " + args[1]);
			}
			else {
				File clinical_f = new File(args[0]);
				System.out.println("Reading data from:  " + clinical_f.getAbsolutePath());
				int numLines = FileUtil.getNumLines(clinical_f);
				System.out.println(" --> total number of lines:  " + numLines);
				pMonitor.setMaxValue(numLines);

				ImportClinicalData importClinicalData = new ImportClinicalData(cancerStudy,
                                                                               clinical_f);
                importClinicalData.importData();
                System.out.println("Success!");
			}
		}
		catch (Exception e) {
			System.err.println ("Error:  " + e.getMessage());
        }
		finally {
            ConsoleUtil.showWarnings(pMonitor);
        }
    }
}
