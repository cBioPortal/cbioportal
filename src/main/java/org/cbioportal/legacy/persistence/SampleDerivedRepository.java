package org.cbioportal.legacy.persistence;

import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.model.meta.BaseMeta;

import java.util.List;

public interface SampleDerivedRepository {
    List<Sample> fetchSamples(List<String> studyIds, List<String> sampleIds, String projection);

    List<Sample> fetchSamplesBySampleListIds(List<String> sampleListIds, String projection);

    BaseMeta fetchMetaSamples(List<String> studyIds, List<String> sampleIds);
    
    BaseMeta fetchMetaSamplesBySampleListIds(List<String> sampleListIds);

    List<Sample> getAllSamplesInStudy(
        String studyId,
        String projection,
        Integer pageSize,
        Integer pageNumber,
        String sortBy,
        String direction
    );

    BaseMeta getMetaSamplesInStudy(String studyId);

    Sample getSampleInStudy(
        String studyId,
        String sampleId
    );

    List<Sample> getAllSamplesOfPatientInStudy(
        String studyId,
        String patientId,
        String projection,
        Integer pageSize,
        Integer pageNumber,
        String sortBy,
        String direction
    );

    BaseMeta getMetaSamplesOfPatientInStudy(String studyId, String patientId);

    List<Sample> getAllSamples(
        String keyword,
        List<String> studyIds,
        String projection,
        Integer pageSize,
        Integer pageNumber,
        String sort,
        String direction
    );

    BaseMeta getMetaSamples(String keyword, List<String> studyIds);
}
