package org.cbioportal.domain.sample.usecase;

import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.legacy.web.parameter.HeaderKeyConstants;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
public class GetMetaSamplesInStudyHeadersUseCase {
    private final GetMetaSamplesInStudyUseCase getMetaSamplesInStudyUseCase;
    
    public GetMetaSamplesInStudyHeadersUseCase(
        GetMetaSamplesInStudyUseCase getMetaSamplesInStudyUseCase
    ) {
        this.getMetaSamplesInStudyUseCase = getMetaSamplesInStudyUseCase;
    }

    public HttpHeaders execute(String studyId) throws StudyNotFoundException {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(
            HeaderKeyConstants.TOTAL_COUNT,
            getMetaSamplesInStudyUseCase.execute(studyId).getTotalCount().toString()
        );

        return responseHeaders;
    }

}
