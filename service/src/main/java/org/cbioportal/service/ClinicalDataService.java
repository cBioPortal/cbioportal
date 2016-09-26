package org.cbioportal.service;

import org.cbioportal.model.PatientClinicalData;
import org.cbioportal.model.SampleClinicalData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.model.summary.ClinicalDataSummary;

import java.util.List;

public interface ClinicalDataService {

    List<SampleClinicalData> getAllClinicalDataOfSampleInStudy(String studyId, String sampleId, String attributeId,
                                                               String projection, Integer pageSize, Integer pageNumber,
                                                               String sortBy, String direction);

    BaseMeta getMetaSampleClinicalData(String studyId, String sampleId, String attributeId);

    List<PatientClinicalData> getAllClinicalDataOfPatientInStudy(String studyId, String patientId, String attributeId,
                                                                 String projection, Integer pageSize,
                                                                 Integer pageNumber, String sortBy, String direction);

    BaseMeta getMetaPatientClinicalData(String studyId, String patientId, String attributeId);

    List<? extends ClinicalDataSummary> getAllClinicalDataInStudy(String studyId, String attributeId,
                                                                  String clinicalDataType, String projection,
                                                                  Integer pageSize, Integer pageNumber, String sortBy,
                                                                  String direction);

    BaseMeta getMetaAllClinicalData(String studyId, String attributeId, String clinicalDataType);

    List<? extends ClinicalDataSummary> fetchClinicalData(List<String> studyIds, List<String> ids, String attributeId,
                                                          String clinicalDataType, String projection);

    BaseMeta fetchMetaClinicalData(List<String> studyIds, List<String> ids, String attributeId,
                                   String clinicalDataType);
}
