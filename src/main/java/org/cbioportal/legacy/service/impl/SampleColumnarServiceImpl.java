package org.cbioportal.legacy.service.impl;

import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.SampleDerivedRepository;
import org.cbioportal.legacy.service.PatientService;
import org.cbioportal.legacy.service.SampleColumnarService;
import org.cbioportal.legacy.service.StudyService;
import org.cbioportal.legacy.service.exception.PatientNotFoundException;
import org.cbioportal.legacy.service.exception.SampleNotFoundException;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.legacy.utils.config.annotation.ConditionalOnProperty;
import org.cbioportal.legacy.web.parameter.HeaderKeyConstants;
import org.cbioportal.legacy.web.parameter.SampleFilter;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.cbioportal.legacy.web.util.UniqueKeyExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(name = "clickhouse_mode", havingValue = "true")
public class SampleColumnarServiceImpl implements SampleColumnarService {

    @Autowired
    StudyService studyService;
    
    @Autowired
    PatientService patientService;
    
    @Autowired
    private SampleDerivedRepository sampleRepository;

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
        String projection
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

    public List<Sample> getAllSamplesInStudy(
        String studyId,
        String projection,
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

    public List<Sample> getAllSamplesOfPatientInStudy(
        String studyId,
        String patientId,
        String projection,
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

    public BaseMeta getMetaSamplesOfPatientInStudy(
        String studyId,
        String patientId
    ) throws StudyNotFoundException, PatientNotFoundException {
        patientService.getPatientInStudy(studyId, patientId);

        return sampleRepository.getMetaSamplesOfPatientInStudy(studyId, patientId);
    }
    
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
        String projection,
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
