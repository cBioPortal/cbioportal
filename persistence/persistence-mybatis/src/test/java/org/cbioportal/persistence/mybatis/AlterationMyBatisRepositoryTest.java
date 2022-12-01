package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.*;
import org.cbioportal.model.util.Select;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class AlterationMyBatisRepositoryTest {

    //    mutation, cna and struct var events in testSql.sql
    //        SAMPLE_ID,        ENTREZ_GENE_ID, HUGO_GENE_SYMBOL, GENETIC_PROFILE_ID, TYPE, MUTATION_TYPE, DRIVER_FILTER, DRIVER_TIERS_FILTER, PATIENT_ID, MUTATION_TYPE
    //        1	    207	        AKT1	    2	CNA         -2	                Putative_Driver	    Tier 1  TCGA-A1-A0SB    germline
    //        2	    207	        AKT1	    2	CNA         2	                Putative_Passenger	Tier 2  TCGA-A1-A0SD    germline
    //        1	    207	        AKT1	    6	MUTATION    Nonsense_Mutation	Putative_Driver	    Tier 1  TCGA-A1-A0SB    germline
    //        2	    207	        AKT1	    6	MUTATION    Missense_Mutation	Putative_Passenger	Tier 2  TCGA-A1-A0SD    germline
    //        1	    208	        AKT2	    2	CNA         2		            <null>              <null>  TCGA-A1-A0SB    germline
    //        3	    208	        AKT2	    6	MUTATION    Splice_Site	        Putative_Passenger	Tier 1  TCGA-A1-A0SE    germline
    //        6	    672	        BRCA1	    6	MUTATION    Missense_Mutation	Putative_Passenger	Tier 2  TCGA-A1-A0SH    germline
    //        6	    672	        BRCA1	    6	MUTATION    Nonsense_Mutation	Putative_Driver	    Tier 1  TCGA-A1-A0SH    NA
    //        7	    672	        BRCA1	    6	MUTATION    Nonsense_Mutation	Putative_Driver	    Tier 2  TCGA-A1-A0SI    germline
    //        12	672	        BRCA1	    6	MUTATION    Splice_Site	        Putative_Passenger	Tier 1  TCGA-A1-A0SO    germline
    //        13	672	        BRCA1	    6	MUTATION    Splice_Site	        Putative_Driver	    Tier 1  TCGA-A1-A0SP    germline
    //        1     27436-238   EML4-ALK    7   SV          Fusion              <noi>               <noi>   TCGA-A1-A0SB    germline
    //        2     27436-238   EML4-ALK    7   SV          Fusion              <noi>               <noi>   TCGA-A1-A0SD    somatic
    //        1     57670-673   KIAA..-BRAF 7   SV          Fusion              <noi>               <noi>   TCGA-A1-A0SB    somatic
    //        2     57670-673   KIAA..-BRAF 7   SV          Fusion              <noi>               <noi>   TCGA-A1-A0SD    germline
    //        2     57670-673   KIAA..-BRAF 7   SV          Fusion              <noi>               <noi>   TCGA-A1-A0SD    somatic
    //       15     57670-673   KIAA..-BRAF 13  SV          Fusion              <noi>               <noi>   TCGA-A1-A0SD    somatic
    //        1     8031-5979   NCOA4-RET   7   SV          Fusion              <noi>               <noi>   TCGA-A1-A0SB    somatic
    //       15     8031-5979   NCOA4-RET   13  SV          Fusion              <noi>               <noi>   TCGA-A1-A0SB    somatic
    //        1     7113-2078   TMPRSS2-ERG 7   SV          Fusion              <noi>               <noi>   TCGA-A1-A0SB    somatic

    @Autowired
    private AlterationMyBatisRepository alterationMyBatisRepository;

    Select<MutationEventType> mutationEventTypes = Select.byValues(Arrays.asList(
        MutationEventType.splice_site,
        MutationEventType.nonsense_mutation,
        MutationEventType.missense_mutation
    ));
    Select<CNA> cnaEventTypes = Select.byValues(Arrays.asList(
        CNA.AMP,
        CNA.HOMDEL
    ));
    Set<MolecularProfileCaseIdentifier> sampleIdToProfileId = new TreeSet<>();
    Set<MolecularProfileCaseIdentifier> svSampleIdToProfileId = new TreeSet<>();
    Set<MolecularProfileCaseIdentifier> patientIdToProfileId = new TreeSet<>();
    Set<MolecularProfileCaseIdentifier> svPatientIdToProfileId = new TreeSet<>();
    AlterationFilter alterationFilter;
    
    Select<Integer> entrezGeneIds;

    @Before
    public void setup() {
        
        sampleIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SB-01", "study_tcga_pub_mutations"));
        sampleIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SE-01", "study_tcga_pub_mutations"));
        sampleIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SH-01", "study_tcga_pub_mutations"));
        sampleIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SI-01", "study_tcga_pub_mutations"));
        sampleIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SO-01", "study_tcga_pub_mutations"));
        sampleIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SP-01", "study_tcga_pub_mutations"));
        sampleIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SD-01", "study_tcga_pub_mutations"));
        sampleIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SB-01", "study_tcga_pub_gistic"));
        sampleIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SD-01", "study_tcga_pub_gistic"));
        
        svSampleIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SB-01", "study_tcga_pub_sv"));
        svSampleIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SD-01", "study_tcga_pub_sv"));

        patientIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SB", "study_tcga_pub_mutations"));
        patientIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SE", "study_tcga_pub_mutations"));
        patientIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SH", "study_tcga_pub_mutations"));
        patientIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SI", "study_tcga_pub_mutations"));
        patientIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SO", "study_tcga_pub_mutations"));
        patientIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SP", "study_tcga_pub_mutations"));
        patientIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SD", "study_tcga_pub_mutations"));
        patientIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SB", "study_tcga_pub_gistic"));
        patientIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SD", "study_tcga_pub_gistic"));
        
        svPatientIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SB", "study_tcga_pub_sv"));
        svPatientIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SD", "study_tcga_pub_sv"));

        entrezGeneIds = Select.byValues(Arrays.asList(207, 208, 672, 2064));
        alterationFilter = new AlterationFilter(
            mutationEventTypes,
            cnaEventTypes,
            true,
            true,
            true,
            true,
            true,
            true,
            Select.all(),
            true
        );
    }

    @Test
    public void getSampleMutationGeneCountAllDriverAnnotationsExcluded() throws Exception {
        alterationFilter.setIncludeDriver(false);
        alterationFilter.setIncludeVUS(false);
        alterationFilter.setIncludeUnknownOncogenicity(false);
        alterationFilter.setCnaTypeSelect(Select.none());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationGeneCounts(
            new HashSet<>(sampleIdToProfileId),
            entrezGeneIds,
            alterationFilter);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleMutationGeneCountAllDriverTiersExcluded() throws Exception {
        alterationFilter.setSelectedTiers(Select.none());
        alterationFilter.setIncludeUnknownTier(false);
        alterationFilter.setCnaTypeSelect(Select.none());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationGeneCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            alterationFilter);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleMutationGeneCountAllDriverTiersExcludedWithNullSelect() throws Exception {
        alterationFilter.setSelectedTiers(null);
        alterationFilter.setIncludeUnknownTier(false);
        alterationFilter.setCnaTypeSelect(Select.none());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationGeneCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            alterationFilter);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleMutationGeneCountAllMutationStatusExcluded() throws Exception {
        alterationFilter.setIncludeSomatic(false);
        alterationFilter.setIncludeGermline(false);
        alterationFilter.setIncludeUnknownStatus(false);
        alterationFilter.setCnaTypeSelect(Select.none());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationGeneCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            alterationFilter);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleMutationGeneCount() throws Exception {
        alterationFilter.setCnaTypeSelect(Select.none());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationGeneCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            alterationFilter);

        Assert.assertEquals(3, result.size());
        AlterationCountByGene result672 = result.stream().filter(r -> r.getEntrezGeneId() == 672).findFirst().get();
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 5, result672.getTotalCount());
        Assert.assertEquals((Integer) 4, result672.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 2, result207.getTotalCount());
        Assert.assertEquals((Integer) 2, result207.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleCnaGeneCount() throws Exception {
        alterationFilter.setMutationTypeSelect(Select.none());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationGeneCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            alterationFilter);

        Assert.assertEquals(2, result.size());
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 2, result207.getTotalCount());
        Assert.assertEquals((Integer) 2, result207.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleMutationAndCnaGeneCount() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationGeneCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            alterationFilter);

        Assert.assertEquals(3, result.size());
        AlterationCountByGene result672 = result.stream().filter(r -> r.getEntrezGeneId() == 672).findFirst().get();
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 5, result672.getTotalCount());
        Assert.assertEquals((Integer) 4, result672.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 4, result207.getTotalCount());
        Assert.assertEquals((Integer) 2, result207.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 2, result208.getTotalCount());
        Assert.assertEquals((Integer) 2, result208.getNumberOfAlteredCases());
    }

    @Test
    public void whenSampleNotProfiledForCNA() throws Exception {

        Set<MolecularProfileCaseIdentifier> sampleIdToProfileId = new TreeSet<>();
        // Sample is not profiled for mutations and not cna
        sampleIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SE-01", "study_tcga_pub_gistic"));

        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationGeneCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            alterationFilter);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCnaGeneCountAllDriverAnnotationsExcluded() throws Exception {
        alterationFilter.setIncludeDriver(false);
        alterationFilter.setIncludeVUS(false);
        alterationFilter.setIncludeUnknownOncogenicity(false);
        alterationFilter.setMutationTypeSelect(Select.none());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationGeneCounts(
            patientIdToProfileId,
            entrezGeneIds,
            alterationFilter);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCnaGeneCountAllDriverTiersExcluded() throws Exception {
        alterationFilter.setSelectedTiers(Select.none());
        alterationFilter.setIncludeUnknownTier(false);
        alterationFilter.setMutationTypeSelect(Select.none());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationGeneCounts(
            patientIdToProfileId,
            entrezGeneIds,
            alterationFilter);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCnaGeneCountAllDriverTiersExcludedNullSelect() throws Exception {
        alterationFilter.setSelectedTiers(null);
        alterationFilter.setIncludeUnknownTier(false);
        alterationFilter.setMutationTypeSelect(Select.none());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationGeneCounts(
            patientIdToProfileId,
            entrezGeneIds,
            alterationFilter);
        Assert.assertEquals(0, result.size());
    }
    
    @Test
    public void getPatientCnaGeneCount() throws Exception {
        alterationFilter.setMutationTypeSelect(Select.none());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationGeneCounts(
            patientIdToProfileId,
            entrezGeneIds,    
            alterationFilter);

        // For testSql.sql there are no more samples per patient for the investigated genes.
        // Therefore, patient level counts are the same as the sample level counts.
        Assert.assertEquals(2, result.size());
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 2, result207.getTotalCount());
        Assert.assertEquals((Integer) 2, result207.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleCnaGeneCountLegacy() throws Exception {

        // FIXME: the CnaCountLegacy endpoint is different from the AlterationCount endpoint
        // because it returns a single additional value 'cytoband'. It would make sense to 
        // harmonize these endpoints (both or none return 'cytoband') and use the AlterationCount
        // endpoint for all counts. Let's discuss...
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaGeneCounts(
            sampleIdToProfileId, 
            entrezGeneIds,
            alterationFilter);

        Assert.assertEquals(3, result.size());
        AlterationCountByGene result207up = result.stream().filter(r -> r.getEntrezGeneId() == 207 && r.getAlteration() == 2).findFirst().get();
        AlterationCountByGene result207down = result.stream().filter(r -> r.getEntrezGeneId() == 207 && r.getAlteration() == -2).findFirst().get();
        AlterationCountByGene result208up = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 1, result207up.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 1, result207down.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 1, result208up.getNumberOfAlteredCases());
    }
    
    @Test
    public void getPatientCnaGeneCountLegacy() throws Exception {

        // FIXME: the CnaCountLegacy endpoint is different from the AlterationCount endpoint
        // because it returns a single additional value 'cytoband'. It would make sense to 
        // harmonize these endpoints (both or none return 'cytoband') and use the AlterationCount
        // endpoint for all counts. Let's discuss...
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaGeneCounts(
            patientIdToProfileId,
            entrezGeneIds,
            alterationFilter);

        // For testSql.sql there are no more samples per patient for the investigated genes.
        // Therefore, patient level counts are the same as the sample level counts.
        Assert.assertEquals(3, result.size());
        AlterationCountByGene result207up = result.stream().filter(r -> r.getEntrezGeneId() == 207 && r.getAlteration() == 2).findFirst().get();
        AlterationCountByGene result207down = result.stream().filter(r -> r.getEntrezGeneId() == 207 && r.getAlteration() == -2).findFirst().get();
        AlterationCountByGene result208up = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 1, result207up.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 1, result207down.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 1, result208up.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleAlterationGeneCountsReturnsZeroForMutationsAndCnaSelectorsInNone() {
        alterationFilter.setCnaTypeSelect(Select.none());
        alterationFilter.setMutationTypeSelect(Select.none());
	alterationFilter.setStructuralVariants(false);
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationGeneCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            alterationFilter);

        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleAlterationGeneCountsReturnsAllForMutationsAndCnaSelectorsInAll() {
        alterationFilter.setCnaTypeSelect(Select.all());
        alterationFilter.setMutationTypeSelect(Select.all());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationGeneCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            alterationFilter);

        Assert.assertEquals(3, result.size());
    }

    @Test
    public void getSampleGeneCountNullIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationGeneCounts(
            null, entrezGeneIds, new AlterationFilter());
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientGeneCountNullIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationGeneCounts(
            null, entrezGeneIds, new AlterationFilter());
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleCnaGeneCountNullIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaGeneCounts(
            null, entrezGeneIds, new AlterationFilter());
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleGeneCountIncludeOnlyDriver() throws Exception {
        alterationFilter.setIncludeVUS(false);
        alterationFilter.setIncludeUnknownOncogenicity(false);
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationGeneCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            alterationFilter);

        Assert.assertEquals(2, result.size());
        AlterationCountByGene result672 = result.stream().filter(r -> r.getEntrezGeneId() == 672).findFirst().get();
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        Assert.assertEquals((Integer) 3, result672.getTotalCount());
        Assert.assertEquals((Integer) 3, result672.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 2, result207.getTotalCount());
        Assert.assertEquals((Integer) 1, result207.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleGeneCountIncludeOnlyVus() throws Exception {
        alterationFilter.setIncludeDriver(false);
        alterationFilter.setIncludeUnknownOncogenicity(false);
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationGeneCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            alterationFilter);

        Assert.assertEquals(3, result.size());
        AlterationCountByGene result672 = result.stream().filter(r -> r.getEntrezGeneId() == 672).findFirst().get();
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 2, result672.getTotalCount());
        Assert.assertEquals((Integer) 2, result672.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 2, result207.getTotalCount());
        Assert.assertEquals((Integer) 1, result207.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleGeneCountIncludeOnlyUnknownOncogenicity() throws Exception {
        alterationFilter.setIncludeDriver(false);
        alterationFilter.setIncludeVUS(false);
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationGeneCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleGeneCountIncludeOnlyTiers() throws Exception {
        // All 'Tier 2' tiers are forced to be interpreted as driver events
        alterationFilter.setSelectedTiers(Select.byValues(Arrays.asList("Tier 2")));
        alterationFilter.setIncludeUnknownTier(false);
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationGeneCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            alterationFilter);

        Assert.assertEquals(2, result.size());
        AlterationCountByGene result672 = result.stream().filter(r -> r.getEntrezGeneId() == 672).findFirst().get();
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        Assert.assertEquals((Integer) 2, result672.getTotalCount());
        Assert.assertEquals((Integer) 2, result672.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 2, result207.getTotalCount());
        Assert.assertEquals((Integer) 1, result207.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleGeneCountIncludeUnknownTier() throws Exception {
        alterationFilter.setSelectedTiers(Select.none());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationGeneCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientMutationAndCnaGeneCount() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationGeneCounts(
            patientIdToProfileId,
            entrezGeneIds,
            alterationFilter);

        // For testSql.sql there are no more samples per patient for the investigated genes.
        // Therefore, patient level counts are the same as the sample level counts.
        Assert.assertEquals(3, result.size());
        AlterationCountByGene result672 = result.stream().filter(r -> r.getEntrezGeneId() == 672).findFirst().get();
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 5, result672.getTotalCount());
        Assert.assertEquals((Integer) 4, result672.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 4, result207.getTotalCount());
        Assert.assertEquals((Integer) 2, result207.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 2, result208.getTotalCount());
        Assert.assertEquals((Integer) 2, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientMutationGeneCountIncludeOnlyGermline() throws Exception {
        alterationFilter.setIncludeSomatic(false);
        alterationFilter.setIncludeUnknownStatus(false);
        alterationFilter.setCnaTypeSelect(Select.none());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationGeneCounts(
            patientIdToProfileId,
            entrezGeneIds,
            alterationFilter);
        // all but one mutations in testSql.sql are Germline mutations
        Assert.assertEquals(3, result.size());
        AlterationCountByGene result672 = result.stream().filter(r -> r.getEntrezGeneId() == 672).findFirst().get();
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 4, result672.getTotalCount());
        Assert.assertEquals((Integer) 4, result672.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 2, result207.getTotalCount());
        Assert.assertEquals((Integer) 2, result207.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientMutationGeneCountIncludeOnlySomatic() throws Exception {
        alterationFilter.setIncludeGermline(false);
        alterationFilter.setIncludeUnknownStatus(false);
        alterationFilter.setCnaTypeSelect(Select.none());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationGeneCounts(
            patientIdToProfileId,
            entrezGeneIds,
            alterationFilter);
        // all but one mutations in testSql.sql are Germline mutations
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientMutationGeneCountIncludeOnlyUnknownStatus() throws Exception {
        alterationFilter.setIncludeGermline(false);
        alterationFilter.setIncludeSomatic(false);
        alterationFilter.setCnaTypeSelect(Select.none());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationGeneCounts(
            patientIdToProfileId,
            entrezGeneIds,
            alterationFilter);
        // all but one mutations in testSql.sql are Germline mutations
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getPatientGeneCountIncludeOnlyDriver() throws Exception {
        alterationFilter.setIncludeVUS(false);
        alterationFilter.setIncludeUnknownOncogenicity(false);
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationGeneCounts(
            patientIdToProfileId,
            entrezGeneIds,
            alterationFilter);

        Assert.assertEquals(2, result.size());
        AlterationCountByGene result672 = result.stream().filter(r -> r.getEntrezGeneId() == 672).findFirst().get();
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        Assert.assertEquals((Integer) 3, result672.getTotalCount());
        Assert.assertEquals((Integer) 3, result672.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 2, result207.getTotalCount());
        Assert.assertEquals((Integer) 1, result207.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientGeneCountIncludeOnlyVUS() throws Exception {
        alterationFilter.setIncludeDriver(false);
        alterationFilter.setIncludeUnknownOncogenicity(false);
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationGeneCounts(
            patientIdToProfileId,
            entrezGeneIds,
            alterationFilter);

        Assert.assertEquals(3, result.size());
        AlterationCountByGene result672 = result.stream().filter(r -> r.getEntrezGeneId() == 672).findFirst().get();
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 2, result672.getTotalCount());
        Assert.assertEquals((Integer) 2, result672.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 2, result207.getTotalCount());
        Assert.assertEquals((Integer) 1, result207.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }
    
    @Test
    public void getPatientGeneCountIncludeOnlyUnknownOncogenicity() throws Exception {
        alterationFilter.setIncludeDriver(false);
        alterationFilter.setIncludeVUS(false);
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationGeneCounts(
            patientIdToProfileId,
            entrezGeneIds,
            alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientGeneCountIncludeOnlyTiers() throws Exception {
        // All 'Tier 2' tiers are forced to be interpreted as driver events
        alterationFilter.setSelectedTiers(Select.byValues(Arrays.asList("Tier 2")));
        alterationFilter.setIncludeUnknownTier(false);
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationGeneCounts(
            patientIdToProfileId,
            entrezGeneIds,
            alterationFilter);

        Assert.assertEquals(2, result.size());
        AlterationCountByGene result672 = result.stream().filter(r -> r.getEntrezGeneId() == 672).findFirst().get();
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        Assert.assertEquals((Integer) 2, result672.getTotalCount());
        Assert.assertEquals((Integer) 2, result672.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 2, result207.getTotalCount());
        Assert.assertEquals((Integer) 1, result207.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientGeneCountIncludeUnknownTier() throws Exception {
        alterationFilter.setSelectedTiers(Select.none());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationGeneCounts(
            patientIdToProfileId,
            entrezGeneIds,
            alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleCnaGeneCountLegacyOnlyDriver() throws Exception {
        alterationFilter.setIncludeVUS(false);
        alterationFilter.setIncludeUnknownOncogenicity(false);
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaGeneCounts(sampleIdToProfileId, entrezGeneIds, alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        Assert.assertEquals((Integer) 1, result207.getTotalCount());
        Assert.assertEquals((Integer) 1, result207.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleCnaGeneCountLegacyOnlyVUS() throws Exception {
        alterationFilter.setIncludeDriver(false);
        alterationFilter.setIncludeUnknownOncogenicity(false);
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaGeneCounts(sampleIdToProfileId, entrezGeneIds, alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        Assert.assertEquals((Integer) 1, result207.getTotalCount());
        Assert.assertEquals((Integer) 1, result207.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleCnaGeneCountLegacyOnlyUnknownOncogenicity() throws Exception {
        alterationFilter.setIncludeDriver(false);
        alterationFilter.setIncludeVUS(false);
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaGeneCounts(
            sampleIdToProfileId, entrezGeneIds, alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleCnaGeneCountLegacyOnlyUnknownTier() throws Exception {
        alterationFilter.setSelectedTiers(Select.none());
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaGeneCounts(
            sampleIdToProfileId, entrezGeneIds, alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleCnaGeneCountLegacyOnlyTier2() throws Exception {
        // All 'Tier 2' tiers are forced to be interpreted as driver events
        alterationFilter.setSelectedTiers(Select.byValues(Arrays.asList("Tier 2")));
        alterationFilter.setIncludeUnknownTier(false);
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaGeneCounts(
            sampleIdToProfileId, entrezGeneIds, alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        Assert.assertEquals((Integer) 1, result207.getTotalCount());
        Assert.assertEquals((Integer) 1, result207.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientCnaGeneCountLegacyOnlyDriver() throws Exception {
        alterationFilter.setIncludeVUS(false);
        alterationFilter.setIncludeUnknownOncogenicity(false);
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaGeneCounts(
            patientIdToProfileId, entrezGeneIds, alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        Assert.assertEquals((Integer) 1, result207.getTotalCount());
        Assert.assertEquals((Integer) 1, result207.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientCnaGeneCountLegacyOnlyVUS() throws Exception {
        alterationFilter.setIncludeDriver(false);
        alterationFilter.setIncludeUnknownOncogenicity(false);
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaGeneCounts(
            patientIdToProfileId, entrezGeneIds, alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        Assert.assertEquals((Integer) 1, result207.getTotalCount());
        Assert.assertEquals((Integer) 1, result207.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientCnaGeneCountLegacyOnlyUnknownOncogenicity() throws Exception {
        alterationFilter.setIncludeDriver(false);
        alterationFilter.setIncludeVUS(false);
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaGeneCounts(
            patientIdToProfileId, entrezGeneIds, alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientCnaGeneCountLegacyOnlyUnknownTier() throws Exception {
        alterationFilter.setSelectedTiers(Select.none());
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaGeneCounts(
            patientIdToProfileId, entrezGeneIds, alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientCnaGeneCountLegacyOnlyTier2() throws Exception {
        // All 'Tier 2' tiers are forced to be interpreted as driver events
        alterationFilter.setSelectedTiers(Select.byValues(Arrays.asList("Tier 2")));
        alterationFilter.setIncludeUnknownTier(false);
        alterationFilter.setSelectedTiers( Select.byValues(Arrays.asList("Tier 2")));
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaGeneCounts(
            patientIdToProfileId, entrezGeneIds, alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        Assert.assertEquals((Integer) 1, result207.getTotalCount());
        Assert.assertEquals((Integer) 1, result207.getNumberOfAlteredCases());
    }


    @Test
    public void getPatientCnaGeneCountNullIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaGeneCounts(
            null, entrezGeneIds, new AlterationFilter());
        Assert.assertEquals(0, result.size());
    }
    
    @Test
    public void getSampleGeneCountNullEntrezGeneIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationGeneCounts(
            sampleIdToProfileId, null, new AlterationFilter());
        Assert.assertEquals(0, result.size());
    }
    
    @Test
    public void getSampleGeneCountEmptyEntrezGeneIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationGeneCounts(
            sampleIdToProfileId, Select.none(), new AlterationFilter());
        Assert.assertEquals(0, result.size());
    }
    
    @Test
    public void getSampleGeneCountAllEntrezGeneIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationGeneCounts(
            sampleIdToProfileId, Select.all(), new AlterationFilter());
        Assert.assertEquals(3, result.size());
    }

    @Test
    public void getPatientGeneCountNullEntrezGeneIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationGeneCounts(
            patientIdToProfileId, null, new AlterationFilter());
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientGeneCountEmptyEntrezGeneIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationGeneCounts(
            patientIdToProfileId, Select.none(), new AlterationFilter());
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientGeneCountAllEntrezGeneIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationGeneCounts(
            patientIdToProfileId, Select.all(), new AlterationFilter());
        Assert.assertEquals(3, result.size());
    }

    @Test
    public void getSampleCnaGeneCountNullEntrezGeneIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaGeneCounts(
            sampleIdToProfileId, null, new AlterationFilter());
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleCnaGeneCountEmptyEntrezGeneIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaGeneCounts(
            sampleIdToProfileId, Select.none(), new AlterationFilter());
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleCnaGeneCountAllEntrezGeneIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaGeneCounts(
            sampleIdToProfileId, Select.all(), new AlterationFilter());
        Assert.assertEquals(3, result.size());
    }

    @Test
    public void getPatientCnaGeneCountNullEntrezGeneIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaGeneCounts(
            patientIdToProfileId, null, new AlterationFilter());
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCnaGeneCountEmptyEntrezGeneIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaGeneCounts(
            patientIdToProfileId, Select.none(), new AlterationFilter());
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCnaGeneCountAllEntrezGeneIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaGeneCounts(
            patientIdToProfileId, Select.all(), new AlterationFilter());
        Assert.assertEquals(3, result.size());
    }

    @Test
    public void getSampleStructuralVariantCount() throws Exception {
        List<AlterationCountByStructuralVariant> result = alterationMyBatisRepository.getSampleStructuralVariantCounts(
            svSampleIdToProfileId,
            alterationFilter);

        // Should be one KIAA154-BRAF and one NCOA4-RET
        Assert.assertEquals(4, result.size());
        AlterationCountByStructuralVariant resultEmlAlk = findStructVarCount("EML4", "ALK", result);
        AlterationCountByStructuralVariant resultKiaaBraf= findStructVarCount("KIAA1549", "BRAF", result);
        AlterationCountByStructuralVariant resultTmprsErg = findStructVarCount("TMPRSS2", "ERG", result);
        AlterationCountByStructuralVariant resultNcoRet = findStructVarCount("NCOA4", "RET", result);
        Assert.assertEquals((Integer) 2, resultEmlAlk.getTotalCount());
        Assert.assertEquals((Integer) 2, resultEmlAlk.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 3, resultKiaaBraf.getTotalCount());
        Assert.assertEquals((Integer) 2, resultKiaaBraf.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 1, resultNcoRet.getTotalCount());
        Assert.assertEquals((Integer) 1, resultNcoRet.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 1, resultTmprsErg.getTotalCount());
        Assert.assertEquals((Integer) 1, resultTmprsErg.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleStructuralVariantCountAllSvStatusExcluded() throws Exception {
    
        // Note: 'NA' for SV status is not allowed as per file-formats.md
        alterationFilter.setIncludeSomatic(false);
        alterationFilter.setIncludeGermline(false);
        alterationFilter.setIncludeUnknownStatus(false);
        List<AlterationCountByStructuralVariant> result = alterationMyBatisRepository.getSampleStructuralVariantCounts(
            sampleIdToProfileId,
            alterationFilter);

        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleStructuralVariantCountSomaticSvStatusExcluded() throws Exception {

        // Note: 'NA' for SV status is not allowed as per file-formats.md
        alterationFilter.setIncludeSomatic(false);
        alterationFilter.setIncludeUnknownStatus(false);
        List<AlterationCountByStructuralVariant> result = alterationMyBatisRepository.getSampleStructuralVariantCounts(
            svSampleIdToProfileId,
            alterationFilter);

        Assert.assertEquals(2, result.size());
        AlterationCountByStructuralVariant resultKiaaBraf= findStructVarCount("KIAA1549", "BRAF", result);
        AlterationCountByStructuralVariant resultEmlAlk = findStructVarCount("EML4", "ALK", result);
        Assert.assertEquals((Integer) 1, resultKiaaBraf.getTotalCount());
        Assert.assertEquals((Integer) 1, resultKiaaBraf.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 1, resultEmlAlk.getTotalCount());
        Assert.assertEquals((Integer) 1, resultEmlAlk.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleStructuralVariantCountGermlineSvStatusExcluded() throws Exception {
        // Note: 'NA' for SV status is not allowed as per file-formats.md
        alterationFilter.setIncludeGermline(false);
        alterationFilter.setIncludeUnknownStatus(false);
        List<AlterationCountByStructuralVariant> result = alterationMyBatisRepository.getSampleStructuralVariantCounts(
            svSampleIdToProfileId,
            alterationFilter);

        Assert.assertEquals(4, result.size());
        AlterationCountByStructuralVariant resultEmlAlk = findStructVarCount("EML4", "ALK", result);
        AlterationCountByStructuralVariant resultKiaaBraf= findStructVarCount("KIAA1549", "BRAF", result);
        AlterationCountByStructuralVariant resultTmprsErg = findStructVarCount("TMPRSS2", "ERG", result);
        AlterationCountByStructuralVariant resultNcoRet = findStructVarCount("NCOA4", "RET", result);
        Assert.assertEquals((Integer) 1, resultEmlAlk.getTotalCount());
        Assert.assertEquals((Integer) 1, resultEmlAlk.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 2, resultKiaaBraf.getTotalCount());
        Assert.assertEquals((Integer) 2, resultKiaaBraf.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 1, resultNcoRet.getTotalCount());
        Assert.assertEquals((Integer) 1, resultNcoRet.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 1, resultTmprsErg.getTotalCount());
        Assert.assertEquals((Integer) 1, resultTmprsErg.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientStructuralVariantCount() throws Exception {
        List<AlterationCountByStructuralVariant> result = alterationMyBatisRepository.getPatientStructuralVariantCounts(
            svPatientIdToProfileId,
            alterationFilter);

        Assert.assertEquals(4, result.size());
        AlterationCountByStructuralVariant resultEmlAlk = findStructVarCount("EML4", "ALK", result);
        AlterationCountByStructuralVariant resultKiaaBraf= findStructVarCount("KIAA1549", "BRAF", result);
        AlterationCountByStructuralVariant resultTmprsErg = findStructVarCount("TMPRSS2", "ERG", result);
        AlterationCountByStructuralVariant resultNcoRet = findStructVarCount("NCOA4", "RET", result);
        Assert.assertEquals((Integer) 2, resultEmlAlk.getTotalCount());
        Assert.assertEquals((Integer) 2, resultEmlAlk.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 3, resultKiaaBraf.getTotalCount());
        Assert.assertEquals((Integer) 2, resultKiaaBraf.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 1, resultNcoRet.getTotalCount());
        Assert.assertEquals((Integer) 1, resultNcoRet.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 1, resultTmprsErg.getTotalCount());
        Assert.assertEquals((Integer) 1, resultTmprsErg.getNumberOfAlteredCases());
    }

    private AlterationCountByStructuralVariant findStructVarCount(String gene1HugoSymbol,
                                                                  String gene2HugoSymbol,
                                                                  List<AlterationCountByStructuralVariant> counts) {
        return counts.stream().filter(
            c -> c.getGene1HugoGeneSymbol().equals(gene1HugoSymbol)
                && c.getGene2HugoGeneSymbol().equals(gene2HugoSymbol)).findFirst().get();
    }
    
}
