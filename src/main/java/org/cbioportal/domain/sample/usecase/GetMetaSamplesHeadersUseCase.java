package org.cbioportal.domain.sample.usecase;

import org.cbioportal.legacy.web.parameter.HeaderKeyConstants;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
public class GetMetaSamplesHeadersUseCase {
    private final GetMetaSamplesUseCase getMetaSamplesUseCase;

    public GetMetaSamplesHeadersUseCase(GetMetaSamplesUseCase getMetaSamplesUseCase) {
        this.getMetaSamplesUseCase = getMetaSamplesUseCase;
    }

    public HttpHeaders execute(String keyword, List<String> studyIds) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(
            HeaderKeyConstants.TOTAL_COUNT,
            getMetaSamplesUseCase.execute(keyword, studyIds).getTotalCount().toString()
        );

        return httpHeaders;
    }
}
