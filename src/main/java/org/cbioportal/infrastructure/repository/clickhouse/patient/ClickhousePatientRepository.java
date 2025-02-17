package org.cbioportal.infrastructure.repository.clickhouse.patient;

import org.cbioportal.legacy.model.CaseListDataCount;
import org.cbioportal.domain.patient.repository.PatientRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("clickhouse")
public class ClickhousePatientRepository implements PatientRepository {

    private final ClickhousePatientMapper mapper;

    public ClickhousePatientRepository(ClickhousePatientMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * @param studyViewFilterContext
     * @return
     */
    @Override
    public int getFilteredPatientCount(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getPatientCount(studyViewFilterContext);
    }

    /**
     * @param studyViewFilterContext
     * @return
     */
    @Override
    public List<CaseListDataCount> getCaseListDataCounts(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getCaseListDataCounts(studyViewFilterContext);
    }
}
