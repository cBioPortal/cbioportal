package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.MutSig;
import org.cbioportal.model.meta.BaseMeta;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class SignificantlyMutatedGeneMyBatisRepositoryTest {

    @Autowired
    private SignificantlyMutatedGeneMyBatisRepository significantlyMutatedGeneMyBatisRepository;

    @Test
    public void getSignificantlyMutatedGenesIdProjection() throws Exception {

        List<MutSig> result = significantlyMutatedGeneMyBatisRepository.getSignificantlyMutatedGenes("study_tcga_pub", 
            "ID", null, null, null, null);

        Assert.assertEquals(2, result.size());
        MutSig mutSig = result.get(0);
        Assert.assertEquals((Integer) 207, mutSig.getEntrezGeneId());
    }

    @Test
    public void getSignificantlyMutatedGenesSummaryProjection() throws Exception {

        List<MutSig> result = significantlyMutatedGeneMyBatisRepository.getSignificantlyMutatedGenes("study_tcga_pub", 
            "SUMMARY", null, null, null, null);

        Assert.assertEquals(2, result.size());
        MutSig mutSig = result.get(0);
        Assert.assertEquals((Integer) 207, mutSig.getEntrezGeneId());
        Assert.assertEquals((Integer) 1, mutSig.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", mutSig.getCancerStudyIdentifier());
        Assert.assertEquals("AKT1", mutSig.getHugoGeneSymbol());
        Assert.assertEquals((Integer) 998421, mutSig.getNumbasescovered());
        Assert.assertEquals((Integer) 17, mutSig.getNummutations());
        Assert.assertEquals((Integer) 1, mutSig.getRank());
        Assert.assertEquals(new BigDecimal("0.00000315"), mutSig.getpValue());
        Assert.assertEquals(new BigDecimal("0.00233"), mutSig.getqValue());
    }

    @Test
    public void getSignificantlyMutatedGenesDetailedProjection() throws Exception {

        List<MutSig> result = significantlyMutatedGeneMyBatisRepository.getSignificantlyMutatedGenes("study_tcga_pub",
            "DETAILED", null, null, null, null);

        Assert.assertEquals(2, result.size());
        MutSig mutSig = result.get(0);
        Assert.assertEquals((Integer) 207, mutSig.getEntrezGeneId());
        Assert.assertEquals((Integer) 1, mutSig.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", mutSig.getCancerStudyIdentifier());
        Assert.assertEquals("AKT1", mutSig.getHugoGeneSymbol());
        Assert.assertEquals((Integer) 998421, mutSig.getNumbasescovered());
        Assert.assertEquals((Integer) 17, mutSig.getNummutations());
        Assert.assertEquals((Integer) 1, mutSig.getRank());
        Assert.assertEquals(new BigDecimal("0.00000315"), mutSig.getpValue());
        Assert.assertEquals(new BigDecimal("0.00233"), mutSig.getqValue());
    }

    @Test
    public void getSignificantlyMutatedGenesSummaryProjection1PageSize() throws Exception {

        List<MutSig> result = significantlyMutatedGeneMyBatisRepository.getSignificantlyMutatedGenes("study_tcga_pub",
            "SUMMARY", 1, 0, null, null);

        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getSignificantlyMutatedGenesSummaryProjectionPValueSort() throws Exception {

        List<MutSig> result = significantlyMutatedGeneMyBatisRepository.getSignificantlyMutatedGenes("study_tcga_pub",
            "SUMMARY", null, null, "pValue", "ASC");

        Assert.assertEquals(2, result.size());
        Assert.assertEquals(new BigDecimal("0.000000012"), result.get(0).getpValue());
        Assert.assertEquals(new BigDecimal("0.00000315"), result.get(1).getpValue());
    }

    @Test
    public void getMetaSignificantlyMutatedGenes() throws Exception {

        BaseMeta result = significantlyMutatedGeneMyBatisRepository.getMetaSignificantlyMutatedGenes("study_tcga_pub");

        Assert.assertEquals((Integer) 2, result.getTotalCount());
    }
}