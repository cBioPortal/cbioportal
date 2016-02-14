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
    private int cancerStudyId;
    private File file;
    
    public ImportCopyNumberSegmentData(File file, int cancerStudyId)
    {
        this.file = file;
        this.cancerStudyId = cancerStudyId;
    }
    
    public void importData() throws Exception
    {
        MySQLbulkLoader.bulkLoadOn();
        FileReader reader = new FileReader(file);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine(); // skip header line
        long segId = DaoCopyNumberSegment.getLargestId();
        while ((line=buf.readLine()) != null) {
            ProgressMonitor.incrementCurValue();
            ConsoleUtil.showProgress();
            
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
            if (s == null) {
                assert StableIdUtil.isNormal(sampleId);
                continue;
            }
            CopyNumberSegment cns = new CopyNumberSegment(cancerStudyId, s.getInternalId(), strs[1], start, end, numProbes, segMean);
            cns.setSegId(++segId);
            DaoCopyNumberSegment.addCopyNumberSegment(cns);
        }
        MySQLbulkLoader.flushAll();
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

		SpringUtil.initDataSource();
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
        ProgressMonitor.setConsoleMode(true);
        
        File file = new File(dataFilename);
        System.out.println("Reading data from:  " + file.getAbsolutePath());
        int numLines = FileUtil.getNumLines(file);
        System.out.println(" --> total number of lines:  " + numLines);
        ProgressMonitor.setMaxValue(numLines);
        ImportCopyNumberSegmentData parser = new ImportCopyNumberSegmentData(file, cancerStudy.getInternalId());
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
