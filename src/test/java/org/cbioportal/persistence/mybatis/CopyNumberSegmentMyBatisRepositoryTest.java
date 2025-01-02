package org.cbioportal.persistence.mybatis;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.mybatis.config.TestConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {CopyNumberSegmentMyBatisRepository.class, TestConfig.class})
public class CopyNumberSegmentMyBatisRepositoryTest {

    @Autowired
    private CopyNumberSegmentMyBatisRepository copyNumberSegmentMyBatisRepository;

    @Test
    public void getCopyNumberSegmentsInSampleInStudySummaryProjection() throws Exception {

        List<CopyNumberSeg> result0 = copyNumberSegmentMyBatisRepository.getCopyNumberSegmentsInSampleInStudy(
            "study_tcga_pub", "TCGA-A1-A0SB-01", null,
            "SUMMARY", null, null, null, null);
        List<CopyNumberSeg> result1 = copyNumberSegmentMyBatisRepository.getCopyNumberSegmentsInSampleInStudy(
            "study_tcga_pub", "TCGA-A1-A0SB-01", "1",
            "SUMMARY", null, null, null, null);
        List<CopyNumberSeg> result2 = copyNumberSegmentMyBatisRepository.getCopyNumberSegmentsInSampleInStudy(
            "study_tcga_pub", "TCGA-A1-A0SB-01", "2",
            "SUMMARY", null, null, null, null);
        List<CopyNumberSeg> result3 = copyNumberSegmentMyBatisRepository.getCopyNumberSegmentsInSampleInStudy(
            "study_tcga_pub", "TCGA-A1-A0SB-01", "3",
            "SUMMARY", null, null, null, null);
        
        Assert.assertEquals(2, result0.size());
        CopyNumberSeg copyNumberSeg = result0.get(0);
        Assert.assertEquals(Long.valueOf(50236594L), copyNumberSeg.getSegId());
        Assert.assertEquals((Integer) 1, copyNumberSeg.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", copyNumberSeg.getCancerStudyIdentifier());
        Assert.assertEquals((Integer) 1, copyNumberSeg.getSampleId());
        Assert.assertEquals("TCGA-A1-A0SB-01", copyNumberSeg.getSampleStableId());
        Assert.assertEquals("1", copyNumberSeg.getChr());
        Assert.assertEquals((Integer) 324556, copyNumberSeg.getStart());
        Assert.assertEquals((Integer) 180057677, copyNumberSeg.getEnd());
        Assert.assertEquals((Integer) 291, copyNumberSeg.getNumProbes());
        Assert.assertEquals(new BigDecimal("0.0519"), copyNumberSeg.getSegmentMean());

        Assert.assertEquals(1, result1.size());
        Assert.assertEquals(1, result2.size());
        Assert.assertEquals(0, result3.size());
    }
    
    @Test
    public void fetchSamplesWithCopyNumberSegments() throws Exception {
        List<String> studies = new ArrayList<>();
        studies.add("acc_tcga");
        List<String> samples = new ArrayList<>();
        samples.add("TCGA-A1-B0SP-01");
        List<Integer> emptyResult = copyNumberSegmentMyBatisRepository.fetchSamplesWithCopyNumberSegments(
                studies, samples, null
        );
        Assert.assertEquals(0, emptyResult.size());
        
        studies = new ArrayList<>();
        studies.add("study_tcga_pub");
        studies.add("acc_tcga");
        studies.add("acc_tcga");
        samples = new ArrayList<>();
        samples.add("TCGA-A1-A0SB-01");
        samples.add("TCGA-A1-B0SP-01");
        samples.add("TCGA-A1-B0SO-01");
        List<Integer> result0 = copyNumberSegmentMyBatisRepository.fetchSamplesWithCopyNumberSegments(
                studies, samples, null
        );
        List<Integer> result1 = copyNumberSegmentMyBatisRepository.fetchSamplesWithCopyNumberSegments(
            studies, samples, "1"
        );
        List<Integer> result2 = copyNumberSegmentMyBatisRepository.fetchSamplesWithCopyNumberSegments(
            studies, samples, "2"
        );
        List<Integer> result3 = copyNumberSegmentMyBatisRepository.fetchSamplesWithCopyNumberSegments(
            studies, samples, "3"
        );
        
        Assert.assertEquals(2, result0.size());
        Assert.assertEquals((Integer)1, result0.get(1));
        Assert.assertEquals((Integer)15, result0.get(0));

        Assert.assertEquals(1, result1.size());
        Assert.assertEquals((Integer)1, result1.get(0));

        Assert.assertEquals(2, result2.size());
        Assert.assertEquals((Integer)1, result2.get(1));
        Assert.assertEquals((Integer)15, result2.get(0));

        Assert.assertEquals(0, result3.size());
    }

    @Test
    public void getCopyNumberSegmentsInSampleInStudyDetailedProjection() throws Exception {

        List<CopyNumberSeg> result = copyNumberSegmentMyBatisRepository.getCopyNumberSegmentsInSampleInStudy(
            "study_tcga_pub", "TCGA-A1-A0SB-01", null,
            "DETAILED", null, null, null, null);

        Assert.assertEquals(2, result.size());
        CopyNumberSeg copyNumberSeg = result.get(0);
        Assert.assertEquals(Long.valueOf(50236594L), copyNumberSeg.getSegId());
        Assert.assertEquals((Integer) 1, copyNumberSeg.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", copyNumberSeg.getCancerStudyIdentifier());
        Assert.assertEquals((Integer) 1, copyNumberSeg.getSampleId());
        Assert.assertEquals("TCGA-A1-A0SB-01", copyNumberSeg.getSampleStableId());
        Assert.assertEquals("1", copyNumberSeg.getChr());
        Assert.assertEquals((Integer) 324556, copyNumberSeg.getStart());
        Assert.assertEquals((Integer) 180057677, copyNumberSeg.getEnd());
        Assert.assertEquals((Integer) 291, copyNumberSeg.getNumProbes());
        Assert.assertEquals(new BigDecimal("0.0519"), copyNumberSeg.getSegmentMean());
    }

    @Test
    public void getCopyNumberSegmentsInSampleInStudySummaryProjection1PageSize() throws Exception {

        List<CopyNumberSeg> result = copyNumberSegmentMyBatisRepository.getCopyNumberSegmentsInSampleInStudy(
            "study_tcga_pub", "TCGA-A1-A0SB-01", null, 
            "SUMMARY", 1, 0, null, null);
        
        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getCopyNumberSegmentsInSampleInStudySummaryProjectionStartSort() throws Exception {

        List<CopyNumberSeg> result = copyNumberSegmentMyBatisRepository.getCopyNumberSegmentsInSampleInStudy(
            "study_tcga_pub", "TCGA-A1-A0SB-01", null, 
            "SUMMARY", null, null, "start", "ASC");
        
        Assert.assertEquals(2, result.size());
        Assert.assertEquals((Integer) 224556, result.get(0).getStart());
        Assert.assertEquals((Integer) 324556, result.get(1).getStart());
    }

    @Test
    public void getMetaCopyNumberSegmentsInSampleInStudy() throws Exception {

        BaseMeta result0 = copyNumberSegmentMyBatisRepository.getMetaCopyNumberSegmentsInSampleInStudy(
            "study_tcga_pub", "TCGA-A1-A0SB-01", null);
        BaseMeta result1 = copyNumberSegmentMyBatisRepository.getMetaCopyNumberSegmentsInSampleInStudy(
            "study_tcga_pub", "TCGA-A1-A0SB-01", "1");
        BaseMeta result2 = copyNumberSegmentMyBatisRepository.getMetaCopyNumberSegmentsInSampleInStudy(
            "study_tcga_pub", "TCGA-A1-A0SB-01", "2");
        BaseMeta result3 = copyNumberSegmentMyBatisRepository.getMetaCopyNumberSegmentsInSampleInStudy(
            "study_tcga_pub", "TCGA-A1-A0SB-01", "3");

        Assert.assertEquals((Integer) 2, result0.getTotalCount());
        Assert.assertEquals((Integer) 1, result1.getTotalCount());
        Assert.assertEquals((Integer) 1, result2.getTotalCount());
        Assert.assertEquals((Integer) 0, result3.getTotalCount());
    }

    @Test
    public void fetchCopyNumberSegments() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        studyIds.add("acc_tcga");
        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SB-01");
        sampleIds.add("TCGA-A1-B0SO-01");
        
        List<CopyNumberSeg> result0 = copyNumberSegmentMyBatisRepository.fetchCopyNumberSegments(
            studyIds, sampleIds, null, "SUMMARY");
        List<CopyNumberSeg> result1 = copyNumberSegmentMyBatisRepository.fetchCopyNumberSegments(
            studyIds, sampleIds, "1", "SUMMARY");
        List<CopyNumberSeg> result2 = copyNumberSegmentMyBatisRepository.fetchCopyNumberSegments(
            studyIds, sampleIds, "2", "SUMMARY");
        List<CopyNumberSeg> result3 = copyNumberSegmentMyBatisRepository.fetchCopyNumberSegments(
            studyIds, sampleIds, "3", "SUMMARY");
        
        Assert.assertEquals(3, result0.size());
        Assert.assertEquals("TCGA-A1-A0SB-01", result0.get(0).getSampleStableId());
        Assert.assertEquals("TCGA-A1-A0SB-01", result0.get(1).getSampleStableId());
        Assert.assertEquals("TCGA-A1-B0SO-01", result0.get(2).getSampleStableId());

        Assert.assertEquals(1, result1.size());
        Assert.assertEquals("TCGA-A1-A0SB-01", result1.get(0).getSampleStableId());

        Assert.assertEquals(2, result2.size());
        Assert.assertEquals("TCGA-A1-A0SB-01", result2.get(0).getSampleStableId());
        Assert.assertEquals("TCGA-A1-B0SO-01", result2.get(1).getSampleStableId());

        Assert.assertEquals(0, result3.size());
    }

    @Test
    public void fetchMetaCopyNumberSegments() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        studyIds.add("acc_tcga");
        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SB-01");
        sampleIds.add("TCGA-A1-B0SO-01");

        BaseMeta result0 = copyNumberSegmentMyBatisRepository.fetchMetaCopyNumberSegments(studyIds, sampleIds, null);
        BaseMeta result1 = copyNumberSegmentMyBatisRepository.fetchMetaCopyNumberSegments(studyIds, sampleIds, "1");
        BaseMeta result2 = copyNumberSegmentMyBatisRepository.fetchMetaCopyNumberSegments(studyIds, sampleIds, "2");
        BaseMeta result3 = copyNumberSegmentMyBatisRepository.fetchMetaCopyNumberSegments(studyIds, sampleIds, "3");

        Assert.assertEquals((Integer) 3, result0.getTotalCount());
        Assert.assertEquals((Integer) 1, result1.getTotalCount());
        Assert.assertEquals((Integer) 2, result2.getTotalCount());
        Assert.assertEquals((Integer) 0, result3.getTotalCount());
    }

    @Test
    public void getCopyNumberSegmentsBySampleListId() throws Exception {
        
        List<CopyNumberSeg> result0 = copyNumberSegmentMyBatisRepository.getCopyNumberSegmentsBySampleListId(
            "study_tcga_pub", "study_tcga_pub_methylation_hm27", null, "SUMMARY");
        List<CopyNumberSeg> result1 = copyNumberSegmentMyBatisRepository.getCopyNumberSegmentsBySampleListId(
            "study_tcga_pub", "study_tcga_pub_methylation_hm27", "1", "SUMMARY");
        List<CopyNumberSeg> result2 = copyNumberSegmentMyBatisRepository.getCopyNumberSegmentsBySampleListId(
            "study_tcga_pub", "study_tcga_pub_methylation_hm27", "2", "SUMMARY");

        Assert.assertEquals(1, result0.size());
        CopyNumberSeg copyNumberSeg = result0.get(0);
        Assert.assertEquals(Long.valueOf(50236593L), copyNumberSeg.getSegId());
        Assert.assertEquals((Integer) 1, copyNumberSeg.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", copyNumberSeg.getCancerStudyIdentifier());
        Assert.assertEquals((Integer) 2, copyNumberSeg.getSampleId());
        Assert.assertEquals("TCGA-A1-A0SD-01", copyNumberSeg.getSampleStableId());
        Assert.assertEquals("2", copyNumberSeg.getChr());
        Assert.assertEquals((Integer) 1402650, copyNumberSeg.getStart());
        Assert.assertEquals((Integer) 190262486, copyNumberSeg.getEnd());
        Assert.assertEquals((Integer) 207, copyNumberSeg.getNumProbes());
        Assert.assertEquals(new BigDecimal("0.0265"), copyNumberSeg.getSegmentMean());

        Assert.assertEquals(0, result1.size());
        
        Assert.assertEquals(1, result2.size());
    }
}
