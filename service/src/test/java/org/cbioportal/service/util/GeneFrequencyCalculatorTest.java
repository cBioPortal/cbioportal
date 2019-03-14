package org.cbioportal.service.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.SampleListService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GeneFrequencyCalculatorTest {

    private static final String MOLECULAR_PROFILE_ID = "molecular_profile_id";
    private static final String GENE_PANEL_ID_1 = "gene_panel_id_1";
    private static final String GENE_PANEL_ID_2 = "gene_panel_id_2";
    private static final Integer ENTREZ_GENE_ID_1 = 1;
    private static final Integer ENTREZ_GENE_ID_2 = 2;
    private static final Integer ENTREZ_GENE_ID_3 = 3;
    private static final String SAMPLE_ID_1 = "sample_id_1";
    private static final String SAMPLE_ID_2 = "sample_id_2";
    private static final String SAMPLE_ID_3 = "sample_id_3";

    @InjectMocks
    private GeneFrequencyCalculator geneFrequencyCalculator;
    
    @Mock
    private SampleListService sampleListService;
    @Mock
    private GenePanelService genePanelService;

    @Test
    public void calculate() throws Exception {


        List<GenePanelData> genePanelDataList = new ArrayList<>();
        GenePanelData genePanelData1 = new GenePanelData();
        genePanelData1.setGenePanelId(GENE_PANEL_ID_1);
        genePanelData1.setProfiled(true);
        genePanelDataList.add(genePanelData1);
        GenePanelData genePanelData2 = new GenePanelData();
        genePanelData2.setGenePanelId(GENE_PANEL_ID_2);
        genePanelData2.setProfiled(true);
        genePanelDataList.add(genePanelData2);
        GenePanelData genePanelData3 = new GenePanelData();
        genePanelData3.setProfiled(true);
        genePanelDataList.add(genePanelData3);

        Mockito.when(genePanelService.fetchGenePanelDataInMultipleMolecularProfiles(Arrays.asList(MOLECULAR_PROFILE_ID, MOLECULAR_PROFILE_ID, 
            MOLECULAR_PROFILE_ID), Arrays.asList(SAMPLE_ID_1, SAMPLE_ID_2, SAMPLE_ID_3))).thenReturn(genePanelDataList);

        List<GenePanel> genePanels = new ArrayList<>();
        GenePanel genePanel1 = new GenePanel();
        genePanel1.setStableId(GENE_PANEL_ID_1);
        List<GenePanelToGene> genes1 = new ArrayList<>();
        GenePanelToGene genePanelToGene1 = new GenePanelToGene();
        genePanelToGene1.setEntrezGeneId(ENTREZ_GENE_ID_1);
        genes1.add(genePanelToGene1);
        GenePanelToGene genePanelToGene2 = new GenePanelToGene();
        genePanelToGene2.setEntrezGeneId(ENTREZ_GENE_ID_2);
        genes1.add(genePanelToGene2);
        genePanel1.setGenes(genes1);
        genePanels.add(genePanel1);
        GenePanel genePanel2 = new GenePanel();
        genePanel2.setStableId(GENE_PANEL_ID_2);
        List<GenePanelToGene> genes2 = new ArrayList<>();
        GenePanelToGene genePanelToGene3 = new GenePanelToGene();
        genePanelToGene3.setEntrezGeneId(ENTREZ_GENE_ID_1);
        genes2.add(genePanelToGene3);
        genePanel2.setGenes(genes2);
        genePanels.add(genePanel2);

        Mockito.when(genePanelService.fetchGenePanels(Arrays.asList(GENE_PANEL_ID_2, GENE_PANEL_ID_1), "DETAILED"))
            .thenReturn(genePanels);

        List<AlterationCountByGene> alterationCounts = new ArrayList<>();
        AlterationCountByGene alterationCount1 = new AlterationCountByGene();
        alterationCount1.setEntrezGeneId(ENTREZ_GENE_ID_1);
        alterationCount1.setNumberOfAlteredCases(3);
        alterationCounts.add(alterationCount1);
        AlterationCountByGene alterationCount2 = new AlterationCountByGene();
        alterationCount2.setEntrezGeneId(ENTREZ_GENE_ID_2);
        alterationCount2.setNumberOfAlteredCases(1);
        alterationCounts.add(alterationCount2);
        AlterationCountByGene alterationCount3 = new AlterationCountByGene();
        alterationCount3.setEntrezGeneId(ENTREZ_GENE_ID_3);
        alterationCount3.setNumberOfAlteredCases(2);
        alterationCounts.add(alterationCount3);

        geneFrequencyCalculator.calculate(Arrays.asList(MOLECULAR_PROFILE_ID, MOLECULAR_PROFILE_ID, MOLECULAR_PROFILE_ID), 
            Arrays.asList(SAMPLE_ID_1, SAMPLE_ID_2, SAMPLE_ID_3), alterationCounts);

        Assert.assertEquals(new BigDecimal("100.00"), alterationCounts.get(0).getFrequency());
        Assert.assertEquals(new BigDecimal("50.00"), alterationCounts.get(1).getFrequency());
        Assert.assertEquals(new BigDecimal("66.67"), alterationCounts.get(2).getFrequency());
    }
}
