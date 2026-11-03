/*
 * Copyright (c) 2016 - 2022 Memorial Sloan Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.scripts;

import java.io.*;
import java.util.*;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.mskcc.cbio.portal.dao.ClickHouseBulkLoader;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoGenePanel;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoMutation;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.dao.DaoSampleProfile;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.GenePanel;
import org.mskcc.cbio.portal.model.shared.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.util.StableIdUtil;

/**
 *
 * @author heinsz, sandertan
 */
public class ImportGenePanelProfileMap extends ConsoleRunnable {

    private File genePanelProfileMapFile;
    private String cancerStudyStableId;

    private static final String NA_STRING = "NA";
    private static final String WXS_STRING = "WXS";
    private static final String WGS_STRING = "WGS";
    private static final String WXS_WGS_STRING = "WXS/WGS";

    @Override
    public void run() {
        try {
            String progName = "ImportGenePanelProfileMap";
            String description = "Import gene panel profile map files.";
            // usage: --data <data_file.txt> --meta <meta_file.txt> [--noprogress]

            OptionParser parser = new OptionParser();
            OptionSpec<String> data = parser.accepts( "data",
                   "gene panel file" ).withRequiredArg().describedAs( "data_file.txt" ).ofType( String.class );
            OptionSpec<String> meta = parser.accepts( "meta",
                   "gene panel file" ).withRequiredArg().describedAs( "meta_file.txt" ).ofType( String.class );
            parser.accepts("noprogress", "this option can be given to avoid the messages regarding memory usage and % complete");

            // supported by the uploader already. Added for uniformity, to do not cause error when upstream software uses this flag
            parser.accepts("overwrite-existing",
                    "Enables re-uploading gene panel profile map data that already exists.")
                    .withOptionalArg().describedAs("overwrite-existing").ofType(String.class);
            OptionSet options;
            try {
                options = parser.parse( args );
            } catch (OptionException e) {
                throw new UsageException(
                        progName, description, parser,
                        e.getMessage());
            }
            File genePanel_f;
            if( options.has( data ) ){
                genePanel_f = new File( options.valueOf( data ) );
            } else {
                throw new UsageException(
                        progName, description, parser,
                        "'data' argument required.");
            }

            if( options.has( meta ) ){
                Properties properties = new TrimmedProperties();
                properties.load(new FileInputStream(options.valueOf(meta)));
                cancerStudyStableId = properties.getProperty("cancer_study_identifier");
            } else {
                throw new UsageException(
                        progName, description, parser,
                        "'meta' argument required.");
            }

            setFile(genePanel_f);
            importData();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* This function will return null for special cases "WXS", "WGS", and "WXS/WGS". Otherwise
       it will throw a RuntimeException if the specified genePanelName is not in the database.
    */
    private Integer determineGenePanelId(String genePanelName) {
        // use null for WXS (whole exome sequencing) or WGS (whole genome sequencing)
        if (WXS_STRING.equals(genePanelName) || WGS_STRING.equals(genePanelName) || WXS_WGS_STRING.equals(genePanelName)) {
            return null;
        }
        // extract gene panel ID
        GenePanel genePanel = DaoGenePanel.getGenePanelByStableId(genePanelName);
        if (genePanel != null) {
            return genePanel.getInternalId();
        } else {
            // Throw an exception if gene panel is not in database
            throw new RuntimeException("Gene panel cannot be found in database: " + genePanelName);
        }
    }

    public void importData() throws Exception {
        ProgressMonitor.setCurrentMessage("Reading data from: " + genePanelProfileMapFile.getAbsolutePath());
        try (FileReader reader = new FileReader(genePanelProfileMapFile);
             BufferedReader buff = new BufferedReader(reader)) {

        // Extract and parse first line which contains the profile names
        List<String> profiles = getProfilesLine(buff);
        Integer sampleIdIndex = profiles.indexOf("SAMPLE_ID");
        if (sampleIdIndex < 0) {
            throw new RuntimeException("Missing SAMPLE_ID column in file " + genePanelProfileMapFile.getAbsolutePath());
        }
        profiles.remove((int)sampleIdIndex);
        List<Integer> profileIds = getProfileIds(profiles);
        
        // Get cancer study
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyStableId);
        
        // Loop over gene panel matrix and load into database
        ProgressMonitor.setCurrentMessage("Loading gene panel profile matrix data to database..");
        String row;
        Set<DaoSampleProfile.SampleProfileTuple> sampleProfileTuples = new HashSet<>();
        ClickHouseBulkLoader.bulkLoadOn();
        ProgressMonitor.logWarning("Reading gene panel profile file line by line.");
        while((row = buff.readLine()) != null) {
            List<String> row_data = new LinkedList<>(Arrays.asList(row.split("\t")));
            
            // Extract and parse sample ID
            // Use StableIdUtil to convert IDs to match what would be stored in DB
            // Specifically, TCGA samples have additional processing that changes the sample stable id
            String sampleId = StableIdUtil.getSampleId(row_data.get(sampleIdIndex));
            
            Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudy.getInternalId(), sampleId);
            row_data.remove((int)sampleIdIndex);


            // Loop over the values in the row
            for (int i = 0; i < row_data.size(); i++) {
                String genePanelName = row_data.get(i);

                // NA triggers specific case to indicate sample was not profiled
                // e.g. an aggregate study from multiple institutions
                // one of which definitely did not profile for the sv profile
                if (NA_STRING.equals(genePanelName)) {
                    continue;
                }

                Integer genePanelId = determineGenePanelId(genePanelName);
                Integer geneticProfileId = profileIds.get(i);
                int sampleInternalId = sample.getInternalId();

                sampleProfileTuples.add(new DaoSampleProfile.SampleProfileTuple(geneticProfileId, sampleInternalId, genePanelId));
            }
        }
        ProgressMonitor.logWarning("Upserting sample to profile mappings into database.");
        DaoSampleProfile.upsertSampleToProfileMapping(sampleProfileTuples);

        // update mutation counts with the latest sequencing information after the sample profile upsert
        // deals with issue where mutation counts are missing due to sample profile not yet updated at the time of
        // the first mutation count calculation during the mutation import
        ProgressMonitor.setCurrentMessage("Updating mutation counts in database..");
        ProgressMonitor.logWarning("Updating mutation counts for genetic profiles.");
        for (int i = 0; i < profileIds.size(); i++) {
            GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileById(profileIds.get(i));
            if (geneticProfile.getGeneticAlterationType() == GeneticAlterationType.MUTATION_EXTENDED) {
                DaoMutation.createMutationCountClinicalData(geneticProfile);
            }
        }
        ProgressMonitor.logWarning("Flushing ClickHouse bulk loader.");
        ClickHouseBulkLoader.flushAll();
        ProgressMonitor.logWarning("Finished updating mutation counts for genetic profiles.");
        } // end try-with-resources
    }

    private List<String> getProfilesLine(BufferedReader buff) throws Exception {
        String line = buff.readLine();
        while(line.startsWith("#")) {
            line = buff.readLine();
        }
        return new LinkedList<>(Arrays.asList(line.split("\t")));
    }

    private List<Integer> getProfileIds(List<String> profiles) {
        List<Integer> geneticProfileIds = new LinkedList<>();
        for(String profile : profiles) {
            if (!profile.startsWith(cancerStudyStableId)) {
                profile = cancerStudyStableId + "_" + profile;
            }
            GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileByStableId(profile);
            if (geneticProfile != null) {
                geneticProfileIds.add(geneticProfile.getGeneticProfileId());
            }
            else {
                throw new RuntimeException("Cannot find genetic profile " + profile + " in the database.");
            }
        }
        return geneticProfileIds;
    }


    public void setFile(File genePanelProfileMapFile)
    {
        this.genePanelProfileMapFile = genePanelProfileMapFile;
    }

    /**
     * Makes an instance to run with the given command line arguments.
     *
     * @param args  the command line arguments to be used
     */
    public ImportGenePanelProfileMap(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args  the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportGenePanelProfileMap(args);
        runner.runInConsole();
    }
}
