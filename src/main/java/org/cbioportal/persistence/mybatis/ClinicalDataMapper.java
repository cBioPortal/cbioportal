package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface ClinicalDataMapper {

    List<ClinicalData> getSampleClinicalData(List<String> studyIds, List<String> sampleIds, List<String> attributeIds,
                                             String projection, Integer limit, Integer offset, String sortBy,
                                             String direction);

    BaseMeta getMetaSampleClinicalData(List<String> studyIds, List<String> sampleIds, List<String> attributeIds);

    List<ClinicalData> getPatientClinicalData(List<String> studyIds, List<String> patientIds, List<String> attributeIds,
                                              String projection, Integer limit, Integer offset, String sortBy,
                                              String direction);

    List<ClinicalData> getSampleClinicalTable(List<String> studyIds, List<String> sampleIds, String projection,
                                              Integer limit, Integer offset, String searchTerm,
                                              String sortByAttrId, Boolean sortAttrIsNumber, Boolean sortIsPatientAttr,
                                              String direction);

    Integer getSampleClinicalTableCount(List<String> studyIds, List<String> sampleIds, String projection,
                                        String searchTerm, String sortBy, String direction);

    BaseMeta getMetaPatientClinicalData(List<String> studyIds, List<String> patientIds, List<String> attributeIds);

    List<ClinicalDataCount> fetchSampleClinicalDataCounts(List<String> studyIds, List<String> sampleIds,
                                                          List<String> attributeIds);

    List<ClinicalDataCount> fetchPatientClinicalDataCounts(List<String> studyIds, List<String> patientIds,
                                                           List<String> attributeIds, String projection);

    List<ClinicalData> getPatientClinicalDataDetailedToSample(List<String> studyIds, List<String> patientIds,
                                                              List<String> attributeIds, String projection, Integer limit,
                                                              Integer offset, String sortBy, String direction);

    List<Integer> getVisibleSampleInternalIdsForClinicalTable(List<String> studyIds, List<String> sampleIds,
                                                              String projection, Integer limit, Integer offset,
                                                              String searchTerm, String sortAttrId, Boolean sortAttrIsNumber,
                                                              Boolean sortIsPatientAttr, String direction);

    List<ClinicalData> getSampleClinicalDataBySampleInternalIds(List<Integer> sampleInternalIds);

    List<ClinicalData> getPatientClinicalDataBySampleInternalIds(List<Integer> sampleInternalIds);
}
