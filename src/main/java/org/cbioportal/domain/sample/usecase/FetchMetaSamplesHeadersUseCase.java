package org.cbioportal.domain.sample.usecase;

import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.web.parameter.HeaderKeyConstants;
import org.cbioportal.legacy.web.parameter.SampleFilter;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
public class FetchMetaSamplesHeadersUseCase {
    private final FetchMetaSamplesUseCase fetchMetaSamplesUseCase;
    
    public FetchMetaSamplesHeadersUseCase(FetchMetaSamplesUseCase fetchMetaSamplesUseCase) {
        this.fetchMetaSamplesUseCase = fetchMetaSamplesUseCase;
    }

    public HttpHeaders execute(
        SampleFilter sampleFilter
    ) {
        HttpHeaders responseHeaders = new HttpHeaders();
        BaseMeta baseMeta = fetchMetaSamplesUseCase.execute(sampleFilter);
        responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, baseMeta.getTotalCount().toString());

        return responseHeaders;
    }
}
