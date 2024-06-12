package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.ClinicalEvent;
import org.cbioportal.model.ClinicalEventData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.mybatis.config.TestConfig;
import org.cbioportal.persistence.mybatis.util.MolecularProfileCaseIdentifierUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {ClinicalEventMyBatisRepository.class, MolecularProfileCaseIdentifierUtil.class, TestConfig.class})
public class ClinicalEventMyBatisRepositoryTest {
    
    @Autowired
    private ClinicalEventMyBatisRepository clinicalEventMyBatisRepository;
    
    @Test
    public void getAllClinicalEventsOfPatientInStudyIdProjection() throws Exception {

        List<ClinicalEvent> result = clinicalEventMyBatisRepository.getAllClinicalEventsOfPatientInStudy(
            "study_tcga_pub", "TCGA-A1-A0SB", "ID", null, null, null, null);

        Assert.assertEquals(2, result.size());

        Optional<ClinicalEvent> clinicalEventOptional =
            result.stream().filter(r -> r.getClinicalEventId() == 2).findAny();
        Assert.assertTrue(clinicalEventOptional.isPresent());
        ClinicalEvent clinicalEvent = clinicalEventOptional.get();

        Assert.assertEquals("study_tcga_pub", clinicalEvent.getStudyId());
        Assert.assertEquals("TCGA-A1-A0SB", clinicalEvent.getPatientId());
        Assert.assertEquals("SPECIMEN", clinicalEvent.getEventType());
    }

    @Test
    public void getAllClinicalEventsOfPatientInStudySummaryProjection() throws Exception {

        List<ClinicalEvent> result = clinicalEventMyBatisRepository.getAllClinicalEventsOfPatientInStudy(
            "study_tcga_pub", "TCGA-A1-A0SB", "SUMMARY", null, null, null, null);

        Assert.assertEquals(2, result.size());

        Optional<ClinicalEvent> clinicalEventOptional =
            result.stream().filter(r -> r.getClinicalEventId() == 2).findAny();
        Assert.assertTrue(clinicalEventOptional.isPresent());
        ClinicalEvent clinicalEvent = clinicalEventOptional.get();

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

        Optional<ClinicalEvent> clinicalEventOptional =
            result.stream().filter(r -> r.getClinicalEventId() == 2).findAny();
        Assert.assertTrue(clinicalEventOptional.isPresent());
        ClinicalEvent clinicalEvent = clinicalEventOptional.get();

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

        Assert.assertEquals(4, result.size());
        ClinicalEventData gisticToGene1 = result.get(0);
        Assert.assertEquals("STATUS", gisticToGene1.getKey());
        Assert.assertEquals("radiographic_progression", gisticToGene1.getValue());
        ClinicalEventData gisticToGene2 = result.get(1);
        Assert.assertEquals("SAMPLE_ID", gisticToGene2.getKey());
        Assert.assertEquals("TCGA-A1-A0SB-01", gisticToGene2.getValue());
        ClinicalEventData gisticToGene3 = result.get(2);
        Assert.assertEquals("SURGERY", gisticToGene3.getKey());
        Assert.assertEquals("OA II Initial", gisticToGene3.getValue());     
        ClinicalEventData gisticToGene4 = result.get(3);
        Assert.assertEquals("SAMPLE_ID", gisticToGene4.getKey());
        Assert.assertEquals("TCGA-A1-A0SB-01", gisticToGene4.getValue());
    }

    @Test
    public void getAllClinicalEventsInStudyIdProjection() throws Exception {
        List<ClinicalEvent> result = clinicalEventMyBatisRepository.getAllClinicalEventsInStudy(
            "study_tcga_pub", "ID", null, null, null, null);

        Assert.assertEquals(5, result.size());

        Optional<ClinicalEvent> clinicalEventOptional =
            result.stream().filter(r -> r.getClinicalEventId() == 2).findAny();
        Assert.assertTrue(clinicalEventOptional.isPresent());
        ClinicalEvent clinicalEvent = clinicalEventOptional.get();

        Assert.assertEquals((Integer) 2, clinicalEvent.getClinicalEventId());
        Assert.assertEquals("study_tcga_pub", clinicalEvent.getStudyId());
        Assert.assertEquals("SPECIMEN", clinicalEvent.getEventType());
    }

    @Test
    public void getAllClinicalEventsInStudySummaryProjection() throws Exception {

        List<ClinicalEvent> result = clinicalEventMyBatisRepository.getAllClinicalEventsInStudy(
            "study_tcga_pub", "SUMMARY", null, null, null, null);

        Assert.assertEquals(5, result.size());

        Optional<ClinicalEvent> clinicalEventOptional =
            result.stream().filter(r -> r.getClinicalEventId() == 2).findAny();
        Assert.assertTrue(clinicalEventOptional.isPresent());
        ClinicalEvent clinicalEvent = clinicalEventOptional.get();

        Assert.assertEquals("study_tcga_pub", clinicalEvent.getStudyId());
        Assert.assertEquals("SPECIMEN", clinicalEvent.getEventType());
        Assert.assertEquals((Integer) 233, clinicalEvent.getStartDate());
        Assert.assertEquals((Integer) 345, clinicalEvent.getStopDate());
    }

    @Test
    public void getAllClinicalEventsInStudyDetailedProjection() throws Exception {

        List<ClinicalEvent> result = clinicalEventMyBatisRepository.getAllClinicalEventsInStudy(
            "study_tcga_pub", "DETAILED", null, null, null, null);

        Assert.assertEquals(5, result.size());

        Optional<ClinicalEvent> clinicalEventOptional =
            result.stream().filter(r -> r.getClinicalEventId() == 2).findAny();
        Assert.assertTrue(clinicalEventOptional.isPresent());
        ClinicalEvent clinicalEvent = clinicalEventOptional.get();

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

        Assert.assertEquals(5, result.size());
        Assert.assertEquals("SPECIMEN", result.get(1).getEventType());
        Assert.assertEquals("STATUS", result.get(2).getEventType());
    }

    @Test
    public void getMetaClinicalEvents() throws Exception {

        BaseMeta result = clinicalEventMyBatisRepository.getMetaClinicalEvents("study_tcga_pub");

        Assert.assertEquals((Integer) 5, result.getTotalCount());
    }
    
    @Test
    public void getSamplesOfPatientsPerEventTypeInStudy() {
       List<String> studyList = new ArrayList<>();
       studyList.add("study_tcga_pub");
       List<String> sampleList = new ArrayList<>();
       sampleList.add("TCGA-A1-A0SB-01");
       Map<String, Set<String>> result = clinicalEventMyBatisRepository
           .getSamplesOfPatientsPerEventTypeInStudy(studyList, sampleList);
       
       Assert.assertNotNull(result.get("STATUS"));
    }
    
    @Test
    public void getPatientsDistinctClinicalEventInStudies() {
        List<String> studyList = new ArrayList<>();
        studyList.add("study_tcga_pub");
        List<String> patientList = new ArrayList<>();
        patientList.add("TCGA-A1-A0SB");
        List<ClinicalEvent> result = clinicalEventMyBatisRepository.getPatientsDistinctClinicalEventInStudies(studyList, patientList);
        
        List<String> eventTypes = result.stream().map(ClinicalEvent::getEventType).collect(Collectors.toList());
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(eventTypes.contains("STATUS"));
    }

    @Test
    public void getTimelineEvents() {
        List<String> studyList = new ArrayList<>();
        studyList.add("study_tcga_pub");
        List<String> patientList = new ArrayList<>();
        patientList.add("TCGA-A1-A0SD");
        
        ClinicalEventData clinicalEventData1 = new ClinicalEventData();
        clinicalEventData1.setKey("AGENT");
        clinicalEventData1.setValue("Madeupanib");

        ClinicalEventData clinicalEventData2 = new ClinicalEventData();
        clinicalEventData2.setKey("AGENT");
        clinicalEventData2.setValue("abc");

        ClinicalEvent requestClinicalEvent =  new ClinicalEvent();
        requestClinicalEvent.setEventType("TREATMENT");
        requestClinicalEvent.setAttributes(Arrays.asList(clinicalEventData1, clinicalEventData2));
        List<ClinicalEvent> result = clinicalEventMyBatisRepository.getTimelineEvents(studyList, patientList, List.of(requestClinicalEvent));

        Assert.assertEquals(1, result.size());
        Assert.assertEquals((Integer) 213, result.getFirst().getStartDate());
        Assert.assertEquals((Integer) 543, result.getFirst().getStopDate());
    }


    @Test
    public void getClinicalEventsMeta() {
        List<String> studyList = new ArrayList<>();
        studyList.add("study_tcga_pub");
        List<String> patientList = new ArrayList<>();
        patientList.add("TCGA-A1-A0SD");

        ClinicalEventData clinicalEventData1 = new ClinicalEventData();
        clinicalEventData1.setKey("AGENT");
        clinicalEventData1.setValue("Madeupanib");

        ClinicalEventData clinicalEventData2 = new ClinicalEventData();
        clinicalEventData2.setKey("AGENT");
        clinicalEventData2.setValue("abc");

        ClinicalEvent requestClinicalEvent =  new ClinicalEvent();
        requestClinicalEvent.setEventType("TREATMENT");
        requestClinicalEvent.setAttributes(Arrays.asList(clinicalEventData1, clinicalEventData2));
        List<ClinicalEvent> result = clinicalEventMyBatisRepository.getClinicalEventsMeta(studyList, patientList, List.of(requestClinicalEvent));
        
        List<String> eventTypes = result.stream().map(ClinicalEvent::getEventType).toList();
        Assert.assertEquals(1, result.size());
        Assert.assertTrue(eventTypes.contains("treatment"));
    }
}
