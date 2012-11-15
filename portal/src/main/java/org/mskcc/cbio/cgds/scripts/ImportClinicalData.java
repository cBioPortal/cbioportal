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

package org.mskcc.cbio.cgds.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.mskcc.cbio.cgds.dao.DaoCancerStudy;
import org.mskcc.cbio.cgds.dao.DaoClinicalData;
import org.mskcc.cbio.cgds.dao.DaoClinicalFreeForm;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.util.ConsoleUtil;
import org.mskcc.cbio.cgds.util.FileUtil;
import org.mskcc.cbio.cgds.util.ProgressMonitor;

/**
 * Imports Clinical Data.
 *
 * @author Ethan Cerami.
 */
public class ImportClinicalData {
    private File clinicalDataFile;
    private ProgressMonitor pMonitor;
    private int caseIdCol = -1;
    private int osMonthsCol = -1;
    private int osStatusCol = -1;
    private int dfsMonthCol = -1;
    private int dfsStatusCol = -1;
    private int ageCol = -1;
    private CancerStudy cancerStudy;
    private ArrayList<String> freeFormHeaders = new ArrayList<String>();
    private List<Integer> freeFormInludeColumns = new ArrayList<Integer>();

    /**
     * Constructor.
     *
     * @param clinicalDataFile File
     * @param pMonitor         ProgressMonitor
     */
    public ImportClinicalData(File clinicalDataFile, ProgressMonitor pMonitor) {
        this.pMonitor = pMonitor;
        this.clinicalDataFile = clinicalDataFile;
    }

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
     * @throws org.mskcc.cgds.dao.DaoException
     */
    public void importData() throws IOException, DaoException {
        // create reader
        FileReader reader = new FileReader(clinicalDataFile);
        BufferedReader buf = new BufferedReader(reader);

        DaoClinicalData daoClinical = new DaoClinicalData();
        DaoClinicalFreeForm daoClinicalFreeForm = new DaoClinicalFreeForm();

        // skip column headings
        String colHeadingLine = buf.readLine();

        //  Figure Out Which Column Number Contains the Data we Want
        extractHeaders(colHeadingLine);
        String[] parts;

        //  At a minimum, we must have Case ID
        if (caseIdCol > -1){// && osStatusCol > -1 && osMonthsCol > -1) {
            // start reading data
            String line = buf.readLine();
            while (line != null) {
                if (pMonitor != null) {
                    pMonitor.incrementCurValue();
                    ConsoleUtil.showProgress(pMonitor);
                }
                if (!line.startsWith("#") && line.trim().length() > 0) {
                    parts = line.split("\t",-1);
                    String caseId = parts[caseIdCol];
                    Double osMonths = getDouble(parts, osMonthsCol);
                    String osStatus = getString(parts, osStatusCol);
                    Double dfsMonths = getDouble(parts, dfsMonthCol);
                    String dfsStatus = getString(parts, dfsStatusCol);
                    Double ageAtDiagnosis = getDouble(parts, ageCol);
                    daoClinical.addCase(caseId, osMonths, osStatus, dfsMonths, dfsStatus,
                            ageAtDiagnosis);
                    
                    if (cancerStudy != null) {
                        for (int i : freeFormInludeColumns) {
                            String name = freeFormHeaders.get(i);
                            String value = parts[i].trim();
                            if (!value.isEmpty() && !"NA".equals(value)) {
                                daoClinicalFreeForm.addDatum(cancerStudy.getInternalId(),
                                            caseId, name, value);
                            }
                        }
                    }
                }
                line = buf.readLine();
            }
        } else {
            pMonitor.logWarning("Could not parse clinical file.");
            pMonitor.logWarning("Appropriate columns could not be identified.  " +
                    "Check the file and try again");
            throw new IOException ("Could not parse clinical file.");
        }
    }

    private void extractHeaders(String colHeadingLine) {
        HashSet<String> caseIdNames = new HashSet<String>();
        caseIdNames.add("BCRPATIENTBARCODE".toLowerCase());
        caseIdNames.add("bcr_patient_barcode".toLowerCase());
        caseIdNames.add("CASE_ID".toLowerCase());
        caseIdNames.add("patient".toLowerCase());
        caseIdNames.add("ID".toLowerCase());
        caseIdNames.add("tcga_id");

        HashSet<String> osStatusNames = new HashSet<String>();
        osStatusNames.add("VITALSTATUS".toLowerCase());
        osStatusNames.add("OS_STATUS".toLowerCase());

        HashSet<String> osMonthsNames = new HashSet<String>();
        osMonthsNames.add("OverallSurvival(mos)".toLowerCase());
        osMonthsNames.add("OS_MONTHS".toLowerCase());

        HashSet<String> dfsMonthsNames = new HashSet<String>();
        dfsMonthsNames.add("ProgressionFreeSurvival (mos)#".toLowerCase());
        dfsMonthsNames.add("DFS_MONTHS".toLowerCase());

        HashSet<String> dfsStatusNames = new HashSet<String>();
        dfsStatusNames.add("ProgressionFreeStatus".toLowerCase());
        dfsStatusNames.add("DFS_STATUS".toLowerCase());

        HashSet<String> ageNames = new HashSet<String>();
        ageNames.add("AgeAtDiagnosis (yrs)".toLowerCase());
        ageNames.add("age");

        String[] parts = colHeadingLine.split("\t");
        
        for (int i = 0; i < parts.length; i++)
        {
            String header = parts[i].toLowerCase();
            
            freeFormHeaders.add(parts[i]);
            
            if (caseIdNames.contains(header)) {
                caseIdCol = i;
            } else if (osStatusNames.contains(header)) {
                osStatusCol = i;
            } else if (osMonthsNames.contains(header)) {
                osMonthsCol = i;
            } else if (dfsMonthsNames.contains(header)) {
                dfsMonthCol = i;
            } else if (dfsStatusNames.contains(header)) {
                dfsStatusCol = i;
            } else if (ageNames.contains(header)) {
                ageCol = i;
            } else {
                freeFormInludeColumns.add(i);
            }
        }
    }
    
    private Double getDouble(String parts[], int index) {
        if (index < 0) {
            return null;
        } else {
            String value = parts[index];
            if (value == null) {
                return null;
            } else if (value.trim().length() == 0) {
                return null;
            } else if (value.equalsIgnoreCase("NA")) {
                return null;
            } else {
                try {
                    Double dValue = Double.parseDouble(value);
                    return dValue;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
    }

    private String getString(String parts[], int index) {
        if (index < 0) {
            return null;
        } else {
            String value = parts[index];
            if (value == null) {
                return null;
            } else if (value.trim().length() == 0) {
                return null;
            } else if (value.equalsIgnoreCase("NA")) {
                return null;
            } else if (value.equalsIgnoreCase("missing")) {
                return null;
            } else {
                return value;
            }
        }
    }

    /**
     * The big deal main.
     */
    public static void main(String[] args) throws DaoException {
        ProgressMonitor pMonitor = new ProgressMonitor();
        
        // check args
        if (args.length < 2) {
            System.out.println("command line usage:  importClinicalData.pl <cancer_study_id> <data_file.txt>");
            System.exit(1);
        }

        try {
            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(args[0]);
            if (cancerStudy == null) {
                System.err.println("Unknown cancer study:  " + args[0]);
            } else {
                File dataFile = new File(args[1]);

                System.err.println("Reading data from:  " + dataFile.getAbsolutePath());
                int numLines = FileUtil.getNumLines(dataFile);
                System.err.println(" --> total number of lines:  " + numLines);
                pMonitor.setMaxValue(numLines);

                ImportClinicalData importClinicalData = new ImportClinicalData(cancerStudy, dataFile, pMonitor);
                importClinicalData.importData();
                System.err.println("Success!");

                DaoClinicalFreeForm daoClinicalFreeForm = new DaoClinicalFreeForm();
                HashSet<String> clinicalParameters = daoClinicalFreeForm.getDistinctParameters
                        (cancerStudy.getInternalId());
                System.err.println("Stored the following clinical variables:");
                for (String key:  clinicalParameters) {
                    System.err.println (key);
                }
            }
        } catch (IOException e) {
            System.err.println ("Error:  " + e.getMessage());
        } catch (DaoException e) {
            System.err.println ("Error:  " + e.getMessage());
        } finally {
            ConsoleUtil.showWarnings(pMonitor);
        }
    }
}
