package org.cbioportal.service;

import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.SampleNotFoundException;

import java.util.List;

public interface SampleService {

    List<Sample> getAllSamplesInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber,
                                      String sortBy, String direction);

    BaseMeta getMetaSamplesInStudy(String studyId);

    Sample getSampleInStudy(String studyId, String sampleId) throws SampleNotFoundException;

    List<Sample> getAllSamplesOfPatientInStudy(String studyId, String patientId, String projection, Integer pageSize,
                                               Integer pageNumber, String sortBy, String direction);

    BaseMeta getMetaSamplesOfPatientInStudy(String studyId, String patientId);

    List<Sample> fetchSamples(List<String> studyIds, List<String> sampleIds, String projection);

    BaseMeta fetchMetaSamples(List<String> studyIds, List<String> sampleIds);
}
