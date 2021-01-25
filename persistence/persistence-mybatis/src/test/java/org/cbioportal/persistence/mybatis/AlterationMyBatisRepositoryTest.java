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
//        SAMPLE_ID, ENTREZ_GENE_ID, HUGO_GENE_SYMBOL, GENETIC_PROFILE_ID, MUTATION_TYPE, DRIVER_FILTER, DRIVER_TIERS_FILTER, PATIENT_ID
//        1	    207	AKT1	2	-2	                Putative_Driver	    Tier 1  TCGA-A1-A0SB
//        2	    207	AKT1	2	2	                Putative_Passenger	Tier 2  TCGA-A1-A0SD
//        1	    207	AKT1	6	Nonsense_Mutation	Putative_Driver	    Tier 1  TCGA-A1-A0SB
//        2	    207	AKT1	6	Missense_Mutation	Putative_Passenger	Tier 2  TCGA-A1-A0SD
//        1	    208	AKT2	2	2		            <null>              <null>  TCGA-A1-A0SB
//        3	    208	AKT2	6	Splice_Site	        Putative_Passenger	Tier 1  TCGA-A1-A0SE
//        6	    672	BRCA1	6	Missense_Mutation	Putative_Passenger	Tier 2  TCGA-A1-A0SH
//        6	    672	BRCA1	6	Nonsense_Mutation	Putative_Driver	    Tier 1  TCGA-A1-A0SH
//        7	    672	BRCA1	6	Nonsense_Mutation	Putative_Driver	    Tier 2  TCGA-A1-A0SI
//        12	672	BRCA1	6	Splice_Site	        Putative_Passenger	Tier 1  TCGA-A1-A0SO
//        13	672	BRCA1	6	Splice_Site	        Putative_Driver	    Tier 1  TCGA-A1-A0SP

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
    }

    @Test
    public void getSampleMutationCount() throws Exception {

        cnaEventTypes = Select.none();
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId, entrezGeneIds, mutationEventTypes, cnaEventTypes, QueryElement.PASS);

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

        mutationEventTypes = Select.none();
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId, entrezGeneIds, mutationEventTypes, cnaEventTypes, QueryElement.PASS);

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

        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId, entrezGeneIds, mutationEventTypes, cnaEventTypes, QueryElement.PASS);

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
    public void getPatientMutationCount() throws Exception {

        cnaEventTypes = Select.none();
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId, entrezGeneIds, mutationEventTypes, cnaEventTypes, QueryElement.PASS);

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

        mutationEventTypes = Select.none();
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId, entrezGeneIds, mutationEventTypes, cnaEventTypes, QueryElement.PASS);

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
    public void getPatientMutationAndCnaCount() throws Exception {

        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId, entrezGeneIds, mutationEventTypes, cnaEventTypes, QueryElement.PASS);

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
    public void getSampleCnaCountLegacy() throws Exception {

        // FIXME: the CnaCountLegacy endpoint is different from the AlterationCount endpoint
        // because it returns a single additional value 'cytoband'. It would make sense to 
        // harmonize these endpoints (both or none return 'cytoband') and use the AlterationCount
        // endpoint for all counts. Let's discuss...
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(
            sampleIdToProfileId, entrezGeneIds, cnaEventTypes);

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
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            patientIdToProfileId, entrezGeneIds, cnaEventTypes);

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

        mutationEventTypes = Select.none();
        cnaEventTypes = Select.none();
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId, entrezGeneIds, mutationEventTypes, cnaEventTypes, QueryElement.PASS);

        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleAlterationCountsReturnsAllForMutationsAndCnaSelectorsInAll() {

        mutationEventTypes = Select.all();
        cnaEventTypes = Select.all();
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId, entrezGeneIds, mutationEventTypes, cnaEventTypes, QueryElement.PASS);

        Assert.assertEquals(3, result.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void disallowMutationTypesAndFusionSearchSamples() throws Exception {
        alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId, entrezGeneIds, mutationEventTypes, cnaEventTypes, QueryElement.ACTIVE);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void disallowMutationTypesAndMutationSearchSamples() throws Exception {
        alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId, entrezGeneIds, mutationEventTypes, cnaEventTypes, QueryElement.INACTIVE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void disallowMutationTypesAndFusionSearchPatients() throws Exception {
        alterationMyBatisRepository.getPatientAlterationCounts(
            sampleIdToProfileId, entrezGeneIds, mutationEventTypes, cnaEventTypes, QueryElement.ACTIVE);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void disallowMutationTypesAndMutationSearchPatients() throws Exception {
        alterationMyBatisRepository.getPatientAlterationCounts(
            sampleIdToProfileId, entrezGeneIds, mutationEventTypes, cnaEventTypes, QueryElement.INACTIVE);
    }

    @Test
    public void getSampleCountNullIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            null, entrezGeneIds, mutationEventTypes, cnaEventTypes, QueryElement.PASS);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleCountNullMutations() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId, entrezGeneIds, null, cnaEventTypes, QueryElement.PASS);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void getSampleCountNullCnas() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId, entrezGeneIds, mutationEventTypes, null, QueryElement.PASS);
        Assert.assertEquals(3, result.size());
    }

    @Test
    public void getPatientCountNullIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            null, entrezGeneIds, mutationEventTypes, cnaEventTypes, QueryElement.PASS);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCountNullMutations() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId, entrezGeneIds, null, cnaEventTypes, QueryElement.PASS);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void getPatientCountNullCnas() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId, entrezGeneIds, mutationEventTypes, null, QueryElement.PASS);
        Assert.assertEquals(3, result.size());
    }

    @Test
    public void getSampleCnaCountNullIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(
            null, entrezGeneIds, cnaEventTypes);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleCnaCountNullCnas() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(
            sampleIdToProfileId, entrezGeneIds, null);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCnaCountNullIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            null, entrezGeneIds, cnaEventTypes);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCnaCountNullCnas() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            patientIdToProfileId, entrezGeneIds, null);
        Assert.assertEquals(0, result.size());
    }
    
    @Test
    public void getSampleCountNullEntrezGeneIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId, null, mutationEventTypes, cnaEventTypes, QueryElement.PASS);
        Assert.assertEquals(0, result.size());
    }
    
    @Test
    public void getSampleCountEmptyEntrezGeneIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId, Select.none(), mutationEventTypes, cnaEventTypes, QueryElement.PASS);
        Assert.assertEquals(0, result.size());
    }
    
    @Test
    public void getSampleCountAllEntrezGeneIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId, Select.all(), mutationEventTypes, cnaEventTypes, QueryElement.PASS);
        Assert.assertEquals(3, result.size());
    }

    @Test
    public void getPatientCountNullEntrezGeneIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId, null, mutationEventTypes, cnaEventTypes, QueryElement.PASS);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCountEmptyEntrezGeneIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId, Select.none(), mutationEventTypes, cnaEventTypes, QueryElement.PASS);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCountAllEntrezGeneIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId, Select.all(), mutationEventTypes, cnaEventTypes, QueryElement.PASS);
        Assert.assertEquals(3, result.size());
    }

    @Test
    public void getSampleCnaCountNullEntrezGeneIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(
            sampleIdToProfileId, null, cnaEventTypes);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleCnaCountEmptyEntrezGeneIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(
            sampleIdToProfileId, Select.none(), cnaEventTypes);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleCnaCountAllEntrezGeneIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(
            sampleIdToProfileId, Select.all(), cnaEventTypes);
        Assert.assertEquals(3, result.size());
    }

    @Test
    public void getPatientCnaCountNullEntrezGeneIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            patientIdToProfileId, null, cnaEventTypes);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCnaCountEmptyEntrezGeneIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            patientIdToProfileId, Select.none(), cnaEventTypes);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCnaCountAllEntrezGeneIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            patientIdToProfileId, Select.all(), cnaEventTypes);
        Assert.assertEquals(3, result.size());
    }
    
}
