package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.AlleleSpecificCopyNumber;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneFilterQuery;
import org.cbioportal.model.GenomicDataCountItem;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByPosition;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.model.util.Select;
import org.cbioportal.persistence.mybatis.config.TestConfig;
import org.cbioportal.persistence.mybatis.util.MolecularProfileCaseIdentifierUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {MutationMyBatisRepository.class, MolecularProfileCaseIdentifierUtil.class, TestConfig.class})
public class MutationMyBatisRepositoryTest {

    //    mutation, cna and struct var events in testSql.sql
    //        SAMPLE_ID, ENTREZ_GENE_ID, HUGO_GENE_SYMBOL, GENETIC_PROFILE_ID, TYPE, MUTATION_TYPE, DRIVER_FILTER, DRIVER_TIERS_FILTER, PATIENT_ID, MUTATION_TYPE
    //        1	    207	AKT1	2	CNA         -2	                Putative_Driver	    Tier 1  TCGA-A1-A0SB    germline
    //        2	    207	AKT1	2	CNA         2	                Putative_Passenger	Tier 2  TCGA-A1-A0SD    germline
    //        1	    207	AKT1	6	MUTATION    Nonsense_Mutation	Putative_Driver	    Tier 1  TCGA-A1-A0SB    germline
    //        2	    207	AKT1	6	MUTATION    Missense_Mutation	Putative_Passenger	Tier 2  TCGA-A1-A0SD    germline
    //        1	    208	AKT2	2	CNA         2		            <null>              <null>  TCGA-A1-A0SB    germline
    //        3	    208	AKT2	6	MUTATION    Splice_Site	        Putative_Passenger	Tier 1  TCGA-A1-A0SE    germline
    //        6	    672	BRCA1	6	MUTATION    Missense_Mutation	Putative_Passenger	Tier 2  TCGA-A1-A0SH    germline
    //        6	    672	BRCA1	6	MUTATION    Nonsense_Mutation	Putative_Driver	    Tier 1  TCGA-A1-A0SH    NA
    //        7	    672	BRCA1	6	MUTATION    Nonsense_Mutation	Putative_Driver	    Tier 2  TCGA-A1-A0SI    germline
    //        12	672	BRCA1	6	MUTATION    Splice_Site	        Putative_Passenger	Tier 1  TCGA-A1-A0SO    germline
    //        13	672	BRCA1	6	MUTATION    Splice_Site	        Putative_Driver	    Tier 1  TCGA-A1-A0SP    germline
    //        1     27436-238   EML4-ALK    7   SV          Fusion              <noi>               <noi>   TCGA-A1-A0SB    germline
    //        2     27436-238   EML4-ALK    7   SV          Fusion              <noi>               <noi>   TCGA-A1-A0SD    somatic
    //        1     57670-673   KIAA..-BRAF 7   SV          Fusion              <noi>               <noi>   TCGA-A1-A0SB    somatic
    //        2     57670-673   KIAA..-BRAF 7   SV          Fusion              <noi>               <noi>   TCGA-A1-A0SD    germline
    //        2     57670-673   KIAA..-BRAF 7   SV          Fusion              <noi>               <noi>   TCGA-A1-A0SD    somatic
    //       15     57670-673   KIAA..-BRAF 13  SV          Fusion              <noi>               <noi>   TCGA-A1-A0SD    somatic
    //        1     8031-5979   NCOA4-RET   7   SV          Fusion              <noi>               <noi>   TCGA-A1-A0SB    somatic
    //       15     8031-5979   NCOA4-RET   13  SV          Fusion              <noi>               <noi>   TCGA-A1-A0SB    somatic
    //        1     7113-2078   TMPRSS2-ERG 7   SV          Fusion              <noi>               <noi>   TCGA-A1-A0SB    somatic
    //       15     8031-NULL   NCOA4-      13  SV          Fusion              <noi>               <noi>   TCGA-A1-A0SB    somatic


    @Autowired
    private MutationMyBatisRepository mutationMyBatisRepository;

    @Before
    public void init() {
        molecularProfileIds = new ArrayList<>();
        molecularProfileIds.add("study_tcga_pub_mutations");
        molecularProfileIds.add("study_tcga_pub_mutations");
        molecularProfileIds.add("study_tcga_pub_mutations");
        molecularProfileIds.add("study_tcga_pub_mutations");
        molecularProfileIds.add("study_tcga_pub_mutations");
        molecularProfileIds.add("study_tcga_pub_mutations");
        molecularProfileIds.add("study_tcga_pub_mutations");
        molecularProfileIds.add("study_tcga_pub_mutations");
        sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-B0SO-01");
        sampleIds.add("TCGA-A1-A0SB-01");
        sampleIds.add("TCGA-A1-A0SD-01");
        sampleIds.add("TCGA-A1-A0SE-01");
        sampleIds.add("TCGA-A1-A0SH-01");
        sampleIds.add("TCGA-A1-A0SI-01");
        sampleIds.add("TCGA-A1-A0SO-01");
        sampleIds.add("TCGA-A1-A0SP-01");
        tiers = Select.all();
        includeUnknownTier = true;
        includeDriver = true;
        includeVUS = true;
        includeUnknownOncogenicity = true;
        includeGermline = true;
        includeSomatic = true;
        includeUnknownStatus = true;
    }

    List<String> molecularProfileIds = new ArrayList<>();
    List<String> sampleIds = new ArrayList<>();
    Select<String> tiers;
    boolean includeUnknownTier;
    boolean includeDriver;
    boolean includeVUS;
    boolean includeUnknownOncogenicity;
    boolean includeGermline;
    boolean includeSomatic;
    boolean includeUnknownStatus;

    @Test
    public void getMutationsInMolecularProfileBySampleListIdIdProjection() throws Exception {

        List<Mutation> result =  mutationMyBatisRepository.getMutationsInMolecularProfileBySampleListId(
            "study_tcga_pub_mutations", "study_tcga_pub_all", null, false, "ID", null, null, null, null);

        Assert.assertEquals(8, result.size());
        Mutation mutation = result.get(0);
        Assert.assertEquals("study_tcga_pub_mutations", mutation.getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-A0SB-01", mutation.getSampleId());
        Assert.assertEquals((Integer) 207, mutation.getEntrezGeneId());
    }

    @Test
    public void getMutationsInMolecularProfileBySampleListIdSummaryProjection() throws Exception {

        List<Mutation> result =  mutationMyBatisRepository.getMutationsInMolecularProfileBySampleListId(
            "study_tcga_pub_mutations", "study_tcga_pub_all", null, false, "SUMMARY", null, null, null, null);

        Assert.assertEquals(8, result.size());

        Optional<Mutation> mutationOptional =
            result.stream().filter(r -> r.getSampleId().equals("TCGA-A1-A0SB-01")).findAny();
        Assert.assertTrue(mutationOptional.isPresent());
        Mutation mutation = mutationOptional.get();

        Assert.assertEquals("study_tcga_pub_mutations", mutation.getMolecularProfileId());
        Assert.assertEquals((Integer) 207, mutation.getEntrezGeneId());
        Assert.assertEquals("cyclases/Protein", mutation.getAminoAcidChange());
        Assert.assertEquals("genome.wustl.edu", mutation.getCenter());
        Assert.assertEquals((Long) 41244748L, mutation.getEndPosition());
        Assert.assertEquals("BRCA1 truncating", mutation.getKeyword());
        Assert.assertEquals("Germline", mutation.getMutationStatus());
        Assert.assertEquals("Nonsense_Mutation", mutation.getMutationType());
        Assert.assertEquals("37", mutation.getNcbiBuild());
        Assert.assertEquals((Integer) (-1), mutation.getNormalAltCount());
        Assert.assertEquals((Integer) (-1), mutation.getNormalRefCount());
        Assert.assertEquals((Integer) 934, mutation.getProteinPosEnd());
        Assert.assertEquals((Integer) 934, mutation.getProteinPosStart());
        Assert.assertEquals("NM_007294", mutation.getRefseqMrnaId());
        Assert.assertEquals("Q934*", mutation.getProteinChange());
        Assert.assertEquals("G", mutation.getReferenceAllele());
        Assert.assertEquals((Long) 41244748L, mutation.getStartPosition());
        Assert.assertEquals((Integer) (-1), mutation.getTumorAltCount());
        Assert.assertEquals((Integer) (-1), mutation.getTumorRefCount());
        Assert.assertEquals("A", mutation.getTumorSeqAllele());
        Assert.assertEquals("Unknown", mutation.getValidationStatus());
        Assert.assertEquals("SNP", mutation.getVariantType());
        Assert.assertEquals("Putative_Driver", mutation.getDriverFilter());
        Assert.assertEquals("Pathogenic", mutation.getDriverFilterAnnotation());
        Assert.assertEquals("Tier 1", mutation.getDriverTiersFilter());
        Assert.assertEquals("Highly Actionable", mutation.getDriverTiersFilterAnnotation());
    }

    @Test
    public void getMutationsInMolecularProfileBySampleListIdAndEntrezGeneIdsSummaryProjection() throws Exception {

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(207);
        entrezGeneIds.add(208);

        List<Mutation> result =  mutationMyBatisRepository.getMutationsInMolecularProfileBySampleListId(
            "study_tcga_pub_mutations", "study_tcga_pub_all", entrezGeneIds, false, "SUMMARY", null, null, null, null);

        Assert.assertEquals(3, result.size());
        Optional<Mutation> mutationOptional =
            result.stream().filter(r -> r.getSampleId().equals("TCGA-A1-A0SB-01")).findAny();
        Assert.assertTrue(mutationOptional.isPresent());
        Mutation mutation = mutationOptional.get();

        Assert.assertEquals("study_tcga_pub_mutations", mutation.getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-A0SB-01", mutation.getSampleId());
        Assert.assertEquals((Integer) 207, mutation.getEntrezGeneId());
        Assert.assertEquals("cyclases/Protein", mutation.getAminoAcidChange());
        Assert.assertEquals("genome.wustl.edu", mutation.getCenter());
        Assert.assertEquals((Long) 41244748L, mutation.getEndPosition());
        Assert.assertEquals("BRCA1 truncating", mutation.getKeyword());
        Assert.assertEquals("Germline", mutation.getMutationStatus());
        Assert.assertEquals("Nonsense_Mutation", mutation.getMutationType());
        Assert.assertEquals("37", mutation.getNcbiBuild());
        Assert.assertEquals((Integer) (-1), mutation.getNormalAltCount());
        Assert.assertEquals((Integer) (-1), mutation.getNormalRefCount());
        Assert.assertEquals((Integer) 934, mutation.getProteinPosEnd());
        Assert.assertEquals((Integer) 934, mutation.getProteinPosStart());
        Assert.assertEquals("NM_007294", mutation.getRefseqMrnaId());
        Assert.assertEquals("Q934*", mutation.getProteinChange());
        Assert.assertEquals("G", mutation.getReferenceAllele());
        Assert.assertEquals((Long) 41244748L, mutation.getStartPosition());
        Assert.assertEquals((Integer) (-1), mutation.getTumorAltCount());
        Assert.assertEquals((Integer) (-1), mutation.getTumorRefCount());
        Assert.assertEquals("A", mutation.getTumorSeqAllele());
        Assert.assertEquals("Unknown", mutation.getValidationStatus());
        Assert.assertEquals("SNP", mutation.getVariantType());
        Assert.assertEquals("Putative_Driver", mutation.getDriverFilter());
        Assert.assertEquals("Pathogenic", mutation.getDriverFilterAnnotation());
        Assert.assertEquals("Tier 1", mutation.getDriverTiersFilter());
        Assert.assertEquals("Highly Actionable", mutation.getDriverTiersFilterAnnotation());
    }

    @Test
    public void getMutationsInMolecularProfileBySampleListIdDetailedProjection() throws Exception {

        List<Mutation> result =  mutationMyBatisRepository.getMutationsInMolecularProfileBySampleListId(
            "study_tcga_pub_mutations", "study_tcga_pub_all", null, false, "DETAILED", null, null, null, null);

        Assert.assertEquals(8, result.size());

        Optional<Mutation> mutationOptional =
            result.stream().filter(r -> r.getSampleId().equals("TCGA-A1-A0SB-01")).findAny();
        Assert.assertTrue(mutationOptional.isPresent());
        Mutation mutation = mutationOptional.get();

        Assert.assertEquals("study_tcga_pub_mutations", mutation.getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-A0SB-01", mutation.getSampleId());
        Assert.assertEquals((Integer) 207, mutation.getEntrezGeneId());
        Assert.assertEquals("cyclases/Protein", mutation.getAminoAcidChange());
        Assert.assertEquals("genome.wustl.edu", mutation.getCenter());
        Assert.assertEquals((Long) 41244748L, mutation.getEndPosition());
        Assert.assertEquals("BRCA1 truncating", mutation.getKeyword());
        Assert.assertEquals("Germline", mutation.getMutationStatus());
        Assert.assertEquals("Nonsense_Mutation", mutation.getMutationType());
        Assert.assertEquals("37", mutation.getNcbiBuild());
        Assert.assertEquals((Integer) (-1), mutation.getNormalAltCount());
        Assert.assertEquals((Integer) (-1), mutation.getNormalRefCount());
        Assert.assertEquals((Integer) 934, mutation.getProteinPosEnd());
        Assert.assertEquals((Integer) 934, mutation.getProteinPosStart());
        Assert.assertEquals("NM_007294", mutation.getRefseqMrnaId());
        Assert.assertEquals("Q934*", mutation.getProteinChange());
        Assert.assertEquals("G", mutation.getReferenceAllele());
        Assert.assertEquals((Long) 41244748L, mutation.getStartPosition());
        Assert.assertEquals((Integer) (-1), mutation.getTumorAltCount());
        Assert.assertEquals((Integer) (-1), mutation.getTumorRefCount());
        Assert.assertEquals("A", mutation.getTumorSeqAllele());
        Assert.assertEquals("Unknown", mutation.getValidationStatus());
        Assert.assertEquals("SNP", mutation.getVariantType());
        Assert.assertEquals("Putative_Driver", mutation.getDriverFilter());
        Assert.assertEquals("Pathogenic", mutation.getDriverFilterAnnotation());
        Assert.assertEquals("Tier 1", mutation.getDriverTiersFilter());
        Assert.assertEquals("Highly Actionable", mutation.getDriverTiersFilterAnnotation());
        Gene gene = mutation.getGene();
        Assert.assertEquals((Integer) 207, gene.getEntrezGeneId());
        Assert.assertEquals("AKT1", gene.getHugoGeneSymbol());
        Assert.assertEquals("protein-coding", gene.getType());
        AlleleSpecificCopyNumber alleleSpecificCopyNumber = mutation.getAlleleSpecificCopyNumber();
        Assert.assertEquals((Integer) (3), alleleSpecificCopyNumber.getAscnIntegerCopyNumber());
        Assert.assertEquals("FACETS", alleleSpecificCopyNumber.getAscnMethod());
        Assert.assertEquals((Float) (1.25f), alleleSpecificCopyNumber.getCcfExpectedCopiesUpper());
        Assert.assertEquals((Float) (1.75f), alleleSpecificCopyNumber.getCcfExpectedCopies());
        Assert.assertEquals("CLONAL", alleleSpecificCopyNumber.getClonal());
        Assert.assertEquals((Integer) (2), alleleSpecificCopyNumber.getMinorCopyNumber());
        Assert.assertEquals((Integer) (1), alleleSpecificCopyNumber.getExpectedAltCopies());
        Assert.assertEquals((Integer) (4), alleleSpecificCopyNumber.getTotalCopyNumber());
    }

    @Test
    public void getMutationsInMolecularProfileBySampleListIdSummaryProjection1PageSize() throws Exception {

        List<Mutation> result =  mutationMyBatisRepository.getMutationsInMolecularProfileBySampleListId(
            "study_tcga_pub_mutations", "study_tcga_pub_all", null, false, "SUMMARY", 1, 0, null, null);

        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getMutationsInMolecularProfileBySampleListIdSummaryProjectionProteinChangeSort() throws Exception {

        List<Mutation> result =  mutationMyBatisRepository.getMutationsInMolecularProfileBySampleListId(
            "study_tcga_pub_mutations", "study_tcga_pub_all", null, false, "SUMMARY", null, null, "proteinChange", "ASC");

        Assert.assertEquals(8, result.size());
        Assert.assertEquals("C27_splice", result.get(0).getProteinChange());
        Assert.assertEquals("C27_splice", result.get(1).getProteinChange());
        Assert.assertEquals("C27_splice", result.get(2).getProteinChange());
        Assert.assertEquals("C61G", result.get(3).getProteinChange());
    }

    @Test
    public void getMetaMutationsInMolecularProfileBySampleListId() throws Exception {

        MutationMeta result = mutationMyBatisRepository.getMetaMutationsInMolecularProfileBySampleListId(
            "study_tcga_pub_mutations", "study_tcga_pub_all", null);

        Assert.assertEquals((Integer) 8, result.getTotalCount());
        Assert.assertEquals((Integer) 7, result.getSampleCount());
    }

    @Test
    public void getMetaMutationsInMolecularProfileBySampleListIdAndEntrezGeneIds() throws Exception {

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(207);
        entrezGeneIds.add(208);

        MutationMeta result = mutationMyBatisRepository.getMetaMutationsInMolecularProfileBySampleListId(
            "study_tcga_pub_mutations", "study_tcga_pub_all", entrezGeneIds);

        Assert.assertEquals((Integer) 3, result.getTotalCount());
        Assert.assertEquals((Integer) 3, result.getSampleCount());
    }

    @Test
    public void getMutationsInMultipleMolecularProfiles() throws Exception {

        List<String> molecularProfileIds = new ArrayList<>();
        molecularProfileIds.add("acc_tcga_mutations");
        molecularProfileIds.add("study_tcga_pub_mutations");

        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-B0SO-01");
        sampleIds.add("TCGA-A1-A0SH-01");

        List<Mutation> result = mutationMyBatisRepository.getMutationsInMultipleMolecularProfiles(molecularProfileIds,
            sampleIds, null, "SUMMARY", null, null, null, null);

        Assert.assertEquals(3, result.size());

        Optional<Mutation> sampleOmutationOptional =
            result.stream().filter(r -> r.getSampleId().equals("TCGA-A1-B0SO-01") && r.getMolecularProfileId().equals("acc_tcga_mutations")).findAny();

        List<Mutation> sampleHmutations = result.stream().filter(
            r -> r.getSampleId().equals("TCGA-A1-A0SH-01") &&
                r.getMolecularProfileId().equals("study_tcga_pub_mutations")).collect(
            Collectors.toList());

        Assert.assertEquals(2, sampleHmutations.size());
        Assert.assertTrue(sampleOmutationOptional.isPresent());
    }
    
    @Test
    public void getMutationsInMultipleMolecularProfilesByGeneQueries() throws Exception {

        GeneFilterQuery geneFilterQuery1 = new GeneFilterQuery("BRCA1", 672, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        GeneFilterQuery geneFilterQuery2 = new GeneFilterQuery("AKT1", 207, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        GeneFilterQuery geneFilterQuery3 = new GeneFilterQuery("AKT2", 208, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        List<GeneFilterQuery> geneQueries =  Arrays.asList(geneFilterQuery1, geneFilterQuery2, geneFilterQuery3);

        List<Mutation> result = mutationMyBatisRepository.getMutationsInMultipleMolecularProfilesByGeneQueries(molecularProfileIds,
            sampleIds, geneQueries, "SUMMARY", null, null, null, null);

        Assert.assertEquals(8, result.size());
    }

    @Test
    public void getMutationsInMultipleMolecularProfilesByGeneQueriesIncludeOnlyGermline() throws Exception {

        includeSomatic = false;
        includeUnknownStatus = false;

        GeneFilterQuery geneFilterQuery1 = new GeneFilterQuery("BRCA1", 672, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        GeneFilterQuery geneFilterQuery2 = new GeneFilterQuery("AKT1", 207, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        GeneFilterQuery geneFilterQuery3 = new GeneFilterQuery("AKT2", 208, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        List<GeneFilterQuery> geneQueries =  Arrays.asList(geneFilterQuery1, geneFilterQuery2, geneFilterQuery3);

        List<Mutation> result = mutationMyBatisRepository.getMutationsInMultipleMolecularProfilesByGeneQueries(molecularProfileIds,
            sampleIds, geneQueries, "SUMMARY", null, null, null, null);
        
        Assert.assertEquals(7, result.size());
    }
    
    @Test
    public void getMutationsInMultipleMolecularProfilesByGeneQueriesIncludeOnlySomatic() throws Exception {

        includeGermline = false;
        includeUnknownStatus = false;

        GeneFilterQuery geneFilterQuery1 = new GeneFilterQuery("BRCA1", 672, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        GeneFilterQuery geneFilterQuery2 = new GeneFilterQuery("AKT1", 207, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        GeneFilterQuery geneFilterQuery3 = new GeneFilterQuery("AKT2", 208, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        List<GeneFilterQuery> geneQueries =  Arrays.asList(geneFilterQuery1, geneFilterQuery2, geneFilterQuery3);

        List<Mutation> result = mutationMyBatisRepository.getMutationsInMultipleMolecularProfilesByGeneQueries(molecularProfileIds,
            sampleIds, geneQueries, "SUMMARY", null, null, null, null);
        
        Assert.assertEquals(0, result.size());
    }
    
    @Test
    public void getMutationsInMultipleMolecularProfilesByGeneQueriesIncludeOnlyUnknownStatus() throws Exception {

        includeSomatic = false;
        includeGermline = false;

        GeneFilterQuery geneFilterQuery1 = new GeneFilterQuery("BRCA1", 672, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        GeneFilterQuery geneFilterQuery2 = new GeneFilterQuery("AKT1", 207, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        GeneFilterQuery geneFilterQuery3 = new GeneFilterQuery("AKT2", 208, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        List<GeneFilterQuery> geneQueries =  Arrays.asList(geneFilterQuery1, geneFilterQuery2, geneFilterQuery3);

        List<Mutation> result = mutationMyBatisRepository.getMutationsInMultipleMolecularProfilesByGeneQueries(molecularProfileIds,
            sampleIds, geneQueries, "SUMMARY", null, null, null, null);
        
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getMutationsInMultipleMolecularProfilesByGeneQueriesIncludeOnlyDriver() throws Exception {

        includeVUS = false;
        includeUnknownOncogenicity = false;

        GeneFilterQuery geneFilterQuery1 = new GeneFilterQuery("BRCA1", 672, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        GeneFilterQuery geneFilterQuery2 = new GeneFilterQuery("AKT1", 207, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        GeneFilterQuery geneFilterQuery3 = new GeneFilterQuery("AKT2", 208, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        List<GeneFilterQuery> geneQueries =  Arrays.asList(geneFilterQuery1, geneFilterQuery2, geneFilterQuery3);

        List<Mutation> result = mutationMyBatisRepository.getMutationsInMultipleMolecularProfilesByGeneQueries(molecularProfileIds,
            sampleIds, geneQueries, "SUMMARY", null, null, null, null);

        Assert.assertEquals(4, result.size());
        List<String> expectedSampleIds = Arrays.asList("TCGA-A1-A0SB-01", "TCGA-A1-A0SH-01", "TCGA-A1-A0SI-01", "TCGA-A1-A0SP-01");
        Assert.assertTrue(result.stream().allMatch(r -> expectedSampleIds.contains(r.getSampleId())));
    }

    @Test
    public void getMutationsInMultipleMolecularProfilesByGeneQueriesIncludeOnlyVUS() throws Exception {

        includeDriver = false;
        includeUnknownOncogenicity = false;

        GeneFilterQuery geneFilterQuery1 = new GeneFilterQuery("BRCA1", 672, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        GeneFilterQuery geneFilterQuery2 = new GeneFilterQuery("AKT1", 207, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        GeneFilterQuery geneFilterQuery3 = new GeneFilterQuery("AKT2", 208, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        List<GeneFilterQuery> geneQueries =  Arrays.asList(geneFilterQuery1, geneFilterQuery2, geneFilterQuery3);

        List<Mutation> result = mutationMyBatisRepository.getMutationsInMultipleMolecularProfilesByGeneQueries(molecularProfileIds,
            sampleIds, geneQueries, "SUMMARY", null, null, null, null);

        Assert.assertEquals(4, result.size());
        List<String> expectedSampleIds = Arrays.asList("TCGA-A1-A0SD-01", "TCGA-A1-A0SE-01", "TCGA-A1-A0SH-01", "TCGA-A1-A0SO-01");
        Assert.assertTrue(result.stream().allMatch(r -> expectedSampleIds.contains(r.getSampleId())));
    }
    
    @Test
    public void getMutationsInMultipleMolecularProfilesByGeneQueriesIncludeOnlyUnknownOncogenicity() throws Exception {

        includeDriver = false;
        includeVUS = false;

        GeneFilterQuery geneFilterQuery1 = new GeneFilterQuery("BRCA1", 672, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        GeneFilterQuery geneFilterQuery2 = new GeneFilterQuery("AKT1", 207, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        GeneFilterQuery geneFilterQuery3 = new GeneFilterQuery("AKT2", 208, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        List<GeneFilterQuery> geneQueries =  Arrays.asList(geneFilterQuery1, geneFilterQuery2, geneFilterQuery3);

        List<Mutation> result = mutationMyBatisRepository.getMutationsInMultipleMolecularProfilesByGeneQueries(molecularProfileIds,
            sampleIds, geneQueries, "SUMMARY", null, null, null, null);

        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getMutationsInMultipleMolecularProfilesByGeneQueriesFilterTiers() throws Exception {

        Select<String> tiers = Select.byValues(Arrays.asList("Tier 2"));
        includeUnknownTier = false;
        
        GeneFilterQuery geneFilterQuery1 = new GeneFilterQuery("BRCA1", 672, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        GeneFilterQuery geneFilterQuery2 = new GeneFilterQuery("AKT1", 207, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        GeneFilterQuery geneFilterQuery3 = new GeneFilterQuery("AKT2", 208, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier, includeGermline, includeSomatic, includeUnknownStatus);
        List<GeneFilterQuery> geneQueries =  Arrays.asList(geneFilterQuery1, geneFilterQuery2, geneFilterQuery3);

        List<Mutation> result = mutationMyBatisRepository.getMutationsInMultipleMolecularProfilesByGeneQueries(molecularProfileIds,
            sampleIds, geneQueries, "SUMMARY", null, null, null, null);

        Assert.assertEquals(3, result.size());
        List<String> expectedSampleIds = Arrays.asList("TCGA-A1-A0SD-01", "TCGA-A1-A0SH-01", "TCGA-A1-A0SI-01");
        Assert.assertTrue(result.stream().allMatch(r -> expectedSampleIds.contains(r.getSampleId())));
    }

    @Test
    public void getMutationsInMultipleMolecularProfilesByGeneQueriesMixed() throws Exception {

        // BRCA1 - VUS only
        // AKT1  - Tier1 only
        // AKT2  - driver only
        GeneFilterQuery geneFilterQuery1 = new GeneFilterQuery("BRCA1", 672, null,
            false, true, false, Select.all(), true, includeGermline, includeSomatic, includeUnknownStatus);
        GeneFilterQuery geneFilterQuery2 = new GeneFilterQuery("AKT1", 207, null,
            includeDriver, includeVUS, includeUnknownOncogenicity, Select.byValues(Arrays.asList("Tier 1")), false, includeGermline, includeSomatic, includeUnknownStatus);
        GeneFilterQuery geneFilterQuery3 = new GeneFilterQuery("AKT2", 208, null,
            true, false, false, Select.all(), true, includeGermline, includeSomatic, includeUnknownStatus);
        List<GeneFilterQuery> geneQueries =  Arrays.asList(geneFilterQuery1, geneFilterQuery2, geneFilterQuery3);

        List<Mutation> result = mutationMyBatisRepository.getMutationsInMultipleMolecularProfilesByGeneQueries(molecularProfileIds,
            sampleIds, geneQueries, "SUMMARY", null, null, null, null);

        Assert.assertEquals(3, result.size());
        List<String> expectedSampleIds = Arrays.asList("TCGA-A1-A0SB-01", "TCGA-A1-A0SH-01", "TCGA-A1-A0SO-01");
        Assert.assertTrue(result.stream().allMatch(r -> expectedSampleIds.contains(r.getSampleId())));
    }

    @Test
    public void getMetaMutationsInMultipleMolecularProfiles() throws Exception {

        List<String> molecularProfileIds = new ArrayList<>();
        molecularProfileIds.add("acc_tcga_mutations");
        molecularProfileIds.add("study_tcga_pub_mutations");

        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-B0SO-01");
        sampleIds.add("TCGA-A1-A0SH-01");

        MutationMeta result = mutationMyBatisRepository.getMetaMutationsInMultipleMolecularProfiles(molecularProfileIds,
            sampleIds, null);

        Assert.assertEquals((Integer) 3, result.getTotalCount());
        Assert.assertEquals((Integer) 2, result.getSampleCount());
    }

    @Test
    public void fetchMutationsInMolecularProfile() throws Exception {

        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SH-01");
        sampleIds.add("TCGA-A1-A0SO-01");

        List<Mutation> result = mutationMyBatisRepository.fetchMutationsInMolecularProfile("study_tcga_pub_mutations",
            sampleIds, null, false, "SUMMARY", null, null, null, null);

        Assert.assertEquals(3, result.size());
        Assert.assertEquals("study_tcga_pub_mutations", result.get(0).getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-A0SH-01", result.get(0).getSampleId());
        Assert.assertEquals("study_tcga_pub_mutations", result.get(1).getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-A0SH-01", result.get(1).getSampleId());
        Assert.assertEquals("study_tcga_pub_mutations", result.get(2).getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-A0SO-01", result.get(2).getSampleId());
    }

    @Test
    public void fetchMetaMutationsInMolecularProfile() throws Exception {

        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SH-01");
        sampleIds.add("TCGA-A1-A0SO-01");

        MutationMeta result = mutationMyBatisRepository.fetchMetaMutationsInMolecularProfile("study_tcga_pub_mutations",
            sampleIds, null);

        Assert.assertEquals((Integer) 3, result.getTotalCount());
        Assert.assertEquals((Integer) 2, result.getSampleCount());
    }

    @Test
    public void getMutationCountByPosition() throws Exception {

        MutationCountByPosition result = mutationMyBatisRepository.getMutationCountByPosition(672, 61, 936);

        Assert.assertEquals((Integer) 672, result.getEntrezGeneId());
        Assert.assertEquals((Integer) 61, result.getProteinPosStart());
        Assert.assertEquals((Integer) 936, result.getProteinPosEnd());
        Assert.assertEquals((Integer) 3, result.getCount());
    }
    
    @Test
    public void getMutationCountsByType() {
        GenomicDataCountItem result = mutationMyBatisRepository.getMutationCountsByType(
            Collections.singletonList("study_tcga_pub_mutations"),
            sampleIds,
            Collections.singletonList(207),
            "mutations"
        );
        
        Assert.assertEquals("AKT1", result.getHugoGeneSymbol());
        Assert.assertEquals("mutations", result.getProfileType());
        Assert.assertEquals(2, result.getCounts().size());
    }
}
