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
import org.mskcc.cbio.portal.service.*;
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

    private boolean isSampleData;
	private File clinicalDataFile;
	private CancerStudy cancerStudy;
    private Entity cancerStudyEntity;
    private ProgressMonitor pMonitor;
	
    public ImportClinicalData(CancerStudy cancerStudy, File clinicalDataFile, boolean isSampleData, ProgressMonitor pMonitor)
    {
        this.pMonitor = pMonitor;
        this.cancerStudy = cancerStudy;
        this.cancerStudyEntity =
            ImportDataUtil.entityService.getCancerStudy(cancerStudy.getCancerStudyStableId()); 
        this.isSampleData = isSampleData;
        this.clinicalDataFile = clinicalDataFile;
    }

    public void importData() throws Exception
    {
        MySQLbulkLoader.bulkLoadOn();

        FileReader reader =  new FileReader(clinicalDataFile);
        BufferedReader buff = new BufferedReader(reader);
        List<ClinicalAttribute> columnAttrs = grabAttrs(buff);

        importData(buff, columnAttrs);
        
        if (MySQLbulkLoader.isBulkLoad()) {
            MySQLbulkLoader.flushAll();
        }
    }

    private void importData(BufferedReader buff, List<ClinicalAttribute> columnAttrs) throws Exception
    {
        String line;
        while ((line = buff.readLine()) != null) {

            line = line.trim();
            if (skipLine(line)) {
                continue;
            }

            String[] fields = line.split(DELIMITER);
            if (validLine(fields, columnAttrs)) {
                addDatum(fields, columnAttrs);
            }
            else {
                System.err.println("Corrupt line in data file, skipping: " + line);
            }
        }
    }

    private boolean skipLine(String line)
    {
        return (line.isEmpty() || line.substring(0,1).equals(METADATA_PREFIX));
    }

    private boolean validLine(String[] fields, List<ClinicalAttribute> columnAttrs)
    {
        if (fields.length < columnAttrs.size()) {
            int origFieldsLen = fields.length;
            fields = Arrays.copyOf(fields, columnAttrs.size());
            Arrays.fill(fields, origFieldsLen, columnAttrs.size(), "");
        }
        return (fields.length == columnAttrs.size());
    }

    private void addDatum(String[] fields, List<ClinicalAttribute> columnAttrs) throws Exception
    {
        int indexOfIdColumn = getIndexOfIdColumn(columnAttrs);
        String stablePatientOrSampleId = fields[indexOfIdColumn];

        int internalPatientOrSampleId = (isSampleData)  ? 
            addSampleToDatabase(stablePatientOrSampleId, fields, columnAttrs) : 
            addPatientToDatabase(stablePatientOrSampleId);

        if (internalPatientOrSampleId == -1) {
            System.err.println("Could not add patient or sample to table, skipping: " +
                               stablePatientOrSampleId);
            return;
        }

        for (int lc = 0; lc < fields.length; lc++) {
            if (addAttributeToDatabase(lc, indexOfIdColumn, fields[lc])) {
                addDatum(internalPatientOrSampleId, columnAttrs.get(lc).getAttrId(), fields[lc]);
                addEntityAttribute(stablePatientOrSampleId, lc, fields, columnAttrs);
            }
        }
    }

    private int getIndexOfIdColumn(List<ClinicalAttribute> columnAttrs) throws Exception
    {
        int indexOfIdColumn = (isSampleData) ? findSampleIdColumn(columnAttrs) : findPatientIdColumn(columnAttrs);

        if (indexOfIdColumn < 0) {
            throw new java.lang.UnsupportedOperationException("Clinical file is missing Id column header");
        }
        else {
            return indexOfIdColumn;
        }
    }

    private int addPatientToDatabase(String stableId) throws Exception
    {
        int internalPatientId = -1;
        if (validPatientId(stableId) && DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(), stableId) == null) {
           Patient patient = new Patient(cancerStudy, stableId);
           internalPatientId = DaoPatient.addPatient(patient);
           Entity patientEntity = ImportDataUtil.entityService.insertEntity(stableId, EntityType.PATIENT);
           ImportDataUtil.entityService.insertEntityLink(cancerStudyEntity.internalId, patientEntity.internalId);
       }

       return internalPatientId;
    }

    private int addSampleToDatabase(String sampleId, String[] fields, List<ClinicalAttribute> columnAttrs) throws Exception
    {
        int internalSampleId = -1;
        String stablePatientId = getStablePatientId(sampleId, fields, columnAttrs);
        if (validPatientId(stablePatientId)) {
            Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(), stablePatientId);
            if (patient == null) {
                addPatientToDatabase(stablePatientId);
                patient = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(), stablePatientId);
            }
            sampleId = StableIdUtil.getSampleId(sampleId);
            if (patient != null && DaoSample.getSampleByCancerStudyAndSampleId(cancerStudy.getInternalId(), sampleId) == null) {
                internalSampleId = DaoSample.addSample(new Sample(sampleId,
                                                                  patient.getInternalId(),
                                                                  cancerStudy.getTypeOfCancerId()));
                Entity sampleEntity = ImportDataUtil.entityService.insertEntity(sampleId, EntityType.SAMPLE);
                Entity patientEntity = ImportDataUtil.entityService.getPatient(cancerStudy.getCancerStudyStableId(),
                                                                patient.getStableId());
                ImportDataUtil.entityService.insertEntityLink(patientEntity.internalId, sampleEntity.internalId);
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

    private boolean addAttributeToDatabase(int attributeIndex, int indexOfIdColumn, String attributeValue)
    {
        return (attributeIndex != indexOfIdColumn && !attributeValue.isEmpty());
    }

    private void addDatum(int internalId, String attrId, String attrVal) throws Exception
    {
        if (isSampleData) {
            DaoClinicalData.addSampleDatum(internalId, attrId, attrVal);
        }
        else {
            DaoClinicalData.addPatientDatum(internalId, attrId, attrVal);
        }
    }

    private void addEntityAttribute(String stablePatientOrSampleId,
                                    int attrIndex, String[] fields,
                                    List<ClinicalAttribute> columnAttrs)
    {
        if (isSampleData) {
            String stableSampleId = StableIdUtil.getSampleId(stablePatientOrSampleId);
            String stablePatientId = getStablePatientId(stablePatientOrSampleId, fields, columnAttrs);
            Entity sampleEntity = ImportDataUtil.entityService.getSample(cancerStudy.getCancerStudyStableId(),
                                                                         stablePatientId, stableSampleId);
            ImportDataUtil.entityAttributeService.insertEntityAttribute(sampleEntity.internalId,
                                                                        columnAttrs.get(attrIndex).getAttrId(),
                                                                        fields[attrIndex]);
        }
        else {
            Entity patientEntity = ImportDataUtil.entityService.getPatient(cancerStudy.getCancerStudyStableId(),
                                                                           stablePatientOrSampleId);
            ImportDataUtil.entityAttributeService.insertEntityAttribute(patientEntity.internalId,
                                                                        columnAttrs.get(attrIndex).getAttrId(),
                                                                        fields[attrIndex]);
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
//        args = new String[] {"/Users/gaoj/projects/cbio-portal-data/studies/prad/su2c/data_clinical.txt",
//            "prad_su2c"};
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
                                                                               clinical_f,
                                                                               (args.length == 3) ? isSampleData(args[2]) : false,
                                                                               pMonitor);
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

    /**
     * Grabs the metadatas (clinical attributes) from the file, inserts them into the database,
     * and returns them as a list.
     *
     * @param buff
     * @return clinicalAttributes
     */
    private List<ClinicalAttribute> grabAttrs(BufferedReader buff) throws DaoException, IOException {
        List<ClinicalAttribute> attrs = new ArrayList<ClinicalAttribute>();

        String line = buff.readLine();
        String[] displayNames = splitFields(line);
        String[] descriptions, datatypes, colnames;
        if (line.startsWith(METADATA_PREFIX)) {
            // contains meta data about the attributes
            descriptions = splitFields(buff.readLine());
            datatypes = splitFields(buff.readLine());
            colnames = splitFields(buff.readLine());

            if (displayNames.length != colnames.length
                    ||  descriptions.length != colnames.length
                    ||  datatypes.length != colnames.length) {
                throw new DaoException("attribute and metadata mismatch in clinical staging file");
            }
        } else {
            // attribute Id header only
            colnames = displayNames;
            descriptions = new String[colnames.length];
            Arrays.fill(descriptions, ClinicalAttribute.MISSING);
            datatypes = new String[colnames.length] ;
            Arrays.fill(datatypes, ClinicalAttribute.DEFAULT_DATATYPE);
            displayNames = new String[colnames.length];
            Arrays.fill(displayNames, ClinicalAttribute.MISSING);
        }

        for (int i = 0; i < colnames.length; i+=1) {
            ClinicalAttribute attr =
                new ClinicalAttribute(colnames[i], displayNames[i], descriptions[i], datatypes[i], (isSampleData) ? false : true);
            if (null==DaoClinicalAttribute.getDatum(attr.getAttrId())) {
                DaoClinicalAttribute.addDatum(attr);
               ImportDataUtil.entityAttributeService.insertAttributeMetadata(colnames[i], displayNames[i],
                                                                             descriptions[i], AttributeType.valueOf(datatypes[i]));
            }
            attrs.add(attr);
        }

        return attrs;
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

    /**
     * helper function for spliting the *next* line in the reader
     * so, N.B. --  ! it alters the state of the reader
     * @param buff
     * @return
     */
    private String[] splitFields(String line) throws IOException {
        line = line.replaceAll("^"+METADATA_PREFIX+"+", "");
        String[] fields = line.split(DELIMITER);

        return fields;
    }

	private static boolean isSampleData(String parameterValue) {
		return (parameterValue.equalsIgnoreCase("t")) ?	true : false;
	}
}
