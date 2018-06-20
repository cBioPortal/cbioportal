package org.cbioportal.persistence;

import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface SampleRepository {

    List<Sample> getAllSamplesInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber,
                                      String sortBy, String direction);

    BaseMeta getMetaSamplesInStudy(String studyId);

    List<Sample> getAllSamplesInStudies(List<String> studyIds, String projection, Integer pageSize, Integer pageNumber,
                                      String sortBy, String direction);

    Sample getSampleInStudy(String studyId, String sampleId);

    List<Sample> getAllSamplesOfPatientInStudy(String studyId, String patientId, String projection, Integer pageSize,
                                               Integer pageNumber, String sortBy, String direction);

    BaseMeta getMetaSamplesOfPatientInStudy(String studyId, String patientId);

    List<Sample> getAllSamplesOfPatientsInStudy(String studyId, List<String> patientIds, String projection);

    List<Sample> getSamplesOfPatientsInMultipleStudies(List<String> studyIds, List<String> patientIds, String projection);
    
    List<Sample> fetchSamples(List<String> studyIds, List<String> sampleIds, String projection);

    List<Sample> fetchSamples(List<String> sampleListIds, String projection);

    BaseMeta fetchMetaSamples(List<String> studyIds, List<String> sampleIds);

    BaseMeta fetchMetaSamples(List<String> sampleListIds);

    List<Sample> getSamplesByInternalIds(List<Integer> internalIds);
}
