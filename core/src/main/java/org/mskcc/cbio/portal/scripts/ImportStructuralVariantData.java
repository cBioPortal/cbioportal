/*
 * Copyright (c) 2018 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
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

import org.mskcc.cbio.maf.TabDelimitedFileUtil;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.model.StructuralVariant;
import org.mskcc.cbio.portal.util.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Imports a structural variant file.
 * @author Sander Tan
 */

public class ImportStructuralVariantData {

    // Initialize variables
    private File structuralVariantFile;
    private int geneticProfileId;
    private String genePanel;
    private Set<String> sampleSet  = new HashSet<>();

    public ImportStructuralVariantData(File structuralVariantFile, int geneticProfileId, String genePanel) throws DaoException {
        this.structuralVariantFile = structuralVariantFile;
        this.geneticProfileId = geneticProfileId;
        this.genePanel = genePanel;
    }

    public void importData() throws IOException, DaoException {

        FileReader reader = new FileReader(this.structuralVariantFile);
        BufferedReader buf = new BufferedReader(reader);
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        String line = buf.readLine();
        StructuralVariantUtil structuralVariantUtil = new StructuralVariantUtil(line);

        int recordCount = 0;
        // Genetic profile is read in first
        GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileById(geneticProfileId);
        ArrayList <Integer> orderedSampleList = new ArrayList<Integer>();
        long id = DaoStructuralVariant.getLargestInternalId();
        while ((line = buf.readLine()) != null) {
            ProgressMonitor.incrementCurValue();
            ConsoleUtil.showProgress();
            if( !line.startsWith("#") && line.trim().length() > 0) {
                recordCount++;
                String parts[] = line.split("\t", -1);
                StructuralVariant structuralVariant = structuralVariantUtil.parseStructuralVariantRecord(parts);
                structuralVariant.setInternalId(++id);
                structuralVariant.setGeneticProfileId(geneticProfileId);
                if (!structuralVariantUtil.hasRequiredStructuralVariantFields(structuralVariant)) {
                    ProgressMonitor.logWarning("Invalid Site 1 or 2 Ensembl transcript ID or exon found, ignoring structural variant for SV record #" +
                            recordCount + " (sample, site 1 gene, site 2 gene):  (" +
                            structuralVariant.getSampleId() + ", " + structuralVariant.getSite1HugoSymbol() +
                            ", " + structuralVariant.getSite2HugoSymbol() + ")");
                    continue;
                }

                // get sample
                Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(
                        geneticProfile.getCancerStudyId(),
                        StableIdUtil.getSampleId(structuralVariant.getSampleId()));

                // check sample existence
                if (sample == null) {
                    ProgressMonitor.logWarning("Sample not found:  " + structuralVariant.getSampleId() + ". Ignoring it.");
                } else {
                    // Set sample internal id
                    structuralVariant.setSampleIdInternal(sample.getInternalId());

                    // The current structural variant model, the input file always contains 2 genes, so in this implementation both are required.

                    //  Assume we are dealing with Entrez Gene Ids
                    String site1HugoSymbol = structuralVariant.getSite1HugoSymbol();
                    String site2HugoSymbol = structuralVariant.getSite2HugoSymbol();
                    long site1EntrezGeneId = structuralVariant.getSite1EntrezGeneId();
                    long site2EntrezGeneId = structuralVariant.getSite2EntrezGeneId();

                    CanonicalGene site1CanonicalGene = setCanonicalGene(site1EntrezGeneId, site1HugoSymbol, daoGene);
                    CanonicalGene site2CanonicalGene = setCanonicalGene(site2EntrezGeneId, site2HugoSymbol, daoGene);

                    // If neither of the genes is recognized, skip the line
                    if(site1CanonicalGene == null) {
                        ProgressMonitor.logWarning("Gene not found:  " + site1HugoSymbol + " ["
                                + site1EntrezGeneId + "]. Ignoring it "
                                + "and all fusion data associated with it!");
                    } else if (site2CanonicalGene == null) {
                        ProgressMonitor.logWarning("Gene not found:  " + site2HugoSymbol + " ["
                                + site2EntrezGeneId + "]. Ignoring it "
                                + "and all fusion data associated with it!");
                    // If both genes are recognized, continue
                    } else {
                        // Save the Entrez Gene Id if it was not saved before
                        if (site1EntrezGeneId == TabDelimitedFileUtil.NA_LONG) {
                            structuralVariant.setSite1EntrezGeneId(site1CanonicalGene.getEntrezGeneId());
                        }
                        if (site2EntrezGeneId == TabDelimitedFileUtil.NA_LONG) {
                            structuralVariant.setSite2EntrezGeneId(site2CanonicalGene.getEntrezGeneId());
                        }
                        // Add structural variant
                        DaoStructuralVariant.addStructuralVariantToBulkLoader(structuralVariant);

                        // Add sample to sample profile list, which is important for gene panels
                        if (!DaoSampleProfile.sampleExistsInGeneticProfile(sample.getInternalId(), geneticProfileId) && !sampleSet.contains(sample.getStableId())) {
                            if (genePanel != null) {
                                DaoSampleProfile.addSampleProfile(sample.getInternalId(), geneticProfileId, GeneticProfileUtil.getGenePanelId(genePanel));
                            } else {
                                DaoSampleProfile.addSampleProfile(sample.getInternalId(), geneticProfileId, null);
                            }
                        }
                        sampleSet.add(sample.getStableId());
                        orderedSampleList.add(sample.getInternalId());
                    }
                }
            }
        }
        DaoGeneticProfileSamples.addGeneticProfileSamples(geneticProfileId, orderedSampleList);

        buf.close();
        MySQLbulkLoader.flushAll();
    }

    private CanonicalGene setCanonicalGene(long siteEntrezGeneId, String siteHugoSymbol, DaoGeneOptimized daoGene) {
        CanonicalGene siteCanonicalGene = null;

        // If the Entrez Gene Id is not "NA" set the canonical gene.
        if (siteEntrezGeneId != TabDelimitedFileUtil.NA_LONG) {
            siteCanonicalGene = daoGene.getGene(siteEntrezGeneId);
        }

        // If no gene can be found based on Entrez Gene ID, try Symbol.
        if (siteCanonicalGene == null) {
            siteCanonicalGene = daoGene.getNonAmbiguousGene(siteHugoSymbol, true);
        }

        return siteCanonicalGene;
    }
}
