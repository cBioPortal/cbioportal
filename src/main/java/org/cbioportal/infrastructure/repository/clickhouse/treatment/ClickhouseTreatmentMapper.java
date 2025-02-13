package org.cbioportal.infrastructure.repository.clickhouse.treatment;

import org.apache.ibatis.annotations.Param;
import org.cbioportal.legacy.model.PatientTreatment;
import org.cbioportal.legacy.model.SampleTreatment;
import org.cbioportal.studyview.StudyViewFilterContext;

import java.util.List;

public interface ClickhouseTreatmentMapper {
    List<PatientTreatment> getPatientTreatments(@Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);
    int getPatientTreatmentCounts(@Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);
    List<SampleTreatment> getSampleTreatmentCounts(@Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);
    int getTotalSampleTreatmentCounts(@Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);
}
