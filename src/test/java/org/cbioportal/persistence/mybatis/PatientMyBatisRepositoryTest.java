package org.cbioportal.persistence.mybatis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.Patient;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.mybatis.config.TestConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {PatientMyBatisRepository.class, TestConfig.class})
public class PatientMyBatisRepositoryTest {
    
    @Autowired
    private PatientMyBatisRepository patientMyBatisRepository;

    @Test
    public void getAllPatients() throws Exception {
        
        List<Patient> result = patientMyBatisRepository.getAllPatients(null, "ID", null, null, null, null);

        Assert.assertEquals(18, result.size());
        Patient patient = result.get(0);
        Assert.assertEquals((Integer) 1, patient.getInternalId());
        Assert.assertEquals("TCGA-A1-A0SB", patient.getStableId());
        Assert.assertNull(patient.getCancerStudy());
    }
    
    @Test
    public void getAllPatientsByKeywordMatchingSample() {
        // Sample TCGA-A1-A0SB-02 belongs to patient TCGA-A1-A0SB
        List<Patient> result = patientMyBatisRepository.getAllPatients("TCGA-A1-A0SB-02", "ID", null, null, null, null);
        List<String> actual = result.stream().map(Patient::getStableId).collect(Collectors.toList());
        List<String> expected = Collections.singletonList("TCGA-A1-A0SB");
        
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getMetaPatients() throws Exception {

        BaseMeta result = patientMyBatisRepository.getMetaPatients(null);

        Assert.assertEquals((Integer) 18, result.getTotalCount());
    }

    @Test
    public void getAllPatientsInStudyIdProjection() throws Exception {

        List<Patient> result = patientMyBatisRepository.getAllPatientsInStudy("study_tcga_pub", "ID", null, null, null,
            null);

        Assert.assertEquals(14, result.size());
        Patient patient = result.get(0);
        Assert.assertEquals((Integer) 1, patient.getInternalId());
        Assert.assertEquals("TCGA-A1-A0SB", patient.getStableId());
        Assert.assertNull(patient.getCancerStudy());
    }

    @Test
    public void getAllPatientsInStudySummaryProjection() throws Exception {

        List<Patient> result = patientMyBatisRepository.getAllPatientsInStudy("study_tcga_pub", "SUMMARY", null, null,
            null, null);

        Assert.assertEquals(14, result.size());
        Patient patient = result.get(0);
        Assert.assertEquals((Integer) 1, patient.getInternalId());
        Assert.assertEquals("TCGA-A1-A0SB", patient.getStableId());
        Assert.assertEquals((Integer) 1, patient.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", patient.getCancerStudyIdentifier());
        Assert.assertNull(patient.getCancerStudy());
    }

    @Test
    public void getAllPatientsInStudyDetailedProjection() throws Exception {

        List<Patient> result = patientMyBatisRepository.getAllPatientsInStudy("study_tcga_pub", "DETAILED", null, null,
            null, null);

        Assert.assertEquals(14, result.size());
        Patient patient = result.get(0);
        Assert.assertEquals((Integer) 1, patient.getInternalId());
        Assert.assertEquals("TCGA-A1-A0SB", patient.getStableId());
        Assert.assertEquals((Integer) 1, patient.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", patient.getCancerStudyIdentifier());
        CancerStudy cancerStudy = patient.getCancerStudy();
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
        Assert.assertEquals((Integer)0 , cancerStudy.getStatus());
    }

    @Test
    public void getAllPatientsInStudySummaryProjection1PageSize() throws Exception {

        List<Patient> result = patientMyBatisRepository.getAllPatientsInStudy("study_tcga_pub", "SUMMARY", 1, 0, null,
            null);

        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getAllPatientsInStudySummaryProjectionStableIdSort() throws Exception {

        List<Patient> result = patientMyBatisRepository.getAllPatientsInStudy("study_tcga_pub", "SUMMARY", null, null,
            "stableId", "ASC");

        Assert.assertEquals(14, result.size());
        Assert.assertEquals("TCGA-A1-A0SB", result.get(0).getStableId());
        Assert.assertEquals("TCGA-A1-A0SD", result.get(1).getStableId());
        Assert.assertEquals("TCGA-A1-A0SE", result.get(2).getStableId());
        Assert.assertEquals("TCGA-A1-A0SF", result.get(3).getStableId());
        Assert.assertEquals("TCGA-A1-A0SG", result.get(4).getStableId());
        Assert.assertEquals("TCGA-A1-A0SH", result.get(5).getStableId());
    }

    @Test
    public void getMetaPatientsInStudy() throws Exception {

        BaseMeta result = patientMyBatisRepository.getMetaPatientsInStudy("study_tcga_pub");

        Assert.assertEquals((Integer) 14, result.getTotalCount());
    }

    @Test
    public void getPatientInStudyNullResult() throws Exception {

        Patient result = patientMyBatisRepository.getPatientInStudy("study_tcga_pub", "invalid_patient");

        Assert.assertNull(result);
    }

    @Test
    public void getPatientInStudy() throws Exception {

        Patient patient = patientMyBatisRepository.getPatientInStudy("study_tcga_pub", "TCGA-A1-A0SI");

        Assert.assertEquals((Integer) 7, patient.getInternalId());
        Assert.assertEquals("TCGA-A1-A0SI", patient.getStableId());
        Assert.assertEquals((Integer) 1, patient.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", patient.getCancerStudyIdentifier());
        CancerStudy cancerStudy = patient.getCancerStudy();
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
        Assert.assertEquals((Integer)0 , cancerStudy.getStatus());
    }

    @Test
    public void fetchPatients() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        studyIds.add("study_tcga_pub");
        List<String> patientIds = new ArrayList<>();
        patientIds.add("TCGA-A1-A0SB");
        patientIds.add("TCGA-A1-A0SE");

        List<Patient> result = patientMyBatisRepository.fetchPatients(studyIds, patientIds, "SUMMARY");

        Assert.assertEquals(2, result.size());
        Assert.assertEquals("TCGA-A1-A0SB", result.get(0).getStableId());
        Assert.assertEquals("TCGA-A1-A0SE", result.get(1).getStableId());
    }

    @Test
    public void fetchMetaPatients() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        studyIds.add("study_tcga_pub");
        List<String> patientIds = new ArrayList<>();
        patientIds.add("TCGA-A1-A0SB");
        patientIds.add("TCGA-A1-A0SE");

        BaseMeta result = patientMyBatisRepository.fetchMetaPatients(studyIds, patientIds);

        Assert.assertEquals((Integer) 2, result.getTotalCount());
    }

    @Test
    public void getPatientIdsOfSamples() throws Exception {

        List<Patient> result = patientMyBatisRepository.getPatientsOfSamples(Arrays.asList("study_tcga_pub", "study_tcga_pub", "study_tcga_pub"), 
            Arrays.asList("TCGA-A1-A0SB-01", "TCGA-A1-A0SD-01", "TCGA-A1-A0SB-02"));
        
        Assert.assertEquals(2, result.size());
        List<String> stableIds =
            result.stream().map(r -> r.getStableId()).collect(Collectors.toList());
        Assert.assertTrue(stableIds.contains("TCGA-A1-A0SD"));
        Assert.assertTrue(stableIds.contains("TCGA-A1-A0SB"));
    }
}
