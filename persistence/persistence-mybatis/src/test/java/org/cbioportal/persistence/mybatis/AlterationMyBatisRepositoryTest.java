package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.*;
import org.cbioportal.model.QueryElement;
import org.cbioportal.model.util.Select;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class AlterationMyBatisRepositoryTest {

    //    mutation and cna events in testSql.sql
    //        SAMPLE_ID, ENTREZ_GENE_ID, HUGO_GENE_SYMBOL, GENETIC_PROFILE_ID, TYPE, MUTATION_TYPE, DRIVER_FILTER, DRIVER_TIERS_FILTER, PATIENT_ID, MUTATION_TYPE
    //        1	    207	AKT1	2	CNA         -2	                Putative_Driver	    Tier 1  TCGA-A1-A0SB    germline
    //        2	    207	AKT1	2	CNA         2	                Putative_Passenger	Tier 2  TCGA-A1-A0SD    germline
    //        1	    207	AKT1	6	MUTATION    Nonsense_Mutation	Putative_Driver	    Tier 1  TCGA-A1-A0SB    germline
    //        2	    207	AKT1	6	MUTATION    Missense_Mutation	Putative_Passenger	Tier 2  TCGA-A1-A0SD    germline
    //        1	    208	AKT2	2	CNA         2		            <null>              <null>  TCGA-A1-A0SB    germline
    //        3	    208	AKT2	6	MUTATION    Splice_Site	        Putative_Passenger	Tier 1  TCGA-A1-A0SE    germline
    //        6	    672	BRCA1	6	MUTATION    Missense_Mutation	Putative_Passenger	Tier 2  TCGA-A1-A0SH    germline
    //        6	    672	BRCA1	6	MUTATION    Nonsense_Mutation	Putative_Driver	    Tier 1  TCGA-A1-A0SH    germline
    //        7	    672	BRCA1	6	MUTATION    Nonsense_Mutation	Putative_Driver	    Tier 2  TCGA-A1-A0SI    germline
    //        12	672	BRCA1	6	MUTATION    Splice_Site	        Putative_Passenger	Tier 1  TCGA-A1-A0SO    germline
    //        13	672	BRCA1	6	MUTATION    Splice_Site	        Putative_Driver	    Tier 1  TCGA-A1-A0SP    germline

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
    List<MolecularProfileCaseIdentifier> sampleIdToProfileId = new ArrayList<>();
    List<MolecularProfileCaseIdentifier> patientIdToProfileId = new ArrayList<>();
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

        patientIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SB", "study_tcga_pub_mutations"));
        patientIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SE", "study_tcga_pub_mutations"));
        patientIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SH", "study_tcga_pub_mutations"));
        patientIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SI", "study_tcga_pub_mutations"));
        patientIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SO", "study_tcga_pub_mutations"));
        patientIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SP", "study_tcga_pub_mutations"));
        patientIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SD", "study_tcga_pub_mutations"));
        patientIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SB", "study_tcga_pub_gistic"));
        patientIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SD", "study_tcga_pub_gistic"));

        entrezGeneIds = Select.byValues(Arrays.asList(207, 208, 672));
        alterationFilter = new AlterationFilter(
            mutationEventTypes,
            cnaEventTypes,
            false,
            false,
            false,
            false,
            false,
            false,
            Select.none(),
            false
        );
    }

    @Test
    public void getSampleMutationCount() throws Exception {

        alterationFilter.setIncludeDriver(true);
        alterationFilter.setIncludeVUS(true);
        alterationFilter.setIncludeUnknownOncogenicity(true);
        alterationFilter.setIncludeGermline(true);
        alterationFilter.setIncludeSomatic(true);
        alterationFilter.setIncludeUnknownStatus(true);
        alterationFilter.setCnaTypeSelect(Select.none());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            QueryElement.PASS,
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
    public void getSampleCnaCount() throws Exception {

        alterationFilter.setIncludeDriver(true);
        alterationFilter.setIncludeVUS(true);
        alterationFilter.setIncludeUnknownOncogenicity(true);
        alterationFilter.setIncludeGermline(true);
        alterationFilter.setIncludeSomatic(true);
        alterationFilter.setIncludeUnknownStatus(true);
        alterationFilter.setSelectedMutationTypes(Select.none());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            QueryElement.PASS,
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
    public void getSampleMutationAndCnaCount() throws Exception {

        alterationFilter.setIncludeDriver(true);
        alterationFilter.setIncludeVUS(true);
        alterationFilter.setIncludeUnknownOncogenicity(true);
        alterationFilter.setIncludeGermline(true);
        alterationFilter.setIncludeSomatic(true);
        alterationFilter.setIncludeUnknownStatus(true);
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            QueryElement.PASS,
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

        List<MolecularProfileCaseIdentifier> sampleIdToProfileId = new ArrayList<>();
        // Sample is not profiled for mutations and not cna
        sampleIdToProfileId.add(new MolecularProfileCaseIdentifier("TCGA-A1-A0SE-01", "study_tcga_pub_gistic"));

        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            QueryElement.PASS,
            alterationFilter);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleMutationCountFilterFusions() throws Exception {

        alterationFilter.setIncludeDriver(true);
        alterationFilter.setIncludeVUS(true);
        alterationFilter.setIncludeUnknownOncogenicity(true);
        alterationFilter.setIncludeGermline(true);
        alterationFilter.setIncludeSomatic(true);
        alterationFilter.setIncludeUnknownStatus(true);
        alterationFilter.setCnaTypeSelect(Select.none());
        alterationFilter.setSelectedMutationTypes(Select.all());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            QueryElement.ACTIVE,
            alterationFilter);
        // there are no fusion mutations in the test db
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientMutationCount() throws Exception {

        alterationFilter.setIncludeDriver(true);
        alterationFilter.setIncludeVUS(true);
        alterationFilter.setIncludeUnknownOncogenicity(true);
        alterationFilter.setIncludeGermline(true);
        alterationFilter.setIncludeSomatic(true);
        alterationFilter.setIncludeUnknownStatus(true);
        alterationFilter.setCnaTypeSelect(Select.none());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId,
            entrezGeneIds,    
            QueryElement.PASS,
            alterationFilter);

        // For testSql.sql there are no more samples per patient for the investigated genes.
        // Therefore, patient level counts are the same as the sample level counts.
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
    public void getPatientCnaCount() throws Exception {

        alterationFilter.setIncludeDriver(true);
        alterationFilter.setIncludeVUS(true);
        alterationFilter.setIncludeUnknownOncogenicity(true);
        alterationFilter.setSelectedMutationTypes(Select.none());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId,
            entrezGeneIds,    
            QueryElement.PASS,
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
    public void getSampleCnaCountLegacy() throws Exception {

        // FIXME: the CnaCountLegacy endpoint is different from the AlterationCount endpoint
        // because it returns a single additional value 'cytoband'. It would make sense to 
        // harmonize these endpoints (both or none return 'cytoband') and use the AlterationCount
        // endpoint for all counts. Let's discuss...
        alterationFilter.setIncludeDriver(true);
        alterationFilter.setIncludeVUS(true);
        alterationFilter.setIncludeUnknownOncogenicity(true);
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(
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
    public void getPatientCnaCountLegacy() throws Exception {

        // FIXME: the CnaCountLegacy endpoint is different from the AlterationCount endpoint
        // because it returns a single additional value 'cytoband'. It would make sense to 
        // harmonize these endpoints (both or none return 'cytoband') and use the AlterationCount
        // endpoint for all counts. Let's discuss...
        alterationFilter.setIncludeDriver(true);
        alterationFilter.setIncludeVUS(true);
        alterationFilter.setIncludeUnknownOncogenicity(true);
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
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
    public void getSampleAlterationCountsReturnsZeroForMutationsAndCnaSelectorsInNone() {

        alterationFilter.setIncludeDriver(true);
        alterationFilter.setIncludeVUS(true);
        alterationFilter.setIncludeUnknownOncogenicity(true);
        alterationFilter.setIncludeGermline(true);
        alterationFilter.setIncludeSomatic(true);
        alterationFilter.setIncludeUnknownStatus(true);
        alterationFilter.setCnaTypeSelect(Select.none());
        alterationFilter.setSelectedMutationTypes(Select.none());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            QueryElement.PASS,
            alterationFilter);

        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleAlterationCountsReturnsAllForMutationsAndCnaSelectorsInAll() {

        alterationFilter.setIncludeDriver(true);
        alterationFilter.setIncludeVUS(true);
        alterationFilter.setIncludeUnknownOncogenicity(true);
        alterationFilter.setIncludeGermline(true);
        alterationFilter.setIncludeSomatic(true);
        alterationFilter.setIncludeUnknownStatus(true);
        alterationFilter.setCnaTypeSelect(Select.all());
        alterationFilter.setSelectedMutationTypes(Select.all());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            QueryElement.PASS,
            alterationFilter);

        Assert.assertEquals(3, result.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void disallowMutationTypesAndFusionSearchSamples() throws Exception {
        alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId, entrezGeneIds, QueryElement.ACTIVE,
            alterationFilter);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void disallowMutationTypesAndMutationSearchSamples() throws Exception {
        alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId, entrezGeneIds, QueryElement.INACTIVE, alterationFilter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void disallowMutationTypesAndFusionSearchPatients() throws Exception {
        alterationMyBatisRepository.getPatientAlterationCounts(
            sampleIdToProfileId, entrezGeneIds, QueryElement.ACTIVE, alterationFilter);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void disallowMutationTypesAndMutationSearchPatients() throws Exception {
        alterationMyBatisRepository.getPatientAlterationCounts(
            sampleIdToProfileId, entrezGeneIds, QueryElement.INACTIVE, alterationFilter);
    }

    @Test
    public void getSampleCountNullIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            null, entrezGeneIds, QueryElement.PASS, new AlterationFilter());
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCountNullIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            null, entrezGeneIds, QueryElement.PASS, new AlterationFilter());
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleCnaCountNullIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(
            null, entrezGeneIds, new AlterationFilter());
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleCountIncludeOnlyDriver() throws Exception {

        alterationFilter.setIncludeDriver(true);
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            QueryElement.PASS,
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
    public void getSampleCountIncludeOnlyVus() throws Exception {

        alterationFilter.setIncludeVUS(true);
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            QueryElement.PASS,
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
    public void getSampleCountIncludeOnlyUnknownOncogenicity() throws Exception {

        alterationFilter.setIncludeUnknownOncogenicity(true);
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            QueryElement.PASS,
            alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleCountIncludeAnyAndUnknownAnnotation() throws Exception {

        alterationFilter.setIncludeDriver(true);
        alterationFilter.setIncludeVUS(true);
        alterationFilter.setIncludeUnknownOncogenicity(true);
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            QueryElement.PASS,
            alterationFilter);

        Assert.assertEquals(3, result.size());
        AlterationCountByGene result672 = result.stream().filter(r -> r.getEntrezGeneId() == 672).findFirst().get();
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 5, result672.getTotalCount());
        Assert.assertEquals((Integer) 4, result672.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 2, result208.getTotalCount());
        Assert.assertEquals((Integer) 2, result208.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 4, result207.getTotalCount());
        Assert.assertEquals((Integer) 2, result207.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleCountIncludeOnlyTiers() throws Exception {

        // All 'Tier 2' tiers are forced to be interpreted as driver events
        alterationFilter.setSelectedTiers(Select.byValues(Arrays.asList("Tier 2")));
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            QueryElement.PASS,
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
    public void getSampleCountIncludeAnyAndUnknownTiers() throws Exception {

        alterationFilter.setSelectedTiers(Select.all());
        alterationFilter.setIncludeUnknownTier(true);
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            QueryElement.PASS,
            alterationFilter);

        Assert.assertEquals(3, result.size());
        AlterationCountByGene result672 = result.stream().filter(r -> r.getEntrezGeneId() == 672).findFirst().get();
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 5, result672.getTotalCount());
        Assert.assertEquals((Integer) 4, result672.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 2, result208.getTotalCount());
        Assert.assertEquals((Integer) 2, result208.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 4, result207.getTotalCount());
        Assert.assertEquals((Integer) 2, result207.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleCountIncludeAllTiers() throws Exception {

        alterationFilter.setSelectedTiers(Select.all());
        alterationFilter.setIncludeUnknownTier(true);
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            QueryElement.PASS,
            alterationFilter);

        Assert.assertEquals(3, result.size());
        AlterationCountByGene result672 = result.stream().filter(r -> r.getEntrezGeneId() == 672).findFirst().get();
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 5, result672.getTotalCount());
        Assert.assertEquals((Integer) 4, result672.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 2, result208.getTotalCount());
        Assert.assertEquals((Integer) 2, result208.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 4, result207.getTotalCount());
        Assert.assertEquals((Integer) 2, result207.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleCountIncludeUnknownTier() throws Exception {

        alterationFilter.setIncludeUnknownTier(true);
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            QueryElement.PASS,
            alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientMutationAndCnaCount() throws Exception {

        alterationFilter.setIncludeDriver(true);
        alterationFilter.setIncludeVUS(true);
        alterationFilter.setIncludeUnknownOncogenicity(true);
        alterationFilter.setIncludeGermline(true);
        alterationFilter.setIncludeSomatic(true);
        alterationFilter.setIncludeUnknownStatus(true);
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId,
            entrezGeneIds,
            QueryElement.PASS,
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
    public void getPatientMutationCountIncludeOnlyGermline() throws Exception {

        alterationFilter.setIncludeGermline(true);
        alterationFilter.setCnaTypeSelect(Select.none());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId,
            entrezGeneIds,
            QueryElement.PASS,
            alterationFilter);
        // all mutations in testSql.sql are Germline mutations
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
    public void getPatientMutationCountIncludeOnlySomatic() throws Exception {

        alterationFilter.setIncludeSomatic(true);
        alterationFilter.setCnaTypeSelect(Select.none());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId,
            entrezGeneIds,
            QueryElement.PASS,
            alterationFilter);
        // all mutations in testSql.sql are Germline mutations
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientMutationCountIncludeOnlyUnknownStatus() throws Exception {

        alterationFilter.setIncludeUnknownStatus(true);
        alterationFilter.setCnaTypeSelect(Select.none());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId,
            entrezGeneIds,
            QueryElement.PASS,
            alterationFilter);
        // all mutations in testSql.sql are Germline mutations
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getPatientCountIncludeAnyAndUnknowStatus() throws Exception {

        alterationFilter.setIncludeGermline(true);
        alterationFilter.setIncludeSomatic(true);
        alterationFilter.setIncludeUnknownStatus(true);
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId,
            entrezGeneIds,
            QueryElement.PASS,
            alterationFilter);

        Assert.assertEquals(3, result.size());
        AlterationCountByGene result672 = result.stream().filter(r -> r.getEntrezGeneId() == 672).findFirst().get();
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 5, result672.getTotalCount());
        Assert.assertEquals((Integer) 4, result672.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 2, result208.getTotalCount());
        Assert.assertEquals((Integer) 2, result208.getNumberOfAlteredCases());
        Assert.assertEquals((Integer) 4, result207.getTotalCount());
        Assert.assertEquals((Integer) 2, result207.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientMutationCountFilterFusions() throws Exception {

        alterationFilter.setIncludeDriver(true);
        alterationFilter.setIncludeVUS(true);
        alterationFilter.setIncludeUnknownOncogenicity(true);
        alterationFilter.setIncludeGermline(true);
        alterationFilter.setIncludeSomatic(true);
        alterationFilter.setIncludeUnknownStatus(true);
        alterationFilter.setCnaTypeSelect(Select.none());
        alterationFilter.setSelectedMutationTypes(Select.all());
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId,
            entrezGeneIds,
            QueryElement.ACTIVE,
            alterationFilter);
        // there are no fusion mutations in the test db
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCountIncludeOnlyDriver() throws Exception {

        alterationFilter.setIncludeDriver(true);
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId,
            entrezGeneIds,
            QueryElement.PASS,
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
    public void getPatientCountIncludeOnlyVUS() throws Exception {

        alterationFilter.setIncludeVUS(true);
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId,
            entrezGeneIds,
            QueryElement.PASS,
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
    public void getPatientCountIncludeOnlyTiers() throws Exception {

        // All 'Tier 2' tiers are forced to be interpreted as driver events
        alterationFilter.setSelectedTiers( Select.byValues(Arrays.asList("Tier 2")));
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId,
            entrezGeneIds,
            QueryElement.PASS,
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
    public void getPatientCountIncludeUnknownTier() throws Exception {

        alterationFilter.setIncludeUnknownTier(true);
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId,
            entrezGeneIds,
            QueryElement.PASS,
            alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleCnaCountLegacyOnlyDriver() throws Exception {

        alterationFilter.setIncludeDriver(true);
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(sampleIdToProfileId, entrezGeneIds, alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        Assert.assertEquals((Integer) 1, result207.getTotalCount());
        Assert.assertEquals((Integer) 1, result207.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleCnaCountLegacyOnlyVUS() throws Exception {

        alterationFilter.setIncludeVUS(true);
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(sampleIdToProfileId, entrezGeneIds, alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        Assert.assertEquals((Integer) 1, result207.getTotalCount());
        Assert.assertEquals((Integer) 1, result207.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleCnaCountLegacyOnlyUnknownOncogenicity() throws Exception {

        alterationFilter.setIncludeUnknownOncogenicity(true);
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(
            sampleIdToProfileId, entrezGeneIds, alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleCnaCountLegacyOnlyUnknownTier() throws Exception {

        alterationFilter.setIncludeUnknownTier(true);
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(
            sampleIdToProfileId, entrezGeneIds, alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleCnaCountLegacyOnlyTier2() throws Exception {

        alterationFilter.setSelectedTiers( Select.byValues(Arrays.asList("Tier 2")));
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(
            sampleIdToProfileId, entrezGeneIds, alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        Assert.assertEquals((Integer) 1, result207.getTotalCount());
        Assert.assertEquals((Integer) 1, result207.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientCnaCountLegacyOnlyDriver() throws Exception {

        alterationFilter.setIncludeDriver(true);
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            patientIdToProfileId, entrezGeneIds, alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        Assert.assertEquals((Integer) 1, result207.getTotalCount());
        Assert.assertEquals((Integer) 1, result207.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientCnaCountLegacyOnlyVUS() throws Exception {

        alterationFilter.setIncludeVUS(true);
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            patientIdToProfileId, entrezGeneIds, alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        Assert.assertEquals((Integer) 1, result207.getTotalCount());
        Assert.assertEquals((Integer) 1, result207.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientCnaCountLegacyOnlyUnknownOncogenicity() throws Exception {

        alterationFilter.setIncludeUnknownOncogenicity(true);
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            patientIdToProfileId, entrezGeneIds, alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientCnaCountLegacyOnlyUnknownTier() throws Exception {

        alterationFilter.setIncludeUnknownTier(true);
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            patientIdToProfileId, entrezGeneIds, alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientCnaCountLegacyOnlyTier2() throws Exception {

        alterationFilter.setSelectedTiers( Select.byValues(Arrays.asList("Tier 2")));
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            patientIdToProfileId, entrezGeneIds, alterationFilter);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        Assert.assertEquals((Integer) 1, result207.getTotalCount());
        Assert.assertEquals((Integer) 1, result207.getNumberOfAlteredCases());
    }


    @Test
    public void getPatientCnaCountNullIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            null, entrezGeneIds, new AlterationFilter());
        Assert.assertEquals(0, result.size());
    }
    
    @Test
    public void getSampleCountNullEntrezGeneIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId, null, QueryElement.PASS, new AlterationFilter());
        Assert.assertEquals(0, result.size());
    }
    
    @Test
    public void getSampleCountEmptyEntrezGeneIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId, Select.none(), QueryElement.PASS, new AlterationFilter());
        Assert.assertEquals(0, result.size());
    }
    
    @Test
    public void getSampleCountAllEntrezGeneIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId, Select.all(), QueryElement.PASS, new AlterationFilter());
        Assert.assertEquals(3, result.size());
    }

    @Test
    public void getPatientCountNullEntrezGeneIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId, null, QueryElement.PASS, new AlterationFilter());
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCountEmptyEntrezGeneIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId, Select.none(), QueryElement.PASS, new AlterationFilter());
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCountAllEntrezGeneIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId, Select.all(), QueryElement.PASS, new AlterationFilter());
        Assert.assertEquals(3, result.size());
    }

    @Test
    public void getSampleCnaCountNullEntrezGeneIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(
            sampleIdToProfileId, null, new AlterationFilter());
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleCnaCountEmptyEntrezGeneIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(
            sampleIdToProfileId, Select.none(), new AlterationFilter());
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleCnaCountAllEntrezGeneIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(
            sampleIdToProfileId, Select.all(), new AlterationFilter());
        Assert.assertEquals(3, result.size());
    }

    @Test
    public void getPatientCnaCountNullEntrezGeneIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            patientIdToProfileId, null, new AlterationFilter());
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCnaCountEmptyEntrezGeneIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            patientIdToProfileId, Select.none(), new AlterationFilter());
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCnaCountAllEntrezGeneIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            patientIdToProfileId, Select.all(), new AlterationFilter());
        Assert.assertEquals(3, result.size());
    }
    
}
