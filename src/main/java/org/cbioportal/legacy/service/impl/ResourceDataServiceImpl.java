package org.cbioportal.legacy.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.legacy.model.ResourceData;
import org.cbioportal.legacy.persistence.ResourceDataRepository;
import org.cbioportal.legacy.service.PatientService;
import org.cbioportal.legacy.service.ResourceDataService;
import org.cbioportal.legacy.service.SampleService;
import org.cbioportal.legacy.service.StudyService;
import org.cbioportal.legacy.service.exception.PatientNotFoundException;
import org.cbioportal.legacy.service.exception.SampleNotFoundException;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResourceDataServiceImpl implements ResourceDataService {

  @Autowired private ResourceDataRepository resourceDataRepository;
  @Autowired private StudyService studyService;
  @Autowired private PatientService patientService;
  @Autowired private SampleService sampleService;

  @Override
  public List<ResourceData> getAllResourceDataOfSampleInStudy(
      String studyId,
      String sampleId,
      String resourceId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction)
      throws SampleNotFoundException, StudyNotFoundException {
    sampleService.getSampleInStudy(studyId, sampleId);

    return resourceDataRepository.getAllResourceDataOfSampleInStudy(
        studyId, sampleId, resourceId, projection, pageSize, pageNumber, sortBy, direction);
  }

  @Override
  public List<ResourceData> getAllResourceDataOfPatientInStudy(
      String studyId,
      String patientId,
      String resourceId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction)
      throws PatientNotFoundException, StudyNotFoundException {

    patientService.getPatientInStudy(studyId, patientId);

    return resourceDataRepository.getAllResourceDataOfPatientInStudy(
        studyId, patientId, resourceId, projection, pageSize, pageNumber, sortBy, direction);
  }

  @Override
  public List<ResourceData> getAllResourceDataForStudy(
      String studyId,
      String resourceId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction)
      throws StudyNotFoundException {

    studyService.getStudy(studyId);

    return resourceDataRepository.getAllResourceDataForStudy(
        studyId, resourceId, projection, pageSize, pageNumber, sortBy, direction);
  }

  @Override
  public List<ResourceData> getAllResourceDataForStudyPatientSample(
      String studyId,
      String resourceId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction)
      throws StudyNotFoundException {

    studyService.getStudy(studyId);

    List<ResourceData> results = new ArrayList<ResourceData>();

    results.addAll(
        resourceDataRepository.getAllResourceDataForStudy(
            studyId, resourceId, projection, pageSize, pageNumber, sortBy, direction));
    results.addAll(
        resourceDataRepository.getResourceDataForAllPatientsInStudy(
            studyId, resourceId, projection, pageSize, pageNumber, sortBy, direction));
    results.addAll(
        resourceDataRepository.getResourceDataForAllSamplesInStudy(
            studyId, resourceId, projection, pageSize, pageNumber, sortBy, direction));
    return results;
  }
}
