package org.cbioportal.persistence.mybatis;

import java.util.List;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.meta.BaseMeta;

public interface ClinicalDataMapper {
    List<ClinicalData> getSampleClinicalData(
        List<String> studyIds,
        List<String> sampleIds,
        List<String> attributeIds,
        String projection,
        Integer limit,
        Integer offset,
        String sortBy,
        String direction
    );

    BaseMeta getMetaSampleClinicalData(
        List<String> studyIds,
        List<String> sampleIds,
        List<String> attributeIds
    );

    List<ClinicalData> getPatientClinicalData(
        List<String> studyIds,
        List<String> patientIds,
        List<String> attributeIds,
        String projection,
        Integer limit,
        Integer offset,
        String sortBy,
        String direction
    );

    BaseMeta getMetaPatientClinicalData(
        List<String> studyIds,
        List<String> patientIds,
        List<String> attributeIds
    );

    List<ClinicalDataCount> fetchSampleClinicalDataCounts(
        List<String> studyIds,
        List<String> sampleIds,
        List<String> attributeIds
    );

    List<ClinicalDataCount> fetchPatientClinicalDataCounts(
        List<String> studyIds,
        List<String> patientIds,
        List<String> attributeIds,
        String projection
    );

    List<ClinicalData> getPatientClinicalDataDetailedToSample(
        List<String> studyIds,
        List<String> patientIds,
        List<String> attributeIds,
        String projection,
        Integer limit,
        Integer offset,
        String sortBy,
        String direction
    );
}
