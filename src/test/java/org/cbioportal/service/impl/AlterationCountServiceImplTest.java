package org.cbioportal.service.impl;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationCountByStructuralVariant;
import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.CNA;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.MutationEventType;
import org.cbioportal.model.util.Select;
import org.cbioportal.persistence.AlterationRepository;
import org.cbioportal.persistence.MolecularProfileRepository;
import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.service.SignificantlyMutatedGeneService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.cbioportal.service.util.MolecularProfileUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AlterationCountServiceImplTest extends BaseServiceImplTest {

    private AlterationCountServiceImpl alterationCountService;
    @Mock
    private AlterationRepository alterationRepository;
    @Mock
    private AlterationEnrichmentUtil<AlterationCountByGene> alterationEnrichmentUtil;
    @Mock
    private AlterationEnrichmentUtil<CopyNumberCountByGene> alterationEnrichmentUtilCna;
    @Mock
    private AlterationEnrichmentUtil<AlterationCountByStructuralVariant> alterationEnrichmentUtilStructVar;
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
    List<AlterationCountByStructuralVariant> expectedStructuralVariantList;
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
        MockitoAnnotations.openMocks(this);

        alterationCountService = new AlterationCountServiceImpl(alterationRepository, alterationEnrichmentUtil,
            alterationEnrichmentUtilCna, alterationEnrichmentUtilStructVar, molecularProfileRepository);
        
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

        final AlterationCountByStructuralVariant alterationCountByStructuralVariant = new AlterationCountByStructuralVariant();
        alterationCountByStructuralVariant.setGene1EntrezGeneId(ENTREZ_GENE_ID_1);
        alterationCountByStructuralVariant.setGene2EntrezGeneId(ENTREZ_GENE_ID_2);
        alterationCountByStructuralVariant.setGene1HugoGeneSymbol(HUGO_GENE_SYMBOL_1);
        alterationCountByStructuralVariant.setGene2HugoGeneSymbol(HUGO_GENE_SYMBOL_2);
        expectedStructuralVariantList = Arrays.asList(alterationCountByStructuralVariant);
        
    }
    
    @Test
    public void getSampleAlterationGeneCounts() {

        // this mock tests correct argument types
        when(alterationRepository.getSampleAlterationGeneCounts(
            new HashSet<>(caseIdentifiers),
            entrezGeneIds,
            alterationFilter)).thenReturn(expectedCountByGeneList);

        Pair<List<AlterationCountByGene>, Long> result = alterationCountService.getSampleAlterationGeneCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter);
        
        verify(alterationEnrichmentUtil, times(1)).includeFrequencyForSamples(anyList(), anyList(), anyBoolean());

    }

    @Test
    public void getPatientAlterationGeneCounts() {

        // this mock tests correct argument types
        when(alterationRepository.getPatientAlterationGeneCounts(
            new HashSet<>(caseIdentifiers),
            entrezGeneIds,
            alterationFilter)).thenReturn(expectedCountByGeneList);

        Pair<List<AlterationCountByGene>, Long> result = alterationCountService.getPatientAlterationGeneCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter);

        verify(alterationEnrichmentUtil, times(1)).includeFrequencyForPatients(anyList(), anyList(), anyBoolean());
    }
    

    @Test
    public void getSampleMutationGeneCounts() {
        // this mock tests correct argument types
        when(alterationRepository.getSampleAlterationGeneCounts(
            new HashSet<>(caseIdentifiers),
            entrezGeneIds,
            alterationFilter)).thenReturn(expectedCountByGeneList);

        Pair<List<AlterationCountByGene>, Long> result = alterationCountService.getSampleMutationGeneCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter);

        Assert.assertEquals(expectedCountByGeneList, result.getFirst());

    }

    @Test
    public void getPatientMutationGeneCounts() throws MolecularProfileNotFoundException {

        // this mock tests correct argument types
        when(alterationRepository.getPatientAlterationGeneCounts(
            new HashSet<>(caseIdentifiers),
            entrezGeneIds,
            alterationFilter)).thenReturn(expectedCountByGeneList);

        Pair<List<AlterationCountByGene>, Long> result = alterationCountService.getPatientMutationGeneCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter);

        Assert.assertEquals(expectedCountByGeneList, result.getFirst());

    }

    @Test
    public void getSampleCnaGeneCounts() {

        // this mock tests correct argument types
        when(alterationRepository.getSampleCnaGeneCounts(
            new TreeSet<>(caseIdentifiers),
            entrezGeneIds,
            alterationFilter)).thenReturn(expectedCnaCountByGeneList);

        Pair<List<CopyNumberCountByGene>, Long> result = alterationCountService.getSampleCnaGeneCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter);

        verify(alterationEnrichmentUtilCna, times(1)).includeFrequencyForSamples(anyList(), anyList(), anyBoolean());
        Assert.assertEquals(expectedCnaCountByGeneList, result.getFirst());
        
    }

    @Test
    public void getPatientCnaGeneCounts() {

        // this mock tests correct argument types
        when(alterationRepository.getPatientCnaGeneCounts(
            new HashSet<>(caseIdentifiers),
            entrezGeneIds,
            alterationFilter)).thenReturn(expectedCnaCountByGeneList);


        Pair<List<CopyNumberCountByGene>, Long> result = alterationCountService.getPatientCnaGeneCounts(
            caseIdentifiers,
            entrezGeneIds,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter);

        verify(alterationEnrichmentUtilCna, times(1)).includeFrequencyForPatients(anyList(), anyList(), anyBoolean());
        Assert.assertEquals(expectedCnaCountByGeneList, result.getFirst());
    }

    @Test
    public void getSampleStructuralVariantCounts() {

        when(alterationRepository.getSampleStructuralVariantCounts(
            new TreeSet<>(caseIdentifiers),
            alterationFilter)).thenReturn(expectedStructuralVariantList);

        Pair<List<AlterationCountByStructuralVariant>, Long> result = alterationCountService.getSampleStructuralVariantCounts(
            caseIdentifiers,
            includeFrequency,
            includeMissingAlterationsFromGenePanel,
            alterationFilter);

        verify(alterationEnrichmentUtilStructVar, times(1)).includeFrequencyForSamples(anyList(), anyList(), anyBoolean());
        Assert.assertEquals(expectedStructuralVariantList, result.getFirst());

    }
}