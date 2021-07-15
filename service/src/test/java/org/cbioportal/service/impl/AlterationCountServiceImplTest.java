package org.cbioportal.service.impl;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.*;
import org.cbioportal.model.QueryElement;
import org.cbioportal.model.util.Select;
import org.cbioportal.persistence.AlterationRepository;
import org.cbioportal.persistence.MolecularProfileRepository;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.cbioportal.service.util.MolecularProfileUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AlterationCountServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private AlterationCountServiceImpl alterationCountService;
    @Mock
    private AlterationRepository alterationRepository;
    @Mock
    private AlterationEnrichmentUtil<AlterationCountByGene> alterationEnrichmentUtil;
    @Mock
    private AlterationEnrichmentUtil<CopyNumberCountByGene> alterationEnrichmentUtilCna;
    @Spy
    @InjectMocks
    private MolecularProfileUtil molecularProfileUtil;
    @Mock
    private MolecularProfileRepository molecularProfileRepository;

    List<MolecularProfileCaseIdentifier> caseIdentifiers = Arrays.asList(new MolecularProfileCaseIdentifier("A", MOLECULAR_PROFILE_ID));
    Select<MutationEventType> mutationEventTypes = Select.byValues(Arrays.asList(MutationEventType.missense_mutation));
    Select<CNA> cnaEventTypes = Select.byValues(Arrays.asList(CNA.AMP));
    Select<Integer> entrezGeneIds = Select.all();
    boolean includeFrequency = true;
    boolean includeMissingAlterationsFromGenePanel = false;
    List<AlterationCountByGene> expectedCountByGeneList;
    List<CopyNumberCountByGene> expectedCnaCountByGeneList;
    AlterationFilter alterationFilter = new AlterationFilter(
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

    @Before
    public void setup() {
        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setStableId(MOLECULAR_PROFILE_ID);
        molecularProfile.setCancerStudyIdentifier(STUDY_ID);

        when(molecularProfileRepository.getMolecularProfiles(
            Collections.singleton(MOLECULAR_PROFILE_ID),
            "SUMMARY"
        )).thenReturn(Arrays.asList(molecularProfile));

        AlterationCountByGene alterationCountByGene = new AlterationCountByGene();
        alterationCountByGene.setEntrezGeneId(ENTREZ_GENE_ID_1);
        expectedCountByGeneList = Arrays.asList(alterationCountByGene);

        CopyNumberCountByGene copyNumberCountByGene = new CopyNumberCountByGene();
        copyNumberCountByGene.setEntrezGeneId(ENTREZ_GENE_ID_1);
        copyNumberCountByGene.setAlteration(2);
        expectedCnaCountByGeneList = Arrays.asList(copyNumberCountByGene);
    }
    
    @Test
    public void getSampleAlterationCounts() {

        // this mock tests correct argument types
        when(alterationRepository.getSampleAlterationCounts(
            new HashSet<>(caseIdentifiers),
            entrezGeneIds,
            QueryElement.PASS,
            alterationFilter)).thenReturn(expectedCountByGeneList);

        Pair<List<AlterationCountByGene>, Long> result = alterationCountService.getSampleAlterationCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            QueryElement.PASS,
            alterationFilter);
        
        verify(alterationEnrichmentUtil, times(1)).includeFrequencyForSamples(anyList(), anyList(), anyBoolean());

    }

    @Test
    public void getPatientAlterationCounts() {

        // this mock tests correct argument types
        when(alterationRepository.getPatientAlterationCounts(
            caseIdentifiers,
            entrezGeneIds,
            QueryElement.PASS,
            alterationFilter)).thenReturn(expectedCountByGeneList);

        Pair<List<AlterationCountByGene>, Long> result = alterationCountService.getPatientAlterationCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            QueryElement.PASS,
            alterationFilter);

        verify(alterationEnrichmentUtil, times(1)).includeFrequencyForPatients(anyList(), anyList(), anyBoolean());
    }
    

    @Test
    public void getSampleMutationCounts() {
        // this mock tests correct argument types
        when(alterationRepository.getSampleAlterationCounts(
            new HashSet<>(caseIdentifiers),
            entrezGeneIds,
            QueryElement.INACTIVE,
            alterationFilter)).thenReturn(expectedCountByGeneList);

        Pair<List<AlterationCountByGene>, Long> result = alterationCountService.getSampleMutationCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter);

        Assert.assertEquals(expectedCountByGeneList, result.getFirst());

    }

    @Test
    public void getPatientMutationCounts() throws MolecularProfileNotFoundException {

        // this mock tests correct argument types
        when(alterationRepository.getPatientAlterationCounts(
            caseIdentifiers,
            entrezGeneIds,
            QueryElement.INACTIVE,
            alterationFilter)).thenReturn(expectedCountByGeneList);

        Pair<List<AlterationCountByGene>, Long> result = alterationCountService.getPatientMutationCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter);

        Assert.assertEquals(expectedCountByGeneList, result.getFirst());

    }

    @Test
    public void getSampleFusionCounts() {

        QueryElement searchFusions = QueryElement.ACTIVE;

        // this mock tests correct argument types
        when(alterationRepository.getSampleAlterationCounts(
            new HashSet<>(caseIdentifiers),
            entrezGeneIds,
            searchFusions,
            alterationFilter
        )).thenReturn(expectedCountByGeneList);
        
        Pair<List<AlterationCountByGene>, Long> result = alterationCountService.getSampleStructuralVariantCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter);

        Assert.assertEquals(expectedCountByGeneList, result.getFirst());
    }

    @Test
    public void getPatientFusionCounts() {

        QueryElement searchFusions = QueryElement.ACTIVE;

        // this mock tests correct argument types
        when(alterationRepository.getPatientAlterationCounts(
            caseIdentifiers,
            entrezGeneIds,
            searchFusions,
            alterationFilter)).thenReturn(expectedCountByGeneList);

        Pair<List<AlterationCountByGene>, Long> result = alterationCountService.getPatientStructuralVariantCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter);

        Assert.assertEquals(expectedCountByGeneList, result.getFirst());
    }

    @Test
    public void getSampleCnaCounts() {

        // this mock tests correct argument types
        when(alterationRepository.getSampleCnaCounts(
            new TreeSet<>(caseIdentifiers),
            entrezGeneIds,
            alterationFilter)).thenReturn(expectedCnaCountByGeneList);

        Pair<List<CopyNumberCountByGene>, Long> result = alterationCountService.getSampleCnaCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter);

        verify(alterationEnrichmentUtilCna, times(1)).includeFrequencyForSamples(anyList(), anyList(), anyBoolean());
        Assert.assertEquals(expectedCnaCountByGeneList, result.getFirst());
        
    }

    @Test
    public void getPatientCnaCounts() {

        // this mock tests correct argument types
        when(alterationRepository.getPatientCnaCounts(
            caseIdentifiers,
            entrezGeneIds,
            alterationFilter)).thenReturn(expectedCnaCountByGeneList);


        Pair<List<CopyNumberCountByGene>, Long> result = alterationCountService.getPatientCnaCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter);

        verify(alterationEnrichmentUtilCna, times(1)).includeFrequencyForPatients(anyList(), anyList(), anyBoolean());
        Assert.assertEquals(expectedCnaCountByGeneList, result.getFirst());
    }
}