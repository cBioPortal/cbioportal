package org.cbioportal.persistence;

import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface SampleRepository {

    @Cacheable("RepositoryCache")
    List<Sample> getAllSamplesInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber,
                                      String sortBy, String direction);

    @Cacheable("RepositoryCache")
    BaseMeta getMetaSamplesInStudy(String studyId);

    @Cacheable("RepositoryCache")
    List<Sample> getAllSamplesInStudies(List<String> studyIds, String projection, Integer pageSize, Integer pageNumber,
                                      String sortBy, String direction);

    @Cacheable("RepositoryCache")
    Sample getSampleInStudy(String studyId, String sampleId);

    @Cacheable("RepositoryCache")
    List<Sample> getAllSamplesOfPatientInStudy(String studyId, String patientId, String projection, Integer pageSize,
                                               Integer pageNumber, String sortBy, String direction);

    @Cacheable("RepositoryCache")
    BaseMeta getMetaSamplesOfPatientInStudy(String studyId, String patientId);

    @Cacheable("RepositoryCache")
    List<Sample> getAllSamplesOfPatientsInStudy(String studyId, List<String> patientIds, String projection);

    @Cacheable("RepositoryCache")
    List<Sample> getSamplesOfPatientsInMultipleStudies(List<String> studyIds, List<String> patientIds, String projection);

    @Cacheable("RepositoryCache")
    List<Sample> fetchSamples(List<String> studyIds, List<String> sampleIds, String projection);

    @Cacheable("RepositoryCache")
    List<Sample> fetchSamples(List<String> sampleListIds, String projection);

    @Cacheable("RepositoryCache")
    BaseMeta fetchMetaSamples(List<String> studyIds, List<String> sampleIds);

    @Cacheable("RepositoryCache")
    BaseMeta fetchMetaSamples(List<String> sampleListIds);

    @Cacheable("RepositoryCache")
    List<Sample> getSamplesByInternalIds(List<Integer> internalIds);
}
