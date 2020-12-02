package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.ClinicalEventSample;
import org.cbioportal.model.Treatment;
import org.cbioportal.persistence.TreatmentRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

import static org.junit.Assert.*;

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
        clinicalEventSample.setTimeTaken(213);

        HashMap<String, List<ClinicalEventSample>> expected = new HashMap<>();
        expected.put("TCGA-A1-A0SD", Collections.singletonList(clinicalEventSample));


        Map<String, List<ClinicalEventSample>> actual = treatmentRepository.getSamplesByPatientId(
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
        Integer expected = 3;
        Integer actual = treatmentRepository.getSampleCount(Collections.singletonList("study_tcga_pub"));
        
        Assert.assertEquals(actual, expected);
    }
    
}