package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.ClinicalEventSample;
import org.cbioportal.model.Treatment;
import org.cbioportal.model.ClinicalEventKeyCode;
import org.cbioportal.persistence.TreatmentRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class TreatmentMyBatisRepositoryTest {
    @Autowired
    TreatmentRepository treatmentRepository;

    @Test
    public void getTreatmentsByPatientId() {
        Treatment treatment = new Treatment();
        treatment.setTreatment("Madeupanib");
        treatment.setStudyId("study_tcga_pub");
        treatment.setPatientId("TCGA-A1-A0SD");
        treatment.setStart(213);
        treatment.setStop(445);

        Map<String, List<Treatment>> expected = new HashMap<>();
        expected.put("TCGA-A1-A0SD", Collections.singletonList(treatment));


        Map<String, List<Treatment>> actual = treatmentRepository.getTreatmentsByPatientId(
            Collections.singletonList("TCGA-A1-A0SD-01"),
            Collections.singletonList("study_tcga_pub"),
            ClinicalEventKeyCode.Agent
        );


        Assert.assertEquals(actual, expected);
    }


    @Test
    public void getTreatmentAgentsByPatientId() {
        Treatment targetA = new Treatment();
        targetA.setTreatment("Directly to forehead");
        targetA.setStudyId("study_tcga_pub");
        targetA.setPatientId("TCGA-A1-A0SD");
        targetA.setStart(213);
        targetA.setStop(445);

        Treatment targetB = new Treatment();
        targetB.setTreatment("Elbow");
        targetB.setStudyId("study_tcga_pub");
        targetB.setPatientId("TCGA-A1-A0SD");
        targetB.setStart(213);
        targetB.setStop(445);

        Map<String, List<Treatment>> expected = new HashMap<>();
        expected.put("TCGA-A1-A0SD", Arrays.asList(targetA, targetB));


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