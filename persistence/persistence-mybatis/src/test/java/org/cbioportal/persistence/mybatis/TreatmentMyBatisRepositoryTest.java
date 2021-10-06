package org.cbioportal.persistence.mybatis;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {TreatmentMyBatisRepository.class, TestConfig.class})
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
            Collections.singletonList("study_tcga_pub")
        );


        Assert.assertEquals(actual, expected);
    }

    @Test
    public void getSamplesByPatientId() {
        ClinicalEventSample clinicalEventSample = new ClinicalEventSample();
        clinicalEventSample.setPatientId("TCGA-A1-A0SD");
        clinicalEventSample.setSampleId("TCGA-A1-A0SD-01");
        clinicalEventSample.setStudyId("study_tcga_pub");
        clinicalEventSample.setTimeTaken(211);

        HashMap<String, List<ClinicalEventSample>> expected = new HashMap<>();
        expected.put("TCGA-A1-A0SD", Collections.singletonList(clinicalEventSample));


        Map<String, List<ClinicalEventSample>> actual = treatmentRepository.getSamplesByPatientId(
            Collections.singletonList("TCGA-A1-A0SD-01"),
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
    public void getAllUniqueTreatments() {
        HashSet<String> expected = new HashSet<>(Collections.singletonList("Madeupanib"));
        
        Set<String> actual = treatmentRepository.getAllUniqueTreatments(
            Collections.singletonList("TCGA-A1-A0SD-01"),
            Collections.singletonList("study_tcga_pub")
        );
        
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void getTreatmentCount() {
        Integer expected = 1;
        Integer actual = treatmentRepository.getTreatmentCount(Collections.singletonList("study_tcga_pub"));
        
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void getSampleCount() {
        Integer expected = 4;
        Integer actual = treatmentRepository.getSampleCount(Collections.singletonList("study_tcga_pub"));
        
        Assert.assertEquals(actual, expected);
    }
    
}