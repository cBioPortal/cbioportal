package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.ClinicalEvent;
import org.cbioportal.model.ClinicalEventData;
import org.cbioportal.model.meta.BaseMeta;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class ClinicalEventMyBatisRepositoryTest {
    
    @Autowired
    private ClinicalEventMyBatisRepository clinicalEventMyBatisRepository;
    
    @Test
    public void getAllClinicalEventsOfPatientInStudyIdProjection() throws Exception {

        List<ClinicalEvent> result = clinicalEventMyBatisRepository.getAllClinicalEventsOfPatientInStudy(
            "study_tcga_pub", "TCGA-A1-A0SB", "ID", null, null, null, null);

        Assert.assertEquals(2, result.size());
        ClinicalEvent clinicalEvent = result.get(0);
        Assert.assertEquals((Integer) 2, clinicalEvent.getClinicalEventId());
        Assert.assertEquals("study_tcga_pub", clinicalEvent.getStudyId());
        Assert.assertEquals("TCGA-A1-A0SB", clinicalEvent.getPatientId());
        Assert.assertEquals("SPECIMEN", clinicalEvent.getEventType());
    }

    @Test
    public void getAllClinicalEventsOfPatientInStudySummaryProjection() throws Exception {

        List<ClinicalEvent> result = clinicalEventMyBatisRepository.getAllClinicalEventsOfPatientInStudy(
            "study_tcga_pub", "TCGA-A1-A0SB", "SUMMARY", null, null, null, null);

        Assert.assertEquals(2, result.size());
        ClinicalEvent clinicalEvent = result.get(0);
        Assert.assertEquals((Integer) 2, clinicalEvent.getClinicalEventId());
        Assert.assertEquals("study_tcga_pub", clinicalEvent.getStudyId());
        Assert.assertEquals("TCGA-A1-A0SB", clinicalEvent.getPatientId());
        Assert.assertEquals("SPECIMEN", clinicalEvent.getEventType());
        Assert.assertEquals((Integer) 233, clinicalEvent.getStartDate());
        Assert.assertEquals((Integer) 345, clinicalEvent.getStopDate());
    }

    @Test
    public void getAllClinicalEventsOfPatientInStudyDetailedProjection() throws Exception {

        List<ClinicalEvent> result = clinicalEventMyBatisRepository.getAllClinicalEventsOfPatientInStudy(
            "study_tcga_pub", "TCGA-A1-A0SB", "DETAILED", null, null, null, null);

        Assert.assertEquals(2, result.size());
        ClinicalEvent clinicalEvent = result.get(0);
        Assert.assertEquals((Integer) 2, clinicalEvent.getClinicalEventId());
        Assert.assertEquals("study_tcga_pub", clinicalEvent.getStudyId());
        Assert.assertEquals("TCGA-A1-A0SB", clinicalEvent.getPatientId());
        Assert.assertEquals("SPECIMEN", clinicalEvent.getEventType());
        Assert.assertEquals((Integer) 233, clinicalEvent.getStartDate());
        Assert.assertEquals((Integer) 345, clinicalEvent.getStopDate());
    }

    @Test
    public void getAllClinicalEventsOfPatientInStudySummaryProjection1PageSize() throws Exception {

        List<ClinicalEvent> result = clinicalEventMyBatisRepository.getAllClinicalEventsOfPatientInStudy(
            "study_tcga_pub", "TCGA-A1-A0SB", "SUMMARY", 1, 0, null, null);

        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getAllClinicalEventsOfPatientInStudySummaryProjectionEventTypeSort() throws Exception {

        List<ClinicalEvent> result = clinicalEventMyBatisRepository.getAllClinicalEventsOfPatientInStudy(
            "study_tcga_pub", "TCGA-A1-A0SB", "SUMMARY", null, null, "eventType", "ASC");

        Assert.assertEquals(2, result.size());
        Assert.assertEquals("SPECIMEN", result.get(0).getEventType());
        Assert.assertEquals("STATUS", result.get(1).getEventType());
    }

    @Test
    public void getMetaPatientClinicalEvents() throws Exception {

        BaseMeta result = clinicalEventMyBatisRepository.getMetaPatientClinicalEvents("study_tcga_pub", "TCGA-A1-A0SB");

        Assert.assertEquals((Integer) 2, result.getTotalCount());
    }

    @Test
    public void getDataOfClinicalEvents() throws Exception {

        List<Integer> clinicalEventIds = new ArrayList<>();
        clinicalEventIds.add(1);
        clinicalEventIds.add(2);
        List<ClinicalEventData> result = clinicalEventMyBatisRepository.getDataOfClinicalEvents(clinicalEventIds);

        Assert.assertEquals(3, result.size());
        ClinicalEventData gisticToGene1 = result.get(0);
        Assert.assertEquals("STATUS", gisticToGene1.getKey());
        Assert.assertEquals("radiographic_progression", gisticToGene1.getValue());
        ClinicalEventData gisticToGene2 = result.get(1);
        Assert.assertEquals("SAMPLE_ID", gisticToGene2.getKey());
        Assert.assertEquals("TCGA-A1-A0SB-01", gisticToGene2.getValue());
        ClinicalEventData gisticToGene3 = result.get(2);
        Assert.assertEquals("SURGERY", gisticToGene3.getKey());
        Assert.assertEquals("OA II Initial", gisticToGene3.getValue());
    }

    @Test
    public void getAllClinicalEventsInStudyIdProjection() throws Exception {
        List<ClinicalEvent> result = clinicalEventMyBatisRepository.getAllClinicalEventsInStudy(
            "study_tcga_pub", "ID", null, null, null, null);

        Assert.assertEquals(4, result.size());
        ClinicalEvent clinicalEvent = result.get(0);
        Assert.assertEquals((Integer) 2, clinicalEvent.getClinicalEventId());
        Assert.assertEquals("study_tcga_pub", clinicalEvent.getStudyId());
        Assert.assertEquals("SPECIMEN", clinicalEvent.getEventType());
    }

    @Test
    public void getAllClinicalEventsInStudySummaryProjection() throws Exception {

        List<ClinicalEvent> result = clinicalEventMyBatisRepository.getAllClinicalEventsInStudy(
            "study_tcga_pub", "SUMMARY", null, null, null, null);

        Assert.assertEquals(4, result.size());
        ClinicalEvent clinicalEvent = result.get(0);
        Assert.assertEquals((Integer) 2, clinicalEvent.getClinicalEventId());
        Assert.assertEquals("study_tcga_pub", clinicalEvent.getStudyId());
        Assert.assertEquals("SPECIMEN", clinicalEvent.getEventType());
        Assert.assertEquals((Integer) 233, clinicalEvent.getStartDate());
        Assert.assertEquals((Integer) 345, clinicalEvent.getStopDate());
    }

    @Test
    public void getAllClinicalEventsInStudyDetailedProjection() throws Exception {

        List<ClinicalEvent> result = clinicalEventMyBatisRepository.getAllClinicalEventsInStudy(
            "study_tcga_pub", "DETAILED", null, null, null, null);

        Assert.assertEquals(4, result.size());
        ClinicalEvent clinicalEvent = result.get(0);
        Assert.assertEquals((Integer) 2, clinicalEvent.getClinicalEventId());
        Assert.assertEquals("study_tcga_pub", clinicalEvent.getStudyId());
        Assert.assertEquals("SPECIMEN", clinicalEvent.getEventType());
        Assert.assertEquals((Integer) 233, clinicalEvent.getStartDate());
        Assert.assertEquals((Integer) 345, clinicalEvent.getStopDate());
    }

    @Test
    public void getAllClinicalEventsInStudySummaryProjection1PageSize() throws Exception {

        List<ClinicalEvent> result = clinicalEventMyBatisRepository.getAllClinicalEventsInStudy(
            "study_tcga_pub", "SUMMARY", 1, 0, null, null);

        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getAllClinicalEventsInStudySummaryProjectionEventTypeSort() throws Exception {

        List<ClinicalEvent> result = clinicalEventMyBatisRepository.getAllClinicalEventsInStudy(
            "study_tcga_pub", "SUMMARY", null, null, "eventType", "ASC");

        Assert.assertEquals(4, result.size());
        Assert.assertEquals("SPECIMEN", result.get(1).getEventType());
        Assert.assertEquals("STATUS", result.get(2).getEventType());
    }

    @Test
    public void getMetaClinicalEvents() throws Exception {

        BaseMeta result = clinicalEventMyBatisRepository.getMetaClinicalEvents("study_tcga_pub");

        Assert.assertEquals((Integer) 4, result.getTotalCount());
    }
}