package org.cbioportal.persistence.mybatis;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.CancerStudyTags;
import org.cbioportal.model.TypeOfCancer;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.mybatis.config.TestConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {StudyMyBatisRepository.class, TestConfig.class})
public class StudyMyBatisRepositoryTest {

    @Autowired
    private StudyMyBatisRepository studyMyBatisRepository;

    @Test
    public void getAllStudiesIdProjection() throws Exception {

        List<CancerStudy> result = studyMyBatisRepository.getAllStudies(null, "ID", null, null, null, null);

        Assert.assertEquals(2, result.size());
        CancerStudy cancerStudy = result.get(0);
        Assert.assertEquals((Integer) 2, cancerStudy.getCancerStudyId());
        Assert.assertEquals("acc_tcga", cancerStudy.getCancerStudyIdentifier());
        Assert.assertNull(cancerStudy.getTypeOfCancer());
    }

    @Test
    public void getAllStudiesSummaryProjection() throws Exception {

        List<CancerStudy> result = studyMyBatisRepository.getAllStudies(null, "SUMMARY", null, null, null, null);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        Assert.assertEquals(2, result.size());
        CancerStudy cancerStudy = result.get(0);
        Assert.assertEquals((Integer) 1, cancerStudy.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", cancerStudy.getCancerStudyIdentifier());
        Assert.assertEquals("brca", cancerStudy.getTypeOfCancerId());
        Assert.assertEquals("Breast Invasive Carcinoma (TCGA, Nature 2012)", cancerStudy.getName());
        Assert.assertEquals("<a href=\\\"http://cancergenome.nih.gov/\\\">The Cancer Genome Atlas (TCGA)</a> Breast" +
                " Invasive Carcinoma project. 825 cases.<br><i>Nature 2012.</i> <a href=\\\"http://tcga-data.nci." +
                "nih.gov/tcga/\\\">Raw data via the TCGA Data Portal</a>.", cancerStudy.getDescription());
        Assert.assertEquals(true, cancerStudy.getPublicStudy());
        Assert.assertEquals("23000897,26451490", cancerStudy.getPmid());
        Assert.assertEquals("TCGA, Nature 2012, ...", cancerStudy.getCitation());
        Assert.assertEquals("SU2C-PI3K;PUBLIC;GDAC", cancerStudy.getGroups());
        Assert.assertEquals((Integer) 0 , cancerStudy.getStatus());
        Assert.assertEquals(simpleDateFormat.parse("2011-12-18 13:17:17+00:00"), cancerStudy.getImportDate());
        Assert.assertEquals((Integer) 14, cancerStudy.getAllSampleCount());
        Assert.assertNull(cancerStudy.getTypeOfCancer());
    }

    @Test
    public void getAllStudiesDetailedProjection() throws Exception {

        List<CancerStudy> result = studyMyBatisRepository.getAllStudies(null, "DETAILED", null, null, null, null);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        Assert.assertEquals(2, result.size());
        CancerStudy cancerStudy = result.get(0);
        Assert.assertEquals((Integer) 1, cancerStudy.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", cancerStudy.getCancerStudyIdentifier());
        Assert.assertEquals("brca", cancerStudy.getTypeOfCancerId());
        Assert.assertEquals("Breast Invasive Carcinoma (TCGA, Nature 2012)", cancerStudy.getName());
        Assert.assertEquals("<a href=\\\"http://cancergenome.nih.gov/\\\">The Cancer Genome Atlas (TCGA)</a> Breast" +
                " Invasive Carcinoma project. 825 cases.<br><i>Nature 2012.</i> <a href=\\\"http://tcga-data.nci." +
                "nih.gov/tcga/\\\">Raw data via the TCGA Data Portal</a>.", cancerStudy.getDescription());
        Assert.assertEquals(true, cancerStudy.getPublicStudy());
        Assert.assertEquals("23000897,26451490", cancerStudy.getPmid());
        Assert.assertEquals("TCGA, Nature 2012, ...", cancerStudy.getCitation());
        Assert.assertEquals("SU2C-PI3K;PUBLIC;GDAC", cancerStudy.getGroups());
        Assert.assertEquals((Integer) 0 , cancerStudy.getStatus());
        Assert.assertEquals(simpleDateFormat.parse("2011-12-18 13:17:17"), cancerStudy.getImportDate());
        Assert.assertEquals((Integer) 14, cancerStudy.getAllSampleCount());
        Assert.assertEquals((Integer) 7, cancerStudy.getCnaSampleCount());
        Assert.assertEquals((Integer) 7, cancerStudy.getCompleteSampleCount());
        Assert.assertEquals((Integer) 1, cancerStudy.getMethylationHm27SampleCount());
        Assert.assertEquals((Integer) 0, cancerStudy.getMiRnaSampleCount());
        Assert.assertEquals((Integer) 8, cancerStudy.getMrnaMicroarraySampleCount());
        Assert.assertEquals((Integer) 0, cancerStudy.getMrnaRnaSeqSampleCount());
        Assert.assertEquals((Integer) 7, cancerStudy.getMrnaRnaSeqV2SampleCount());
        Assert.assertEquals((Integer) 0, cancerStudy.getRppaSampleCount());
        Assert.assertEquals((Integer) 7, cancerStudy.getSequencedSampleCount());
        TypeOfCancer typeOfCancer = cancerStudy.getTypeOfCancer();
        Assert.assertEquals("brca", typeOfCancer.getTypeOfCancerId());
        Assert.assertEquals("Breast Invasive Carcinoma", typeOfCancer.getName());
        Assert.assertEquals("HotPink", typeOfCancer.getDedicatedColor());
        Assert.assertEquals("Breast", typeOfCancer.getShortName());
        Assert.assertEquals("tissue", typeOfCancer.getParent());
    }

    @Test
    public void getAllStudiesSummaryProjection1PageSize() throws Exception {

        List<CancerStudy> result = studyMyBatisRepository.getAllStudies(null, "SUMMARY", 1, 0, null, null);

        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getAllStudiesSummaryProjectionCancerStudyIdentifierSort() throws Exception {

        List<CancerStudy> result = studyMyBatisRepository.getAllStudies(null, "SUMMARY", null, null, "cancerStudyIdentifier",
                "ASC");

        Assert.assertEquals(2, result.size());
        Assert.assertEquals("acc_tcga", result.get(0).getCancerStudyIdentifier());
        Assert.assertEquals("study_tcga_pub", result.get(1).getCancerStudyIdentifier());
    }

    @Test
    public void getMetaStudies() throws Exception {

        BaseMeta result = studyMyBatisRepository.getMetaStudies(null);

        Assert.assertEquals((Integer) 2, result.getTotalCount());
    }

    @Test
    public void getStudyNullResult() throws Exception {

        CancerStudy result = studyMyBatisRepository.getStudy("invalid_study", "DETAILED");

        Assert.assertNull(result);
    }

    @Test
    public void getStudy() throws Exception {

        CancerStudy result = studyMyBatisRepository.getStudy("study_tcga_pub", "DETAILED");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        Assert.assertEquals((Integer) 1, result.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", result.getCancerStudyIdentifier());
        Assert.assertEquals("brca", result.getTypeOfCancerId());
        Assert.assertEquals("Breast Invasive Carcinoma (TCGA, Nature 2012)", result.getName());
        Assert.assertEquals("<a href=\\\"http://cancergenome.nih.gov/\\\">The Cancer Genome Atlas (TCGA)</a> Breast" +
                " Invasive Carcinoma project. 825 cases.<br><i>Nature 2012.</i> <a href=\\\"http://tcga-data.nci." +
                "nih.gov/tcga/\\\">Raw data via the TCGA Data Portal</a>.", result.getDescription());
        Assert.assertEquals(true, result.getPublicStudy());
        Assert.assertEquals("23000897,26451490", result.getPmid());
        Assert.assertEquals("TCGA, Nature 2012, ...", result.getCitation());
        Assert.assertEquals("SU2C-PI3K;PUBLIC;GDAC", result.getGroups());
        Assert.assertEquals((Integer) 0 , result.getStatus());
        Assert.assertEquals(simpleDateFormat.parse("2011-12-18 13:17:17"), result.getImportDate());
        Assert.assertEquals((Integer) 14, result.getAllSampleCount());
        Assert.assertEquals((Integer) 7, result.getCnaSampleCount());
        Assert.assertEquals((Integer) 7, result.getCompleteSampleCount());
        Assert.assertEquals((Integer) 1, result.getMethylationHm27SampleCount());
        Assert.assertEquals((Integer) 0, result.getMiRnaSampleCount());
        Assert.assertEquals((Integer) 8, result.getMrnaMicroarraySampleCount());
        Assert.assertEquals((Integer) 0, result.getMrnaRnaSeqSampleCount());
        Assert.assertEquals((Integer) 7, result.getMrnaRnaSeqV2SampleCount());
        Assert.assertEquals((Integer) 0, result.getRppaSampleCount());
        Assert.assertEquals((Integer) 7, result.getSequencedSampleCount());
        TypeOfCancer typeOfCancer = result.getTypeOfCancer();
        Assert.assertEquals("brca", typeOfCancer.getTypeOfCancerId());
        Assert.assertEquals("Breast Invasive Carcinoma", typeOfCancer.getName());
        Assert.assertEquals("HotPink", typeOfCancer.getDedicatedColor());
        Assert.assertEquals("Breast", typeOfCancer.getShortName());
        Assert.assertEquals("tissue", typeOfCancer.getParent());
    }

    @Test
    public void fetchStudies() throws Exception {

        List<CancerStudy> result = studyMyBatisRepository.fetchStudies(Arrays.asList("study_tcga_pub", "acc_tcga"), "SUMMARY");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        Assert.assertEquals(2, result.size());
        CancerStudy cancerStudy = result.get(0);
        Assert.assertEquals((Integer) 1, cancerStudy.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", cancerStudy.getCancerStudyIdentifier());
        Assert.assertEquals("brca", cancerStudy.getTypeOfCancerId());
        Assert.assertEquals("Breast Invasive Carcinoma (TCGA, Nature 2012)", cancerStudy.getName());
        Assert.assertEquals("<a href=\\\"http://cancergenome.nih.gov/\\\">The Cancer Genome Atlas (TCGA)</a> Breast" +
                " Invasive Carcinoma project. 825 cases.<br><i>Nature 2012.</i> <a href=\\\"http://tcga-data.nci." +
                "nih.gov/tcga/\\\">Raw data via the TCGA Data Portal</a>.", cancerStudy.getDescription());
        Assert.assertEquals(true, cancerStudy.getPublicStudy());
        Assert.assertEquals("23000897,26451490", cancerStudy.getPmid());
        Assert.assertEquals("TCGA, Nature 2012, ...", cancerStudy.getCitation());
        Assert.assertEquals("SU2C-PI3K;PUBLIC;GDAC", cancerStudy.getGroups());
        Assert.assertEquals((Integer) 0 , cancerStudy.getStatus());
        Assert.assertEquals(simpleDateFormat.parse("2011-12-18 13:17:17"), cancerStudy.getImportDate());
        Assert.assertEquals((Integer) 14, cancerStudy.getAllSampleCount());
        Assert.assertNull(cancerStudy.getTypeOfCancer());
    }

    @Test
    public void fetchMetaStudies() throws Exception {

        BaseMeta result = studyMyBatisRepository.fetchMetaStudies(Arrays.asList("study_tcga_pub", "acc_tcga"));

        Assert.assertEquals((Integer) 2, result.getTotalCount());
    }
    
    @Test
    public void getTags() throws Exception {

        CancerStudyTags result = studyMyBatisRepository.getTags("study_tcga_pub");
 
        Assert.assertEquals("{\"Analyst\": {\"Name\": \"Jack\", \"Email\": \"jack@something.com\"}, \"Load id\": 35}", result.getTags());
    }
    
    @Test
    public void getMultipleTags() throws Exception {

        List<CancerStudyTags> result = studyMyBatisRepository.getTagsForMultipleStudies(Arrays.asList("study_tcga_pub", "acc_tcga"));

        Assert.assertEquals(2, result.size());
        CancerStudyTags cancerStudyTags1 = result.get(1);
        Assert.assertEquals((Integer) 1, cancerStudyTags1.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", cancerStudyTags1.getStudyId());
        Assert.assertEquals("{\"Analyst\": {\"Name\": \"Jack\", \"Email\": \"jack@something.com\"}, \"Load id\": 35}", cancerStudyTags1.getTags());
        CancerStudyTags cancerStudyTags2 = result.get(0);
        Assert.assertEquals((Integer) 2, cancerStudyTags2.getCancerStudyId());
        Assert.assertEquals("acc_tcga", cancerStudyTags2.getStudyId());
        Assert.assertEquals("{\"Load id\": 36}", cancerStudyTags2.getTags());
    }
}
