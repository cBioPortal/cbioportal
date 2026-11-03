/*
 * Copyright (c) 2018 - 2022 The Hyve B.V.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.scripts;

import java.io.*;
import java.util.*;
import org.mskcc.cbio.maf.TabDelimitedFileUtil;
import org.mskcc.cbio.portal.dao.ClickHouseBulkLoader;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.dao.DaoSampleProfile;
import org.mskcc.cbio.portal.dao.DaoStructuralVariant;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.model.StructuralVariant;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.GeneticProfileUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.util.StableIdUtil;
import org.mskcc.cbio.portal.util.StructuralVariantUtil;
import org.mskcc.cbio.portal.util.TsvUtil;

/**
 * Imports a structural variant file.
 * @author Sander Tan
 */

public class ImportStructuralVariantData {

    // Initialize variables
    private final File structuralVariantFile;
    private final int geneticProfileId;
    private final Integer genePanelId;
    private final Set<String> namespaces;

    private final boolean isIncrementalUpdateMode;

    public ImportStructuralVariantData(
        File structuralVariantFile, 
        int geneticProfileId, 
        String genePanel, 
        Set<String> namespaces,
        boolean isIncrementalUpdateMode
    ) throws DaoException {
        this.structuralVariantFile = structuralVariantFile;
        this.geneticProfileId = geneticProfileId;
        this.genePanelId = (genePanel == null) ? null : GeneticProfileUtil.getGenePanelId(genePanel);
        this.namespaces = namespaces;
        this.isIncrementalUpdateMode = isIncrementalUpdateMode;
    }

    public void importData() throws IOException, DaoException {

        FileReader reader = new FileReader(this.structuralVariantFile);
        BufferedReader buf = new BufferedReader(reader);
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        String line = buf.readLine();
        StructuralVariantUtil structuralVariantUtil = new StructuralVariantUtil(line, namespaces);

        int recordCount = 0;
        // Genetic profile is read in first
        GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileById(geneticProfileId);
        Set<Integer> sampleIds = new HashSet<>();
        long id = DaoStructuralVariant.getLargestInternalId();
        Set<String> uniqueSVs = new HashSet<>();
        while ((line = buf.readLine()) != null) {
            ProgressMonitor.incrementCurValue();
            ConsoleUtil.showProgress();
            if(TsvUtil.isDataLine(line)) {
                recordCount++;
                String parts[] = TsvUtil.splitTsvLine(line);
                StructuralVariant structuralVariant = structuralVariantUtil.parseStructuralVariantRecord(parts);
                structuralVariant.setInternalId(++id);
                structuralVariant.setGeneticProfileId(geneticProfileId);
                if (!structuralVariantUtil.hasRequiredStructuralVariantFields(structuralVariant)) {
                    ProgressMonitor.logWarning(
                            "Data requirements not satisfied for SV record #" + recordCount +
                            " : sampleId was '" + structuralVariant.getSampleId() + "' (required)." +
                            " And at least one involved gene must be identified by entrez_id or hugo symbol: " +
                            " site1 geneId : '" + structuralVariant.getSite1EntrezGeneId() + "'" +
                            " site1 hugoSymbol : '" + structuralVariant.getSite1EntrezGeneId() + "'" +
                            " site2 geneId : '" + structuralVariant.getSite2EntrezGeneId() + "'" +
                            " site2 hugoSymbol : '" + structuralVariant.getSite2EntrezGeneId() + "'");
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

                    // Check for an invalid entrez gene id and make it null (but keep processing record)
                    if (site1EntrezGeneId != TabDelimitedFileUtil.NA_LONG && site1CanonicalGene == null) {
                        ProgressMonitor.logWarning("Could not find entrez gene id for gene 1: [" + site1EntrezGeneId
                                + "]. If we found gene 2, this record will still be loaded but without gene 1.");
                        site1EntrezGeneId = TabDelimitedFileUtil.NA_LONG;
                    }
                    if (site2EntrezGeneId != TabDelimitedFileUtil.NA_LONG && site2CanonicalGene == null) {
                        ProgressMonitor.logWarning("Could not find entrez gene id for gene 2: [" + site2EntrezGeneId
                                + "]. If we found gene 1, this record will still be loaded but without gene 2.");
                        site2EntrezGeneId = TabDelimitedFileUtil.NA_LONG;
                    }

                    // If neither of the genes is recognized, skip the line
                    if(site1CanonicalGene == null && site2CanonicalGene == null) {
                        ProgressMonitor.logWarning("Could not find gene 1: " + site1HugoSymbol + " [" + site1EntrezGeneId
                                + "] or gene 2: " + site2HugoSymbol + " ["
                                + site2EntrezGeneId + "]. Ignoring it "
                                + "and all SV data associated with it!");
                    // If at least one gene is recognized, continue
                    } else {
                        // Save the Entrez Gene Id if it was not saved before
                        if (site1EntrezGeneId == TabDelimitedFileUtil.NA_LONG || site1EntrezGeneId != site1CanonicalGene.getEntrezGeneId()) {
                            if (site1CanonicalGene != null) {
                                structuralVariant.setSite1EntrezGeneId(site1CanonicalGene.getEntrezGeneId());
                            } else {
                                structuralVariant.setSite1EntrezGeneId(null); // we want this to be null in the database, not NA_LONG
                            }
                        }
                        if (site2EntrezGeneId == TabDelimitedFileUtil.NA_LONG || site2EntrezGeneId != site2CanonicalGene.getEntrezGeneId()) {
                            if (site2CanonicalGene != null) {
                                structuralVariant.setSite2EntrezGeneId(site2CanonicalGene.getEntrezGeneId());
                            } else {
                                structuralVariant.setSite2EntrezGeneId(null); // we want this to be null in the database, not NA_LONG
                            }
                        }

                        // check this is unique within the file
                        String key = getSVKey(structuralVariant);
                        if (!uniqueSVs.add(key)) { // we have already seen this SV in this file
                            ProgressMonitor.logWarning("Structural variant with sample id: " + structuralVariant.getSampleId() + ", site 1 Entrez gene id: "
                                + structuralVariant.getSite1EntrezGeneId() + ", site 1 chromosome: " + structuralVariant.getSite1Chromosome()
                                + ", site 1 position: " + structuralVariant.getSite1Position() + ", site 1 region number: "
                                + structuralVariant.getSite1RegionNumber() + ", site 1 Ensembl transcript id: " + structuralVariant.getSite1EnsemblTranscriptId()
                                + ", site 2 Entrez gene id: "
                                + structuralVariant.getSite2EntrezGeneId() + ", site 2 chromosome: " + structuralVariant.getSite2Chromosome()
                                + ", site 2 position: " + structuralVariant.getSite2Position() + ", site 2 region number: "
                                + structuralVariant.getSite2RegionNumber() + ", site 2 Ensembl transcript id: " + structuralVariant.getSite2EnsemblTranscriptId());
                            continue;
                        }

                        // Add structural variant
                        DaoStructuralVariant.addStructuralVariantToBulkLoader(structuralVariant);

                        sampleIds.add(sample.getInternalId());
                    }
                }
            }
        }

        if (isIncrementalUpdateMode) {
            DaoStructuralVariant.deleteStructuralVariants(geneticProfileId, sampleIds);
        }
        DaoSampleProfile.upsertSampleToProfileMapping(sampleIds, geneticProfileId, genePanelId);

        buf.close();
        ClickHouseBulkLoader.flushAll();
    }

    private CanonicalGene setCanonicalGene(long siteEntrezGeneId, String siteHugoSymbol, DaoGeneOptimized daoGene) {
        CanonicalGene siteCanonicalGene = null;

        // If the Entrez Gene Id is not "NA" set the canonical gene.
        if (siteEntrezGeneId != TabDelimitedFileUtil.NA_LONG) {
            siteCanonicalGene = daoGene.getGene(siteEntrezGeneId);
        }

        // If no gene can be found based on Entrez Gene ID, try Symbol.
        if (siteCanonicalGene == null && !TabDelimitedFileUtil.NA_STRING.equals(siteHugoSymbol)) {
            siteCanonicalGene = daoGene.getNonAmbiguousGene(siteHugoSymbol, true);
        }

        return siteCanonicalGene;
    }

    private String getSVKey(StructuralVariant sv) {
        StringBuffer sb = new StringBuffer(sv.getSampleId());
        sb.append(sv.getSite1EntrezGeneId());
        sb.append(sv.getSite1Chromosome());
        sb.append(sv.getSite1Position());
        sb.append(sv.getSite1RegionNumber());
        sb.append(sv.getSite1EnsemblTranscriptId());
        sb.append(sv.getSite2EntrezGeneId());
        sb.append(sv.getSite2Chromosome());
        sb.append(sv.getSite2Position());
        sb.append(sv.getSite2RegionNumber());
        sb.append(sv.getSite2EnsemblTranscriptId());
        sb.append(sv.getEventInfo());
        return sb.toString();
    }
}
