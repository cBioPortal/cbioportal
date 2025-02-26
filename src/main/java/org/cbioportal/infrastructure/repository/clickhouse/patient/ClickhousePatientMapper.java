package org.cbioportal.infrastructure.repository.clickhouse.patient;

import org.apache.ibatis.annotations.Param;
import org.cbioportal.legacy.model.CaseListDataCount;
import org.cbioportal.domain.studyview.StudyViewFilterContext;

import java.util.List;

/**
 * Mapper interface for retrieving patient data from ClickHouse.
 * This interface provides methods for fetching patient counts and case list data counts.
 */
public interface ClickhousePatientMapper {

    /**
     * Retrieves the patient count based on the study view filter context.
     *
     * @param studyViewFilterContext the context of the study view filter
     * @return the patient count
     */
    int getPatientCount(@Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);

    /**
     * Retrieves case list data counts based on the study view filter context.
     *
     * @param studyViewFilterContext the context of the study view filter
     * @return a list of case list data counts
     */
    List<CaseListDataCount> getCaseListDataCounts(@Param("studyViewFilterContext") StudyViewFilterContext studyViewFilterContext);
}

