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

import java.util.*;

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
        //We do not test order here
        result.sort(new Comparator<DiscreteCopyNumberData>() {
            @Override
            public int compare(DiscreteCopyNumberData o1, DiscreteCopyNumberData o2) {
                int sampleIdCompare = o1.getSampleId().compareTo(o2.getSampleId());
                if (sampleIdCompare == 0) {
                    return o1.getEntrezGeneId().compareTo(o2.getEntrezGeneId());
                }
                return sampleIdCompare;
            }
        });
        DiscreteCopyNumberData discreteCopyNumberDataB207 = result.get(0);
        Assert.assertEquals("study_tcga_pub_gistic", discreteCopyNumberDataB207.getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-A0SB-01", discreteCopyNumberDataB207.getSampleId());
        Assert.assertEquals((Integer) 207, discreteCopyNumberDataB207.getEntrezGeneId());
        Assert.assertEquals((Integer) (-2), discreteCopyNumberDataB207.getAlteration());
        Assert.assertEquals("Putative_Driver", discreteCopyNumberDataB207.getDriverFilter());
        Assert.assertEquals("Pathogenic", discreteCopyNumberDataB207.getDriverFilterAnnotation());
        Assert.assertEquals("Tier 1", discreteCopyNumberDataB207.getDriverTiersFilter());
        Assert.assertEquals("Highly Actionable", discreteCopyNumberDataB207.getDriverTiersFilterAnnotation());
        Gene gene = discreteCopyNumberDataB207.getGene();
        Assert.assertEquals((Integer) 207, gene.getEntrezGeneId());
        Assert.assertEquals("AKT1", gene.getHugoGeneSymbol());
        ReferenceGenomeGene refGene = refGeneMyBatisRepository.getReferenceGenomeGene(gene.getEntrezGeneId(), "hg19");
        Assert.assertEquals("14q32.33", refGene.getCytoband());
        Assert.assertEquals((Integer) 10838, refGene.getLength());

        DiscreteCopyNumberData discreteCopyNumberDataB208 = result.get(1);
        Assert.assertEquals("study_tcga_pub_gistic", discreteCopyNumberDataB208.getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-A0SB-01", discreteCopyNumberDataB208.getSampleId());
        Assert.assertEquals((Integer) 208, discreteCopyNumberDataB208.getEntrezGeneId());
        Assert.assertEquals((Integer) (2), discreteCopyNumberDataB208.getAlteration());
        Assert.assertNull(discreteCopyNumberDataB208.getDriverFilter());
        Assert.assertNull(discreteCopyNumberDataB208.getDriverFilterAnnotation());
        Assert.assertNull(discreteCopyNumberDataB208.getDriverTiersFilter());
        Assert.assertNull(discreteCopyNumberDataB208.getDriverTiersFilterAnnotation());

        DiscreteCopyNumberData discreteCopyNumberDataD207 = result.get(2);
        Assert.assertEquals("study_tcga_pub_gistic", discreteCopyNumberDataD207.getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-A0SD-01", discreteCopyNumberDataD207.getSampleId());
        Assert.assertEquals((Integer) 207, discreteCopyNumberDataD207.getEntrezGeneId());
        Assert.assertEquals((Integer) (2), discreteCopyNumberDataD207.getAlteration());
        Assert.assertEquals("Putative_Passenger", discreteCopyNumberDataD207.getDriverFilter());
        Assert.assertEquals("Pathogenic", discreteCopyNumberDataD207.getDriverFilterAnnotation());
        Assert.assertEquals("Tier 2", discreteCopyNumberDataD207.getDriverTiersFilter());
        Assert.assertEquals("Potentially Actionable", discreteCopyNumberDataD207.getDriverTiersFilterAnnotation());
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

}
