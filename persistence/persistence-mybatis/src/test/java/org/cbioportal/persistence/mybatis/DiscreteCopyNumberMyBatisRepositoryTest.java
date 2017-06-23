package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.CopyNumberSampleCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.Gene;
import org.cbioportal.model.meta.BaseMeta;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class DiscreteCopyNumberMyBatisRepositoryTest {

    @Autowired
    private DiscreteCopyNumberMyBatisRepository discreteCopyNumberMyBatisRepository;
    
    @Test
    public void getDiscreteCopyNumbersInGeneticProfileBySampleListIdSummaryProjection() throws Exception {

        List<Integer> alterations = new ArrayList<>();
        alterations.add(-2);
        alterations.add(2);
        
        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(207);
        entrezGeneIds.add(208);
        
        List<DiscreteCopyNumberData> result =  discreteCopyNumberMyBatisRepository
            .getDiscreteCopyNumbersInGeneticProfileBySampleListId("study_tcga_pub_gistic", "study_tcga_pub_all", 
                entrezGeneIds, alterations, "SUMMARY");

        Assert.assertEquals(3, result.size());
        DiscreteCopyNumberData discreteCopyNumberData = result.get(0);
        Assert.assertEquals("study_tcga_pub_gistic", discreteCopyNumberData.getGeneticProfileId());
        Assert.assertEquals("TCGA-A1-A0SB-01", discreteCopyNumberData.getSampleId());
        Assert.assertEquals((Integer) 207, discreteCopyNumberData.getEntrezGeneId());
        Assert.assertEquals((Integer) (-2), discreteCopyNumberData.getAlteration());
        Assert.assertNull(discreteCopyNumberData.getGene());
    }

    @Test
    public void getDiscreteCopyNumbersInGeneticProfileBySampleListIdDetailedProjection() throws Exception {

        List<Integer> alterations = new ArrayList<>();
        alterations.add(-2);
        alterations.add(2);
        
        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(207);
        entrezGeneIds.add(208);

        List<DiscreteCopyNumberData> result =  discreteCopyNumberMyBatisRepository
            .getDiscreteCopyNumbersInGeneticProfileBySampleListId("study_tcga_pub_gistic", "study_tcga_pub_all", 
                entrezGeneIds, alterations, "DETAILED");

        Assert.assertEquals(3, result.size());
        DiscreteCopyNumberData discreteCopyNumberData = result.get(0);
        Assert.assertEquals("study_tcga_pub_gistic", discreteCopyNumberData.getGeneticProfileId());
        Assert.assertEquals("TCGA-A1-A0SB-01", discreteCopyNumberData.getSampleId());
        Assert.assertEquals((Integer) 207, discreteCopyNumberData.getEntrezGeneId());
        Assert.assertEquals((Integer) (-2), discreteCopyNumberData.getAlteration());
        Gene gene = discreteCopyNumberData.getGene();
        Assert.assertEquals((Integer) 207, gene.getEntrezGeneId());
        Assert.assertEquals("AKT1", gene.getHugoGeneSymbol());
        Assert.assertEquals("protein-coding", gene.getType());
        Assert.assertEquals("14q32.32", gene.getCytoband());
        Assert.assertEquals((Integer) 10838, gene.getLength());
    }

    @Test
    public void getMetaDiscreteCopyNumbersInGeneticProfileBySampleListId() throws Exception {

        List<Integer> alterations = new ArrayList<>();
        alterations.add(-2);
        alterations.add(2);

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(207);
        entrezGeneIds.add(208);

        BaseMeta result = discreteCopyNumberMyBatisRepository.getMetaDiscreteCopyNumbersInGeneticProfileBySampleListId(
            "study_tcga_pub_gistic", "study_tcga_pub_all", entrezGeneIds, alterations);
        
        Assert.assertEquals((Integer) 3, result.getTotalCount());
    }

    @Test
    public void fetchDiscreteCopyNumbersInGeneticProfile() throws Exception {

        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SB-01");
        sampleIds.add("TCGA-A1-A0SD-01");

        List<Integer> alterations = new ArrayList<>();
        alterations.add(-2);
        alterations.add(2);
        
        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(207);
        entrezGeneIds.add(208);
        
        List<DiscreteCopyNumberData> result = discreteCopyNumberMyBatisRepository
            .fetchDiscreteCopyNumbersInGeneticProfile("study_tcga_pub_gistic", sampleIds, entrezGeneIds, alterations, 
                "SUMMARY");
        
        Assert.assertEquals(3, result.size());
        Assert.assertEquals("study_tcga_pub_gistic", result.get(0).getGeneticProfileId());
        Assert.assertEquals("TCGA-A1-A0SB-01", result.get(0).getSampleId());
        Assert.assertEquals("study_tcga_pub_gistic", result.get(1).getGeneticProfileId());
        Assert.assertEquals("TCGA-A1-A0SD-01", result.get(1).getSampleId());
        Assert.assertEquals("study_tcga_pub_gistic", result.get(2).getGeneticProfileId());
        Assert.assertEquals("TCGA-A1-A0SB-01", result.get(2).getSampleId());
    }

    @Test
    public void fetchMetaDiscreteCopyNumbersInGeneticProfile() throws Exception {

        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SB-01");
        sampleIds.add("TCGA-A1-A0SD-01");

        List<Integer> alterations = new ArrayList<>();
        alterations.add(-2);
        alterations.add(2);

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(207);
        entrezGeneIds.add(208);

        BaseMeta result = discreteCopyNumberMyBatisRepository
            .fetchMetaDiscreteCopyNumbersInGeneticProfile("study_tcga_pub_gistic", sampleIds, entrezGeneIds,
                alterations);

        Assert.assertEquals((Integer) 3, result.getTotalCount());
    }

    @Test
    public void getSampleCountByGeneAndAlteration() throws Exception {

        List<CopyNumberSampleCountByGene> result  = discreteCopyNumberMyBatisRepository
            .getSampleCountByGeneAndAlterationAndSampleIds("study_tcga_pub_gistic", null, Arrays.asList(207, 208), 
                Arrays.asList(-2, 2));
        
        Assert.assertEquals(2, result.size());
        CopyNumberSampleCountByGene copyNumberSampleCountByGene1 = result.get(0);
        Assert.assertEquals((Integer) 207, copyNumberSampleCountByGene1.getEntrezGeneId());
        Assert.assertEquals((Integer) (-2), copyNumberSampleCountByGene1.getAlteration());
        Assert.assertEquals((Integer) 1, copyNumberSampleCountByGene1.getSampleCount());
        CopyNumberSampleCountByGene copyNumberSampleCountByGene2 = result.get(1);
        Assert.assertEquals((Integer) 208, copyNumberSampleCountByGene2.getEntrezGeneId());
        Assert.assertEquals((Integer) (2), copyNumberSampleCountByGene2.getAlteration());
        Assert.assertEquals((Integer) 1, copyNumberSampleCountByGene2.getSampleCount());
    }
}