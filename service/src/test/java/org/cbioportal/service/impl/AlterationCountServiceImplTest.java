package org.cbioportal.service.impl;

import org.cbioportal.model.*;
import org.cbioportal.model.QueryElement;
import org.cbioportal.model.util.Select;
import org.cbioportal.persistence.AlterationRepository;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

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

    List<MolecularProfileCaseIdentifier> caseIdentifiers = Arrays.asList(new MolecularProfileCaseIdentifier("A", MOLECULAR_PROFILE_ID));
    Select<MutationEventType> mutationEventTypes = Select.byValues(Arrays.asList(MutationEventType.missense_mutation));
    Select<CNA> cnaEventTypes = Select.byValues(Arrays.asList(CNA.AMP));
    Select<Integer> entrezGeneIds = Select.all();
    boolean includeFrequency = false;
    boolean includeMissingAlterationsFromGenePanel = false;
    List<AlterationCountByGene> expectedCountByGeneList = Arrays.asList(new AlterationCountByGene());
    List<CopyNumberCountByGene> expectedCnaCountByGeneList = Arrays.asList(new CopyNumberCountByGene());
    
    @Test
    public void getSampleAlterationCounts() {

        boolean includeFrequency = true;

        // this mock tests correct argument types
        when(alterationRepository.getSampleAlterationCounts(
            caseIdentifiers,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes, QueryElement.PASS)).thenReturn(expectedCountByGeneList);

        List<AlterationCountByGene> result = alterationCountService.getSampleAlterationCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            mutationEventTypes,
            cnaEventTypes, QueryElement.PASS
        );
        
        verify(alterationEnrichmentUtil, times(1)).includeFrequencyForSamples(anyList(), anyList(), anyBoolean());

    }

    @Test
    public void getPatientAlterationCounts() {

        boolean includeFrequency = true;

        // this mock tests correct argument types
        when(alterationRepository.getPatientAlterationCounts(
            caseIdentifiers,
            entrezGeneIds,
            mutationEventTypes,
            cnaEventTypes, QueryElement.PASS)).thenReturn(expectedCountByGeneList);

        List<AlterationCountByGene> result = alterationCountService.getPatientAlterationCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            mutationEventTypes,
            cnaEventTypes, QueryElement.PASS
        );

        verify(alterationEnrichmentUtil, times(1)).includeFrequencyForPatients(anyList(), anyList(), anyBoolean());
    }
    

    @Test
    public void getSampleMutationCounts() {
        // this mock tests correct argument types
        when(alterationRepository.getSampleAlterationCounts(
            caseIdentifiers,
            entrezGeneIds,
            mutationEventTypes,
            Select.none(), QueryElement.INACTIVE)).thenReturn(expectedCountByGeneList);

        List<AlterationCountByGene> result = alterationCountService.getSampleMutationCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            mutationEventTypes);

        Assert.assertEquals(expectedCountByGeneList, result);

    }

    @Test
    public void getPatientMutationCounts() throws MolecularProfileNotFoundException {

        // this mock tests correct argument types
        when(alterationRepository.getPatientAlterationCounts(
            caseIdentifiers,
            entrezGeneIds,
            mutationEventTypes,
            Select.none(), QueryElement.INACTIVE)).thenReturn(expectedCountByGeneList);

        List<AlterationCountByGene> result = alterationCountService.getPatientMutationCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            mutationEventTypes);

        Assert.assertEquals(expectedCountByGeneList, result);

    }

    @Test
    public void getSampleFusionCounts() {
        
        boolean searchFusions = true;

        // this mock tests correct argument types
        when(alterationRepository.getSampleAlterationCounts(
            caseIdentifiers,
            entrezGeneIds,
            mutationEventTypes,
            Select.none(), QueryElement.ACTIVE)).thenReturn(expectedCountByGeneList);

        List<AlterationCountByGene> result = alterationCountService.getSampleFusionCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            mutationEventTypes);

        Assert.assertEquals(expectedCountByGeneList, result);
    }

    @Test
    public void getPatientFusionCounts() {
        
        boolean searchFusions = true;

        // this mock tests correct argument types
        when(alterationRepository.getPatientAlterationCounts(
            caseIdentifiers,
            entrezGeneIds,
            mutationEventTypes,
            Select.none(),
            QueryElement.ACTIVE)).thenReturn(expectedCountByGeneList);

        List<AlterationCountByGene> result = alterationCountService.getPatientFusionCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            mutationEventTypes);

        Assert.assertEquals(expectedCountByGeneList, result);
    }

    @Test
    public void getSampleCnaCounts() {

        boolean includeFrequency = true;

        // this mock tests correct argument types
        when(alterationRepository.getSampleCnaCounts(
            caseIdentifiers,
            entrezGeneIds,
            cnaEventTypes)).thenReturn(expectedCnaCountByGeneList);

        List<CopyNumberCountByGene> result = alterationCountService.getSampleCnaCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            cnaEventTypes);

        verify(alterationEnrichmentUtilCna, times(1)).includeFrequencyForSamples(anyList(), anyList(), anyBoolean());
        Assert.assertEquals(expectedCnaCountByGeneList, result);
        
    }

    @Test
    public void getPatientCnaCounts() {

        boolean includeFrequency = true;

        // this mock tests correct argument types
        when(alterationRepository.getPatientCnaCounts(
            caseIdentifiers,
            entrezGeneIds,
            cnaEventTypes)).thenReturn(expectedCnaCountByGeneList);


        List<CopyNumberCountByGene> result = alterationCountService.getPatientCnaCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            cnaEventTypes);

        verify(alterationEnrichmentUtilCna, times(1)).includeFrequencyForPatients(anyList(), anyList(), anyBoolean());
        Assert.assertEquals(expectedCnaCountByGeneList, result);
    }
}