package org.cbioportal.service.impl;

import org.cbioportal.model.Patient;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.PatientRepository;
import org.cbioportal.service.exception.PatientNotFoundException;
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

    @Test
    public void getMetaPatientsInStudy() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(patientRepository.getMetaPatientsInStudy(STUDY_ID)).thenReturn(expectedBaseMeta);
        BaseMeta result = patientService.getMetaPatientsInStudy(STUDY_ID);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = PatientNotFoundException.class)
    public void getPatientInStudyNotFound() throws Exception {

        Mockito.when(patientRepository.getPatientInStudy(STUDY_ID, PATIENT_ID)).thenReturn(null);
        patientService.getPatientInStudy(STUDY_ID, PATIENT_ID);
    }

    @Test
    public void getPatientInStudy() throws Exception {

        Patient expectedPatient = new Patient();
        Mockito.when(patientRepository.getPatientInStudy(STUDY_ID, PATIENT_ID)).thenReturn(expectedPatient);
        Patient result = patientService.getPatientInStudy(STUDY_ID, PATIENT_ID);

        Assert.assertEquals(expectedPatient, result);
    }

    @Test
    public void fetchPatients() throws Exception {

        List<Patient> expectedPatientList = new ArrayList<>();
        Patient patient = new Patient();
        expectedPatientList.add(patient);

        Mockito.when(patientRepository.fetchPatients(Arrays.asList(STUDY_ID), Arrays.asList(PATIENT_ID), PROJECTION))
            .thenReturn(expectedPatientList);

        List<Patient> result = patientService.fetchPatients(Arrays.asList(STUDY_ID), Arrays.asList(PATIENT_ID),
            PROJECTION);

        Assert.assertEquals(expectedPatientList, result);
    }

    @Test
    public void fetchMetaPatients() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(patientRepository.fetchMetaPatients(Arrays.asList(STUDY_ID), Arrays.asList(PATIENT_ID)))
            .thenReturn(expectedBaseMeta);
        BaseMeta result = patientService.fetchMetaPatients(Arrays.asList(STUDY_ID), Arrays.asList(PATIENT_ID));

        Assert.assertEquals(expectedBaseMeta, result);
    }
}
