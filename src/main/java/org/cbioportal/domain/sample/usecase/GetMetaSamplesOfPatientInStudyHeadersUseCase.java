package org.cbioportal.domain.sample.usecase;

import org.cbioportal.legacy.service.exception.PatientNotFoundException;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.legacy.web.parameter.HeaderKeyConstants;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
public class GetMetaSamplesOfPatientInStudyHeadersUseCase {
    private final GetMetaSamplesOfPatientInStudyUseCase getMetaSamplesOfPatientInStudyUseCase;

    public GetMetaSamplesOfPatientInStudyHeadersUseCase(GetMetaSamplesOfPatientInStudyUseCase getMetaSamplesOfPatientInStudyUseCase) {
        this.getMetaSamplesOfPatientInStudyUseCase = getMetaSamplesOfPatientInStudyUseCase;
    }

    public HttpHeaders execute(
        String studyId,
        String patientId
    ) throws StudyNotFoundException, PatientNotFoundException
    {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(
            HeaderKeyConstants.TOTAL_COUNT,
            getMetaSamplesOfPatientInStudyUseCase.execute(studyId, patientId).getTotalCount().toString()
        );

        return responseHeaders;
    }
}
