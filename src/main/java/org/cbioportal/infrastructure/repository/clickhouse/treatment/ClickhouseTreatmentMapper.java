package org.cbioportal.infrastructure.repository.clickhouse.treatment;

import org.apache.ibatis.annotations.Param;
import org.cbioportal.legacy.model.PatientTreatment;
import org.cbioportal.legacy.model.SampleTreatment;
import org.cbioportal.domain.studyview.StudyViewFilterContext;

import java.util.List;

/**
 * Mapper interface for retrieving treatment-related data from ClickHouse.
 * This interface provides methods to fetch patient treatments, sample treatment counts, and more.
 */
public interface ClickhouseTreatmentMapper {

    /**
     * Retrieves patient treatments based on the study view filter context.
     *
     * @param studyViewFilterContext the context of the study view filter
     * @return a list of patient treatments
     */
    List<PatientTreatment> getPatientTreatments(@Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);

    /**
     * Retrieves the patient treatment counts based on the study view filter context.
     *
     * @param studyViewFilterContext the context of the study view filter
     * @return the patient treatment count
     */
    int getPatientTreatmentCounts(@Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);

    /**
     * Retrieves sample treatment counts based on the study view filter context.
     *
     * @param studyViewFilterContext the context of the study view filter
     * @return a list of sample treatment counts
     */
    List<SampleTreatment> getSampleTreatmentCounts(@Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);

    /**
     * Retrieves the total sample treatment counts based on the study view filter context.
     *
     * @param studyViewFilterContext the context of the study view filter
     * @return the total sample treatment count
     */
    int getTotalSampleTreatmentCounts(@Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);
}

