package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalDataRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class ClinicalDataClickHouseRepository implements ClinicalDataRepository {

	@Override
	public List<ClinicalData> getAllClinicalDataOfSampleInStudy(String studyId, String sampleId, String attributeId,
			String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<ClinicalData>();
	}

	@Override
	public BaseMeta getMetaSampleClinicalData(String studyId, String sampleId, String attributeId) {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public List<ClinicalData> getAllClinicalDataOfPatientInStudy(String studyId, String patientId, String attributeId,
			String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<ClinicalData>();
	}

	@Override
	public BaseMeta getMetaPatientClinicalData(String studyId, String patientId, String attributeId) {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public List<ClinicalData> getAllClinicalDataInStudy(String studyId, String attributeId, String clinicalDataType,
			String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<ClinicalData>();
	}

	@Override
	public BaseMeta getMetaAllClinicalData(String studyId, String attributeId, String clinicalDataType) {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public List<ClinicalData> fetchAllClinicalDataInStudy(String studyId, List<String> ids, List<String> attributeIds,
			String clinicalDataType, String projection) {
		// TODO Auto-generated method stub
		return new ArrayList<ClinicalData>();
	}

	@Override
	public BaseMeta fetchMetaClinicalDataInStudy(String studyId, List<String> ids, List<String> attributeIds,
			String clinicalDataType) {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public List<ClinicalData> fetchClinicalData(List<String> studyIds, List<String> ids, List<String> attributeIds,
			String clinicalDataType, String projection) {
		// TODO Auto-generated method stub
		return new ArrayList<ClinicalData>();
	}

	@Override
	public List<ClinicalData> fetchSampleClinicalTable(List<String> studyIds, List<String> ids, Integer pageSize,
			Integer pageNumber, String searchTerm, String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<ClinicalData>();
	}

	@Override
	public Integer fetchSampleClinicalTableCount(List<String> studyIds, List<String> ids, String searchTerm,
			String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new Integer(0);
	}

	@Override
	public BaseMeta fetchMetaClinicalData(List<String> studyIds, List<String> ids, List<String> attributeIds,
			String clinicalDataType) {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public List<ClinicalDataCount> fetchClinicalDataCounts(List<String> studyIds, List<String> sampleIds,
			List<String> attributeIds, String clinicalDataType, String projection) {
		// TODO Auto-generated method stub
		return new ArrayList<ClinicalDataCount>();
	}

	@Override
	public List<ClinicalData> getPatientClinicalDataDetailedToSample(List<String> studyIds, List<String> patientIds,
			List<String> attributeIds) {
		// TODO Auto-generated method stub
		return new ArrayList<ClinicalData>();
	}

}
