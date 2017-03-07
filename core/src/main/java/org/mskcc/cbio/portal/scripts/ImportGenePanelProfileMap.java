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

import java.io.*;
import java.util.*;

import org.mskcc.cbio.portal.model.GenePanel;
import org.mskcc.cbio.portal.repository.GenePanelRepository;
import org.cbioportal.model.*;
import joptsimple.*;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.util.SpringUtil;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author heinsz
 */
public class ImportGenePanelProfileMap extends ConsoleRunnable {
  
    private File genePanelProfileMapFile;
    private static Properties properties;
    private String cancerStudyStableId;
    private ApplicationContext context;

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
            if (context == null) {
                SpringUtil.initDataSource();
            }
            setFile(genePanel_f);
            importData();          
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
  
    public void importData() throws Exception {
        ProgressMonitor.setCurrentMessage("Reading data from:  " + genePanelProfileMapFile.getAbsolutePath());
        GenePanelRepository genePanelRepository = context == null ? SpringUtil.getGenePanelRepository() : (GenePanelRepository)context.getBean("genePanelRepository");
      
        FileReader reader =  new FileReader(genePanelProfileMapFile);
        BufferedReader buff = new BufferedReader(reader);
        List<String> profiles = getProfilesLine(buff);
        Integer sampleIdIndex = profiles.indexOf("SAMPLE_ID");
        if (sampleIdIndex < 0) {
            throw new RuntimeException("Missing SAMPLE_ID column in file " + genePanelProfileMapFile.getAbsolutePath());
        }
        profiles.remove((int)sampleIdIndex);
        List<Integer> profileIds = getProfileIds(profiles, genePanelRepository);
      
        // delete if the profile mapping are there already
        for (Integer id : profileIds) {
            if(genePanelRepository.sampleProfileMappingExistsByProfile(id)) {
                genePanelRepository.deleteSampleProfileMappingByProfile(id);
            }
        }

        String line;
        while((line = buff.readLine()) != null) {
            List<String> data  = new LinkedList<>(Arrays.asList(line.split("\t")));
            String sampleId = data.get(sampleIdIndex);
            Sample sample = genePanelRepository.getSampleByStableIdAndStudyId(sampleId, cancerStudyStableId);
          
            data.remove((int)sampleIdIndex);
            for (int i = 0; i < data.size(); i++) {
                List<GenePanel> genePanelList = genePanelRepository.getGenePanelByStableId(data.get(i));            
                if (genePanelList != null && genePanelList.size() > 0) {     
                    GenePanel genePanel = genePanelList.get(0);
                    Map<String, Object> map = new HashMap<>();
                    map.put("sampleId", sample.getInternalId());
                    map.put("profileId", profileIds.get(i));
                    map.put("panelId", genePanel.getInternalId());
                    genePanelRepository.insertGenePanelSampleProfileMap(map);
                }
                else {
                    ProgressMonitor.logWarning("No gene panel exists: " + data.get(i));
                }
            }          
        }                                
    }
  
    public List<String> getProfilesLine(BufferedReader buff) throws Exception {      
        String line = buff.readLine();
        while(line.startsWith("#")) {
            line = buff.readLine();
        }
      
        List<String> profiles = new LinkedList<>(Arrays.asList(line.split("\t")));
      
        return profiles;
    }
  
    public List<Integer> getProfileIds(List<String> profiles, GenePanelRepository genePanelRepository) {
        List<Integer> geneticProfileIds = new LinkedList<>();
        for(String profile : profiles) {
            if (!profile.startsWith(cancerStudyStableId)) {
                profile = cancerStudyStableId + "_" + profile;
            }
            GeneticProfile geneticProfile = genePanelRepository.getGeneticProfileByStableId(profile);          
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
  
    public void setContext(ApplicationContext context) {
        this.context = context;
    }
}
