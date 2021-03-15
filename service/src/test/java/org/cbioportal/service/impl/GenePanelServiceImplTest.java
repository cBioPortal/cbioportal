package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GenePanelRepository;
import org.cbioportal.service.exception.GenePanelNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GenePanelServiceImplTest extends BaseServiceImplTest {
    
    @InjectMocks
    private GenePanelServiceImpl genePanelService;
    
    @Mock
    private GenePanelRepository genePanelRepository;
    
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

        Mockito.when(genePanelRepository.fetchGenePanelData(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1, SAMPLE_ID2)))
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

        Mockito.when(genePanelRepository.fetchGenePanelDataInMultipleMolecularProfiles(
            Arrays.asList(MOLECULAR_PROFILE_ID, MOLECULAR_PROFILE_ID, "invalid_profile"), Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3)))
            .thenReturn(genePanelDataList);

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
