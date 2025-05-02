package org.cbioportal.legacy.service.impl;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.AlterationCountByStructuralVariant;
import org.cbioportal.legacy.model.AlterationFilter;
import org.cbioportal.legacy.model.CNA;
import org.cbioportal.legacy.model.CopyNumberCountByGene;
import org.cbioportal.legacy.model.GenePanel;
import org.cbioportal.legacy.model.GenePanelData;
import org.cbioportal.legacy.model.GenePanelToGene;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;
import org.cbioportal.legacy.model.MutationEventType;
import org.cbioportal.legacy.model.util.Select;
import org.cbioportal.legacy.persistence.AlterationRepository;
import org.cbioportal.legacy.persistence.MolecularProfileRepository;
import org.cbioportal.legacy.service.GenePanelService;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.service.util.AlterationEnrichmentUtil;
import org.cbioportal.legacy.service.util.MolecularProfileUtil;
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

import static org.junit.Assert.assertEquals;
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
    @Mock
    private GenePanelService genePanelService;

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
            alterationEnrichmentUtilCna, alterationEnrichmentUtilStructVar, molecularProfileRepository, genePanelService);
        
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
            alterationFilter,
            true);
        
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
            alterationFilter,
            true);

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

        assertEquals(expectedCountByGeneList, result.getFirst());

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

        assertEquals(expectedCountByGeneList, result.getFirst());

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
        assertEquals(expectedCnaCountByGeneList, result.getFirst());
        
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
        assertEquals(expectedCnaCountByGeneList, result.getFirst());
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
        assertEquals(expectedStructuralVariantList, result.getFirst());

    }

    @Test
    public void testFilterOffPanelAlterations() {
        // Constants for testing
        final int GENE_ON_PANEL1 = 207;   // AKT1 - On TESTPANEL1
        final int GENE_ON_PANEL2 = 208;   // AKT2 - On TESTPANEL2
        final String PANEL_ID_1 = "TESTPANEL1";

        // Setup molecular profile case identifiers for one sample on panel 1
        List<MolecularProfileCaseIdentifier> caseIds = Collections.singletonList(
            new MolecularProfileCaseIdentifier("SAMPLE1", MOLECULAR_PROFILE_ID)
        );

        // Setup gene panel data - sample uses PANEL_ID_1
        GenePanelData panelData = new GenePanelData();
        panelData.setSampleId("SAMPLE1");
        panelData.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        panelData.setGenePanelId(PANEL_ID_1);

        when(genePanelService.fetchGenePanelDataInMultipleMolecularProfiles(caseIds))
            .thenReturn(Collections.singletonList(panelData));

        // Setup panel with only GENE_ON_PANEL1
        GenePanel panel = new GenePanel();
        panel.setStableId(PANEL_ID_1);
        GenePanelToGene genePanelToGene = new GenePanelToGene();
        genePanelToGene.setEntrezGeneId(GENE_ON_PANEL1);
        panel.setGenes(Collections.singletonList(genePanelToGene));

        when(genePanelService.fetchGenePanels(Collections.singletonList(PANEL_ID_1), "DETAILED"))
            .thenReturn(Collections.singletonList(panel));

        // Create alterations for both genes
        AlterationCountByGene alterationOnPanel = new AlterationCountByGene();
        alterationOnPanel.setEntrezGeneId(GENE_ON_PANEL1);
        alterationOnPanel.setHugoGeneSymbol("AKT1");
        alterationOnPanel.setTotalCount(5);

        AlterationCountByGene alterationOffPanel = new AlterationCountByGene();
        alterationOffPanel.setEntrezGeneId(GENE_ON_PANEL2);
        alterationOffPanel.setHugoGeneSymbol("AKT2");
        alterationOffPanel.setTotalCount(4);

        List<AlterationCountByGene> alterations = Arrays.asList(
            alterationOnPanel, alterationOffPanel
        );

        // Mock the repository to return our prepared alterations
        when(alterationRepository.getSampleAlterationGeneCounts(
            new HashSet<>(caseIds), entrezGeneIds, alterationFilter)).thenReturn(alterations);

        // Mock the gene check - GENE_ON_PANEL2 is on another panel (not associated with this sample)
        when(genePanelService.findGeneIdsAssociatedWithAnyPanel(Collections.singleton(GENE_ON_PANEL2)))
            .thenReturn(Collections.singleton(GENE_ON_PANEL2));

        // Test the filter behavior
        Pair<List<AlterationCountByGene>, Long> result =
            alterationCountService.getSampleAlterationGeneCounts(
                caseIds, entrezGeneIds, false, false, alterationFilter, false);

        // Should only keep GENE_ON_PANEL1 (on the associated panel)
        // Should filter out GENE_ON_PANEL2 (on another panel but not on the associated panel)
        assertEquals(1, result.getFirst().size());
        assertEquals(GENE_ON_PANEL1, result.getFirst().getFirst().getEntrezGeneId().intValue());
    }
}