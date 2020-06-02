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

import java.io.*;
import java.util.*;
import org.mskcc.cbio.maf.*;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.model.ExtendedMutation.MutationEvent;
import org.mskcc.cbio.portal.util.*;

/**
 * Imports a fusion file.
 * Columns may be in any order.
 * Creates an ExtendedMutation instances for each row.
 *
 * @author Selcuk Onur Sumer
 */
public class ImportFusionData {
    public static final String FUSION = "Fusion";
    private File fusionFile;
    private int geneticProfileId;
    private String genePanelID;
    private Set<String> sampleSet  = new HashSet<>();

    public ImportFusionData(File fusionFile, int geneticProfileId, String genePanelID) {
        this.fusionFile = fusionFile;
        this.geneticProfileId = geneticProfileId;
        this.genePanelID = genePanelID;
    }

    public void importData() throws IOException, DaoException {
        Map<MutationEvent, MutationEvent> existingEvents =
                new HashMap<MutationEvent, MutationEvent>();
        Map<ExtendedMutation,ExtendedMutation> mutations = new HashMap<ExtendedMutation,ExtendedMutation>();
        long mutationEventId = DaoMutation.getLargestMutationEventId();
        GenePanel genePanel = null;
        if (genePanelID != null) {
            genePanel = DaoGenePanel.getGenePanelByStableId(genePanelID);
        }

        // Initialize, this makes sure that mutation_events are always loaded before mutations:
        MySQLbulkLoader.getMySQLbulkLoader("mutation_event");
        MySQLbulkLoader.getMySQLbulkLoader("mutation");

        FileReader reader = new FileReader(this.fusionFile);
        BufferedReader buf = new BufferedReader(reader);
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        //  The MAF File Changes fairly frequently, and we cannot use column index constants.
        String line = buf.readLine();
        line = line.trim();
        FusionFileUtil fusionUtil = new FusionFileUtil(line);
        boolean addEvent;
        GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileById(geneticProfileId);
        while ((line = buf.readLine()) != null) {
            ProgressMonitor.incrementCurValue();
            ConsoleUtil.showProgress();
            if( !line.startsWith("#") && line.trim().length() > 0) {
                FusionRecord record = fusionUtil.parseRecord(line);
                // process case id
                String barCode = record.getTumorSampleID();
                // backwards compatible part (i.e. in the new process, the sample should already be there.
                //TODO - replace this workaround later with an exception:
                Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(
                        geneticProfile.getCancerStudyId(),
                        StableIdUtil.getSampleId(barCode));
                // can be null in case of 'normal' sample:
                if (sample == null) {
                    if (StableIdUtil.isNormal(barCode)) {
                        line = buf.readLine();
                        continue;
                    }
                    else {
                        throw new RuntimeException("Unknown sample id '" + barCode + "' found in tab-delimited file: " + this.fusionFile.getCanonicalPath());
                    }
                }
                //  Assume we are dealing with Entrez Gene Ids (this is the best / most stable option)
                String geneSymbol = record.getHugoGeneSymbol();
                long entrezGeneId = record.getEntrezGeneId();
                CanonicalGene gene = null;
                if (entrezGeneId != TabDelimitedFileUtil.NA_LONG) {
                    gene = daoGene.getGene(entrezGeneId);
                }
                if (gene == null) {
                    // If Entrez Gene ID Fails, try Symbol.
                    gene = daoGene.getNonAmbiguousGene(geneSymbol, true);
                }
                if(gene == null) {
                    ProgressMonitor.logWarning("Gene not found:  " + geneSymbol + " ["
                            + entrezGeneId + "]. Ignoring it "
                            + "and all fusion data associated with it!");
                    continue;
                }
                if (genePanel != null && !GeneticProfileUtil.geneInPanel(gene, genePanel)) {
                    ProgressMonitor.logWarning("Gene is not profiled in gene panel:  " + geneSymbol + " ["
                                               + entrezGeneId + "] " + genePanel.getStableId() + ". Ignoring it "
                                               + "and all fusion data associated with it!");
                } else {
                    // create a mutation instance with default values
                    ExtendedMutation mutation = ExtendedMutationUtil.newMutation();
                    mutation.setGeneticProfileId(geneticProfileId);
                    mutation.setSampleId(sample.getInternalId());
                    mutation.setGene(gene);
                    mutation.setSequencingCenter(record.getCenter());
                    mutation.setProteinChange(record.getFusion());
                    // TODO we may need get mutation type from the file
                    // instead of defining a constant
                    mutation.setMutationType(FUSION);
                    mutation.setMutationStatus(record.getFusionStatus().toUpperCase());
                    MutationEvent event =
                        existingEvents.containsKey(mutation.getEvent()) ?
                        existingEvents.get(mutation.getEvent()) :
                        DaoMutation.getMutationEvent(mutation.getEvent());
                    if (event != null) {
                        mutation.setEvent(event);
                        addEvent = false;
                    } else {
                        mutation.setMutationEventId(++mutationEventId);
                        existingEvents.put(mutation.getEvent(), mutation.getEvent());
                        addEvent = true;
                    }
                    // add fusion (as a mutation)
                    ExtendedMutation existingMutation = mutations.get(mutation);
                    if (existingMutation != null) {
                        ProgressMonitor.logWarning("Duplicate fusion entry found: " + mutation.getGeneSymbol() + " for " + mutation.getProteinChange() + ". Skipping.");
                        continue;
                    } else {
                        // add fusion (as a mutation)
                        DaoMutation.addMutation(mutation, addEvent);
                        mutations.put(mutation, mutation);
                    }
                    if (!sampleSet.contains(sample.getStableId()) && !DaoSampleProfile.sampleExistsInGeneticProfile(sample.getInternalId(), geneticProfileId)) {
                        Integer panelId = (genePanel == null) ? null : genePanel.getInternalId();
                        DaoSampleProfile.addSampleProfile(sample.getInternalId(), geneticProfileId, panelId);
                    }                    
                    sampleSet.add(sample.getStableId());
                }
            }
        }
        buf.close();
        if( MySQLbulkLoader.isBulkLoad()) {
            MySQLbulkLoader.flushAll();
        }
    }
}
