package org.cbioportal.persistence.spark;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.Patient;
import org.cbioportal.model.Sample;
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
@ContextConfiguration("/testSparkContext.xml")
@Configurable
public class SampleSparkRepositoryTest {

    @Autowired
    private SampleSparkRepository sampleSparkRepository;
    
    private static final String STUDY_ID = "msk_impact_2017";
    private static final String PATIENT_ID = "P-0000004";
    private static final String STABLE_ID = "P-0000004-T01-IM3";
    
    @Test
    public void fetchSamples() throws Exception {

        List<String> studyIds = Arrays.asList(STUDY_ID);
        List<String> sampleIds = Arrays.asList("P-0012400-T01-IM5", "P-0012406-T01-IM5");

        List<Sample> result = sampleSparkRepository.fetchSamples(studyIds, sampleIds, "SUMMARY");

        Assert.assertEquals(2, result.size());
        Assert.assertEquals("P-0012400-T01-IM5", result.get(0).getStableId());
    }

    @Test
    public void getAllSamplesInStudyIdProjection() throws Exception {

        List<Sample> result = sampleSparkRepository.getAllSamplesInStudy(STUDY_ID, "ID", null, null, null,
            null);

        Assert.assertEquals(10945, result.size());
        Sample sample = result.get(0);
        Assert.assertEquals((Integer) 1, sample.getInternalId());
        Assert.assertEquals(STABLE_ID, sample.getStableId());
        Assert.assertNull(sample.getPatient());
    }

    @Test
    public void getAllSamplesInStudySummaryProjection() throws Exception {

        List<Sample> result = sampleSparkRepository.getAllSamplesInStudy(STUDY_ID, "SUMMARY", null, null,
            null, null);

        Assert.assertEquals(10945, result.size());
        Sample sample = result.get(0);
        Assert.assertEquals((Integer) 1, sample.getInternalId());
        Assert.assertEquals(STABLE_ID, sample.getStableId());
        Assert.assertEquals(Sample.SampleType.PRIMARY_SOLID_TUMOR, sample.getSampleType());
        Assert.assertEquals((Integer) 1, sample.getPatientId());
        Assert.assertEquals(PATIENT_ID, sample.getPatientStableId());
        Assert.assertNull(sample.getPatient());
    }

    @Test
    public void getAllSamplesInStudyDetailedProjection() throws Exception {

        List<Sample> result = sampleSparkRepository.getAllSamplesInStudy(STUDY_ID, "DETAILED", null, null,
            null, null);

        Assert.assertEquals(10945, result.size());
        Sample sample = result.get(0);
        Assert.assertEquals((Integer) 1, sample.getInternalId());
        Assert.assertEquals(STABLE_ID, sample.getStableId());
        Assert.assertEquals(Sample.SampleType.PRIMARY_SOLID_TUMOR, sample.getSampleType());
        Assert.assertEquals((Integer) 1, sample.getPatientId());
        Assert.assertEquals(PATIENT_ID, sample.getPatientStableId());
        
        Patient patient = sample.getPatient();
        Assert.assertEquals((Integer) 1, patient.getInternalId());
        Assert.assertEquals(PATIENT_ID, patient.getStableId());
        Assert.assertEquals((Integer) 1, patient.getCancerStudyId());
        Assert.assertEquals(STUDY_ID, patient.getCancerStudyIdentifier());
        CancerStudy cancerStudy = patient.getCancerStudy();
        Assert.assertEquals((Integer) 1, cancerStudy.getCancerStudyId());
        Assert.assertEquals(STUDY_ID, cancerStudy.getCancerStudyIdentifier());
        Assert.assertEquals("mixed", cancerStudy.getTypeOfCancerId());
        Assert.assertEquals("MSK-IMPACT Clinical Sequencing Cohort (MSKCC, Nat Med 2017)", cancerStudy.getName());
        Assert.assertEquals("MSK-IMPACT", cancerStudy.getShortName());
        Assert.assertEquals("Targeted sequencing of 10,000 clinical cases using the MSK-IMPACT assay", cancerStudy.getDescription());
        Assert.assertEquals(true, cancerStudy.getPublicStudy());
        Assert.assertEquals("28481359", cancerStudy.getPmid());
        Assert.assertEquals("Zehir et al. Nat Med 2017", cancerStudy.getCitation());
        Assert.assertEquals("PUBLIC", cancerStudy.getGroups());
        Assert.assertEquals((Integer) 1, cancerStudy.getStatus());
    }

    @Test
    public void getAllSamplesInStudySummaryProjection1PageSize() throws Exception {

        List<Sample> result = sampleSparkRepository.getAllSamplesInStudy(STUDY_ID, "SUMMARY", 1, 0, null,
            null);

        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getAllSamplesInStudySummaryProjectionStableIdSort() throws Exception {

        List<Sample> result = sampleSparkRepository.getAllSamplesInStudy(STUDY_ID, "SUMMARY", null, null,
            "stableId", "ASC");

        Assert.assertEquals(10945, result.size());
        Assert.assertEquals("P-0000004-T01-IM3", result.get(0).getStableId());
        Assert.assertEquals("P-0000015-T01-IM3", result.get(1).getStableId());
        Assert.assertEquals("P-0000023-T01-IM3", result.get(2).getStableId());
        Assert.assertEquals("P-0000024-T01-IM3", result.get(3).getStableId());
        Assert.assertEquals("P-0000025-T01-IM3", result.get(4).getStableId());
        Assert.assertEquals("P-0000025-T02-IM5", result.get(5).getStableId());
    }

    @Test
    public void getAllSamplesOfPatientInStudyIdProjection() throws Exception {

        List<Sample> result = sampleSparkRepository.getAllSamplesOfPatientInStudy(STUDY_ID, PATIENT_ID,
            "ID", null, null, null, null);

        Assert.assertEquals(1, result.size());
        Sample sample = result.get(0);
        Assert.assertEquals((Integer) 1, sample.getInternalId());
        Assert.assertEquals(STABLE_ID, sample.getStableId());
        Assert.assertNull(sample.getPatient());
    }

    @Test
    public void getAllSamplesOfPatientInStudySummaryProjection() throws Exception {

        List<Sample> result = sampleSparkRepository.getAllSamplesOfPatientInStudy(STUDY_ID, PATIENT_ID,
            "SUMMARY", null, null, null, null);

        Assert.assertEquals(1, result.size());
        Sample sample = result.get(0);
        Assert.assertEquals((Integer) 1, sample.getInternalId());
        Assert.assertEquals(STABLE_ID, sample.getStableId());
        Assert.assertEquals(Sample.SampleType.PRIMARY_SOLID_TUMOR, sample.getSampleType());
        Assert.assertNull(sample.getPatient());
    }

    @Test
    public void getAllSamplesOfPatientInStudyDetailedProjection() throws Exception {

        List<Sample> result = sampleSparkRepository.getAllSamplesOfPatientInStudy(STUDY_ID, PATIENT_ID,
            "DETAILED", null, null, null, null);

        Assert.assertEquals(1, result.size());
        Sample sample = result.get(0);
        Assert.assertEquals((Integer) 1, sample.getInternalId());
        Assert.assertEquals(STABLE_ID, sample.getStableId());
        Assert.assertEquals(Sample.SampleType.PRIMARY_SOLID_TUMOR, sample.getSampleType());
        Assert.assertEquals((Integer) 1, sample.getPatientId());
        Assert.assertEquals(PATIENT_ID, sample.getPatientStableId());
        
        Patient patient = sample.getPatient();
        Assert.assertEquals((Integer) 1, patient.getInternalId());
        Assert.assertEquals(PATIENT_ID, patient.getStableId());
        Assert.assertEquals((Integer) 1, patient.getCancerStudyId());
        Assert.assertEquals(STUDY_ID, patient.getCancerStudyIdentifier());
        
        CancerStudy cancerStudy = patient.getCancerStudy();
        Assert.assertEquals((Integer) 1, cancerStudy.getCancerStudyId());
        Assert.assertEquals(STUDY_ID, cancerStudy.getCancerStudyIdentifier());
        Assert.assertEquals("mixed", cancerStudy.getTypeOfCancerId());
        Assert.assertEquals("MSK-IMPACT Clinical Sequencing Cohort (MSKCC, Nat Med 2017)", cancerStudy.getName());
        Assert.assertEquals("MSK-IMPACT", cancerStudy.getShortName());
        Assert.assertEquals("Targeted sequencing of 10,000 clinical cases using the MSK-IMPACT assay", cancerStudy.getDescription());
        Assert.assertEquals(true, cancerStudy.getPublicStudy());
        Assert.assertEquals("28481359", cancerStudy.getPmid());
        Assert.assertEquals("Zehir et al. Nat Med 2017", cancerStudy.getCitation());
        Assert.assertEquals("PUBLIC", cancerStudy.getGroups());
        Assert.assertEquals((Integer) 1, cancerStudy.getStatus());
    }

    @Test
    public void getAllSamplesOfPatientInStudySummaryProjection1PageSize() throws Exception {

        List<Sample> result = sampleSparkRepository.getAllSamplesOfPatientInStudy(STUDY_ID, PATIENT_ID,
            "SUMMARY", 1, 0, null, null);

        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getAllSamplesOfPatientInStudySummaryProjectionStableIdSort() throws Exception {

        List<Sample> result = sampleSparkRepository.getAllSamplesOfPatientInStudy(STUDY_ID, PATIENT_ID,
            "SUMMARY", null, null, "stableId", "ASC");

        Assert.assertEquals(1, result.size());
        Assert.assertEquals(STABLE_ID, result.get(0).getStableId());
    }

}
