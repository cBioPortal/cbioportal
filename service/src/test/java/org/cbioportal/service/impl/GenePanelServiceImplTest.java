package org.cbioportal.service.impl;

import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GenePanelRepository;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.exception.GenePanelNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class GenePanelServiceImplTest extends BaseServiceImplTest {
    
    @InjectMocks
    private GenePanelServiceImpl genePanelService;
    
    @Mock
    private GenePanelRepository genePanelRepository;
    @Mock
    private MolecularProfileService molecularProfileService;
    
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
        
        List<GenePanelData> expectedGenePanelDataList = new ArrayList<>();
        GenePanelData genePanelData = new GenePanelData();
        genePanelData.setGenePanelId(GENE_PANEL_ID);
        expectedGenePanelDataList.add(genePanelData);
        List<GenePanelToGene> expectedGenePanelToGeneList = new ArrayList<>();
        GenePanelToGene genePanelToGene = new GenePanelToGene();
        genePanelToGene.setGenePanelId(GENE_PANEL_ID);
        genePanelToGene.setEntrezGeneId(ENTREZ_GENE_ID);
        expectedGenePanelToGeneList.add(genePanelToGene);
        
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(new MolecularProfile());
        
        Mockito.when(genePanelRepository.getGenePanelData(MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID))
            .thenReturn(expectedGenePanelDataList);

        Mockito.when(genePanelRepository.getGenesOfPanels(Arrays.asList(GENE_PANEL_ID)))
            .thenReturn(expectedGenePanelToGeneList);
        
        List<GenePanelData> result = genePanelService.getGenePanelData(MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID, 
            Arrays.asList(ENTREZ_GENE_ID));
        
        Assert.assertEquals(expectedGenePanelDataList, result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(genePanelData, result.get(0));
        Assert.assertEquals(1, result.get(0).getEntrezGeneIds().size());
        Assert.assertEquals(ENTREZ_GENE_ID, result.get(0).getEntrezGeneIds().get(0));
    }

    @Test
    public void fetchGenePanelData() throws Exception {

        List<GenePanelData> expectedGenePanelDataList = new ArrayList<>();
        GenePanelData genePanelData = new GenePanelData();
        genePanelData.setGenePanelId(GENE_PANEL_ID);
        expectedGenePanelDataList.add(genePanelData);
        List<GenePanelToGene> expectedGenePanelToGeneList = new ArrayList<>();
        GenePanelToGene genePanelToGene = new GenePanelToGene();
        genePanelToGene.setGenePanelId(GENE_PANEL_ID);
        genePanelToGene.setEntrezGeneId(ENTREZ_GENE_ID);
        expectedGenePanelToGeneList.add(genePanelToGene);

        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(new MolecularProfile());

        Mockito.when(genePanelRepository.fetchGenePanelData(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1, SAMPLE_ID2)))
            .thenReturn(expectedGenePanelDataList);

        Mockito.when(genePanelRepository.getGenesOfPanels(Arrays.asList(GENE_PANEL_ID)))
            .thenReturn(expectedGenePanelToGeneList);

        List<GenePanelData> result = genePanelService.fetchGenePanelData(MOLECULAR_PROFILE_ID,
            Arrays.asList(SAMPLE_ID1, SAMPLE_ID2), Arrays.asList(ENTREZ_GENE_ID));

        Assert.assertEquals(expectedGenePanelDataList, result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(genePanelData, result.get(0));
        Assert.assertEquals(1, result.get(0).getEntrezGeneIds().size());
        Assert.assertEquals(ENTREZ_GENE_ID, result.get(0).getEntrezGeneIds().get(0));
    }

    @Test
    public void fetchGenePanelDataInMultipleMolecularProfiles() throws Exception {

        List<GenePanelData> expectedGenePanelDataList = new ArrayList<>();
        GenePanelData genePanelData = new GenePanelData();
        genePanelData.setGenePanelId(GENE_PANEL_ID);
        expectedGenePanelDataList.add(genePanelData);
        List<GenePanelToGene> expectedGenePanelToGeneList = new ArrayList<>();
        GenePanelToGene genePanelToGene = new GenePanelToGene();
        genePanelToGene.setGenePanelId(GENE_PANEL_ID);
        genePanelToGene.setEntrezGeneId(ENTREZ_GENE_ID);
        expectedGenePanelToGeneList.add(genePanelToGene);

        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(new MolecularProfile());

        Mockito.when(genePanelRepository.fetchGenePanelDataInMultipleMolecularProfiles(
            Arrays.asList(MOLECULAR_PROFILE_ID), Arrays.asList(SAMPLE_ID1, SAMPLE_ID2))).thenReturn(expectedGenePanelDataList);

        Mockito.when(genePanelRepository.getGenesOfPanels(Arrays.asList(GENE_PANEL_ID)))
            .thenReturn(expectedGenePanelToGeneList);

        List<GenePanelData> result = genePanelService.fetchGenePanelDataInMultipleMolecularProfiles(
            Arrays.asList(MOLECULAR_PROFILE_ID), Arrays.asList(SAMPLE_ID1, SAMPLE_ID2), Arrays.asList(ENTREZ_GENE_ID));

        Assert.assertEquals(expectedGenePanelDataList, result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(genePanelData, result.get(0));
        Assert.assertEquals(1, result.get(0).getEntrezGeneIds().size());
        Assert.assertEquals(ENTREZ_GENE_ID, result.get(0).getEntrezGeneIds().get(0));
    }
}
