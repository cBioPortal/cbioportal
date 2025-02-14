package org.cbioportal.domain.sample.service;

import org.cbioportal.domain.sample.repository.SampleRepository;
import org.cbioportal.domain.sample.Sample;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.service.PatientService;
import org.cbioportal.legacy.service.StudyService;
import org.cbioportal.legacy.service.exception.PatientNotFoundException;
import org.cbioportal.legacy.service.exception.SampleNotFoundException;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.legacy.web.parameter.HeaderKeyConstants;
import org.cbioportal.legacy.web.parameter.SampleFilter;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.cbioportal.legacy.web.util.UniqueKeyExtractor;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.context.annotation.Profile;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Profile("clickhouse")
public class ColumnStoreSampleServiceImpl implements SampleService {
    
    private final StudyService studyService;
    
    private final PatientService patientService;
    
    private final SampleRepository sampleRepository;

    public ColumnStoreSampleServiceImpl(
        StudyService studyService,
        PatientService patientService,
        SampleRepository sampleRepository
    ) {
        this.studyService = studyService;
        this.patientService = patientService;
        this.sampleRepository = sampleRepository;
    }

    @Override
    public BaseMeta fetchMetaSamples(
        SampleFilter sampleFilter
    ) {
        BaseMeta baseMeta;

        if (sampleFilter.getSampleListIds() != null) {
            baseMeta = sampleRepository.fetchMetaSamplesBySampleListIds(sampleFilter.getSampleListIds());
        } else {
            Pair<List<String>, List<String>> studyAndSampleIds = extractStudyAndSampleIds(sampleFilter);
            baseMeta = sampleRepository.fetchMetaSamples(studyAndSampleIds.getFirst(), studyAndSampleIds.getSecond());
        }
        
        return baseMeta;
    }

    @Override
    public HttpHeaders fetchMetaSamplesHeaders(
        SampleFilter sampleFilter
    ) {
        HttpHeaders responseHeaders = new HttpHeaders();
        BaseMeta baseMeta = fetchMetaSamples(sampleFilter);
        responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, baseMeta.getTotalCount().toString());

        return responseHeaders;
    }
    
    @Override
    public List<Sample> fetchSamples(
        SampleFilter sampleFilter,
        ProjectionType projection
    ) {
        List<Sample> samples;
        
        if (sampleFilter.getSampleListIds() != null) {
            List<String> sampleListIds = sampleFilter.getSampleListIds();
            samples = sampleRepository.fetchSamplesBySampleListIds(sampleListIds, projection);
        } else {
            Pair<List<String>, List<String>> studyAndSampleIds = extractStudyAndSampleIds(sampleFilter);
            samples = sampleRepository.fetchSamples(studyAndSampleIds.getFirst(), studyAndSampleIds.getSecond(), projection);
        }
        
        return samples;
    }

    @Override
    public List<Sample> getAllSamplesInStudy(
        String studyId,
        ProjectionType projection,
        Integer pageSize,
        Integer pageNumber,
        String sortBy,
        String direction
    ) throws StudyNotFoundException {
        studyService.getStudy(studyId);
        
        return sampleRepository.getAllSamplesInStudy(
            studyId,
            projection,
            pageSize,
            pageNumber,
            sortBy,
            direction
        );
    }

    @Override
    public Sample getSampleInStudy(
        String studyId,
        String sampleId
    ) throws SampleNotFoundException, StudyNotFoundException {
        studyService.getStudy(studyId);
        Sample sample = sampleRepository.getSampleInStudy(studyId, sampleId);

        if (sample == null) {
            throw new SampleNotFoundException(studyId, sampleId);
        }
        
        return sample;
    }

    @Override
    public List<Sample> getAllSamplesOfPatientInStudy(
        String studyId,
        String patientId,
        ProjectionType projection,
        Integer pageSize,
        Integer pageNumber,
        String sortBy,
        String direction
    ) throws StudyNotFoundException, PatientNotFoundException {
        patientService.getPatientInStudy(studyId, patientId);
        
        return sampleRepository.getAllSamplesOfPatientInStudy(
            studyId,
            patientId,
            projection,
            pageSize,
            pageNumber,
            sortBy,
            direction
        );
    }
    
    @Override
    public HttpHeaders getMetaSamplesInStudyHeaders(String studyId) throws StudyNotFoundException {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(
            HeaderKeyConstants.TOTAL_COUNT,
            getMetaSamplesInStudy(studyId).getTotalCount().toString()
        );
        
        return responseHeaders;
    }
    
    @Override
    public BaseMeta getMetaSamplesInStudy(String studyId) throws StudyNotFoundException {
        studyService.getStudy(studyId);

        return sampleRepository.getMetaSamplesInStudy(studyId);
    }

    @Override
    public BaseMeta getMetaSamplesOfPatientInStudy(
        String studyId,
        String patientId
    ) throws StudyNotFoundException, PatientNotFoundException {
        patientService.getPatientInStudy(studyId, patientId);

        return sampleRepository.getMetaSamplesOfPatientInStudy(studyId, patientId);
    }
    
    @Override
    public HttpHeaders getMetaSamplesOfPatientInStudyHeaders(
        String studyId,
        String patientId
    ) throws StudyNotFoundException, PatientNotFoundException
    {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(
            HeaderKeyConstants.TOTAL_COUNT,
            getMetaSamplesOfPatientInStudy(studyId, patientId).getTotalCount().toString()
        );
        
        return responseHeaders;
    }

    @Override
    public List<Sample> getAllSamples(
        String keyword,
        List<String> studyIds,
        ProjectionType projection,
        Integer pageSize,
        Integer pageNumber,
        String sort,
        String direction
    ) {
        return sampleRepository.getAllSamples(keyword, studyIds, projection, pageSize, pageNumber, sort, direction);
    }

    @Override
    public BaseMeta getMetaSamples(String keyword, List<String> studyIds) {
        return sampleRepository.getMetaSamples(keyword, studyIds);
    }

    @Override
    public HttpHeaders getMetaSamplesHeaders(String keyword, List<String> studyIds) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(
            HeaderKeyConstants.TOTAL_COUNT,
            getMetaSamples(keyword, studyIds).getTotalCount().toString()
        );

        return httpHeaders;
    }

    private Pair<List<String>, List<String>> extractStudyAndSampleIds(
        SampleFilter sampleFilter
    ) {
        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();

        if (sampleFilter.getSampleIdentifiers() != null) {
            for (SampleIdentifier sampleIdentifier : sampleFilter.getSampleIdentifiers()) {
                studyIds.add(sampleIdentifier.getStudyId());
                sampleIds.add(sampleIdentifier.getSampleId());
            }
        } else {
            UniqueKeyExtractor.extractUniqueKeys(sampleFilter.getUniqueSampleKeys(), studyIds, sampleIds);
        }
        
        return Pair.of(studyIds, sampleIds);
    }
}
