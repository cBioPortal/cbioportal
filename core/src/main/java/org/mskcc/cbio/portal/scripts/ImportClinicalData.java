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

import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoClinicalData;
import org.mskcc.cbio.portal.dao.DaoClinicalAttribute;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.ClinicalAttribute;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.FileUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;

import java.io.*;
import java.util.*;
import org.mskcc.cbio.portal.dao.MySQLbulkLoader;

public class ImportClinicalData {

    public static final String METADATA_PREIX = "#";
    public static final String DELIMITER = "\t";
    public static final String CASE_ID_COLUMN_NAME = "SAMPLE_ID";

	private File clinicalDataFile;
	private CancerStudy cancerStudy;
    private ProgressMonitor pMonitor;
	
    /**
     * Constructor.
     *
     * @param cancerStudy   Cancer Study
     * @param clinicalDataFile File
     * @param pMonitor         ProgressMonitor
     */
    public ImportClinicalData(CancerStudy cancerStudy, File clinicalDataFile, ProgressMonitor pMonitor) {
        this.cancerStudy = cancerStudy;
        this.pMonitor = pMonitor;
        this.clinicalDataFile = clinicalDataFile;
    }

    /**
     * Method to import data.
     *
     * @throws java.io.IOException
     * @throws org.mskcc.cbio.portal.dao.DaoException
     */
    public void importData() throws IOException, DaoException {
        MySQLbulkLoader.bulkLoadOn();
        FileReader reader =  new FileReader(clinicalDataFile);
        BufferedReader buff = new BufferedReader(reader);

        List<ClinicalAttribute> columnAttrs = grabAttrs(buff);
        int iCaseId = findCaseIDColumn(columnAttrs);

        Set<String> caseIds = new HashSet<String>();
        
        String line;
        while ((line = buff.readLine()) != null) {
            line = line.trim();
            
            if (line.isEmpty() || line.substring(0,1).equals(METADATA_PREIX)) {
                // ignore lines with the METADATA_PREFIX
                continue;
            }

            String[] fields = line.split(DELIMITER);
            if (fields.length > columnAttrs.size()) {
                System.err.println("more attributes than header: "+line);
                continue;
            }
            
            String caseId = fields[iCaseId].trim();
            for (int i = 0; i < fields.length; i++) {
                if (i!=iCaseId && !fields[i].isEmpty()) {
                    DaoClinicalData.addDatum(cancerStudy.getInternalId(), caseId, columnAttrs.get(i).getAttrId(), fields[i].trim());
                    caseIds.add(caseId);
                }
            }
        }
        
        Set<String> attrIds = new HashSet<String>();
        for (ClinicalAttribute attr : columnAttrs) {
            attrIds.add(attr.getAttrId());
        }
        // overwrite the old data
        DaoClinicalData.removeData(cancerStudy.getInternalId(), caseIds, attrIds);
        
        MySQLbulkLoader.flushAll();
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

        if (args.length != 2) {
            System.out.println("command line usage:  importClinical <clinical.txt> <cancer_study_id>");
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

				ImportClinicalData importClinicalData = new ImportClinicalData(cancerStudy, clinical_f, pMonitor);
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
        if (line.startsWith(METADATA_PREIX)) {
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
            // attribute ID header only
            descriptions = displayNames;
            colnames = displayNames;
            datatypes = new String[displayNames.length] ;
            Arrays.fill(datatypes, "STRING"); // STRING by default -- TODO: better to guess from data
        }

        for (int i = 0; i < colnames.length; i+=1) {
            ClinicalAttribute attr =
                    new ClinicalAttribute(colnames[i], displayNames[i], descriptions[i], datatypes[i]);
            if (null==DaoClinicalAttribute.getDatum(attr.getAttrId())) {
                DaoClinicalAttribute.addDatum(attr);
            }
            attrs.add(attr);
        }

        return attrs;
    }
    
    private int findCaseIDColumn(List<ClinicalAttribute> attrs) {
        for (int i=0; i<attrs.size(); i++) {
            String attrId = attrs.get(i).getAttrId();
            if (attrId.equalsIgnoreCase("SAMPLE_ID")||attrId.equalsIgnoreCase("CASE_ID")) {
                return i;
            }
        }
        
        throw new java.lang.UnsupportedOperationException("Clinicla file must contain a column of SAMPLE_ID");
    }

    /**
     * helper function for spliting the *next* line in the reader
     * so, N.B. --  ! it alters the state of the reader
     * @param buff
     * @return
     */
    private String[] splitFields(String line) throws IOException {
        line = line.replaceAll("^"+METADATA_PREIX+"+", "");
        String[] fields = line.split(DELIMITER);

        return fields;
    }
}
