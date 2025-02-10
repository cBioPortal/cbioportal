package org.cbioportal.legacy.service;

import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.web.parameter.SampleFilter;
import org.springframework.http.HttpHeaders;

import java.util.List;

public interface SampleColumnarService {
    HttpHeaders fetchMetaSamples(SampleFilter sampleFilter);
    
    List<Sample> fetchSamples(SampleFilter sampleFilter, String projection);
}
