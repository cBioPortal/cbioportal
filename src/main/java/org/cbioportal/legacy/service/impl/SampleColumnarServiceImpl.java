package org.cbioportal.legacy.service.impl;

import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.SampleDerivedRepository;
import org.cbioportal.legacy.service.SampleColumnarService;
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
    private SampleDerivedRepository sampleRepository;

    @Override
    public HttpHeaders fetchMetaSamples(
        SampleFilter sampleFilter
    ) {
        HttpHeaders responseHeaders = new HttpHeaders();
        BaseMeta baseMeta;

        if (sampleFilter.getSampleListIds() != null) {
            baseMeta = sampleRepository.fetchMetaSamplesBySampleListIds(sampleFilter.getSampleListIds());
        } else {
            Pair<List<String>, List<String>> studyAndSampleIds = extractStudyAndSampleIds(sampleFilter);
            baseMeta = sampleRepository.fetchMetaSamples(studyAndSampleIds.getFirst(), studyAndSampleIds.getSecond());
        }
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
