package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.Patient;
import org.cbioportal.model.Sample;
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
public class SampleMyBatisRepositoryTest {

    @Autowired
    private SampleMyBatisRepository sampleMyBatisRepository;

    @Test
    public void getAllSamplesInStudyIdProjection() throws Exception {

        List<Sample> result = sampleMyBatisRepository.getAllSamplesInStudy("study_tcga_pub", "ID", null, null, null,
                null);

        Assert.assertEquals(15, result.size());
        Sample sample = result.get(0);
        Assert.assertEquals((Integer) 1, sample.getInternalId());
        Assert.assertEquals("TCGA-A1-A0SB-01", sample.getStableId());
        Assert.assertNull(sample.getPatient());
    }

    @Test
    public void getAllSamplesInStudySummaryProjection() throws Exception {

        List<Sample> result = sampleMyBatisRepository.getAllSamplesInStudy("study_tcga_pub", "SUMMARY", null, null,
                null, null);

        Assert.assertEquals(15, result.size());
        Sample sample = result.get(0);
        Assert.assertEquals((Integer) 1, sample.getInternalId());
        Assert.assertEquals("TCGA-A1-A0SB-01", sample.getStableId());
        Assert.assertEquals(Sample.SampleType.PRIMARY_SOLID_TUMOR, sample.getSampleType());
        Assert.assertEquals((Integer) 1, sample.getPatientId());
        Assert.assertEquals("TCGA-A1-A0SB", sample.getPatientStableId());
        Assert.assertNull(sample.getPatient());
    }

    @Test
    public void getAllSamplesInStudyDetailedProjection() throws Exception {

        List<Sample> result = sampleMyBatisRepository.getAllSamplesInStudy("study_tcga_pub", "DETAILED", null, null,
                null, null);

        Assert.assertEquals(15, result.size());
        Sample sample = result.get(0);
        Assert.assertEquals((Integer) 1, sample.getInternalId());
        Assert.assertEquals("TCGA-A1-A0SB-01", sample.getStableId());
        Assert.assertEquals(Sample.SampleType.PRIMARY_SOLID_TUMOR, sample.getSampleType());
        Assert.assertEquals((Integer) 1, sample.getPatientId());
        Assert.assertEquals("TCGA-A1-A0SB", sample.getPatientStableId());
        Patient patient = sample.getPatient();
        Assert.assertEquals((Integer) 1, patient.getInternalId());
        Assert.assertEquals("TCGA-A1-A0SB", patient.getStableId());
        Assert.assertEquals((Integer) 1, patient.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", patient.getCancerStudyIdentifier());
        CancerStudy cancerStudy = patient.getCancerStudy();
        Assert.assertEquals((Integer) 1, cancerStudy.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", cancerStudy.getCancerStudyIdentifier());
        Assert.assertEquals("brca", cancerStudy.getTypeOfCancerId());
        Assert.assertEquals("Breast Invasive Carcinoma (TCGA, Nature 2012)", cancerStudy.getName());
        Assert.assertEquals("BRCA (TCGA)", cancerStudy.getShortName());
        Assert.assertEquals("<a href=\\\"http://cancergenome.nih.gov/\\\">The Cancer Genome Atlas (TCGA)</a> Breast" +
                " Invasive Carcinoma project. 825 cases.<br><i>Nature 2012.</i> <a href=\\\"http://tcga-data.nci." +
                "nih.gov/tcga/\\\">Raw data via the TCGA Data Portal</a>.", cancerStudy.getDescription());
        Assert.assertEquals(true, cancerStudy.getPublicStudy());
        Assert.assertEquals("23000897", cancerStudy.getPmid());
        Assert.assertEquals("TCGA, Nature 2012", cancerStudy.getCitation());
        Assert.assertEquals("SU2C-PI3K;PUBLIC;GDAC", cancerStudy.getGroups());
        Assert.assertEquals((Integer)0 , cancerStudy.getStatus());
    }

    @Test
    public void getAllSamplesInStudySummaryProjection1PageSize() throws Exception {

        List<Sample> result = sampleMyBatisRepository.getAllSamplesInStudy("study_tcga_pub", "SUMMARY", 1, 0, null,
                null);

        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getAllSamplesInStudySummaryProjectionStableIdSort() throws Exception {

        List<Sample> result = sampleMyBatisRepository.getAllSamplesInStudy("study_tcga_pub", "SUMMARY", null, null,
                "stableId", "ASC");

        Assert.assertEquals(15, result.size());
        Assert.assertEquals("TCGA-A1-A0SB-01", result.get(0).getStableId());
        Assert.assertEquals("TCGA-A1-A0SB-02", result.get(1).getStableId());
        Assert.assertEquals("TCGA-A1-A0SD-01", result.get(2).getStableId());
        Assert.assertEquals("TCGA-A1-A0SE-01", result.get(3).getStableId());
        Assert.assertEquals("TCGA-A1-A0SF-01", result.get(4).getStableId());
        Assert.assertEquals("TCGA-A1-A0SG-01", result.get(5).getStableId());
    }

    @Test
    public void getMetaSamplesInStudy() throws Exception {

        BaseMeta result = sampleMyBatisRepository.getMetaSamplesInStudy("study_tcga_pub");

        Assert.assertEquals((Integer) 15, result.getTotalCount());
    }

    @Test
    public void getSampleInStudyNullResult() throws Exception {

        Sample result = sampleMyBatisRepository.getSampleInStudy("study_tcga_pub", "invalid_sample");

        Assert.assertNull(result);
    }

    @Test
    public void getSampleInStudy() throws Exception {

        Sample sample = sampleMyBatisRepository.getSampleInStudy("study_tcga_pub", "TCGA-A1-A0SI-01");

        Assert.assertEquals((Integer) 7, sample.getInternalId());
        Assert.assertEquals("TCGA-A1-A0SI-01", sample.getStableId());
        Assert.assertEquals(Sample.SampleType.PRIMARY_SOLID_TUMOR, sample.getSampleType());
        Assert.assertEquals((Integer) 7, sample.getPatientId());
        Assert.assertEquals("TCGA-A1-A0SI", sample.getPatientStableId());
        Patient patient = sample.getPatient();
        Assert.assertEquals((Integer) 7, patient.getInternalId());
        Assert.assertEquals("TCGA-A1-A0SI", patient.getStableId());
        Assert.assertEquals((Integer) 1, patient.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", patient.getCancerStudyIdentifier());
        CancerStudy cancerStudy = patient.getCancerStudy();
        Assert.assertEquals((Integer) 1, cancerStudy.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", cancerStudy.getCancerStudyIdentifier());
        Assert.assertEquals("brca", cancerStudy.getTypeOfCancerId());
        Assert.assertEquals("Breast Invasive Carcinoma (TCGA, Nature 2012)", cancerStudy.getName());
        Assert.assertEquals("BRCA (TCGA)", cancerStudy.getShortName());
        Assert.assertEquals("<a href=\\\"http://cancergenome.nih.gov/\\\">The Cancer Genome Atlas (TCGA)</a> Breast" +
                " Invasive Carcinoma project. 825 cases.<br><i>Nature 2012.</i> <a href=\\\"http://tcga-data.nci." +
                "nih.gov/tcga/\\\">Raw data via the TCGA Data Portal</a>.", cancerStudy.getDescription());
        Assert.assertEquals(true, cancerStudy.getPublicStudy());
        Assert.assertEquals("23000897", cancerStudy.getPmid());
        Assert.assertEquals("TCGA, Nature 2012", cancerStudy.getCitation());
        Assert.assertEquals("SU2C-PI3K;PUBLIC;GDAC", cancerStudy.getGroups());
        Assert.assertEquals((Integer)0 , cancerStudy.getStatus());
    }

    @Test
    public void getAllSamplesOfPatientInStudyIdProjection() throws Exception {

        List<Sample> result = sampleMyBatisRepository.getAllSamplesOfPatientInStudy("study_tcga_pub", "TCGA-A1-A0SB",
                "ID", null, null, null, null);

        Assert.assertEquals(2, result.size());
        Sample sample = result.get(0);
        Assert.assertEquals((Integer) 1, sample.getInternalId());
        Assert.assertEquals("TCGA-A1-A0SB-01", sample.getStableId());
        Assert.assertNull(sample.getPatient());
    }

    @Test
    public void getAllSamplesOfPatientInStudySummaryProjection() throws Exception {

        List<Sample> result = sampleMyBatisRepository.getAllSamplesOfPatientInStudy("study_tcga_pub", "TCGA-A1-A0SB",
                "SUMMARY", null, null, null, null);

        Assert.assertEquals(2, result.size());
        Sample sample = result.get(0);
        Assert.assertEquals((Integer) 1, sample.getInternalId());
        Assert.assertEquals("TCGA-A1-A0SB-01", sample.getStableId());
        Assert.assertEquals(Sample.SampleType.PRIMARY_SOLID_TUMOR, sample.getSampleType());
        Assert.assertEquals((Integer) 1, sample.getPatientId());
        Assert.assertEquals("TCGA-A1-A0SB", sample.getPatientStableId());
        Assert.assertNull(sample.getPatient());
    }

    @Test
    public void getAllSamplesOfPatientInStudyDetailedProjection() throws Exception {

        List<Sample> result = sampleMyBatisRepository.getAllSamplesOfPatientInStudy("study_tcga_pub", "TCGA-A1-A0SB",
                "DETAILED", null, null, null, null);

        Assert.assertEquals(2, result.size());
        Sample sample = result.get(0);
        Assert.assertEquals((Integer) 1, sample.getInternalId());
        Assert.assertEquals("TCGA-A1-A0SB-01", sample.getStableId());
        Assert.assertEquals(Sample.SampleType.PRIMARY_SOLID_TUMOR, sample.getSampleType());
        Assert.assertEquals((Integer) 1, sample.getPatientId());
        Assert.assertEquals("TCGA-A1-A0SB", sample.getPatientStableId());
        Patient patient = sample.getPatient();
        Assert.assertEquals((Integer) 1, patient.getInternalId());
        Assert.assertEquals("TCGA-A1-A0SB", patient.getStableId());
        Assert.assertEquals((Integer) 1, patient.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", patient.getCancerStudyIdentifier());
        CancerStudy cancerStudy = patient.getCancerStudy();
        Assert.assertEquals((Integer) 1, cancerStudy.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", cancerStudy.getCancerStudyIdentifier());
        Assert.assertEquals("brca", cancerStudy.getTypeOfCancerId());
        Assert.assertEquals("Breast Invasive Carcinoma (TCGA, Nature 2012)", cancerStudy.getName());
        Assert.assertEquals("BRCA (TCGA)", cancerStudy.getShortName());
        Assert.assertEquals("<a href=\\\"http://cancergenome.nih.gov/\\\">The Cancer Genome Atlas (TCGA)</a> Breast" +
                " Invasive Carcinoma project. 825 cases.<br><i>Nature 2012.</i> <a href=\\\"http://tcga-data.nci." +
                "nih.gov/tcga/\\\">Raw data via the TCGA Data Portal</a>.", cancerStudy.getDescription());
        Assert.assertEquals(true, cancerStudy.getPublicStudy());
        Assert.assertEquals("23000897", cancerStudy.getPmid());
        Assert.assertEquals("TCGA, Nature 2012", cancerStudy.getCitation());
        Assert.assertEquals("SU2C-PI3K;PUBLIC;GDAC", cancerStudy.getGroups());
        Assert.assertEquals((Integer)0 , cancerStudy.getStatus());
    }

    @Test
    public void getAllSamplesOfPatientInStudySummaryProjection1PageSize() throws Exception {

        List<Sample> result = sampleMyBatisRepository.getAllSamplesOfPatientInStudy("study_tcga_pub", "TCGA-A1-A0SB",
                "SUMMARY", 1, 0, null, null);

        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getAllSamplesOfPatientInStudySummaryProjectionStableIdSort() throws Exception {

        List<Sample> result = sampleMyBatisRepository.getAllSamplesOfPatientInStudy("study_tcga_pub", "TCGA-A1-A0SB",
                "SUMMARY", null, null, "stableId", "ASC");

        Assert.assertEquals(2, result.size());
        Assert.assertEquals("TCGA-A1-A0SB-01", result.get(0).getStableId());
        Assert.assertEquals("TCGA-A1-A0SB-02", result.get(1).getStableId());
    }

    @Test
    public void getMetaSamplesOfPatientInStudy() throws Exception {

        BaseMeta result = sampleMyBatisRepository.getMetaSamplesOfPatientInStudy("study_tcga_pub", "TCGA-A1-A0SB");

        Assert.assertEquals((Integer) 2, result.getTotalCount());
    }

    @Test
    public void getAllSamplesOfPatientsInStudy() throws Exception {

        List<Sample> result = sampleMyBatisRepository.getAllSamplesOfPatientsInStudy("study_tcga_pub", 
            Arrays.asList("TCGA-A1-A0SB", "TCGA-A1-A0SE"), "SUMMARY");

        Assert.assertEquals(3, result.size());
        Sample sample = result.get(0);
        Assert.assertEquals((Integer) 1, sample.getInternalId());
        Assert.assertEquals("TCGA-A1-A0SB-01", sample.getStableId());
        Assert.assertEquals(Sample.SampleType.PRIMARY_SOLID_TUMOR, sample.getSampleType());
        Assert.assertEquals((Integer) 1, sample.getPatientId());
        Assert.assertEquals("TCGA-A1-A0SB", sample.getPatientStableId());
        Assert.assertNull(sample.getPatient());
    }

    @Test
    public void fetchSamples() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        studyIds.add("study_tcga_pub");
        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SB-01");
        sampleIds.add("TCGA-A1-A0SE-01");

        List<Sample> result = sampleMyBatisRepository.fetchSamples(studyIds, sampleIds, "SUMMARY");

        Assert.assertEquals(2, result.size());
        Assert.assertEquals("TCGA-A1-A0SB-01", result.get(0).getStableId());
        Assert.assertEquals("TCGA-A1-A0SE-01", result.get(1).getStableId());
    }

    @Test
    public void fetchSamplesBySampleListId() throws Exception {

        List<String> sampleListIds = new ArrayList<>();
        sampleListIds.add("study_tcga_pub_all");
        sampleListIds.add("study_tcga_pub_acgh");

        List<Sample> result = sampleMyBatisRepository.fetchSamples(sampleListIds, "SUMMARY");

        Assert.assertEquals(14, result.size());
        Assert.assertEquals("TCGA-A1-A0SB-01", result.get(0).getStableId());
        Assert.assertEquals("TCGA-A1-A0SD-01", result.get(1).getStableId());
        Assert.assertEquals("TCGA-A1-A0SE-01", result.get(2).getStableId());
        Assert.assertEquals("TCGA-A1-A0SF-01", result.get(3).getStableId());
        Assert.assertEquals("TCGA-A1-A0SG-01", result.get(4).getStableId());
        Assert.assertEquals("TCGA-A1-A0SH-01", result.get(5).getStableId());
    }

    @Test
    public void fetchMetaSamples() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        studyIds.add("study_tcga_pub");
        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SB-01");
        sampleIds.add("TCGA-A1-A0SE-01");

        BaseMeta result = sampleMyBatisRepository.fetchMetaSamples(studyIds, sampleIds);

        Assert.assertEquals((Integer) 2, result.getTotalCount());
    }

    @Test
    public void fetchMetaSamplesBySampleListId() throws Exception {

        List<String> sampleListIds = new ArrayList<>();
        sampleListIds.add("study_tcga_pub_all");
        sampleListIds.add("study_tcga_pub_acgh");

        BaseMeta result = sampleMyBatisRepository.fetchMetaSamples(sampleListIds);

        Assert.assertEquals((Integer) 14, result.getTotalCount());
    }

    @Test
    public void getSamplesByInternalIds() throws Exception {

        List<Sample> result = sampleMyBatisRepository.getSamplesByInternalIds(Arrays.asList(1, 2));

        Assert.assertEquals(2, result.size());
        Assert.assertEquals("TCGA-A1-A0SB-01", result.get(0).getStableId());
        Assert.assertEquals("TCGA-A1-A0SD-01", result.get(1).getStableId());
     }
}