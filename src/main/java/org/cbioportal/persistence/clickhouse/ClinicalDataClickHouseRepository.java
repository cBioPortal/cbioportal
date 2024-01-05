package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalDataRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.clickhouse.mapper.ClinicalDataMapper;
import org.cbioportal.persistence.clickhouse.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class ClinicalDataClickHouseRepository implements ClinicalDataRepository {
	
	@Autowired
	private ClinicalDataMapper clinicalDataMapper;

	@Override
	public List<ClinicalData> getAllClinicalDataOfSampleInStudy(String studyId, String sampleId, String attributeId,
			String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
        return clinicalDataMapper.getSampleClinicalData(Arrays.asList(studyId), Arrays.asList(sampleId),
                attributeId != null ? Arrays.asList(attributeId) : null, projection, pageSize, 
                OffsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
	}

	@Override
	public BaseMeta getMetaSampleClinicalData(String studyId, String sampleId, String attributeId) {
        return clinicalDataMapper.getMetaSampleClinicalData(Arrays.asList(studyId), Arrays.asList(sampleId),
                attributeId != null ? Arrays.asList(attributeId) : null);

	}

	@Override
	public List<ClinicalData> getAllClinicalDataOfPatientInStudy(String studyId, String patientId, String attributeId,
			String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
        return clinicalDataMapper.getPatientClinicalData(Arrays.asList(studyId), Arrays.asList(patientId),
                attributeId != null ? Arrays.asList(attributeId) : null, projection, pageSize, 
                OffsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
	}

	@Override
	public BaseMeta getMetaPatientClinicalData(String studyId, String patientId, String attributeId) {
        return clinicalDataMapper.getMetaPatientClinicalData(Arrays.asList(studyId), Arrays.asList(patientId),
                attributeId != null ? Arrays.asList(attributeId) : null);
	}

	@Override
	public List<ClinicalData> getAllClinicalDataInStudy(String studyId, String attributeId, String clinicalDataType,
			String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
		
        if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
            return clinicalDataMapper.getSampleClinicalData(Arrays.asList(studyId), null, 
                attributeId != null ? Arrays.asList(attributeId) : null, projection, pageSize, 
                OffsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
        } else {
            return clinicalDataMapper.getPatientClinicalData(Arrays.asList(studyId), null, 
                attributeId != null ? Arrays.asList(attributeId) : null, projection, pageSize, 
                OffsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
        }
        
	}

	@Override
	public BaseMeta getMetaAllClinicalData(String studyId, String attributeId, String clinicalDataType) {
        BaseMeta baseMeta = new BaseMeta();

        if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
            baseMeta.setTotalCount(clinicalDataMapper.getMetaSampleClinicalData(Arrays.asList(studyId), null,
                attributeId != null ? Arrays.asList(attributeId) : null).getTotalCount());
        } else {
            baseMeta.setTotalCount(clinicalDataMapper.getMetaPatientClinicalData(Arrays.asList(studyId), null,
                attributeId != null ? Arrays.asList(attributeId) : null).getTotalCount());
        }

        return baseMeta;
	}

	@Override
	public List<ClinicalData> fetchAllClinicalDataInStudy(String studyId, List<String> ids, List<String> attributeIds,
			String clinicalDataType, String projection) {
        if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
            return clinicalDataMapper.getSampleClinicalData(Arrays.asList(studyId), ids, attributeIds,
                projection, 0, 0, null, null);
        } else {
            return clinicalDataMapper.getPatientClinicalData(Arrays.asList(studyId), ids, attributeIds,
                projection, 0, 0, null, null);
        }
	}

	@Override
	public BaseMeta fetchMetaClinicalDataInStudy(String studyId, List<String> ids, List<String> attributeIds,
			String clinicalDataType) {
        BaseMeta baseMeta = new BaseMeta();

        if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
            baseMeta.setTotalCount(clinicalDataMapper.getMetaSampleClinicalData(Arrays.asList(studyId), ids,
                attributeIds).getTotalCount());
        } else {
            baseMeta.setTotalCount(clinicalDataMapper.getMetaPatientClinicalData(Arrays.asList(studyId), ids,
                attributeIds).getTotalCount());
        }

        return baseMeta;
	}

	@Override
	public List<ClinicalData> fetchClinicalData(List<String> studyIds, List<String> ids, List<String> attributeIds,
			String clinicalDataType, String projection) {
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
            return clinicalDataMapper.getSampleClinicalData(studyIds, ids, attributeIds, projection, 0, 0, null, null);
        } else {
            return clinicalDataMapper.getPatientClinicalData(studyIds, ids, attributeIds, projection, 0, 0, null, null);
        }
	}

	@Override
	public List<ClinicalData> fetchSampleClinicalTable(List<String> studyIds, List<String> ids, Integer pageSize,
			Integer pageNumber, String searchTerm, String sortBy, String direction) {
		if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        return clinicalDataMapper.getSampleClinicalTable(studyIds, ids,"SUMMARY", pageSize,
        		OffsetCalculator.calculate(pageSize, pageNumber), searchTerm, sortBy, direction);
	}

	@Override
	public Integer fetchSampleClinicalTableCount(List<String> studyIds, List<String> sampleIds, String searchTerm,
			String sortBy, String direction) {
        return clinicalDataMapper.getSampleClinicalTableCount(studyIds, sampleIds,"SUMMARY", 
                searchTerm, sortBy, direction);

	}

	@Override
	public BaseMeta fetchMetaClinicalData(List<String> studyIds, List<String> ids, List<String> attributeIds,
			String clinicalDataType) {
        BaseMeta baseMeta = new BaseMeta();

        if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
            baseMeta.setTotalCount(clinicalDataMapper.getMetaSampleClinicalData(studyIds, ids, attributeIds)
                .getTotalCount());
        } else {
            baseMeta.setTotalCount(clinicalDataMapper.getMetaPatientClinicalData(studyIds, ids, attributeIds)
                .getTotalCount());
        }

        return baseMeta;
	}

	@Override
	public List<ClinicalDataCount> fetchClinicalDataCounts(List<String> studyIds, List<String> sampleIds,
			List<String> attributeIds, String clinicalDataType, String projection) {
        if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
            return clinicalDataMapper.fetchSampleClinicalDataCounts(studyIds, sampleIds, attributeIds);
        } else {
            return clinicalDataMapper.fetchPatientClinicalDataCounts(studyIds, 
                sampleIds, attributeIds, projection);
        }
	}

	@Override
	public List<ClinicalData> getPatientClinicalDataDetailedToSample(List<String> studyIds, List<String> patientIds,
			List<String> attributeIds) {
		return clinicalDataMapper.getPatientClinicalDataDetailedToSample(studyIds, patientIds, attributeIds, "SUMMARY",
                0, 0, null, null);
	}

}
