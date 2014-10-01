
package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.model.*;

import joptsimple.*;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.*;

/**
 * Import protein array antibody information into database.
 * @author jj
 */
public class ImportCopyNumberSegmentData {
    private ProgressMonitor pMonitor;
    private int cancerStudyId;
    private File file;
    
    public ImportCopyNumberSegmentData(File file, int cancerStudyId, ProgressMonitor pMonitor)
    {
        this.file = file;
        this.cancerStudyId = cancerStudyId;
        this.pMonitor = pMonitor;
    }
    
    public void importData() throws Exception
    {
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
            ImportDataUtil.addPatients(new String[] { strs[0] }, cancerStudy);
            ImportDataUtil.addSamples(new String[] { strs[0] }, cancerStudy);

            String sampleId = StableIdUtil.getSampleId(strs[0]);
            long start = Double.valueOf(strs[2]).longValue();
            long end = Double.valueOf(strs[3]).longValue();
            int numProbes = new BigDecimal((strs[4])).intValue();
            double segMean = Double.parseDouble(strs[5]);
            
            Sample s = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudyId, sampleId);
            CopyNumberSegment cns = new CopyNumberSegment(cancerStudyId, s.getInternalId(), strs[1], start, end, numProbes, segMean);
        
        }
    }
    
    public static void main(String[] args) throws Exception
    {
        if (args.length < 4) {
            System.out.println("command line usage:  importCopyNumberSegmentData --data <copy_number_segment_file.seg> --meta <meta_cna_seg.txt>");
            return;
        }

        String[] filenames = getFilenames(args);
        Properties properties = new Properties();
        properties.load(new FileInputStream(filenames[1]));
        
        CancerStudy cancerStudy = getCancerStudy(properties);
        
        if (segmentDataExistsForCancerStudy(cancerStudy)) {
            System.err.println("Ignoring this file since seg data for cancer study " + cancerStudy.getCancerStudyStableId() + " has already been imported: " + filenames[0]);
            return;
        }

        importCopyNumberSegmentFileMetadata(cancerStudy, properties);
        importCopyNumberSegmentFileData(cancerStudy, filenames[0]);
        
        System.err.println("Done.");
    }

    private static String[] getFilenames(String[] args) throws Exception
    {
        String[] filenames = new String[2];
        OptionParser parser = new OptionParser();
        OptionSpec<String> data = parser.accepts( "data",
            "copy number segment data file" ).withRequiredArg().describedAs( "copy_number_segment_file.seg" ).ofType( String.class );
        OptionSpec<String> meta = parser.accepts( "meta",
            "meta (description) file" ).withRequiredArg().describedAs( "meta_cna_seg.txt" ).ofType( String.class );
        parser.acceptsAll(Arrays.asList("dbmsAction", "loadMode"));
        OptionSet options = parser.parse( args );

        if (options.has(data)) {
            filenames[0] = options.valueOf(data);
        }
        else {
            throw new Exception ("'data' argument is missing!");
        }

        if (options.has(meta)) {
            filenames[1] = options.valueOf(meta);
        }
        else {
            throw new Exception ("'meta' argument is missing!");
        }

        return filenames;
    }

    private static CancerStudy getCancerStudy(Properties properties) throws Exception
    {
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(properties.getProperty("cancer_study_identifier"));
        if (cancerStudy == null) {
            throw new Exception("Unknown cancer study: " + properties.getProperty("cancer_study_identifier"));
        }
        return cancerStudy;
    }

    private static boolean segmentDataExistsForCancerStudy(CancerStudy cancerStudy) throws Exception
    {
        return (DaoCopyNumberSegment.segmentDataExistForCancerStudy(cancerStudy.getInternalId()));
    }

    private static void importCopyNumberSegmentFileMetadata(CancerStudy cancerStudy, Properties properties) throws Exception
    {
        CopyNumberSegmentFile copyNumSegFile = new CopyNumberSegmentFile();
        copyNumSegFile.cancerStudyId = cancerStudy.getInternalId();
        copyNumSegFile.referenceGenomeId = getRefGenId(properties.getProperty("reference_genome_id")); 
        copyNumSegFile.description = properties.getProperty("description");
        copyNumSegFile.filename = properties.getProperty("data_filename");
        DaoCopyNumberSegmentFile.addCopyNumberSegmentFile(copyNumSegFile);
    }

    private static void importCopyNumberSegmentFileData(CancerStudy cancerStudy, String dataFilename) throws Exception
    {
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);
        
        File file = new File(dataFilename);
        System.out.println("Reading data from:  " + file.getAbsolutePath());
        int numLines = FileUtil.getNumLines(file);
        System.out.println(" --> total number of lines:  " + numLines);
        pMonitor.setMaxValue(numLines);
        ImportCopyNumberSegmentData parser = new ImportCopyNumberSegmentData(file, cancerStudy.getInternalId(), pMonitor);
        parser.importData();
    }

    private static CopyNumberSegmentFile.ReferenceGenomeId getRefGenId(String potentialRefGenId) throws Exception
    {
        if (CopyNumberSegmentFile.ReferenceGenomeId.has(potentialRefGenId)) {
            return CopyNumberSegmentFile.ReferenceGenomeId.valueOf(potentialRefGenId);
        }
        else {
            throw new Exception ("Unknown reference genome id: " + potentialRefGenId);
        }
    }
}
