/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.scripts;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import joptsimple.OptionSet;
import org.mskcc.cbio.portal.dao.ClickHouseBulkLoader;
import org.mskcc.cbio.portal.dao.ClickHouseOptimizer;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoCopyNumberSegment;
import org.mskcc.cbio.portal.dao.DaoCopyNumberSegmentFile;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CopyNumberSegment;
import org.mskcc.cbio.portal.model.CopyNumberSegmentFile;
import org.mskcc.cbio.portal.model.ReferenceGenome;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.FileUtil;
import org.mskcc.cbio.portal.util.FractionGenomeAlteredCalculator;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.util.StableIdUtil;

/**
 * Import Segment data into database.
 * @author jj
 */
public class ImportCopyNumberSegmentData extends ConsoleRunnable {

    private int entriesSkipped;
    private boolean isIncrementalUpdateMode;
    private Set<Integer> processedSampleIds;
    private FractionGenomeAlteredCalculator fractionGenomeAlteredCalculator;

    private void importData(File file, int cancerStudyId) throws IOException, DaoException {
        FileReader reader = new FileReader(file);
        BufferedReader buf = new BufferedReader(reader);
        try {
            String line = buf.readLine(); // skip header line
            long segId = DaoCopyNumberSegment.getLargestId();
            processedSampleIds = new HashSet<>();
            fractionGenomeAlteredCalculator = new FractionGenomeAlteredCalculator();
            while ((line=buf.readLine()) != null) {
                ProgressMonitor.incrementCurValue();
                ConsoleUtil.showProgress();
                
                String[] strs = line.split("\t");
                if (strs.length<6) {
                    System.err.println("wrong format: "+line);
                }

                String chrom = strs[1].trim();
                //validate in same way as GistitReader:
                ValidationUtils.validateChromosome(chrom);
                
                long start = Double.valueOf(strs[2]).longValue();
                long end = Double.valueOf(strs[3]).longValue();
                if (start >= end) {
                    //workaround to skip with warning, according to https://github.com/cBioPortal/cbioportal/issues/839#issuecomment-203452415
                    ProgressMonitor.logWarning("Start position of segment is not lower than end position. Skipping this entry.");
                    entriesSkipped++;
                    continue;
                }            
                int numProbes = new BigDecimal((strs[4])).intValue();
                double segMean = Double.parseDouble(strs[5]);

                String sampleId = StableIdUtil.getSampleId(strs[0]);
                Sample s = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudyId, sampleId);
                if (s == null) {
                    if (StableIdUtil.isNormal(sampleId)) {
                        entriesSkipped++;
                        continue;
                    }
                    else {
                        //this likely will not be reached since samples are added on the fly above if not known to database
                        throw new RuntimeException("Unknown sample id '" + sampleId + "' found in seg file: " + file.getCanonicalPath());
                    }
                }
                CopyNumberSegment cns = new CopyNumberSegment(cancerStudyId, s.getInternalId(), chrom, start, end, numProbes, segMean);
                cns.setSegId(++segId);
                DaoCopyNumberSegment.addCopyNumberSegment(cns);
                processedSampleIds.add(s.getInternalId());
                fractionGenomeAlteredCalculator.addSegment(s.getInternalId(), start, end, segMean);
            }
            if (isIncrementalUpdateMode) {
                DaoCopyNumberSegment.deleteSegmentDataForSamples(cancerStudyId, processedSampleIds);
            }
        }
        finally {
            buf.close();
        }
    }
    
    public void run() {
        try {
            String description = "Import 'segment data' files";
            
            OptionSet options = ConsoleUtil.parseStandardDataAndMetaOptions(args, description, true);
            if (options.has("loadMode") && !"bulkLoad".equalsIgnoreCase((String) options.valueOf("loadMode"))) {
                throw new UnsupportedOperationException("This loader supports bulkLoad load mode only, but "
                        + options.valueOf("loadMode")
                        + " has been supplied.");
            }
            String dataFile = (String) options.valueOf("data");
            File descriptorFile = new File((String) options.valueOf("meta"));
            isIncrementalUpdateMode = options.has("overwrite-existing");
        
            Properties properties = new Properties();
            properties.load(new FileInputStream(descriptorFile));
            
            ProgressMonitor.setCurrentMessage("Reading data from:  " + dataFile);
            
            CancerStudy cancerStudy = getCancerStudy(properties);
            
            if (!isIncrementalUpdateMode && segmentDataExistsForCancerStudy(cancerStudy)) {
                 throw new IllegalArgumentException("Seg data for cancer study " + cancerStudy.getCancerStudyStableId() + " has already been imported: " + dataFile);
            }
            ClickHouseBulkLoader.bulkLoadOn();
            importCopyNumberSegmentFileMetadata(cancerStudy, properties);
            importCopyNumberSegmentFileData(cancerStudy, dataFile);
            DaoCopyNumberSegment.createFractionGenomeAlteredClinicalData(cancerStudy.getInternalId(),
                    fractionGenomeAlteredCalculator.getFractionGenomeAltered(), isIncrementalUpdateMode);
            ClickHouseOptimizer.optimizeTables("clinical_sample", "clinical_patient");
            ClickHouseBulkLoader.bulkLoadOff();
        } catch (RuntimeException e) {
            throw e;
        } catch (IOException|DaoException e) {
            throw new RuntimeException(e);
        }
    }

    private static CancerStudy getCancerStudy(Properties properties) throws DaoException {
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(properties.getProperty("cancer_study_identifier").trim());
        if (cancerStudy == null) {
            throw new RuntimeException("Unknown cancer study: " + properties.getProperty("cancer_study_identifier").trim());
        }
        return cancerStudy;
    }

    private static boolean segmentDataExistsForCancerStudy(CancerStudy cancerStudy) throws DaoException {
        return (DaoCopyNumberSegment.segmentDataExistForCancerStudy(cancerStudy.getInternalId()));
    }

    private void importCopyNumberSegmentFileMetadata(CancerStudy cancerStudy, Properties properties) throws DaoException {
        CopyNumberSegmentFile copyNumSegFile = new CopyNumberSegmentFile();
        copyNumSegFile.cancerStudyId = cancerStudy.getInternalId();
        String referenceGenomeId = properties.getProperty("reference_genome_id").trim();
        String referenceGenome = cancerStudy.getReferenceGenome();
        if (referenceGenome == null) {
            referenceGenome = ReferenceGenome.HOMO_SAPIENS_DEFAULT_GENOME_NAME;
        }
        if (!referenceGenomeId.equalsIgnoreCase(referenceGenome)) {
            ProgressMonitor.setCurrentMessage(" Genome Build Name does not match, expecting " 
                    + cancerStudy.getReferenceGenome());
        }
        copyNumSegFile.referenceGenomeId = getRefGenId(referenceGenomeId); 
        copyNumSegFile.description = properties.getProperty("description").trim();
        copyNumSegFile.filename = properties.getProperty("data_filename").trim();
        CopyNumberSegmentFile storedCopyNumSegFile = DaoCopyNumberSegmentFile.getCopyNumberSegmentFile(cancerStudy.getInternalId());
        if (isIncrementalUpdateMode && storedCopyNumSegFile != null) {
            if (storedCopyNumSegFile.referenceGenomeId != copyNumSegFile.referenceGenomeId) {
                throw new IllegalStateException("You are trying to upload "
                        + copyNumSegFile.referenceGenomeId
                        + " reference genome data into "
                        + storedCopyNumSegFile.referenceGenomeId
                        + " reference genome data.");
            }
        } else {
            DaoCopyNumberSegmentFile.addCopyNumberSegmentFile(copyNumSegFile);
        }
    }

    private void importCopyNumberSegmentFileData(CancerStudy cancerStudy, String dataFilename) throws IOException, DaoException {
        File file = new File(dataFilename);
        int numLines = FileUtil.getNumLines(file);
        ProgressMonitor.setCurrentMessage(" --> total number of data lines:  " + (numLines-1));
        ProgressMonitor.setMaxValue(numLines);
        entriesSkipped = 0;
        importData(file, cancerStudy.getInternalId());
        ProgressMonitor.setCurrentMessage(" --> total number of entries skipped:  " + entriesSkipped);
    }

    private static CopyNumberSegmentFile.ReferenceGenomeId getRefGenId(String potentialRefGenId) {
        if (CopyNumberSegmentFile.ReferenceGenomeId.has(potentialRefGenId)) {
            return CopyNumberSegmentFile.ReferenceGenomeId.valueOf(potentialRefGenId);
        }
        else {
            throw new RuntimeException ("Unknown reference genome id: " + potentialRefGenId);
        }
    }

    /**
     * Makes an instance to run with the given command line arguments.
     *
     * @param args  the command line arguments to be used
     */
    public ImportCopyNumberSegmentData(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args  the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportCopyNumberSegmentData(args);
        runner.runInConsole();
    }
}
