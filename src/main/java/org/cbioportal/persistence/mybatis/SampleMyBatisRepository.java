package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.SampleRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.mybatis.util.PaginationCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class SampleMyBatisRepository implements SampleRepository {

    @Autowired
    private SampleMapper sampleMapper;

    @Override
    public List<Sample> getAllSamples(
            String keyword, List<String> studyIds, String projection,
            Integer pageSize, Integer pageNumber, String sort, String direction
    ) {
        return sampleMapper.getSamples(
                studyIds, null, null, keyword, projection, pageSize,
                PaginationCalculator.offset(pageSize, pageNumber), sort, direction);
    }

    @Override
    public BaseMeta getMetaSamples(String keyword, List<String> studyIds) {
        return sampleMapper.getMetaSamples(studyIds, null, null, keyword);
    }

    @Override
    public List<Sample> getAllSamplesInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber,
                                             String sortBy, String direction) {

        return sampleMapper.getSamples(Arrays.asList(studyId), null, null, null, projection, pageSize,
                PaginationCalculator.offset(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaSamplesInStudy(String studyId) {

        return sampleMapper.getMetaSamples(Arrays.asList(studyId), null, null, null);
    }

    @Override
    public List<Sample> getAllSamplesInStudies(List<String> studyIds, String projection, Integer pageSize,
            Integer pageNumber, String sortBy, String direction) {
        return sampleMapper.getSamples(studyIds, null, null, null, projection, pageSize, 
                PaginationCalculator.offset(pageSize, pageNumber), sortBy, direction);
    }
    
    @Override
    public Sample getSampleInStudy(String studyId, String sampleId) {

        return sampleMapper.getSample(studyId, sampleId, PersistenceConstants.DETAILED_PROJECTION);
    }

    @Override
    public List<Sample> getAllSamplesOfPatientInStudy(String studyId, String patientId, String projection,
                                                      Integer pageSize, Integer pageNumber, String sortBy,
                                                      String direction) {

        return sampleMapper.getSamples(Arrays.asList(studyId), patientId, null, null, projection,
                pageSize, PaginationCalculator.offset(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaSamplesOfPatientInStudy(String studyId, String patientId) {

        return sampleMapper.getMetaSamples(Arrays.asList(studyId), patientId, null, null);
    }

    @Override
    public List<Sample> getAllSamplesOfPatientsInStudy(String studyId, List<String> patientIds, String projection) {

        return sampleMapper.getSamplesOfPatients(studyId, patientIds, projection);
    }

    @Override
    public List<Sample> getSamplesOfPatientsInMultipleStudies(List<String> studyIds, List<String> patientIds,
            String projection) {
                
        return sampleMapper.getSamplesOfPatientsInMultipleStudies(studyIds, patientIds, projection);
    }

    @Override
    public List<Sample> fetchSamples(List<String> studyIds, List<String> sampleIds, String projection) {

        return sampleMapper.getSamples(studyIds, null, sampleIds, null,
                projection, 0, 0, null, null);
    }

    @Override
    public List<Sample> fetchSamplesBySampleListIds(List<String> sampleListIds, String projection) {
        // Rather than get all the samples for all the sample list IDs all at once,
        // we get them 1 sample list ID at a time. This allows for more efficient caching, since
        // the samples associated with each sample list are only cached once.
        // We also need to remove duplicates samples because samples from multiple sample list IDs can have overlapping
        return sampleListIds.stream()
            .flatMap(id -> fetchSampleBySampleListId(id, projection).stream())
            .distinct()
            .collect(Collectors.toList());
    }

    @Override
    public List<Sample> fetchSampleBySampleListId(String sampleListId, String projection) {
        return sampleMapper.getSamplesBySampleListIds(Collections.singletonList(sampleListId), projection);
    }

    @Override
    public BaseMeta fetchMetaSamples(List<String> studyIds, List<String> sampleIds) {

        return sampleMapper.getMetaSamples(studyIds, null, sampleIds, null);
    }

    @Override
    public BaseMeta fetchMetaSamples(List<String> sampleListIds) {
        
        return sampleMapper.getMetaSamplesBySampleListIds(sampleListIds);
    }

    @Override
    public List<Sample> getSamplesByInternalIds(List<Integer> internalIds) {

        return sampleMapper.getSamplesByInternalIds(internalIds, PersistenceConstants.ID_PROJECTION);
    }
}
