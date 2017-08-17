/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
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
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

import java.io.*;
import joptsimple.*;
import java.util.*;
import org.mskcc.cbio.portal.repository.GenePanelRepositoryLegacy;

/**
 *
 * @author heinsz
 */
public class ImportGenePanelProfileMap extends ConsoleRunnable {

    private File genePanelProfileMapFile;
    private static Properties properties;
    private String cancerStudyStableId;

    @Override
    public void run() {
        try {
            String progName = "ImportGenePanelProfileMap";
            String description = "Import gene panel profile map files.";
            // usage: --data <data_file.txt> --meta <meta_file.txt> --loadMode [directLoad|bulkLoad (default)] [--noprogress]

            OptionParser parser = new OptionParser();
            OptionSpec<String> data = parser.accepts( "data",
                   "gene panel file" ).withRequiredArg().describedAs( "data_file.txt" ).ofType( String.class );
            OptionSpec<String> meta = parser.accepts( "meta",
                   "gene panel file" ).withRequiredArg().describedAs( "meta_file.txt" ).ofType( String.class );
            parser.accepts("noprogress", "this option can be given to avoid the messages regarding memory usage and % complete");

            OptionSet options = null;
            try {
                options = parser.parse( args );
            } catch (OptionException e) {
                throw new UsageException(
                        progName, description, parser,
                        e.getMessage());
            }
            File genePanel_f = null;
            if( options.has( data ) ){
                genePanel_f = new File( options.valueOf( data ) );
            } else {
                throw new UsageException(
                        progName, description, parser,
                        "'data' argument required.");
            }

            if( options.has( meta ) ){
                properties = new TrimmedProperties();
                properties.load(new FileInputStream(options.valueOf(meta)));
                cancerStudyStableId = properties.getProperty("cancer_study_identifier");
            } else {
                throw new UsageException(
                        progName, description, parser,
                        "'meta' argument required.");
            }

            setFile(genePanel_f);
            SpringUtil.initDataSource();
            importData();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void importData() throws Exception {
        List<Integer> samplesToDelete = new ArrayList();
        List<Integer> profilesToDelete = new ArrayList();
        ProgressMonitor.setCurrentMessage("Reading data from:  " + genePanelProfileMapFile.getAbsolutePath());
        GenePanelRepositoryLegacy genePanelRepositoryLegacy = (GenePanelRepositoryLegacy)SpringUtil.getApplicationContext().getBean("genePanelRepositoryLegacy");

        FileReader reader =  new FileReader(genePanelProfileMapFile);
        BufferedReader buff = new BufferedReader(reader);
        List<String> profiles = getProfilesLine(buff);
        Integer sampleIdIndex = profiles.indexOf("SAMPLE_ID");
        if (sampleIdIndex < 0) {
            throw new RuntimeException("Missing SAMPLE_ID column in file " + genePanelProfileMapFile.getAbsolutePath());
        }
        profiles.remove((int)sampleIdIndex);
        List<Integer> profileIds = getProfileIds(profiles, genePanelRepositoryLegacy);

        // delete if the profile mapping are there already
        for (Integer id : profileIds) {
            if(genePanelRepositoryLegacy.sampleProfileMappingExistsByProfile(id)) {
                genePanelRepositoryLegacy.deleteSampleProfileMappingByProfile(id);
            }
        }

        String line;
        CancerStudy cs = DaoCancerStudy.getCancerStudyByStableId(cancerStudyStableId);
        while((line = buff.readLine()) != null) {
            List<String> data  = new LinkedList<>(Arrays.asList(line.split("\t")));
            String sampleId = data.get(sampleIdIndex);

            Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(cs.getInternalId() ,sampleId);

            data.remove((int)sampleIdIndex);
            for (int i = 0; i < data.size(); i++) {
                GenePanel genePanel = DaoGenePanel.getGenePanelByStableId(data.get(i));             
                if (genePanel != null) {
                    if (DaoSampleProfile.sampleExistsInGeneticProfile(sample.getInternalId(), profileIds.get(i))) {
                        samplesToDelete.add(sample.getInternalId());
                        profilesToDelete.add(profileIds.get(i));                           
                    }   
                    DaoSampleProfile.addSampleProfile(sample.getInternalId(), profileIds.get(i), genePanel.getInternalId());
                }
                else {
                    ProgressMonitor.logWarning("No gene panel exists: " + data.get(i));
                }
            }
        }
        ProgressMonitor.setCurrentMessage("Deleting necessary records from sample_profile.");
        DaoSampleProfile.deleteRecords(samplesToDelete, profilesToDelete);
        ProgressMonitor.setCurrentMessage("Loading gene panel profile matrix data to database..");
        MySQLbulkLoader.flushAll();
    }

    public List<String> getProfilesLine(BufferedReader buff) throws Exception {
        String line = buff.readLine();
        while(line.startsWith("#")) {
            line = buff.readLine();
        }

        List<String> profiles = new LinkedList<>(Arrays.asList(line.split("\t")));

        return profiles;
    }

    public List<Integer> getProfileIds(List<String> profiles, GenePanelRepositoryLegacy genePanelRepositoryLegacy) {
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
