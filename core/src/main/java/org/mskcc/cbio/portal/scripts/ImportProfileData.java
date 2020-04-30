/*
 * Copyright (c) 2015 - 2016 Memorial Sloan-Kettering Cancer Center.
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import joptsimple.*;

import org.cbioportal.model.EntityType;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

/**
 * Import 'profile' files that contain data matrices indexed by gene, case.
 *
 * @author ECerami
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class ImportProfileData extends ConsoleRunnable {

    public void run() {
        try {
            // Parse arguments
            // using a real options parser, helps avoid bugs
            String description = "Import 'profile' files that contain data matrices indexed by gene, case";
            OptionSet options = ConsoleUtil.parseStandardDataAndMetaUpdateOptions(args, description, true);
            File dataFile = new File((String) options.valueOf("data"));
            File descriptorFile = new File((String) options.valueOf( "meta" ) );
            // Check options, set default as false
            boolean updateInfo = false;
            if (options.has("update-info") && (((String) options.valueOf("update-info")).equalsIgnoreCase("true") || options.valueOf("update-info").equals("1"))) {
                updateInfo = true;
            }
            SpringUtil.initDataSource();
            ProgressMonitor.setCurrentMessage("Reading data from:  " + dataFile.getAbsolutePath());
            // Load genetic profile and gene panel
            GeneticProfile geneticProfile = null;
            String genePanel = null;
            try {
                geneticProfile = GeneticProfileReader.loadGeneticProfile( descriptorFile );
                genePanel = GeneticProfileReader.loadGenePanelInformation( descriptorFile );
            } catch (java.io.FileNotFoundException e) {
                throw new java.io.FileNotFoundException("Descriptor file '" + descriptorFile + "' not found.");
            }
            
            // Print profile report
            int numLines = FileUtil.getNumLines(dataFile);
            ProgressMonitor.setCurrentMessage(
                    " --> profile id:  " + geneticProfile.getGeneticProfileId() +
                    "\n --> profile name:  " + geneticProfile.getProfileName() +
                    "\n --> genetic alteration type:  " + geneticProfile.getGeneticAlterationType().name());
            ProgressMonitor.setMaxValue(numLines);
            
            // Check genetic alteration type 
            if (geneticProfile.getGeneticAlterationType() == GeneticAlterationType.MUTATION_EXTENDED || 
                geneticProfile.getGeneticAlterationType() == GeneticAlterationType.MUTATION_UNCALLED) {
                Set<String> filteredMutations = GeneticProfileReader.getVariantClassificationFilter( descriptorFile );
                Set<String> namespaces = GeneticProfileReader.getNamespaces( descriptorFile );
                ImportExtendedMutationData importer = new ImportExtendedMutationData(dataFile, geneticProfile.getGeneticProfileId(), genePanel, filteredMutations, namespaces);
                String swissprotIdType = geneticProfile.getOtherMetaDataField("swissprot_identifier");
                if (swissprotIdType != null && swissprotIdType.equals("accession")) {
                    importer.setSwissprotIsAccession(true);
                } else if (swissprotIdType != null && !swissprotIdType.equals("name")) {
                    throw new RuntimeException( "Unrecognized swissprot_identifier specification, must be 'name' or 'accession'.");
                }
                importer.importData();
            } else if (geneticProfile.getGeneticAlterationType() == GeneticAlterationType.FUSION) {
                ImportFusionData importer = new ImportFusionData(dataFile, geneticProfile.getGeneticProfileId(), genePanel);
                importer.importData();
            } else if (geneticProfile.getGeneticAlterationType() == GeneticAlterationType.STRUCTURAL_VARIANT) {
                ImportStructuralVariantData importer = new ImportStructuralVariantData(dataFile, geneticProfile.getGeneticProfileId(), genePanel);
                importer.importData();
            } else if (geneticProfile.getGeneticAlterationType() == GeneticAlterationType.GENERIC_ASSAY) {
                // add all missing `genetic_entities` for this assay to the database
                ImportGenericAssayEntity.importData(dataFile, geneticProfile.getGeneticAlterationType(), geneticProfile.getOtherMetaDataField("generic_entity_meta_properties"), updateInfo);
                
                ImportTabDelimData genericAssayProfileImporter = new ImportTabDelimData(dataFile, geneticProfile.getTargetLine(), geneticProfile.getGeneticProfileId(), genePanel, geneticProfile.getOtherMetaDataField("generic_entity_meta_properties"));
                genericAssayProfileImporter.importData(numLines);
            } else {
                ImportTabDelimData importer = new ImportTabDelimData(dataFile, geneticProfile.getTargetLine(), geneticProfile.getGeneticProfileId(), genePanel);
                importer.importData(numLines);
            }
       }
       catch (Exception e) {
    	   e.printStackTrace();
           throw new RuntimeException(e);
       }
    }

    /**
     * Makes an instance to run with the given command line arguments.
     *
     * @param args  the command line arguments to be used
     */
    public ImportProfileData(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args  the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportProfileData(args);
        runner.runInConsole();
    }
}
