package org.cbioportal.infrastructure.repository.clickhouse.treatment;

import org.cbioportal.legacy.model.PatientTreatment;
import org.cbioportal.legacy.model.SampleTreatment;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.domain.treatment.repository.TreatmentRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("clickhouse")
public class ClickhouseTreatmentRepository implements TreatmentRepository {
    private final ClickhouseTreatmentMapper mapper;

    public ClickhouseTreatmentRepository(ClickhouseTreatmentMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * @param studyViewFilterContext
     * @return
     */
    @Override
    public List<PatientTreatment> getPatientTreatments(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getPatientTreatments(studyViewFilterContext);
    }

    /**
     * @param studyViewFilterContext
     * @return
     */
    @Override
    public int getTotalPatientTreatmentCount(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getPatientTreatmentCounts(studyViewFilterContext);
    }

    /**
     * @param studyViewFilterContext
     * @return
     */
    @Override
    public List<SampleTreatment> getSampleTreatments(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getSampleTreatmentCounts(studyViewFilterContext);
    }

    /**
     * @param studyViewFilterContext
     * @return
     */
    @Override
    public int getTotalSampleTreatmentCount(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getTotalSampleTreatmentCounts(studyViewFilterContext);
    }
}
