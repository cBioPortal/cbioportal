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
    Select<Integer> entrezGeneIds;
    Select<String> tiers = Select.none();
    boolean includeUnknownTier = false;
    boolean includeDriver = false;
    boolean includeVUS = false;
    boolean includeUnknownOncogenicity = false;
    boolean includeGermline = false;
    boolean includeSomatic = false;
    boolean includeUnknownStatus = false;

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
        
        tiers = Select.none();
        includeUnknownTier = false;
        includeDriver = false;
        includeVUS = false;
        includeUnknownOncogenicity = false;
        includeGermline = false;
        includeSomatic = false;
        includeUnknownStatus = false;
    }

    @Test
    public void getSampleMutationCount() throws Exception {

        includeDriver = true;
        includeVUS = true;
        includeUnknownOncogenicity = true;
        includeGermline = true;
        includeSomatic = true;
        includeUnknownStatus = true;
        cnaEventTypes = Select.none();
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.PASS,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier,
            includeGermline,
            includeSomatic,
            includeUnknownStatus);

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

        includeDriver = true;
        includeVUS = true;
        includeUnknownOncogenicity = true;
        includeGermline = true;
        includeSomatic = true;
        includeUnknownStatus = true;
        mutationEventTypes = Select.none();
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.PASS,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier,
            includeGermline,
            includeSomatic,
            includeUnknownStatus);

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

        includeDriver = true;
        includeVUS = true;
        includeUnknownOncogenicity = true;
        includeGermline = true;
        includeSomatic = true;
        includeUnknownStatus = true;
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.PASS,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier,
            includeGermline,
            includeSomatic,
            includeUnknownStatus);

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
    public void getSampleMutationCountFilterFusions() throws Exception {

        boolean includeDriver = true;
        boolean includeVUS = true;
        boolean includeUnknownOncogenicity = true;
        boolean includeGermline = true;
        boolean includeSomatic = true;
        boolean includeUnknownStatus = true;
        cnaEventTypes = Select.none();
        mutationEventTypes = Select.all();
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.ACTIVE,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier,
            includeGermline,
            includeSomatic,
            includeUnknownStatus);
        // there are no fusion mutations in the test db
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientMutationCount() throws Exception {

        boolean includeDriver = true;
        boolean includeVUS = true;
        boolean includeUnknownOncogenicity = true;
        boolean includeGermline = true;
        boolean includeSomatic = true;
        boolean includeUnknownStatus = true;
        cnaEventTypes = Select.none();
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.PASS,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier,
            includeGermline,
            includeSomatic,
            includeUnknownStatus);

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

        boolean includeDriver = true;
        boolean includeVUS = true;
        boolean includeUnknownOncogenicity = true;
        mutationEventTypes = Select.none();
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.PASS,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier,
            includeGermline,
            includeSomatic,
            includeUnknownStatus);

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
        includeDriver = true;
        includeVUS = true;
        includeUnknownOncogenicity = true;
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(
            sampleIdToProfileId, entrezGeneIds, cnaEventTypes, includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier);

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
        includeDriver = true;
        includeVUS = true;
        includeUnknownOncogenicity = true;
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            patientIdToProfileId, entrezGeneIds, cnaEventTypes, includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier);

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

        boolean includeDriver = true;
        boolean includeVUS = true;
        boolean includeUnknownOncogenicity = true;
        boolean includeGermline = true;
        boolean includeSomatic = true;
        boolean includeUnknownStatus = true;
        mutationEventTypes = Select.none();
        cnaEventTypes = Select.none();
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.PASS,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier,
            includeGermline,
            includeSomatic,
            includeUnknownStatus);

        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleAlterationCountsReturnsAllForMutationsAndCnaSelectorsInAll() {

        boolean includeDriver = true;
        boolean includeVUS = true;
        boolean includeUnknownOncogenicity = true;
        boolean includeGermline = true;
        boolean includeSomatic = true;
        boolean includeUnknownStatus = true;
        mutationEventTypes = Select.all();
        cnaEventTypes = Select.all();
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.PASS,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier,
            includeGermline,
            includeSomatic,
            includeUnknownStatus);

        Assert.assertEquals(3, result.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void disallowMutationTypesAndFusionSearchSamples() throws Exception {
        alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId, entrezGeneIds, mutationEventTypes, cnaEventTypes, QueryElement.ACTIVE,
            true, true, true, Select.all(), true,
            true, true, true);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void disallowMutationTypesAndMutationSearchSamples() throws Exception {
        alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId, entrezGeneIds, mutationEventTypes, cnaEventTypes, QueryElement.INACTIVE,
            true, true, true, Select.all(), true,
            true, true, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void disallowMutationTypesAndFusionSearchPatients() throws Exception {
        alterationMyBatisRepository.getPatientAlterationCounts(
            sampleIdToProfileId, entrezGeneIds, mutationEventTypes, cnaEventTypes, QueryElement.ACTIVE,
            true, true, true, Select.all(), true,
            true, true, true);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void disallowMutationTypesAndMutationSearchPatients() throws Exception {
        alterationMyBatisRepository.getPatientAlterationCounts(
            sampleIdToProfileId, entrezGeneIds, mutationEventTypes, cnaEventTypes, QueryElement.INACTIVE,
            true, true, true, Select.all(), true,
            true, true, true);
    }

    @Test
    public void getSampleCountNullIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            null, entrezGeneIds, mutationEventTypes, cnaEventTypes, QueryElement.PASS,
            true, true, true, Select.all(), true,
            true, true, true);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleCountNullMutations() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId, entrezGeneIds, null, cnaEventTypes, QueryElement.PASS,
            true, true, true, Select.all(), true,
            true, true, true);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void getSampleCountNullCnas() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId, entrezGeneIds, mutationEventTypes, null, QueryElement.PASS,
            true, true, true, Select.all(), true,
            true, true, true);
        Assert.assertEquals(3, result.size());
    }

    @Test
    public void getPatientCountNullIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            null, entrezGeneIds, mutationEventTypes, cnaEventTypes, QueryElement.PASS,
            true, true, true, Select.all(), true,
            true, true, true);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCountNullMutations() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId, entrezGeneIds, null, cnaEventTypes, QueryElement.PASS,
            true, true, true, Select.all(), true,
            true, true, true);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void getPatientCountNullCnas() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId, entrezGeneIds, mutationEventTypes, null, QueryElement.PASS,
            true, true, true, Select.all(), true,
            true, true, true);
        Assert.assertEquals(3, result.size());
    }

    @Test
    public void getSampleCnaCountNullIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(
            null, entrezGeneIds, cnaEventTypes,
            true, true, true, Select.all(), true);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleCnaCountNullCnas() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(
            sampleIdToProfileId, entrezGeneIds, null,
            true, true, true, Select.all(), true);
        Assert.assertEquals(0, result.size());
    }


    @Test
    public void getSampleCountIncludeOnlyDriver() throws Exception {

        boolean includeDriver = true;
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.PASS,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier,
            includeGermline,
            includeSomatic,
            includeUnknownStatus);

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

        boolean includeVUS = true;
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.PASS,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier, includeGermline,
            includeSomatic,
            includeUnknownStatus);

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

        boolean includeUnknownOncogenicity = true;
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.PASS,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier, includeGermline,
            includeSomatic,
            includeUnknownStatus);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleCountIncludeAnyAndUnknownAnnotation() throws Exception {

        includeDriver = true;
        includeVUS = true;
        includeUnknownOncogenicity = true;
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.PASS,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier,
            includeGermline,
            includeSomatic,
            includeUnknownStatus);

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
        tiers = Select.byValues(Arrays.asList("Tier 2"));
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.PASS,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier, includeGermline,
            includeSomatic,
            includeUnknownStatus);

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

        tiers = Select.all();
        includeUnknownTier = true;
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.PASS,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier,
            includeGermline,
            includeSomatic,
            includeUnknownStatus);

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

        tiers = Select.all();
        includeUnknownTier = true;
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.PASS,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier,
            includeGermline,
            includeSomatic,
            includeUnknownStatus);

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

        includeUnknownTier = true;
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.PASS,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier,
            includeGermline,
            includeSomatic,
            includeUnknownStatus);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientMutationAndCnaCount() throws Exception {

        boolean includeDriver = true;
        boolean includeVUS = true;
        boolean includeUnknownOncogenicity = true;
        boolean includeGermline = true;
        boolean includeSomatic = true;
        boolean includeUnknownStatus = true;
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.PASS,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier, includeGermline,
            includeSomatic,
            includeUnknownStatus);

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

        boolean includeGermline = true;
        cnaEventTypes = Select.none();
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.PASS,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier,
            includeGermline,
            includeSomatic,
            includeUnknownStatus);
        // all mutations in testSql.sql are Germline mutations
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
    public void getPatientMutationCountIncludeOnlySomatic() throws Exception {

        boolean includeSomatic = true;
        cnaEventTypes = Select.none();
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.PASS,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier, includeGermline,
            includeSomatic,
            includeUnknownStatus);
        // all mutations in testSql.sql are Germline mutations
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientMutationCountIncludeOnlyUnknownStatus() throws Exception {

        boolean includeUnknownStatus = true;
        cnaEventTypes = Select.none();
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.PASS,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier, includeGermline,
            includeSomatic,
            includeUnknownStatus);
        // all mutations in testSql.sql are Germline mutations
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCountIncludeAnyAndUnknowStatus() throws Exception {

        includeGermline = true;
        includeSomatic = true;
        includeUnknownStatus = true;
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.PASS,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier,
            includeGermline,
            includeSomatic,
            includeUnknownStatus);

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

        boolean includeDriver = true;
        boolean includeVUS = true;
        boolean includeUnknownOncogenicity = true;
        boolean includeGermline = true;
        boolean includeSomatic = true;
        boolean includeUnknownStatus = true;
        cnaEventTypes = Select.none();
        mutationEventTypes = Select.all();
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.ACTIVE,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier,
            includeGermline,
            includeSomatic,
            includeUnknownStatus);
        // there are no fusion mutations in the test db
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCountIncludeOnlyDriver() throws Exception {

        boolean includeDriver = true;
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.PASS,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier,
            includeGermline,
            includeSomatic,
            includeUnknownStatus);

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

        boolean includeVUS = true;
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.PASS,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier,
            includeGermline,
            includeSomatic,
            includeUnknownStatus);

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
        tiers = Select.byValues(Arrays.asList("Tier 2"));
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.PASS,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier, includeGermline,
            includeSomatic,
            includeUnknownStatus);

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

        includeUnknownTier = true;
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes,
            QueryElement.PASS,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            tiers,
            includeUnknownTier,
            includeGermline,
            includeSomatic,
            includeUnknownStatus);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleCnaCountLegacyOnlyDriver() throws Exception {

        includeDriver = true;
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(
            sampleIdToProfileId, entrezGeneIds, cnaEventTypes, includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        Assert.assertEquals((Integer) 1, result207.getTotalCount());
        Assert.assertEquals((Integer) 1, result207.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleCnaCountLegacyOnlyVUS() throws Exception {

        includeVUS = true;
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(
            sampleIdToProfileId, entrezGeneIds, cnaEventTypes, includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        Assert.assertEquals((Integer) 1, result207.getTotalCount());
        Assert.assertEquals((Integer) 1, result207.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleCnaCountLegacyOnlyUnknownOncogenicity() throws Exception {

        includeUnknownOncogenicity = true;
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(
            sampleIdToProfileId, entrezGeneIds, cnaEventTypes, includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleCnaCountLegacyOnlyUnknownTier() throws Exception {

        includeUnknownTier = true;
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(
            sampleIdToProfileId, entrezGeneIds, cnaEventTypes, includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getSampleCnaCountLegacyOnlyTier2() throws Exception {

        tiers = Select.byValues(Arrays.asList("Tier 2"));
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(
            sampleIdToProfileId, entrezGeneIds, cnaEventTypes, includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        Assert.assertEquals((Integer) 1, result207.getTotalCount());
        Assert.assertEquals((Integer) 1, result207.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientCnaCountLegacyOnlyDriver() throws Exception {

        includeDriver = true;
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            patientIdToProfileId, entrezGeneIds, cnaEventTypes, includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        Assert.assertEquals((Integer) 1, result207.getTotalCount());
        Assert.assertEquals((Integer) 1, result207.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientCnaCountLegacyOnlyVUS() throws Exception {

        includeVUS = true;
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            patientIdToProfileId, entrezGeneIds, cnaEventTypes, includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        Assert.assertEquals((Integer) 1, result207.getTotalCount());
        Assert.assertEquals((Integer) 1, result207.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientCnaCountLegacyOnlyUnknownOncogenicity() throws Exception {

        includeUnknownOncogenicity = true;
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            patientIdToProfileId, entrezGeneIds, cnaEventTypes, includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientCnaCountLegacyOnlyUnknownTier() throws Exception {

        includeUnknownTier = true;
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            patientIdToProfileId, entrezGeneIds, cnaEventTypes, includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result208 = result.stream().filter(r -> r.getEntrezGeneId() == 208).findFirst().get();
        Assert.assertEquals((Integer) 1, result208.getTotalCount());
        Assert.assertEquals((Integer) 1, result208.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientCnaCountLegacyOnlyTier2() throws Exception {

        tiers = Select.byValues(Arrays.asList("Tier 2"));
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            patientIdToProfileId, entrezGeneIds, cnaEventTypes, includeDriver, includeVUS, includeUnknownOncogenicity, tiers, includeUnknownTier);

        Assert.assertEquals(1, result.size());
        AlterationCountByGene result207 = result.stream().filter(r -> r.getEntrezGeneId() == 207).findFirst().get();
        Assert.assertEquals((Integer) 1, result207.getTotalCount());
        Assert.assertEquals((Integer) 1, result207.getNumberOfAlteredCases());
    }


    @Test
    public void getPatientCnaCountNullIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            null, entrezGeneIds, cnaEventTypes, true, true, true, Select.all(), true);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCnaCountNullCnas() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            patientIdToProfileId, entrezGeneIds, null, true, true, true, Select.all(), true);
        Assert.assertEquals(0, result.size());
    }
    
    @Test
    public void getSampleCountNullEntrezGeneIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId, null, mutationEventTypes, cnaEventTypes, QueryElement.PASS,
            true, true, true, Select.all(), true,
        true, true, true);
        Assert.assertEquals(0, result.size());
    }
    
    @Test
    public void getSampleCountEmptyEntrezGeneIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId, Select.none(), mutationEventTypes, cnaEventTypes, QueryElement.PASS,
            true, true, true, Select.all(), true,
            true, true, true);
        Assert.assertEquals(0, result.size());
    }
    
    @Test
    public void getSampleCountAllEntrezGeneIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getSampleAlterationCounts(
            sampleIdToProfileId, Select.all(), mutationEventTypes, cnaEventTypes, QueryElement.PASS,
            true, true, true, Select.all(), true,
            true, true, true);
        Assert.assertEquals(3, result.size());
    }

    @Test
    public void getPatientCountNullEntrezGeneIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId, null, mutationEventTypes, cnaEventTypes, QueryElement.PASS,
            true, true, true, Select.all(), true,
            true, true, true);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCountEmptyEntrezGeneIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId, Select.none(), mutationEventTypes, cnaEventTypes, QueryElement.PASS, 
            true, true, true, Select.all(), true,
            true, true, true);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCountAllEntrezGeneIds() throws Exception {
        List<AlterationCountByGene> result = alterationMyBatisRepository.getPatientAlterationCounts(
            patientIdToProfileId, Select.all(), mutationEventTypes, cnaEventTypes, QueryElement.PASS, 
            true, true, true, Select.all(), true,
            true, true, true);
        Assert.assertEquals(3, result.size());
    }

    @Test
    public void getSampleCnaCountNullEntrezGeneIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(
            sampleIdToProfileId, null, cnaEventTypes, true, true,
            true, Select.all(), true);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleCnaCountEmptyEntrezGeneIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(
            sampleIdToProfileId, Select.none(), cnaEventTypes, true, true,
            true, Select.all(), true);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getSampleCnaCountAllEntrezGeneIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getSampleCnaCounts(
            sampleIdToProfileId, Select.all(), cnaEventTypes, true, true,
            true, Select.all(), true);
        Assert.assertEquals(3, result.size());
    }

    @Test
    public void getPatientCnaCountNullEntrezGeneIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            patientIdToProfileId, null, cnaEventTypes, true, true,
            true, Select.all(), true);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCnaCountEmptyEntrezGeneIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            patientIdToProfileId, Select.none(), cnaEventTypes, true, true,
            true, Select.all(), true);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getPatientCnaCountAllEntrezGeneIds() throws Exception {
        List<CopyNumberCountByGene> result = alterationMyBatisRepository.getPatientCnaCounts(
            patientIdToProfileId, Select.all(), cnaEventTypes, true, true,
            true, Select.all(), true);
        Assert.assertEquals(3, result.size());
    }
    
}
