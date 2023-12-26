package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalAttributeCount;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalAttributeRepository;
import org.cbioportal.persistence.clickhouse.mapper.ClinicalAttributeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class ClinicalAttributeClickHouseRepository implements ClinicalAttributeRepository {
	
	@Autowired
	private ClinicalAttributeMapper clinicalAttributeMapper;

	@Override
	public List<ClinicalAttribute> getAllClinicalAttributes(String projection, Integer pageSize, Integer pageNumber,
			String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<ClinicalAttribute>();
	}

	@Override
	public BaseMeta getMetaClinicalAttributes() {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public ClinicalAttribute getClinicalAttribute(String studyId, String clinicalAttributeId) {
		// TODO Auto-generated method stub
		return new ClinicalAttribute();
	}

	@Override
	public List<ClinicalAttribute> getAllClinicalAttributesInStudy(String studyId, String projection, Integer pageSize,
			Integer pageNumber, String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<ClinicalAttribute>();
	}

	@Override
	public BaseMeta getMetaClinicalAttributesInStudy(String studyId) {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public List<ClinicalAttribute> fetchClinicalAttributes(List<String> studyIds, String projection) {
		// TODO Auto-generated method stub
		
		
		return clinicalAttributeMapper.getClinicalAttributes(studyIds, projection, 0, 0, null, null);
	}

	@Override
	public BaseMeta fetchMetaClinicalAttributes(List<String> studyIds) {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public List<ClinicalAttributeCount> getClinicalAttributeCountsBySampleIds(List<String> studyIds,
			List<String> sampleIds) {
		// TODO Auto-generated method stub
		return new ArrayList<ClinicalAttributeCount>();
	}

	@Override
	public List<ClinicalAttributeCount> getClinicalAttributeCountsBySampleListId(String sampleListId) {
		// TODO Auto-generated method stub
		return new ArrayList<ClinicalAttributeCount>();
	}

	@Override
	public List<ClinicalAttribute> getClinicalAttributesByStudyIdsAndAttributeIds(List<String> studyIds,
			List<String> attributeIds) {
		// TODO Auto-generated method stub
		return clinicalAttributeMapper.getClinicalAttributesByStudyIdsAndAttributeIds(studyIds, attributeIds);
		
	}

}
