package org.cbioportal.legacy.service;

import java.util.List;
import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.service.exception.PatientNotFoundException;
import org.cbioportal.legacy.service.exception.SampleNotFoundException;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;

public interface SampleService {

  List<Sample> getAllSamplesInStudy(
      String studyId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction)
      throws StudyNotFoundException;

  BaseMeta getMetaSamplesInStudy(String studyId) throws StudyNotFoundException;

  List<Sample> getAllSamplesInStudies(
      List<String> studyIds,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction);

  Sample getSampleInStudy(String studyId, String sampleId)
      throws SampleNotFoundException, StudyNotFoundException;

  List<Sample> getAllSamplesOfPatientInStudy(
      String studyId,
      String patientId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction)
      throws StudyNotFoundException, PatientNotFoundException;

  BaseMeta getMetaSamplesOfPatientInStudy(String studyId, String patientId)
      throws StudyNotFoundException, PatientNotFoundException;

  List<Sample> getAllSamplesOfPatientsInStudy(
      String studyId, List<String> patientIds, String projection);

  List<Sample> getSamplesOfPatientsInMultipleStudies(
      List<String> studyIds, List<String> patientIds, String projection);

  List<Sample> fetchSamples(List<String> studyIds, List<String> sampleIds, String projection);

  List<Sample> fetchSamples(List<String> sampleListIds, String projection);

  BaseMeta fetchMetaSamples(List<String> studyIds, List<String> sampleIds);

  BaseMeta fetchMetaSamples(List<String> sampleListIds);

  // TODO get rid of this method. Use static ids instead
  List<Sample> getSamplesByInternalIds(List<Integer> internalIds);

  List<Sample> getAllSamples(
      String keyword,
      List<String> studyIds,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sort,
      String direction);

  BaseMeta getMetaSamples(String keyword, List<String> studyIds);
}
