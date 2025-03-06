package org.cbioportal.infrastructure.repository.treatment;

import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.PatientTreatment;
import org.cbioportal.legacy.model.SampleTreatment;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class V2TreatmentRepository implements org.cbioportal.domain.treatment.repository.TreatmentRepository {
    private final V2TreatmentMapper mapper;

    public V2TreatmentRepository(V2TreatmentMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<PatientTreatment> getPatientTreatments(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getPatientTreatments(studyViewFilterContext);
    }

    @Override
    public int getTotalPatientTreatmentCount(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getPatientTreatmentCounts(studyViewFilterContext);
    }

    @Override
    public List<SampleTreatment> getSampleTreatments(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getSampleTreatmentCounts(studyViewFilterContext);
    }

    @Override
    public int getTotalSampleTreatmentCount(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getTotalSampleTreatmentCounts(studyViewFilterContext);
    }
}
