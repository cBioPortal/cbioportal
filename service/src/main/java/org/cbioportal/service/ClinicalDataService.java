package org.cbioportal.service;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface ClinicalDataService {

    List<ClinicalData> getAllClinicalDataOfSampleInStudy(String studyId, String sampleId, String attributeId,
                                                         String projection, Integer pageSize, Integer pageNumber,
                                                         String sortBy, String direction);

    BaseMeta getMetaSampleClinicalData(String studyId, String sampleId, String attributeId);

    List<ClinicalData> getAllClinicalDataOfPatientInStudy(String studyId, String patientId, String attributeId,
                                                                 String projection, Integer pageSize,
                                                                 Integer pageNumber, String sortBy, String direction);

    BaseMeta getMetaPatientClinicalData(String studyId, String patientId, String attributeId);

    List<ClinicalData> getAllClinicalDataInStudy(String studyId, String attributeId,
                                                                  String clinicalDataType, String projection,
                                                                  Integer pageSize, Integer pageNumber, String sortBy,
                                                                  String direction);

    BaseMeta getMetaAllClinicalData(String studyId, String attributeId, String clinicalDataType);

    List<ClinicalData> fetchClinicalData(List<String> studyIds, List<String> ids, String attributeId,
                                                          String clinicalDataType, String projection);

    BaseMeta fetchMetaClinicalData(List<String> studyIds, List<String> ids, String attributeId,
                                   String clinicalDataType);
}
