package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.ClinicalEventKeyCode;
import org.cbioportal.model.ClinicalEventSample;
import org.cbioportal.model.Treatment;
import org.cbioportal.persistence.TreatmentRepository;
import org.cbioportal.persistence.mybatis.config.TestConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {TreatmentMyBatisRepository.class, TestConfig.class})
public class TreatmentMyBatisRepositoryTest {

    @Autowired
    TreatmentRepository treatmentRepository;

    @Test
    public void getTreatmentsByPatientId() {
        Treatment treatment1 = new Treatment();
        treatment1.setTreatment("Madeupanib");
        treatment1.setStudyId("study_tcga_pub");
        treatment1.setPatientId("TCGA-A1-A0SD");
        treatment1.setStart(213);
        treatment1.setStop(445);
        
        Treatment treatment2 = new Treatment();
        treatment2.setTreatment("abc");
        treatment2.setStudyId("study_tcga_pub");
        treatment2.setPatientId("TCGA-A1-A0SD");
        treatment2.setStart(313);
        treatment2.setStop(543);

        Map<String, List<Treatment>> expected = new HashMap<>();
        expected.put("TCGA-A1-A0SD", Arrays.asList(treatment1, treatment2));

        Map<String, List<Treatment>> actual = treatmentRepository.getTreatmentsByPatientId(
            Collections.singletonList("TCGA-A1-A0SD-01"),
            Collections.singletonList("study_tcga_pub"),
            ClinicalEventKeyCode.Agent
        );

        Assert.assertEquals(actual, expected);
    }

    @Test
    public void getTreatmentAgentsByPatientId() {
        Treatment treatment1 = new Treatment();
        treatment1.setTreatment("Directly to forehead");
        treatment1.setStudyId("study_tcga_pub");
        treatment1.setPatientId("TCGA-A1-A0SD");
        treatment1.setStart(213);
        treatment1.setStop(445);

        Treatment treatment2 = new Treatment();
        treatment2.setTreatment("Elbow");
        treatment2.setStudyId("study_tcga_pub");
        treatment2.setPatientId("TCGA-A1-A0SD");
        treatment2.setStart(213);
        treatment2.setStop(445);
        
        Treatment treatment3 = new Treatment();
        treatment3.setTreatment("Left arm");
        treatment3.setStudyId("study_tcga_pub");
        treatment3.setPatientId("TCGA-A1-A0SD");
        treatment3.setStart(313);
        treatment3.setStop(543);
        
        Treatment treatment4 = new Treatment();
        treatment4.setTreatment("Ankle");
        treatment4.setStudyId("study_tcga_pub");
        treatment4.setPatientId("TCGA-A1-A0SD");
        treatment4.setStart(313);
        treatment4.setStop(543);

        Map<String, List<Treatment>> expected = new HashMap<>();
        expected.put("TCGA-A1-A0SD", Arrays.asList(treatment1, treatment2, treatment3, treatment4));

        Map<String, List<Treatment>> actual = treatmentRepository.getTreatmentsByPatientId(
            Collections.singletonList("TCGA-A1-A0SD-01"),
            Collections.singletonList("study_tcga_pub"),
            ClinicalEventKeyCode.AgentTarget
        );

        Assert.assertEquals(actual, expected);
    }

    @Test
    public void getSamplesByPatientIdWhenNoMatchingSamples() {
        ClinicalEventSample clinicalEventSample = new ClinicalEventSample();
        clinicalEventSample.setPatientId("TCGA-A1-A0SD");
        clinicalEventSample.setSampleId("TCGA-A1-A0SD-01");
        clinicalEventSample.setStudyId("study_tcga_pub");
        clinicalEventSample.setTimeTaken(211);

        HashMap<String, List<ClinicalEventSample>> expected = new HashMap<>();

        Map<String, List<ClinicalEventSample>> actual = treatmentRepository.getSamplesByPatientId(
            Collections.singletonList("TCGA-A1-A0SD-01"),
            Collections.singletonList("study_tcga_pub")
        );
        
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void getSamplesByPatientId() {
        ClinicalEventSample clinicalEventSample = new ClinicalEventSample();
        clinicalEventSample.setPatientId("TCGA-A1-A0SB");
        clinicalEventSample.setSampleId("TCGA-A1-A0SB-01");
        clinicalEventSample.setStudyId("study_tcga_pub");
        clinicalEventSample.setTimeTaken(211);

        HashMap<String, List<ClinicalEventSample>> expected = new HashMap<>();
        expected.put("TCGA-A1-A0SB", Collections.singletonList(clinicalEventSample));
        
        Map<String, List<ClinicalEventSample>> actual = treatmentRepository.getSamplesByPatientId(
            Collections.singletonList("TCGA-A1-A0SB-01"),
            Collections.singletonList("study_tcga_pub")
        );

        Assert.assertEquals(actual, expected);
    }

    @Test
    public void getShallowSamplesByPatientId() {
        ClinicalEventSample clinicalEventSample = new ClinicalEventSample();
        clinicalEventSample.setPatientId("TCGA-A1-A0SD");
        clinicalEventSample.setSampleId("TCGA-A1-A0SD-01");
        clinicalEventSample.setStudyId("study_tcga_pub");
        clinicalEventSample.setTimeTaken(213);

        HashMap<String, List<ClinicalEventSample>> expected = new HashMap<>();
        expected.put("TCGA-A1-A0SD", Collections.singletonList(clinicalEventSample));


        Map<String, List<ClinicalEventSample>> actual = treatmentRepository.getShallowSamplesByPatientId(
            Collections.singletonList("TCGA-A1-A0SD-01"),
            Collections.singletonList("study_tcga_pub")
        );


        Assert.assertEquals(actual, expected);
    }

    @Test
    public void hasTreatmentData() {

        Assert.assertEquals(true, treatmentRepository.hasTreatmentData(Collections.singletonList("study_tcga_pub"), ClinicalEventKeyCode.Agent));

        Assert.assertEquals(false, treatmentRepository.hasTreatmentData(Collections.singletonList("acc_tcga"), ClinicalEventKeyCode.Agent));

    }

    @Test
    public void hasSampleTimelineData() {

        Assert.assertEquals(true, treatmentRepository.hasSampleTimelineData(Collections.singletonList("study_tcga_pub")));

        Assert.assertEquals(false, treatmentRepository.hasSampleTimelineData(Collections.singletonList("acc_tcga")));
    }
    
}