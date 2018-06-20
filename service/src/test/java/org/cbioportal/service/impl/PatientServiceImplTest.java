package org.cbioportal.service.impl;

import org.cbioportal.model.Patient;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.PatientRepository;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class PatientServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private PatientServiceImpl patientService;

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private StudyService studyService;

    @Test
    public void getAllPatientsInStudy() throws Exception {

        List<Patient> expectedPatientList = new ArrayList<>();
        Patient patient = new Patient();
        expectedPatientList.add(patient);

        Mockito.when(patientRepository.getAllPatientsInStudy(STUDY_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT,
            DIRECTION)).thenReturn(expectedPatientList);

        List<Patient> result = patientService.getAllPatientsInStudy(STUDY_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT,
            DIRECTION);

        Assert.assertEquals(expectedPatientList, result);
    }
    
    @Test(expected = StudyNotFoundException.class)
    public void getAllPatientsInStudyNotFound() throws Exception {
        
        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        patientService.getAllPatientsInStudy(STUDY_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);
    }

    @Test
    public void getMetaPatientsInStudy() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(patientRepository.getMetaPatientsInStudy(STUDY_ID)).thenReturn(expectedBaseMeta);
        BaseMeta result = patientService.getMetaPatientsInStudy(STUDY_ID);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = StudyNotFoundException.class)
    public void getMetaPatientsInStudyNotFound() throws Exception {
        
        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        patientService.getMetaPatientsInStudy(STUDY_ID);
    }

    @Test(expected = PatientNotFoundException.class)
    public void getPatientInStudyPatientNotFound() throws Exception {

        Mockito.when(patientRepository.getPatientInStudy(STUDY_ID, PATIENT_ID_1)).thenReturn(null);
        patientService.getPatientInStudy(STUDY_ID, PATIENT_ID_1);
    }

    @Test(expected = StudyNotFoundException.class)
    public void getPatientInStudyNotFound() throws Exception {

        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        patientService.getPatientInStudy(STUDY_ID, PATIENT_ID_1);
    }

    @Test
    public void getPatientInStudy() throws Exception {

        Patient expectedPatient = new Patient();
        Mockito.when(patientRepository.getPatientInStudy(STUDY_ID, PATIENT_ID_1)).thenReturn(expectedPatient);
        Patient result = patientService.getPatientInStudy(STUDY_ID, PATIENT_ID_1);

        Assert.assertEquals(expectedPatient, result);
    }

    @Test
    public void fetchPatients() throws Exception {

        List<Patient> expectedPatientList = new ArrayList<>();
        Patient patient = new Patient();
        expectedPatientList.add(patient);

        Mockito.when(patientRepository.fetchPatients(Arrays.asList(STUDY_ID), Arrays.asList(PATIENT_ID_1), PROJECTION))
            .thenReturn(expectedPatientList);

        List<Patient> result = patientService.fetchPatients(Arrays.asList(STUDY_ID), Arrays.asList(PATIENT_ID_1),
            PROJECTION);

        Assert.assertEquals(expectedPatientList, result);
    }

    @Test
    public void fetchMetaPatients() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(patientRepository.fetchMetaPatients(Arrays.asList(STUDY_ID), Arrays.asList(PATIENT_ID_1)))
            .thenReturn(expectedBaseMeta);
        BaseMeta result = patientService.fetchMetaPatients(Arrays.asList(STUDY_ID), Arrays.asList(PATIENT_ID_1));

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test
    public void getPatientIdsOfSamples() throws Exception {

        Mockito.when(patientRepository.getPatientIdsOfSamples(Arrays.asList(SAMPLE_ID1))).thenReturn(Arrays.asList(PATIENT_ID_1));

        List<String> result = patientService.getPatientIdsOfSamples(Arrays.asList(SAMPLE_ID1));
        
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(PATIENT_ID_1, result.get(0));
    }
}
