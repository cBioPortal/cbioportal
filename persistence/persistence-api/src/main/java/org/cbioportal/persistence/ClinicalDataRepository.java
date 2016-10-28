package org.cbioportal.persistence;

import org.cbioportal.model.PatientClinicalData;
import org.cbioportal.model.SampleClinicalData;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface ClinicalDataRepository {

    List<SampleClinicalData> getAllClinicalDataOfSampleInStudy(List<String> studyIds, List<String> sampleIds,
                                                               String attributeId, String projection, Integer pageSize,
                                                               Integer pageNumber, String sortBy, String direction);

    BaseMeta getMetaSampleClinicalData(List<String> studyIds, List<String> sampleIds, String attributeId);

    List<PatientClinicalData> getAllClinicalDataOfPatientInStudy(List<String> studyIds, List<String> patientIds,
                                                                 String attributeId, String projection,
                                                                 Integer pageSize, Integer pageNumber, String sortBy,
                                                                 String direction);

    BaseMeta getMetaPatientClinicalData(List<String> studyIds, List<String> patientIds, String attributeId);
}
