package org.cbioportal.persistence.mybatis;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.cbioportal.model.Gistic;
import org.cbioportal.model.GisticToGene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.mybatis.config.TestConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {SignificantCopyNumberRegionMyBatisRepository.class, TestConfig.class})
public class SignificantCopyNumberRegionMyBatisRepositoryTest {
    
    @Autowired
    private SignificantCopyNumberRegionMyBatisRepository significantCopyNumberRegionMyBatisRepository;
    
    @Test
    public void getSignificantCopyNumberRegionsIdProjection() throws Exception {

        List<Gistic> result = significantCopyNumberRegionMyBatisRepository.getSignificantCopyNumberRegions(
            "study_tcga_pub", "ID", null, null, null, null);

        Assert.assertEquals(2, result.size());
        Gistic gistic = result.get(0);
        Assert.assertEquals((Long) 1L, gistic.getGisticRoiId());
        Assert.assertEquals("study_tcga_pub", gistic.getCancerStudyId());
        Assert.assertEquals((Integer) 1, gistic.getChromosome());
        Assert.assertEquals("1q32.32", gistic.getCytoband());
    }

    @Test
    public void getSignificantCopyNumberRegionsSummaryProjection() throws Exception {

        List<Gistic> result = significantCopyNumberRegionMyBatisRepository.getSignificantCopyNumberRegions(
            "study_tcga_pub", "SUMMARY", null, null, null, null);

        Assert.assertEquals(2, result.size());
        Gistic gistic = result.get(0);
        Assert.assertEquals((Long) 1L, gistic.getGisticRoiId());
        Assert.assertEquals("study_tcga_pub", gistic.getCancerStudyId());
        Assert.assertEquals((Integer) 1, gistic.getChromosome());
        Assert.assertEquals("1q32.32", gistic.getCytoband());
        Assert.assertEquals((Integer) 123, gistic.getWidePeakStart());
        Assert.assertEquals((Integer) 136, gistic.getWidePeakEnd());
        Assert.assertEquals(new BigDecimal("0.0208839997649193"), gistic.getqValue());
        Assert.assertEquals(false, gistic.getAmp());
    }

    @Test
    public void getSignificantCopyNumberRegionsDetailedProjection() throws Exception {

        List<Gistic> result = significantCopyNumberRegionMyBatisRepository.getSignificantCopyNumberRegions(
            "study_tcga_pub", "DETAILED", null, null, null, null);

        Assert.assertEquals(2, result.size());
        Gistic gistic = result.get(0);
        Assert.assertEquals((Long) 1L, gistic.getGisticRoiId());
        Assert.assertEquals("study_tcga_pub", gistic.getCancerStudyId());
        Assert.assertEquals((Integer) 1, gistic.getChromosome());
        Assert.assertEquals("1q32.32", gistic.getCytoband());
        Assert.assertEquals((Integer) 123, gistic.getWidePeakStart());
        Assert.assertEquals((Integer) 136, gistic.getWidePeakEnd());
        Assert.assertEquals(new BigDecimal("0.0208839997649193"), gistic.getqValue());
        Assert.assertEquals(false, gistic.getAmp());
    }

    @Test
    public void getSignificantCopyNumberRegionsSummaryProjection1PageSize() throws Exception {

        List<Gistic> result = significantCopyNumberRegionMyBatisRepository.getSignificantCopyNumberRegions(
            "study_tcga_pub", "SUMMARY", 1, 0, null, null);

        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getSignificantCopyNumberRegionsSummaryProjectionQValueSort() throws Exception {

        List<Gistic> result = significantCopyNumberRegionMyBatisRepository.getSignificantCopyNumberRegions(
            "study_tcga_pub", "SUMMARY", null, null, "qValue", "ASC");

        Assert.assertEquals(2, result.size());
        Assert.assertEquals(new BigDecimal("0.000323799991747364"), result.get(0).getqValue());
        Assert.assertEquals(new BigDecimal("0.0208839997649193"), result.get(1).getqValue());
    }

    @Test
    public void getMetaSignificantCopyNumberRegions() throws Exception {

        BaseMeta result = significantCopyNumberRegionMyBatisRepository.getMetaSignificantCopyNumberRegions(
            "study_tcga_pub");

        Assert.assertEquals((Integer) 2, result.getTotalCount());
    }

    @Test
    public void getGenesOfRegions() throws Exception {

        List<Long> gisticRoiIds = new ArrayList<>();
        gisticRoiIds.add(1L);
        gisticRoiIds.add(2L);
        List<GisticToGene> result = significantCopyNumberRegionMyBatisRepository.getGenesOfRegions(gisticRoiIds);
        
        Assert.assertEquals(3, result.size());
        GisticToGene gisticToGene1 = result.get(0);
        Assert.assertEquals((Integer) 207, gisticToGene1.getEntrezGeneId());
        Assert.assertEquals("AKT1", gisticToGene1.getHugoGeneSymbol());
        GisticToGene gisticToGene2 = result.get(1);
        Assert.assertEquals((Integer) 208, gisticToGene2.getEntrezGeneId());
        Assert.assertEquals("AKT2", gisticToGene2.getHugoGeneSymbol());
        GisticToGene gisticToGene3 = result.get(2);
        Assert.assertEquals((Integer) 207, gisticToGene3.getEntrezGeneId());
        Assert.assertEquals("AKT1", gisticToGene3.getHugoGeneSymbol());
    }
}