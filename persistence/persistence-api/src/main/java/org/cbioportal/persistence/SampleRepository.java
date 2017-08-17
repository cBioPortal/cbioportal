package org.cbioportal.persistence;

import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface SampleRepository {

    List<Sample> getAllSamplesInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber,
                                      String sortBy, String direction);

    BaseMeta getMetaSamplesInStudy(String studyId);

    Sample getSampleInStudy(String studyId, String sampleId);

    List<Sample> getAllSamplesOfPatientInStudy(String studyId, String patientId, String projection, Integer pageSize,
                                               Integer pageNumber, String sortBy, String direction);

    BaseMeta getMetaSamplesOfPatientInStudy(String studyId, String patientId);

    List<Sample> getAllSamplesOfPatientsInStudy(String studyId, List<String> patientIds, String projection);
    
    List<Sample> fetchSamples(List<String> studyIds, List<String> sampleIds, String projection);

    BaseMeta fetchMetaSamples(List<String> studyIds, List<String> sampleIds);

    List<Sample> getSamplesByInternalIds(List<Integer> internalIds);
}
