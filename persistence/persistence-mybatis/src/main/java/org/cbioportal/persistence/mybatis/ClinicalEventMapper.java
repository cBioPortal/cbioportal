package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.ClinicalEvent;
import org.cbioportal.model.ClinicalEventData;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface ClinicalEventMapper {

    List<ClinicalEvent> getPatientClinicalEvent(String studyId, String patientId, String projection, Integer limit, 
                                                Integer offset, String sortBy, String direction);

    BaseMeta getMetaPatientClinicalEvent(String studyId, String patientId);

    List<ClinicalEventData> getDataOfClinicalEvents(List<Integer> clinicalEventIds);
}
