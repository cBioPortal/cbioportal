package org.cbioportal.legacy.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.legacy.model.Patient;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.PatientRepository;
import org.cbioportal.legacy.service.PatientService;
import org.cbioportal.legacy.service.StudyService;
import org.cbioportal.legacy.service.exception.PatientNotFoundException;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

@Service
public class PatientServiceImpl implements PatientService {

  @Autowired private PatientRepository patientRepository;
  @Autowired private StudyService studyService;

  @Value("${authenticate:false}")
  private String AUTHENTICATE;

  @Override
  @PostFilter(
      "hasPermission(filterObject, T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public List<Patient> getAllPatients(
      String keyword,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {

    List<Patient> patients =
        patientRepository.getAllPatients(
            keyword, projection, pageSize, pageNumber, sortBy, direction);
    // copy the list before returning so @PostFilter doesn't taint the list stored in the
    // persistence layer cache
    return (AUTHENTICATE.equals("false")) ? patients : new ArrayList<Patient>(patients);
  }

  @Override
  public BaseMeta getMetaPatients(String keyword) {

    return patientRepository.getMetaPatients(keyword);
  }

  @Override
  public List<Patient> getAllPatientsInStudy(
      String studyId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction)
      throws StudyNotFoundException {

    studyService.studyExists(studyId);

    return patientRepository.getAllPatientsInStudy(
        studyId, projection, pageSize, pageNumber, sortBy, direction);
  }

  @Override
  public BaseMeta getMetaPatientsInStudy(String studyId) throws StudyNotFoundException {

    studyService.studyExists(studyId);

    return patientRepository.getMetaPatientsInStudy(studyId);
  }

  @Override
  public Patient getPatientInStudy(String studyId, String patientId)
      throws PatientNotFoundException, StudyNotFoundException {

    studyService.studyExists(studyId);

    Patient patient = patientRepository.getPatientInStudy(studyId, patientId);

    if (patient == null) {
      throw new PatientNotFoundException(studyId, patientId);
    }

    return patient;
  }

  @Override
  public List<Patient> fetchPatients(
      List<String> studyIds, List<String> patientIds, String projection) {

    return patientRepository.fetchPatients(studyIds, patientIds, projection);
  }

  @Override
  public BaseMeta fetchMetaPatients(List<String> studyIds, List<String> patientIds) {

    return patientRepository.fetchMetaPatients(studyIds, patientIds);
  }

  @Override
  public List<Patient> getPatientsOfSamples(List<String> studyIds, List<String> sampleIds) {

    return patientRepository.getPatientsOfSamples(studyIds, sampleIds);
  }
}
