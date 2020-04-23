package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.SampleList;
import org.cbioportal.model.meta.BaseMeta;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class SampleListMyBatisRepositoryTest {

    @Autowired
    private SampleListMyBatisRepository sampleListMyBatisRepository;
    
    @Test
    public void getAllSampleListsIdProjection() throws Exception {

        List<SampleList> result = sampleListMyBatisRepository.getAllSampleLists("ID", null, null, null,
            null);

        Assert.assertEquals(14, result.size());
        SampleList sampleList = result.get(0);
        Assert.assertEquals((Integer) 14, sampleList.getListId());
        Assert.assertEquals("acc_tcga_all", sampleList.getStableId());
        Assert.assertNull(sampleList.getCancerStudy());
    }

    @Test
    public void getAllSampleListsSummaryProjection() throws Exception {

        List<SampleList> result = sampleListMyBatisRepository.getAllSampleLists("SUMMARY", null, null, null,
            null);

        Assert.assertEquals(14, result.size());
        SampleList sampleList = result.get(0);
        Assert.assertEquals((Integer) 1, sampleList.getListId());
        Assert.assertEquals("study_tcga_pub_all", sampleList.getStableId());
        Assert.assertEquals((Integer) 1, sampleList.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", sampleList.getCancerStudyIdentifier());
        Assert.assertEquals("other", sampleList.getCategory());
        Assert.assertEquals("All Tumors", sampleList.getName());
        Assert.assertEquals("All tumor samples",
            sampleList.getDescription());
        Assert.assertNull(sampleList.getCancerStudy());
    }

    @Test
    public void getAllSampleListsDetailedProjection() throws Exception {

        List<SampleList> result = sampleListMyBatisRepository.getAllSampleLists("DETAILED", null, null, null,
            null);

        Assert.assertEquals(14, result.size());
        SampleList sampleList = result.get(0);
        Assert.assertEquals((Integer) 1, sampleList.getListId());
        Assert.assertEquals("study_tcga_pub_all", sampleList.getStableId());
        Assert.assertEquals((Integer) 1, sampleList.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", sampleList.getCancerStudyIdentifier());
        Assert.assertEquals("other", sampleList.getCategory());
        Assert.assertEquals("All Tumors", sampleList.getName());
        Assert.assertEquals("All tumor samples",
            sampleList.getDescription());
        CancerStudy cancerStudy = sampleList.getCancerStudy();
        Assert.assertEquals((Integer) 1, cancerStudy.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", cancerStudy.getCancerStudyIdentifier());
        Assert.assertEquals("brca", cancerStudy.getTypeOfCancerId());
        Assert.assertEquals("Breast Invasive Carcinoma (TCGA, Nature 2012)", cancerStudy.getName());
        Assert.assertEquals("BRCA (TCGA)", cancerStudy.getShortName());
        Assert.assertEquals("<a href=\\\"http://cancergenome.nih.gov/\\\">The Cancer Genome Atlas (TCGA)</a> Breast" +
            " Invasive Carcinoma project. 825 cases.<br><i>Nature 2012.</i> <a href=\\\"http://tcga-data.nci." +
            "nih.gov/tcga/\\\">Raw data via the TCGA Data Portal</a>.", cancerStudy.getDescription());
        Assert.assertEquals(true, cancerStudy.getPublicStudy());
        Assert.assertEquals("23000897,26451490", cancerStudy.getPmid());
        Assert.assertEquals("TCGA, Nature 2012, ...", cancerStudy.getCitation());
        Assert.assertEquals("SU2C-PI3K;PUBLIC;GDAC", cancerStudy.getGroups());
        Assert.assertEquals((Integer)0 , cancerStudy.getStatus());
    }

    @Test
    public void getAllSampleListsSummaryProjection1PageSize() throws Exception {

        List<SampleList> result = sampleListMyBatisRepository.getAllSampleLists("SUMMARY", 1, 0, null, null);

        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getAllSampleListsSummaryProjectionStableIdSort() throws Exception {

        List<SampleList> result = sampleListMyBatisRepository.getAllSampleLists("SUMMARY", null, null, "stableId",
            "ASC");

        Assert.assertEquals(14, result.size());
        Assert.assertEquals("acc_tcga_all", result.get(0).getStableId());
        Assert.assertEquals("study_tcga_pub_3way_complete", result.get(1).getStableId());
        Assert.assertEquals("study_tcga_pub_acgh", result.get(2).getStableId());
        Assert.assertEquals("study_tcga_pub_all", result.get(3).getStableId());
        Assert.assertEquals("study_tcga_pub_cna", result.get(4).getStableId());
        Assert.assertEquals("study_tcga_pub_cnaseq", result.get(5).getStableId());
    }

    @Test
    public void getMetaSampleLists() throws Exception {

        BaseMeta result = sampleListMyBatisRepository.getMetaSampleLists();

        Assert.assertEquals((Integer) 14, result.getTotalCount());
    }

    @Test
    public void getSampleListNullResult() throws Exception {

        SampleList result = sampleListMyBatisRepository.getSampleList("invalid_sample_list");

        Assert.assertNull(result);
    }

    @Test
    public void getSampleList() throws Exception {

        SampleList sampleList = sampleListMyBatisRepository.getSampleList("study_tcga_pub_all");

        Assert.assertEquals((Integer) 1, sampleList.getListId());
        Assert.assertEquals("study_tcga_pub_all", sampleList.getStableId());
        Assert.assertEquals((Integer) 1, sampleList.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", sampleList.getCancerStudyIdentifier());
        Assert.assertEquals("other", sampleList.getCategory());
        Assert.assertEquals("All Tumors", sampleList.getName());
        Assert.assertEquals("All tumor samples",
            sampleList.getDescription());
        CancerStudy cancerStudy = sampleList.getCancerStudy();
        Assert.assertEquals((Integer) 1, cancerStudy.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", cancerStudy.getCancerStudyIdentifier());
        Assert.assertEquals("brca", cancerStudy.getTypeOfCancerId());
        Assert.assertEquals("Breast Invasive Carcinoma (TCGA, Nature 2012)", cancerStudy.getName());
        Assert.assertEquals("BRCA (TCGA)", cancerStudy.getShortName());
        Assert.assertEquals("<a href=\\\"http://cancergenome.nih.gov/\\\">The Cancer Genome Atlas (TCGA)</a> Breast" +
            " Invasive Carcinoma project. 825 cases.<br><i>Nature 2012.</i> <a href=\\\"http://tcga-data.nci." +
            "nih.gov/tcga/\\\">Raw data via the TCGA Data Portal</a>.", cancerStudy.getDescription());
        Assert.assertEquals(true, cancerStudy.getPublicStudy());
        Assert.assertEquals("23000897,26451490", cancerStudy.getPmid());
        Assert.assertEquals("TCGA, Nature 2012, ...", cancerStudy.getCitation());
        Assert.assertEquals("SU2C-PI3K;PUBLIC;GDAC", cancerStudy.getGroups());
        Assert.assertEquals((Integer)0 , cancerStudy.getStatus());
    }

    @Test
    public void getSampleLists() throws Exception {

        List<SampleList> result = sampleListMyBatisRepository.getSampleLists(Arrays.asList("study_tcga_pub_all", 
            "study_tcga_pub_acgh"), "SUMMARY");

        Assert.assertEquals(2, result.size());
        SampleList sampleList = result.get(0);
        Assert.assertEquals((Integer) 2, sampleList.getListId());
        Assert.assertEquals("study_tcga_pub_acgh", sampleList.getStableId());
        Assert.assertEquals((Integer) 1, sampleList.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", sampleList.getCancerStudyIdentifier());
        Assert.assertEquals("other", sampleList.getCategory());
        Assert.assertEquals("Tumors aCGH", sampleList.getName());
        Assert.assertEquals("All tumors with aCGH data",
            sampleList.getDescription());
        Assert.assertNull(sampleList.getCancerStudy());
    }

    @Test
    public void getAllSampleListsInStudySummaryProjection() throws Exception {

        List<SampleList> result = sampleListMyBatisRepository.getAllSampleListsInStudies(Arrays.asList("study_tcga_pub"),
            "SUMMARY", null, null, null, null);

        Assert.assertEquals(13, result.size());
        SampleList sampleList = result.get(0);
        Assert.assertEquals((Integer) 1, sampleList.getListId());
        Assert.assertEquals("study_tcga_pub_all", sampleList.getStableId());
        Assert.assertEquals((Integer) 1, sampleList.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", sampleList.getCancerStudyIdentifier());
        Assert.assertEquals("other", sampleList.getCategory());
        Assert.assertEquals("All Tumors", sampleList.getName());
        Assert.assertEquals("All tumor samples",
            sampleList.getDescription());
        Assert.assertNull(sampleList.getCancerStudy());
    }

    @Test
    public void getAllSampleListsInStudyDetailedProjection() throws Exception {

        List<SampleList> result = sampleListMyBatisRepository.getAllSampleListsInStudies(Arrays.asList("study_tcga_pub"),
            "DETAILED", null, null, null, null);

        Assert.assertEquals(13, result.size());
        SampleList sampleList = result.get(0);
        Assert.assertEquals((Integer) 1, sampleList.getListId());
        Assert.assertEquals("study_tcga_pub_all", sampleList.getStableId());
        Assert.assertEquals((Integer) 1, sampleList.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", sampleList.getCancerStudyIdentifier());
        Assert.assertEquals("other", sampleList.getCategory());
        Assert.assertEquals("All Tumors", sampleList.getName());
        Assert.assertEquals("All tumor samples",
            sampleList.getDescription());
        CancerStudy cancerStudy = sampleList.getCancerStudy();
        Assert.assertEquals((Integer) 1, cancerStudy.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", cancerStudy.getCancerStudyIdentifier());
        Assert.assertEquals("brca", cancerStudy.getTypeOfCancerId());
        Assert.assertEquals("Breast Invasive Carcinoma (TCGA, Nature 2012)", cancerStudy.getName());
        Assert.assertEquals("BRCA (TCGA)", cancerStudy.getShortName());
        Assert.assertEquals("<a href=\\\"http://cancergenome.nih.gov/\\\">The Cancer Genome Atlas (TCGA)</a> Breast" +
            " Invasive Carcinoma project. 825 cases.<br><i>Nature 2012.</i> <a href=\\\"http://tcga-data.nci." +
            "nih.gov/tcga/\\\">Raw data via the TCGA Data Portal</a>.", cancerStudy.getDescription());
        Assert.assertEquals(true, cancerStudy.getPublicStudy());
        Assert.assertEquals("23000897,26451490", cancerStudy.getPmid());
        Assert.assertEquals("TCGA, Nature 2012, ...", cancerStudy.getCitation());
        Assert.assertEquals("SU2C-PI3K;PUBLIC;GDAC", cancerStudy.getGroups());
        Assert.assertEquals((Integer)0 , cancerStudy.getStatus());
    }

    @Test
    public void getMetaSampleListsInStudy() throws Exception {

        BaseMeta result = sampleListMyBatisRepository.getMetaSampleListsInStudy("study_tcga_pub");

        Assert.assertEquals((Integer) 13, result.getTotalCount());
    }

    @Test
    public void getAllSampleIdsInSampleList() throws Exception {

        List<String> result = sampleListMyBatisRepository.getAllSampleIdsInSampleList("study_tcga_pub_all");
        
        Assert.assertEquals(14, result.size());
        Assert.assertEquals("TCGA-A1-A0SB-01", result.get(0));
        Assert.assertEquals("TCGA-A1-A0SD-01", result.get(1));
        Assert.assertEquals("TCGA-A1-A0SE-01", result.get(2));
        Assert.assertEquals("TCGA-A1-A0SF-01", result.get(3));
        Assert.assertEquals("TCGA-A1-A0SG-01", result.get(4));
        Assert.assertEquals("TCGA-A1-A0SQ-01", result.get(13));
    }
}
