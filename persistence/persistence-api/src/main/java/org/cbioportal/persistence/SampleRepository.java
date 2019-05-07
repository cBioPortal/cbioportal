package org.cbioportal.persistence;

import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface SampleRepository {

    @Cacheable("GeneralRepositoryCache")
    List<Sample> getAllSamplesInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber,
                                      String sortBy, String direction);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta getMetaSamplesInStudy(String studyId);

    @Cacheable("GeneralRepositoryCache")
    List<Sample> getAllSamplesInStudies(List<String> studyIds, String projection, Integer pageSize, Integer pageNumber,
                                      String sortBy, String direction);

    @Cacheable("GeneralRepositoryCache")
    Sample getSampleInStudy(String studyId, String sampleId);

    @Cacheable("GeneralRepositoryCache")
    List<Sample> getAllSamplesOfPatientInStudy(String studyId, String patientId, String projection, Integer pageSize,
                                               Integer pageNumber, String sortBy, String direction);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta getMetaSamplesOfPatientInStudy(String studyId, String patientId);

    @Cacheable("GeneralRepositoryCache")
    List<Sample> getAllSamplesOfPatientsInStudy(String studyId, List<String> patientIds, String projection);

    @Cacheable("GeneralRepositoryCache")
    List<Sample> getSamplesOfPatientsInMultipleStudies(List<String> studyIds, List<String> patientIds, String projection);

    @Cacheable("GeneralRepositoryCache")
    List<Sample> fetchSamples(List<String> studyIds, List<String> sampleIds, String projection);

    @Cacheable("GeneralRepositoryCache")
    List<Sample> fetchSamples(List<String> sampleListIds, String projection);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta fetchMetaSamples(List<String> studyIds, List<String> sampleIds);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta fetchMetaSamples(List<String> sampleListIds);

    @Cacheable("GeneralRepositoryCache")
    List<Sample> getSamplesByInternalIds(List<Integer> internalIds);
}
