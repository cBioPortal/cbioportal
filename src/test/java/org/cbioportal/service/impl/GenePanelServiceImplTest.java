package org.cbioportal.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.cbioportal.model.*;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GenePanelRepository;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.exception.GenePanelNotFoundException;
import org.cbioportal.service.util.MolecularProfileUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GenePanelServiceImplTest extends BaseServiceImplTest {
    
    @InjectMocks
    private GenePanelServiceImpl genePanelService;
    
    @Mock
    private GenePanelRepository genePanelRepository;
    @Mock
    private MolecularProfileService molecularProfileService;
    @Spy
    @InjectMocks
    private MolecularProfileUtil molecularProfileUtil;
    
    @Test
    public void getAllGenePanelsSummaryProjection() throws Exception {

        List<GenePanel> expectedGenePanelList = new ArrayList<>();

        Mockito.when(genePanelRepository.getAllGenePanels("SUMMARY", PAGE_SIZE, PAGE_NUMBER, SORT,
            DIRECTION)).thenReturn(expectedGenePanelList);
        
        List<GenePanel> result = genePanelService.getAllGenePanels("SUMMARY", PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedGenePanelList, result);
    }

    @Test
    public void getAllGenePanelsDetailedProjection() throws Exception {

        List<GenePanel> expectedGenePanelList = new ArrayList<>();
        GenePanel genePanel = new GenePanel();
        genePanel.setStableId(GENE_PANEL_ID);
        expectedGenePanelList.add(genePanel);
        List<GenePanelToGene> expectedGenePanelToGeneList = new ArrayList<>();
        GenePanelToGene genePanelToGene = new GenePanelToGene();
        genePanelToGene.setGenePanelId(GENE_PANEL_ID);
        expectedGenePanelToGeneList.add(genePanelToGene);

        Mockito.when(genePanelRepository.getAllGenePanels("DETAILED", PAGE_SIZE, PAGE_NUMBER, SORT,
            DIRECTION)).thenReturn(expectedGenePanelList);

        Mockito.when(genePanelRepository.getGenesOfPanels(Arrays.asList(GENE_PANEL_ID)))
            .thenReturn(expectedGenePanelToGeneList);

        List<GenePanel> result = genePanelService.getAllGenePanels("DETAILED", PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedGenePanelList, result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(genePanel, result.get(0));
        Assert.assertEquals(1, result.get(0).getGenes().size());
        Assert.assertEquals(genePanelToGene, result.get(0).getGenes().get(0));
    }

    @Test
    public void getMetaGenePanels() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();

        Mockito.when(genePanelRepository.getMetaGenePanels()).thenReturn(expectedBaseMeta);

        BaseMeta result = genePanelService.getMetaGenePanels();

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = GenePanelNotFoundException.class)
    public void getGenePanelNotFound() throws Exception {

        Mockito.when(genePanelRepository.getGenePanel(GENE_PANEL_ID)).thenReturn(null);
    
        genePanelService.getGenePanel(GENE_PANEL_ID);
    }

    @Test
    public void getGenePanel() throws Exception {
        
        GenePanel genePanel = new GenePanel();
        genePanel.setStableId(GENE_PANEL_ID);
        List<GenePanelToGene> expectedGenePanelToGeneList = new ArrayList<>();
        GenePanelToGene genePanelToGene = new GenePanelToGene();
        genePanelToGene.setGenePanelId(GENE_PANEL_ID);
        expectedGenePanelToGeneList.add(genePanelToGene);

        Mockito.when(genePanelRepository.getGenePanel(GENE_PANEL_ID)).thenReturn(genePanel);

        Mockito.when(genePanelRepository.getGenesOfPanels(Arrays.asList(GENE_PANEL_ID)))
            .thenReturn(expectedGenePanelToGeneList);

        GenePanel result = genePanelService.getGenePanel(GENE_PANEL_ID);

        Assert.assertEquals(genePanel, result);
        Assert.assertEquals(1, result.getGenes().size());
        Assert.assertEquals(genePanelToGene, result.getGenes().get(0));
    }

    @Test
    public void getGenePanelData() throws Exception {
        
        List<GenePanelData> genePanelDataList = new ArrayList<>();
        GenePanelData genePanelData = new GenePanelData();
        genePanelData.setGenePanelId(GENE_PANEL_ID);
        genePanelData.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        genePanelData.setSampleId(SAMPLE_ID1);
        genePanelData.setPatientId(PATIENT_ID_1);
        genePanelData.setStudyId(STUDY_ID);
        genePanelData.setProfiled(true);
        genePanelDataList.add(genePanelData);
        GenePanelData genePanelData2 = new GenePanelData();
        genePanelData2.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        genePanelData2.setSampleId(SAMPLE_ID2);
        genePanelData2.setPatientId(PATIENT_ID_2);
        genePanelData2.setStudyId(STUDY_ID);
        genePanelData2.setProfiled(false);
        genePanelDataList.add(genePanelData2);

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setStableId(MOLECULAR_PROFILE_ID);
        molecularProfile.setCancerStudyIdentifier(STUDY_ID);
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.COPY_NUMBER_ALTERATION);

        Mockito.when(molecularProfileService
            .getMolecularProfile(MOLECULAR_PROFILE_ID))
            .thenReturn(molecularProfile);

        Mockito.when(genePanelRepository.getGenePanelDataBySampleListId(MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID))
            .thenReturn(genePanelDataList);
        
        List<GenePanelData> result = genePanelService.getGenePanelData(MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID);
        
        Assert.assertEquals(2, result.size());
        GenePanelData resultGenePanelData1 = result.get(0);
        Assert.assertEquals(SAMPLE_ID1, resultGenePanelData1.getSampleId());
        Assert.assertEquals(GENE_PANEL_ID, resultGenePanelData1.getGenePanelId());
        Assert.assertEquals(MOLECULAR_PROFILE_ID, resultGenePanelData1.getMolecularProfileId());
        Assert.assertEquals(PATIENT_ID_1, resultGenePanelData1.getPatientId());
        Assert.assertEquals(STUDY_ID, resultGenePanelData1.getStudyId());
        Assert.assertEquals(true, resultGenePanelData1.getProfiled());
        GenePanelData resultGenePanelData2 = result.get(1);
        Assert.assertEquals(SAMPLE_ID2, resultGenePanelData2.getSampleId());
        Assert.assertNull(resultGenePanelData2.getGenePanelId());
        Assert.assertEquals(MOLECULAR_PROFILE_ID, resultGenePanelData2.getMolecularProfileId());
        Assert.assertEquals(PATIENT_ID_2, resultGenePanelData2.getPatientId());
        Assert.assertEquals(STUDY_ID, resultGenePanelData2.getStudyId());
        Assert.assertEquals(false, resultGenePanelData2.getProfiled());
    }

    @Test
    public void getGenePanelDataInvalidSampleListId() throws Exception {

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setStableId(MOLECULAR_PROFILE_ID);
        molecularProfile.setCancerStudyIdentifier(STUDY_ID);
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.COPY_NUMBER_ALTERATION);

        Mockito.when(molecularProfileService
            .getMolecularProfile(MOLECULAR_PROFILE_ID))
            .thenReturn(molecularProfile);
        
        List<GenePanelData> result = genePanelService.getGenePanelData(MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID);
        
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void fetchGenePanelData() throws Exception {

        List<GenePanelData> genePanelDataList = new ArrayList<>();
        GenePanelData genePanelData = new GenePanelData();
        genePanelData.setGenePanelId(GENE_PANEL_ID);
        genePanelData.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        genePanelData.setSampleId(SAMPLE_ID1);
        genePanelData.setPatientId(PATIENT_ID_1);
        genePanelData.setStudyId(STUDY_ID);
        genePanelData.setProfiled(true);
        genePanelDataList.add(genePanelData);

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setStableId(MOLECULAR_PROFILE_ID);
        molecularProfile.setCancerStudyIdentifier(STUDY_ID);
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.COPY_NUMBER_ALTERATION);

        Mockito.when(molecularProfileService
            .getMolecularProfiles(Collections.singleton(MOLECULAR_PROFILE_ID), "SUMMARY"))
            .thenReturn(Arrays.asList(molecularProfile));

        Mockito.when(genePanelRepository
            .fetchGenePanelDataByMolecularProfileId(MOLECULAR_PROFILE_ID))
            .thenReturn(genePanelDataList);

        List<GenePanelData> result = genePanelService.fetchGenePanelData(MOLECULAR_PROFILE_ID, 
            Arrays.asList(SAMPLE_ID1, SAMPLE_ID2));

        Assert.assertEquals(1, result.size());
        GenePanelData resultGenePanelData1 = result.get(0);
        Assert.assertEquals(SAMPLE_ID1, resultGenePanelData1.getSampleId());
        Assert.assertEquals(GENE_PANEL_ID, resultGenePanelData1.getGenePanelId());
        Assert.assertEquals(MOLECULAR_PROFILE_ID, resultGenePanelData1.getMolecularProfileId());
        Assert.assertEquals(PATIENT_ID_1, resultGenePanelData1.getPatientId());
        Assert.assertEquals(STUDY_ID, resultGenePanelData1.getStudyId());
        Assert.assertEquals(true, resultGenePanelData1.getProfiled());
    }

    @Test
    public void fetchGenePanelDataInMultipleMolecularProfiles() throws Exception {

        List<GenePanelData> genePanelDataList = new ArrayList<>();
        GenePanelData genePanelData = new GenePanelData();
        genePanelData.setGenePanelId(GENE_PANEL_ID);
        genePanelData.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        genePanelData.setSampleId(SAMPLE_ID1);
        genePanelData.setPatientId(PATIENT_ID_1);
        genePanelData.setStudyId(STUDY_ID);
        genePanelData.setProfiled(true);
        genePanelDataList.add(genePanelData);

        GenePanelData genePanelData2 = new GenePanelData();
        genePanelData2.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        genePanelData2.setSampleId(SAMPLE_ID2);
        genePanelData2.setPatientId(PATIENT_ID_2);
        genePanelData2.setStudyId(STUDY_ID);
        genePanelData2.setProfiled(true);
        genePanelDataList.add(genePanelData2);

        Set<MolecularProfileCaseIdentifier> molecularProfileSampleIdentifiers = new HashSet<>();
        MolecularProfileCaseIdentifier profileCaseIdentifier = new MolecularProfileCaseIdentifier();
        profileCaseIdentifier.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        profileCaseIdentifier.setCaseId(SAMPLE_ID1);
        molecularProfileSampleIdentifiers.add(profileCaseIdentifier);

        MolecularProfileCaseIdentifier profileCaseIdentifier2 = new MolecularProfileCaseIdentifier();
        profileCaseIdentifier2.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        profileCaseIdentifier2.setCaseId(SAMPLE_ID2);
        molecularProfileSampleIdentifiers.add(profileCaseIdentifier2);


        MolecularProfileCaseIdentifier profileCaseIdentifier3 = new MolecularProfileCaseIdentifier();
        profileCaseIdentifier3.setMolecularProfileId("invalid_profile");
        profileCaseIdentifier3.setCaseId(SAMPLE_ID3);
        molecularProfileSampleIdentifiers.add(profileCaseIdentifier3);

        Mockito.when(genePanelRepository
            .fetchGenePanelDataByMolecularProfileId(MOLECULAR_PROFILE_ID))
            .thenReturn(genePanelDataList);
        Set<String> molecularProfileIds = molecularProfileSampleIdentifiers.stream().map(MolecularProfileCaseIdentifier::getMolecularProfileId).collect(Collectors.toSet());

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setStableId(MOLECULAR_PROFILE_ID);
        molecularProfile.setCancerStudyIdentifier(STUDY_ID);
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.COPY_NUMBER_ALTERATION);
        
        Mockito.when(molecularProfileService.getMolecularProfiles(new HashSet<>(molecularProfileIds), "SUMMARY"))
            .thenReturn(Arrays.asList(molecularProfile));

        List<GenePanelData> result = genePanelService.fetchGenePanelDataInMultipleMolecularProfiles(new ArrayList<>(molecularProfileSampleIdentifiers));

        Assert.assertEquals(2, result.size());
        GenePanelData resultGenePanelData1 = result.get(0);
        Assert.assertEquals(SAMPLE_ID1, resultGenePanelData1.getSampleId());
        Assert.assertEquals(GENE_PANEL_ID, resultGenePanelData1.getGenePanelId());
        Assert.assertEquals(MOLECULAR_PROFILE_ID, resultGenePanelData1.getMolecularProfileId());
        Assert.assertEquals(PATIENT_ID_1, resultGenePanelData1.getPatientId());
        Assert.assertEquals(STUDY_ID, resultGenePanelData1.getStudyId());
        Assert.assertEquals(true, resultGenePanelData1.getProfiled());
        GenePanelData resultGenePanelData2 = result.get(1);
        Assert.assertEquals(SAMPLE_ID2, resultGenePanelData2.getSampleId());
        Assert.assertNull(resultGenePanelData2.getGenePanelId());
        Assert.assertEquals(MOLECULAR_PROFILE_ID, resultGenePanelData2.getMolecularProfileId());
        Assert.assertEquals(PATIENT_ID_2, resultGenePanelData2.getPatientId());
        Assert.assertEquals(STUDY_ID, resultGenePanelData2.getStudyId());
        Assert.assertEquals(true, resultGenePanelData2.getProfiled());
    }
}
