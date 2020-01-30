package org.cbioportal.persistence.mybatis;

import java.util.List;
import org.cbioportal.model.ClinicalEvent;
import org.cbioportal.model.ClinicalEventData;
import org.cbioportal.model.meta.BaseMeta;

public interface ClinicalEventMapper {
    List<ClinicalEvent> getPatientClinicalEvent(
        String studyId,
        String patientId,
        String projection,
        Integer limit,
        Integer offset,
        String sortBy,
        String direction
    );

    BaseMeta getMetaPatientClinicalEvent(String studyId, String patientId);

    List<ClinicalEventData> getDataOfClinicalEvents(
        List<Integer> clinicalEventIds
    );

    List<ClinicalEvent> getStudyClinicalEvent(
        String studyId,
        String projection,
        Integer limit,
        Integer offset,
        String sortBy,
        String direction
    );

    BaseMeta getMetaClinicalEvent(String studyId);
}
