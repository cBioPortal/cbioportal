
package org.mskcc.cgds.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mskcc.cgds.dao.DaoCopyNumberSegment;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.CopyNumberSegment;
import org.mskcc.cgds.util.ConsoleUtil;
import org.mskcc.cgds.util.FileUtil;
import org.mskcc.cgds.util.ProgressMonitor;

/**
 * Import protein array antibody information into database.
 * @author jj
 */
public class ImportCopyNumberSegmentData {
    private ProgressMonitor pMonitor;
    private File file;
    private Pattern p = Pattern.compile("(TCGA\\-[^\\-]+-[^\\-]+).*");
    
    public ImportCopyNumberSegmentData(File file, ProgressMonitor pMonitor) {
        this.file = file;
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

            String caseId = strs[0];
            Matcher m = p.matcher(caseId);
            if (m.matches()) {
                caseId = m.group(1);
            }
            long start = Double.valueOf(strs[2]).longValue();
            long end = Double.valueOf(strs[3]).longValue();
            int numProbes = Integer.parseInt(strs[4]);
            double segMean = Double.parseDouble(strs[5]);
            
            CopyNumberSegment cns = new CopyNumberSegment(caseId, strs[1], start, end, numProbes, segMean);
            DaoCopyNumberSegment.addCopyNumberSegment(cns);
        }
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("command line usage:  importCopyNumberSegmentData.pl <copy_number_segment_file.seg>");
            System.exit(1);
        }
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);
        
        File file = new File(args[0]);
        System.out.println("Reading data from:  " + file.getAbsolutePath());
        int numLines = FileUtil.getNumLines(file);
        System.out.println(" --> total number of lines:  " + numLines);
        pMonitor.setMaxValue(numLines);
        ImportCopyNumberSegmentData parser = new ImportCopyNumberSegmentData(file, pMonitor);
        parser.importData();
        ConsoleUtil.showWarnings(pMonitor);
        System.err.println("Done.");
    }
}
