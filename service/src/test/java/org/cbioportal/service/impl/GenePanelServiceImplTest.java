package org.cbioportal.service.impl;

import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.model.SampleList;
import org.cbioportal.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GenePanelRepository;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleListService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.GenePanelNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GenePanelServiceImplTest extends BaseServiceImplTest {
    
    @InjectMocks
    private GenePanelServiceImpl genePanelService;
    
    @Mock
    private GenePanelRepository genePanelRepository;
    @Mock
    private MolecularProfileService molecularProfileService;
    @Mock
    private SampleListRepository sampleListRepository;
    @Mock
    private SampleService sampleService;
    @Mock
    private SampleListService sampleListService;
    
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
        genePanelDataList.add(genePanelData);

        List<GenePanelToGene> genePanelToGeneList = new ArrayList<>();
        GenePanelToGene genePanelToGene = new GenePanelToGene();
        genePanelToGene.setGenePanelId(GENE_PANEL_ID);
        genePanelToGene.setEntrezGeneId(ENTREZ_GENE_ID_1);
        genePanelToGeneList.add(genePanelToGene);

        List<SampleList> sampleLists = new ArrayList<>();
        SampleList sampleList = new SampleList();
        sampleList.setSampleIds(Arrays.asList(SAMPLE_ID1));
        sampleList.setCancerStudyIdentifier(STUDY_ID);
        sampleLists.add(sampleList);

        List<MolecularProfile> molecularProfiles = new ArrayList<>();
        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setStableId(MOLECULAR_PROFILE_ID);
        molecularProfile.setMolecularAlterationType(MolecularAlterationType.MUTATION_EXTENDED);
        molecularProfile.setCancerStudyIdentifier(STUDY_ID);
        molecularProfiles.add(molecularProfile);

        List<Sample> samples = new ArrayList<>();
        Sample sample1 = new Sample();
        sample1.setStableId(SAMPLE_ID1);
        sample1.setPatientStableId(PATIENT_ID_1);
        sample1.setCancerStudyIdentifier(STUDY_ID);
        samples.add(sample1);
        Sample sample2 = new Sample();
        sample2.setStableId(SAMPLE_ID2);
        sample2.setPatientStableId(PATIENT_ID_2);
        sample2.setCancerStudyIdentifier(STUDY_ID);
        samples.add(sample2);
        
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfile);

        Mockito.when(sampleListRepository.getAllSampleIdsInSampleList(SAMPLE_LIST_ID))
            .thenReturn(Arrays.asList(SAMPLE_ID1, SAMPLE_ID2));

        Mockito.when(sampleListService.fetchSampleLists(Arrays.asList(STUDY_ID + SEQUENCED_LIST_SUFFIX), 
            "DETAILED")).thenReturn(sampleLists);

        Mockito.when(molecularProfileService.getMolecularProfiles(
            Arrays.asList(MOLECULAR_PROFILE_ID, MOLECULAR_PROFILE_ID), "SUMMARY")).thenReturn(molecularProfiles);

        Mockito.when(sampleService.fetchSamples(Arrays.asList(STUDY_ID, STUDY_ID), 
            Arrays.asList(SAMPLE_ID1, SAMPLE_ID2), "ID")).thenReturn(samples);
        
        Mockito.when(genePanelRepository.getGenePanelData(MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID))
            .thenReturn(genePanelDataList);

        Mockito.when(genePanelRepository.getGenesOfPanels(Arrays.asList(GENE_PANEL_ID)))
            .thenReturn(genePanelToGeneList);
        
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

        Mockito.when(sampleListRepository.getAllSampleIdsInSampleList(SAMPLE_LIST_ID))
            .thenReturn(Collections.<String>emptyList() );
        
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
        genePanelDataList.add(genePanelData);

        List<GenePanelToGene> genePanelToGeneList = new ArrayList<>();
        GenePanelToGene genePanelToGene = new GenePanelToGene();
        genePanelToGene.setGenePanelId(GENE_PANEL_ID);
        genePanelToGene.setEntrezGeneId(ENTREZ_GENE_ID_1);
        genePanelToGeneList.add(genePanelToGene);

        List<SampleList> sampleLists = new ArrayList<>();
        SampleList sampleList = new SampleList();
        sampleList.setSampleIds(Arrays.asList(SAMPLE_ID1));
        sampleList.setCancerStudyIdentifier(STUDY_ID);
        sampleLists.add(sampleList);

        List<MolecularProfile> molecularProfiles = new ArrayList<>();
        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setStableId(MOLECULAR_PROFILE_ID);
        molecularProfile.setCancerStudyIdentifier(STUDY_ID);
        molecularProfile.setMolecularAlterationType(MolecularAlterationType.MUTATION_EXTENDED);
        molecularProfiles.add(molecularProfile);

        List<Sample> samples = new ArrayList<>();
        Sample sample1 = new Sample();
        sample1.setStableId(SAMPLE_ID1);
        sample1.setPatientStableId(PATIENT_ID_1);
        sample1.setCancerStudyIdentifier(STUDY_ID);
        samples.add(sample1);
        
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfile);

        Mockito.when(sampleListService.fetchSampleLists(Arrays.asList(STUDY_ID + SEQUENCED_LIST_SUFFIX), 
            "DETAILED")).thenReturn(sampleLists);

        Mockito.when(molecularProfileService.getMolecularProfiles(
            Arrays.asList(MOLECULAR_PROFILE_ID, MOLECULAR_PROFILE_ID), "SUMMARY")).thenReturn(molecularProfiles);

        Mockito.when(sampleService.fetchSamples(Arrays.asList(STUDY_ID, STUDY_ID), 
            Arrays.asList(SAMPLE_ID1, SAMPLE_ID2), "ID")).thenReturn(samples);
        
        Mockito.when(genePanelRepository.fetchGenePanelData(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1, SAMPLE_ID2)))
            .thenReturn(genePanelDataList);

        Mockito.when(genePanelRepository.getGenesOfPanels(Arrays.asList(GENE_PANEL_ID)))
            .thenReturn(genePanelToGeneList);
        
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
        genePanelDataList.add(genePanelData);

        List<GenePanelToGene> genePanelToGeneList = new ArrayList<>();
        GenePanelToGene genePanelToGene = new GenePanelToGene();
        genePanelToGene.setGenePanelId(GENE_PANEL_ID);
        genePanelToGene.setEntrezGeneId(ENTREZ_GENE_ID_1);
        genePanelToGeneList.add(genePanelToGene);

        List<SampleList> sampleLists = new ArrayList<>();

        List<MolecularProfile> molecularProfiles = new ArrayList<>();
        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setStableId(MOLECULAR_PROFILE_ID);
        molecularProfile.setCancerStudyIdentifier(STUDY_ID);
        molecularProfile.setMolecularAlterationType(MolecularAlterationType.MUTATION_EXTENDED);
        molecularProfiles.add(molecularProfile);

        List<Sample> samples = new ArrayList<>();
        Sample sample1 = new Sample();
        sample1.setStableId(SAMPLE_ID1);
        sample1.setPatientStableId(PATIENT_ID_1);
        sample1.setCancerStudyIdentifier(STUDY_ID);
        samples.add(sample1);
        Sample sample2 = new Sample();
        sample2.setStableId(SAMPLE_ID2);
        sample2.setPatientStableId(PATIENT_ID_2);
        sample2.setCancerStudyIdentifier(STUDY_ID);
        samples.add(sample2);

        Mockito.when(sampleListService.fetchSampleLists(Arrays.asList(STUDY_ID + SEQUENCED_LIST_SUFFIX), 
            "DETAILED")).thenReturn(sampleLists);

        Mockito.when(molecularProfileService.getMolecularProfiles(
            Arrays.asList(MOLECULAR_PROFILE_ID, MOLECULAR_PROFILE_ID, "invalid_profile"), "SUMMARY")).thenReturn(molecularProfiles);

        Mockito.when(sampleService.fetchSamples(Arrays.asList(STUDY_ID, STUDY_ID), 
            Arrays.asList(SAMPLE_ID1, SAMPLE_ID2), "ID")).thenReturn(samples);
        
        Mockito.when(genePanelRepository.fetchGenePanelDataInMultipleMolecularProfiles(
            Arrays.asList(MOLECULAR_PROFILE_ID, MOLECULAR_PROFILE_ID, "invalid_profile"), Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3)))
            .thenReturn(genePanelDataList);

        Mockito.when(genePanelRepository.getGenesOfPanels(Arrays.asList(GENE_PANEL_ID)))
            .thenReturn(genePanelToGeneList);
        
        List<GenePanelData> result = genePanelService.fetchGenePanelDataInMultipleMolecularProfiles(
            new ArrayList<>(Arrays.asList(MOLECULAR_PROFILE_ID, MOLECULAR_PROFILE_ID, "invalid_profile")), 
            new ArrayList<>(Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3)));
        
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
