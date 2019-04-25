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

/**
 *
 * @author heinsz, sandertan
 */
public class ImportGenePanelProfileMap extends ConsoleRunnable {

    private File genePanelProfileMapFile;
    private String cancerStudyStableId;

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
            SpringUtil.initDataSource();
            importData();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void importData() throws Exception {
        
        ProgressMonitor.setCurrentMessage("Reading data from: " + genePanelProfileMapFile.getAbsolutePath());
        FileReader reader = new FileReader(genePanelProfileMapFile);
        BufferedReader buff = new BufferedReader(reader);
        
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
        while((row = buff.readLine()) != null) {
            List<String> row_data = new LinkedList<>(Arrays.asList(row.split("\t")));
            
            // Extract and parse sample ID
            String sampleId = row_data.get(sampleIdIndex);
            Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudy.getInternalId(), sampleId);
            row_data.remove((int)sampleIdIndex);
            
            // Loop over the values in the row
            for (int i = 0; i < row_data.size(); i++) {
                
                // Extract gene panel ID
                String genePanelName = row_data.get(i);
                GenePanel genePanel = DaoGenePanel.getGenePanelByStableId(genePanelName);

                // Add gene panel information to database
                if (genePanel != null) {
                    DaoSampleProfile.updateSampleProfile(
                        sample.getInternalId(), 
                        profileIds.get(i), 
                        genePanel.getInternalId());

                // Throw an error if gene panel is not in database and is not NA
                } else {
                    if (!genePanelName.equals("NA")) {
                        throw new RuntimeException("Gene panel cannot be found in database: " + genePanelName);
                    }
                }
            }
        }
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
