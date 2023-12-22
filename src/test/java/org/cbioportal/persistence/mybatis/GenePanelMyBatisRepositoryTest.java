package org.cbioportal.persistence.mybatis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.mybatis.config.TestConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {GenePanelMyBatisRepository.class, TestConfig.class})
public class GenePanelMyBatisRepositoryTest {
    
    @Autowired
    private GenePanelMyBatisRepository genePanelMyBatisRepository;
    
    @Test
    public void getAllGenePanelsIdProjection() throws Exception {
        
        List<GenePanel> result = genePanelMyBatisRepository.getAllGenePanels("ID", null, null, null, null);

        Assert.assertEquals(2, result.size());
        GenePanel genePanel = result.get(0);
        Assert.assertEquals((Integer) 1, genePanel.getInternalId());
        Assert.assertEquals("TESTPANEL1", genePanel.getStableId());
    }

    @Test
    public void getAllGenePanelsSummaryProjection() throws Exception {

        List<GenePanel> result = genePanelMyBatisRepository.getAllGenePanels("SUMMARY", null, null, null, null);

        Assert.assertEquals(2, result.size());
        GenePanel genePanel = result.get(0);
        Assert.assertEquals((Integer) 1, genePanel.getInternalId());
        Assert.assertEquals("TESTPANEL1", genePanel.getStableId());
        Assert.assertEquals("A test panel consisting of a few genes", genePanel.getDescription());
    }

    @Test
    public void getMetaGenePanels() throws Exception {

        BaseMeta result = genePanelMyBatisRepository.getMetaGenePanels();

        Assert.assertEquals((Integer) 2, result.getTotalCount());
    }

    @Test
    public void getGenePanelNullResult() throws Exception {
        
        GenePanel result = genePanelMyBatisRepository.getGenePanel("invalid_gene_panel");
        
        Assert.assertNull(result);
    }

    @Test
    public void getGenePanel() throws Exception {

        GenePanel result = genePanelMyBatisRepository.getGenePanel("TESTPANEL1");

        Assert.assertEquals((Integer) 1, result.getInternalId());
        Assert.assertEquals("TESTPANEL1", result.getStableId());
        Assert.assertEquals("A test panel consisting of a few genes", result.getDescription());
    }

    @Test
    public void getGenePanelData() throws Exception {
        
        List<GenePanelData> result = genePanelMyBatisRepository.getGenePanelDataBySampleListId("study_tcga_pub_mrna", 
            "study_tcga_pub_all");
        
        Assert.assertEquals(14, result.size());
        GenePanelData genePanelData = result.get(0);
        Assert.assertEquals("study_tcga_pub_mrna", genePanelData.getMolecularProfileId());
        Assert.assertEquals("TESTPANEL1", genePanelData.getGenePanelId());
        Assert.assertEquals("TCGA-A1-A0SB-01", genePanelData.getSampleId());
    }

    @Test
    public void fetchGenePanelData() throws Exception {

        List<GenePanelData> result = genePanelMyBatisRepository.fetchGenePanelData("study_tcga_pub_mrna",
            Arrays.asList("TCGA-A1-A0SB-01", "TCGA-A1-A0SD-01"));

        Assert.assertEquals(2, result.size());
        GenePanelData genePanelData = result.get(0);
        Assert.assertEquals("study_tcga_pub_mrna", genePanelData.getMolecularProfileId());
        Assert.assertEquals("TESTPANEL1", genePanelData.getGenePanelId());
        Assert.assertEquals("TCGA-A1-A0SB-01", genePanelData.getSampleId());
    }

    @Test
    public void fetchGenePanelDataInMultipleMolecularProfiles() throws Exception {

        List<MolecularProfileCaseIdentifier> molecularProfileSampleIdentifiers = new ArrayList<>();
        MolecularProfileCaseIdentifier profileCaseIdentifier = new MolecularProfileCaseIdentifier();
        profileCaseIdentifier.setMolecularProfileId("study_tcga_pub_mrna");
        profileCaseIdentifier.setCaseId("TCGA-A1-A0SB-01");
        molecularProfileSampleIdentifiers.add(profileCaseIdentifier);

        MolecularProfileCaseIdentifier profileCaseIdentifier2 = new MolecularProfileCaseIdentifier();
        profileCaseIdentifier2.setMolecularProfileId("study_tcga_pub_log2CNA");
        profileCaseIdentifier2.setCaseId("TCGA-A1-A0SD-01");
        molecularProfileSampleIdentifiers.add(profileCaseIdentifier2);

        List<GenePanelData> result = genePanelMyBatisRepository.fetchGenePanelDataInMultipleMolecularProfiles(molecularProfileSampleIdentifiers);

        Assert.assertEquals(2, result.size());
        GenePanelData genePanelData = result.get(0);
        Assert.assertEquals("study_tcga_pub_mrna", genePanelData.getMolecularProfileId());
        Assert.assertEquals("TESTPANEL1", genePanelData.getGenePanelId());
        Assert.assertEquals("TCGA-A1-A0SB-01", genePanelData.getSampleId());
    }

    @Test
    public void getGenesOfPanels() throws Exception {
        
        List<GenePanelToGene> result = genePanelMyBatisRepository.getGenesOfPanels(Arrays.asList("TESTPANEL1"));
        
        Assert.assertEquals(3, result.size());
        GenePanelToGene genePanelToGene = result.get(0);
        Assert.assertEquals("TESTPANEL1", genePanelToGene.getGenePanelId());
        Assert.assertEquals((Integer) 207, genePanelToGene.getEntrezGeneId());
        Assert.assertEquals("AKT1", genePanelToGene.getHugoGeneSymbol());
    }
}