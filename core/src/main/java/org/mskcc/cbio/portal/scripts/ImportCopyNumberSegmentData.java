
package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

import java.io.*;

/**
 * Import protein array antibody information into database.
 * @author jj
 */
public class ImportCopyNumberSegmentData {
    private ProgressMonitor pMonitor;
    private int cancerStudyId;
    private File file;
    
    public ImportCopyNumberSegmentData(File file, int cancerStudyId, ProgressMonitor pMonitor) {
        this.file = file;
        this.cancerStudyId = cancerStudyId;
        this.pMonitor = pMonitor;
    }
    
    /**
     * Import protein array antibody information. Antibodies that already exist 
     * in the database (based on array id) will be skipped.
     * @throws IOException
     * @throws DaoException 
     */
    public void importData() throws IOException, DaoException {
        FileReader reader = new FileReader(file);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine(); // skip header line
        while ((line=buf.readLine()) != null) {
            if (pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
            
            String[] strs = line.split("\t");
            if (strs.length<6) {
                System.err.println("wrong format: "+line);
            }

            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(cancerStudyId);
            ImportProfileData.addPatients(new String[] { CaseIdUtil.getPatientId(strs[0]) }, cancerStudy);
            ImportProfileData.addSamples(new String[] { CaseIdUtil.getSampleId(strs[0]) }, cancerStudy);

            String sampleId = CaseIdUtil.getSampleId(strs[0]);
            long start = Double.valueOf(strs[2]).longValue();
            long end = Double.valueOf(strs[3]).longValue();
            int numProbes = Double.valueOf(strs[4]).intValue();
            double segMean = Double.parseDouble(strs[5]);
            
            CopyNumberSegment cns = new CopyNumberSegment(cancerStudyId, sampleId, strs[1], start, end, numProbes, segMean);
            DaoCopyNumberSegment.addCopyNumberSegment(cns);
        }
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("command line usage:  importCopyNumberSegmentData.pl <copy_number_segment_file.seg> cancer_study_id");
            return;
        }
        
        int cancerStudyId = DaoCancerStudy.getCancerStudyByStableId(args[1]).getInternalId();
        
        if (DaoCopyNumberSegment.segmentDataExistForCancerStudy(cancerStudyId)) {
            System.err.println("Ignore this file since seg data for cancer study "+args[1]+" has already been imported.");
            return;
        }
        
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);
        
        File file = new File(args[0]);
        System.out.println("Reading data from:  " + file.getAbsolutePath());
        int numLines = FileUtil.getNumLines(file);
        System.out.println(" --> total number of lines:  " + numLines);
        pMonitor.setMaxValue(numLines);
        ImportCopyNumberSegmentData parser = new ImportCopyNumberSegmentData(file, cancerStudyId, pMonitor);
        parser.importData();
        ConsoleUtil.showWarnings(pMonitor);
        System.err.println("Done.");
    }
}
