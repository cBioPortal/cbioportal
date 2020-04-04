package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.Gene;
import org.cbioportal.model.ReferenceGenomeGene;
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
    
    @Autowired
    private ReferenceGenomeGeneMyBatisRepository refGeneMyBatisRepository;
    
    @Test
    public void getDiscreteCopyNumbersInMolecularProfileBySampleListIdSummaryProjection() throws Exception {

        List<Integer> alterations = new ArrayList<>();
        alterations.add(-2);
        alterations.add(2);
        
        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(207);
        entrezGeneIds.add(208);
        
        List<DiscreteCopyNumberData> result =  discreteCopyNumberMyBatisRepository
            .getDiscreteCopyNumbersInMolecularProfileBySampleListId("study_tcga_pub_gistic", "study_tcga_pub_all", 
                entrezGeneIds, alterations, "SUMMARY");

        Assert.assertEquals(3, result.size());
        DiscreteCopyNumberData discreteCopyNumberData = result.get(0);
        Assert.assertEquals("study_tcga_pub_gistic", discreteCopyNumberData.getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-A0SB-01", discreteCopyNumberData.getSampleId());
        Assert.assertEquals((Integer) 207, discreteCopyNumberData.getEntrezGeneId());
        Assert.assertEquals((Integer) (-2), discreteCopyNumberData.getAlteration());
        Assert.assertNull(discreteCopyNumberData.getGene());
    }

    @Test
    public void getDiscreteCopyNumbersInMolecularProfileBySampleListIdDetailedProjection() throws Exception {

        List<Integer> alterations = new ArrayList<>();
        alterations.add(-2);
        alterations.add(2);
        
        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(207);
        entrezGeneIds.add(208);

        List<DiscreteCopyNumberData> result =  discreteCopyNumberMyBatisRepository
            .getDiscreteCopyNumbersInMolecularProfileBySampleListId("study_tcga_pub_gistic", "study_tcga_pub_all", 
                entrezGeneIds, alterations, "DETAILED");

        Assert.assertEquals(3, result.size());
        DiscreteCopyNumberData discreteCopyNumberData = result.get(0);
        Assert.assertEquals("study_tcga_pub_gistic", discreteCopyNumberData.getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-A0SB-01", discreteCopyNumberData.getSampleId());
        Assert.assertEquals((Integer) 207, discreteCopyNumberData.getEntrezGeneId());
        Assert.assertEquals((Integer) (-2), discreteCopyNumberData.getAlteration());
        Gene gene = discreteCopyNumberData.getGene();
        Assert.assertEquals((Integer) 207, gene.getEntrezGeneId());
        Assert.assertEquals("AKT1", gene.getHugoGeneSymbol());
        ReferenceGenomeGene refGene = refGeneMyBatisRepository.getReferenceGenomeGene(gene.getEntrezGeneId(), "hg19");
        Assert.assertEquals("14q32.33", refGene.getCytoband());
        Assert.assertEquals((Integer) 10838, refGene.getLength());
    }

    @Test
    public void getMetaDiscreteCopyNumbersInMolecularProfileBySampleListId() throws Exception {

        List<Integer> alterations = new ArrayList<>();
        alterations.add(-2);
        alterations.add(2);

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(207);
        entrezGeneIds.add(208);

        BaseMeta result = discreteCopyNumberMyBatisRepository.getMetaDiscreteCopyNumbersInMolecularProfileBySampleListId(
            "study_tcga_pub_gistic", "study_tcga_pub_all", entrezGeneIds, alterations);
        
        Assert.assertEquals((Integer) 3, result.getTotalCount());
    }

    @Test
    public void fetchDiscreteCopyNumbersInMolecularProfile() throws Exception {

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
            .fetchDiscreteCopyNumbersInMolecularProfile("study_tcga_pub_gistic", sampleIds, entrezGeneIds, alterations, 
                "SUMMARY");
        
        Assert.assertEquals(3, result.size());
        Assert.assertEquals("study_tcga_pub_gistic", result.get(0).getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-A0SB-01", result.get(0).getSampleId());
        Assert.assertEquals("study_tcga_pub_gistic", result.get(1).getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-A0SD-01", result.get(1).getSampleId());
        Assert.assertEquals("study_tcga_pub_gistic", result.get(2).getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-A0SB-01", result.get(2).getSampleId());
    }

    @Test
    public void fetchMetaDiscreteCopyNumbersInMolecularProfile() throws Exception {

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
            .fetchMetaDiscreteCopyNumbersInMolecularProfile("study_tcga_pub_gistic", sampleIds, entrezGeneIds,
                alterations);

        Assert.assertEquals((Integer) 3, result.getTotalCount());
    }

    @Test
    public void getSampleCountByGeneAndAlterationAndSampleIds() throws Exception {

        List<CopyNumberCountByGene> result  = discreteCopyNumberMyBatisRepository
            .getSampleCountByGeneAndAlterationAndSampleIds("study_tcga_pub_gistic", null, 
                Arrays.asList(207, 208), Arrays.asList(-2, 2));
        
        Assert.assertEquals(2, result.size());
        CopyNumberCountByGene copyNumberSampleCountByGene1 = result.get(0);
        Assert.assertEquals((Integer) 207, copyNumberSampleCountByGene1.getEntrezGeneId());
        Assert.assertEquals((Integer) (-2), copyNumberSampleCountByGene1.getAlteration());
        Assert.assertEquals((Integer) 1, copyNumberSampleCountByGene1.getNumberOfAlteredCases());
        CopyNumberCountByGene copyNumberSampleCountByGene2 = result.get(1);
        Assert.assertEquals((Integer) 208, copyNumberSampleCountByGene2.getEntrezGeneId());
        Assert.assertEquals((Integer) (2), copyNumberSampleCountByGene2.getAlteration());
        Assert.assertEquals((Integer) 1, copyNumberSampleCountByGene2.getNumberOfAlteredCases());
    }

    @Test
    public void getPatientCountInMultipleMolecularProfiles() throws Exception {

        List<CopyNumberCountByGene> result  = discreteCopyNumberMyBatisRepository
            .getPatientCountInMultipleMolecularProfiles(Arrays.asList("study_tcga_pub_gistic"), null, 
                Arrays.asList(207, 208), Arrays.asList(-2, 2));
        
        Assert.assertEquals(2, result.size());
        CopyNumberCountByGene copyNumberSampleCountByGene1 = result.get(0);
        Assert.assertEquals((Integer) 207, copyNumberSampleCountByGene1.getEntrezGeneId());
        Assert.assertEquals((Integer) (-2), copyNumberSampleCountByGene1.getAlteration());
        Assert.assertEquals((Integer) 1, copyNumberSampleCountByGene1.getNumberOfAlteredCases());
        CopyNumberCountByGene copyNumberSampleCountByGene2 = result.get(1);
        Assert.assertEquals((Integer) 208, copyNumberSampleCountByGene2.getEntrezGeneId());
        Assert.assertEquals((Integer) (2), copyNumberSampleCountByGene2.getAlteration());
        Assert.assertEquals((Integer) 1, copyNumberSampleCountByGene2.getNumberOfAlteredCases());
    }
}
