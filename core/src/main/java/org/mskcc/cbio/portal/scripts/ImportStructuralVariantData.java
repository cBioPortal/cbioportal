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

import java.io.*;
import java.util.*;
import org.mskcc.cbio.maf.*;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.model.ExtendedMutation.MutationEvent;
import org.mskcc.cbio.portal.util.*;

/**
 * Imports a structural variant file.
 * @author Sander Tan
 */

public class ImportStructuralVariantData {
    // Column names structural variant file
    public static final String SAMPLE_ID = "Sample_ID";
    public static final String SITE1_ENTREZ_GENE_ID = "Site1_Entrez_Gene_Id";
    public static final String SITE1_HUGO_SYMBOL = "Site1_Hugo_Symbol";
    public static final String SITE1_ENSEMBL_TRANSCRIPT_ID = "Site1_Ensembl_Transcript_Id";
    public static final String SITE1_EXON = "Site1_Exon";
    public static final String SITE1_CHROMOSOME = "Site1_Chromosome";
    public static final String SITE1_POSITION = "Site1_Position";
    public static final String SITE1_DESCRIPTION = "Site1_Description";
    public static final String SITE2_ENTREZ_GENE_ID = "Site2_Entrez_Gene_Id";
    public static final String SITE2_HUGO_SYMBOL = "Site2_Hugo_Symbol";
    public static final String SITE2_ENSEMBL_TRANSCRIPT_ID = "Site2_Ensembl_Transcript_Id";
    public static final String SITE2_EXON = "Site2_Exon";
    public static final String SITE2_CHROMOSOME = "Site2_Chromosome";
    public static final String SITE2_POSITION = "Site2_Position";
    public static final String SITE2_DESCRIPTION = "Site2_Description";
    public static final String SITE2_EFFECT_ON_FRAME = "Site2_Effect_On_Frame";
    public static final String NCBI_BUILD = "NCBI_Build";
    public static final String DNA_SUPPORT = "DNA_Support";
    public static final String RNA_SUPPORT = "RNA_Support";
    public static final String NORMAL_READ_COUNT = "Normal_Read_Count";
    public static final String TUMOR_READ_COUNT = "Tumor_Read_Count";
    public static final String NORMAL_VARIANT_COUNT = "Normal_Variant_Count";
    public static final String TUMOR_VARIANT_COUNT = "Tumor_Variant_Count";
    public static final String NORMAL_PAIRED_END_READ_COUNT = "Normal_Paired_End_Read_Count";
    public static final String TUMOR_PAIRED_END_READ_COUNT = "Tumor_Paired_End_Read_Count";
    public static final String NORMAL_SPLIT_READ_COUNT = "Normal_Split_Read_Count";
    public static final String TUMOR_SPLIT_READ_COUNT = "Tumor_Split_Read_Count";
    public static final String ANNOTATION = "Annotation";
    public static final String BREAKPOINT_TYPE = "Breakpoint_Type";
    public static final String CENTER = "Center";
    public static final String CONNECTION_TYPE = "Connection_Type";
    public static final String EVENT_INFO = "Event_Info";
    public static final String VARIANT_CLASS = "Class";
    public static final String LENGTH = "Length";
    public static final String COMMENTS = "Comments";
    public static final String EXTERNAL_ANNOTATION = "External_Annotation";
    public static final String DRIVER_FILTER = "cbp_driver";
    public static final String DRIVER_FILTER_ANNOTATION = "cbp_driver_annotation";
    public static final String DRIVER_TIERS_FILTER = "cbp_driver_tiers";
    public static final String DRIVER_TIERS_FILTER_ANNOTATION = "cbp_driver_tiers_annotation";

    // Other constants
    public static final String PROTEIN_CHANGE_FUSION = "FUSION";

    // Initialize variables
    private File structuralVariantFile;
    private int geneticProfileId;
    private int mutationGeneticProfileId;
    private String genePanel;
    private Set<String> sampleSet  = new HashSet<>();
    private HashMap<String, Integer> columnIndexMap;


    public ImportStructuralVariantData(File structuralVariantFile, int geneticProfileId, String genePanel) throws DaoException {
        this.structuralVariantFile = structuralVariantFile;
        this.geneticProfileId = geneticProfileId;
        this.genePanel = genePanel;
        this.columnIndexMap = new HashMap<String, Integer>();
    }

    public void importData() throws IOException, DaoException {

        FileReader reader = new FileReader(this.structuralVariantFile);
        BufferedReader buf = new BufferedReader(reader);
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

        // Read header of file
        String headerParts[] = buf.readLine().trim().split("\t");

        // Find header indices
        for (int i=0; i<headerParts.length; i++) {
            // Put the index in the map
            this.columnIndexMap.put(headerParts[i].toLowerCase(), i);
        }

        // Genetic profile is read in first
        GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileById(geneticProfileId);
        String line = new String();
        ArrayList <Integer> orderedSampleList = new ArrayList<Integer>();
        while ((line = buf.readLine()) != null) {
            ProgressMonitor.incrementCurValue();
            ConsoleUtil.showProgress();
            if( !line.startsWith("#") && line.trim().length() > 0) {
                String parts[] = line.split("\t", -1);
                StructuralVariant structuralVariant = new StructuralVariant();

                structuralVariant.setGeneticProfileId(geneticProfileId);
                structuralVariant.setSampleId(TabDelimitedFileUtil.getPartString(getColumnIndex(SAMPLE_ID), parts));
                structuralVariant.setSite1EntrezGeneId(TabDelimitedFileUtil.getPartLong(getColumnIndex(SITE1_ENTREZ_GENE_ID), parts));
                structuralVariant.setSite1HugoSymbol(TabDelimitedFileUtil.getPartString(getColumnIndex(SITE1_HUGO_SYMBOL), parts));
                structuralVariant.setSite1EnsemblTranscriptId(TabDelimitedFileUtil.getPartString(getColumnIndex(SITE1_ENSEMBL_TRANSCRIPT_ID), parts));
                structuralVariant.setSite1Exon(TabDelimitedFileUtil.getPartInt(getColumnIndex(SITE1_EXON), parts));
                structuralVariant.setSite1Chromosome(TabDelimitedFileUtil.getPartString(getColumnIndex(SITE1_CHROMOSOME), parts));
                structuralVariant.setSite1Position(TabDelimitedFileUtil.getPartInt(getColumnIndex(SITE1_POSITION), parts));
                structuralVariant.setSite1Description(TabDelimitedFileUtil.getPartString(getColumnIndex(SITE1_DESCRIPTION), parts));
                structuralVariant.setSite2EntrezGeneId(TabDelimitedFileUtil.getPartLong(getColumnIndex(SITE2_ENTREZ_GENE_ID), parts));
                structuralVariant.setSite2HugoSymbol(TabDelimitedFileUtil.getPartString(getColumnIndex(SITE2_HUGO_SYMBOL), parts));
                structuralVariant.setSite2EnsemblTranscriptId(TabDelimitedFileUtil.getPartString(getColumnIndex(SITE2_ENSEMBL_TRANSCRIPT_ID), parts));
                structuralVariant.setSite2Exon(TabDelimitedFileUtil.getPartInt(getColumnIndex(SITE2_EXON), parts));
                structuralVariant.setSite2Chromosome(TabDelimitedFileUtil.getPartString(getColumnIndex(SITE2_CHROMOSOME), parts));
                structuralVariant.setSite2Position(TabDelimitedFileUtil.getPartInt(getColumnIndex(SITE2_POSITION), parts));
                structuralVariant.setSite2Description(TabDelimitedFileUtil.getPartString(getColumnIndex(SITE2_DESCRIPTION), parts));
                structuralVariant.setSite2EffectOnFrame(TabDelimitedFileUtil.getPartString(getColumnIndex(SITE2_EFFECT_ON_FRAME), parts));
                structuralVariant.setNcbiBuild(TabDelimitedFileUtil.getPartString(getColumnIndex(NCBI_BUILD), parts));
                structuralVariant.setDnaSupport(TabDelimitedFileUtil.getPartString(getColumnIndex(DNA_SUPPORT), parts));
                structuralVariant.setRnaSupport(TabDelimitedFileUtil.getPartString(getColumnIndex(RNA_SUPPORT), parts));
                structuralVariant.setNormalReadCount(TabDelimitedFileUtil.getPartInt(getColumnIndex(NORMAL_READ_COUNT), parts));
                structuralVariant.setTumorReadCount(TabDelimitedFileUtil.getPartInt(getColumnIndex(TUMOR_READ_COUNT), parts));
                structuralVariant.setNormalVariantCount(TabDelimitedFileUtil.getPartInt(getColumnIndex(NORMAL_VARIANT_COUNT), parts));
                structuralVariant.setTumorVariantCount(TabDelimitedFileUtil.getPartInt(getColumnIndex(TUMOR_VARIANT_COUNT), parts));
                structuralVariant.setNormalPairedEndReadCount(TabDelimitedFileUtil.getPartInt(getColumnIndex(NORMAL_PAIRED_END_READ_COUNT), parts));
                structuralVariant.setTumorPairedEndReadCount(TabDelimitedFileUtil.getPartInt(getColumnIndex(TUMOR_PAIRED_END_READ_COUNT), parts));
                structuralVariant.setNormalSplitReadCount(TabDelimitedFileUtil.getPartInt(getColumnIndex(NORMAL_SPLIT_READ_COUNT), parts));
                structuralVariant.setTumorSplitReadCount(TabDelimitedFileUtil.getPartInt(getColumnIndex(TUMOR_SPLIT_READ_COUNT), parts));
                structuralVariant.setAnnotation(TabDelimitedFileUtil.getPartString(getColumnIndex(ANNOTATION), parts));
                structuralVariant.setBreakpointType(TabDelimitedFileUtil.getPartString(getColumnIndex(BREAKPOINT_TYPE), parts));
                structuralVariant.setCenter(TabDelimitedFileUtil.getPartString(getColumnIndex(CENTER), parts));
                structuralVariant.setConnectionType(TabDelimitedFileUtil.getPartString(getColumnIndex(CONNECTION_TYPE), parts));
                structuralVariant.setEventInfo(TabDelimitedFileUtil.getPartString(getColumnIndex(EVENT_INFO), parts));
                structuralVariant.setVariantClass(TabDelimitedFileUtil.getPartString(getColumnIndex(VARIANT_CLASS), parts));
                structuralVariant.setLength(TabDelimitedFileUtil.getPartInt(getColumnIndex(LENGTH), parts));
                structuralVariant.setComments(TabDelimitedFileUtil.getPartString(getColumnIndex(COMMENTS), parts));
                structuralVariant.setExternalAnnotation(TabDelimitedFileUtil.getPartString(getColumnIndex(EXTERNAL_ANNOTATION), parts));
                structuralVariant.setDriverFilter(TabDelimitedFileUtil.getPartString(getColumnIndex(DRIVER_FILTER), parts));
                structuralVariant.setDriverFilterAnn(TabDelimitedFileUtil.getPartString(getColumnIndex(DRIVER_FILTER_ANNOTATION), parts));
                structuralVariant.setDriverTiersFilter(TabDelimitedFileUtil.getPartString(getColumnIndex(DRIVER_TIERS_FILTER), parts));
                structuralVariant.setDriverTiersFilterAnn(TabDelimitedFileUtil.getPartString(getColumnIndex(DRIVER_TIERS_FILTER_ANNOTATION), parts));

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

    private int getColumnIndex(String colName) {
        Integer index = this.columnIndexMap.get(colName.toLowerCase());
        if (index == null) {
            index = -1;
        }
        return index;
    }
}
