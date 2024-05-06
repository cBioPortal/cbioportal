package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalAttributeCount;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalAttributeRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.mybatis.util.PaginationCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class ClinicalAttributeMyBatisRepository implements ClinicalAttributeRepository {

    @Autowired
    private ClinicalAttributeMapper clinicalAttributeMapper;
    @Autowired
    private PaginationCalculator paginationCalculator;

    @Override
    public List<ClinicalAttribute> getAllClinicalAttributes(String projection, Integer pageSize, Integer pageNumber,
                                                            String sortBy, String direction) {

        return clinicalAttributeMapper.getClinicalAttributes(null, projection, pageSize,
                paginationCalculator.offset(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaClinicalAttributes() {

        return clinicalAttributeMapper.getMetaClinicalAttributes(null);
    }

    @Override
    public ClinicalAttribute getClinicalAttribute(String studyId, String clinicalAttributeId) {

        return clinicalAttributeMapper.getClinicalAttribute(studyId, clinicalAttributeId,
                PersistenceConstants.DETAILED_PROJECTION);
    }

    @Override
    public List<ClinicalAttribute> getAllClinicalAttributesInStudy(String studyId, String projection, Integer pageSize,
                                                                   Integer pageNumber, String sortBy,
                                                                   String direction) {

        return clinicalAttributeMapper.getClinicalAttributes(Arrays.asList(studyId), projection, pageSize,
                paginationCalculator.offset(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaClinicalAttributesInStudy(String studyId) {

        return clinicalAttributeMapper.getMetaClinicalAttributes(Arrays.asList(studyId));
    }

	@Override
	public List<ClinicalAttribute> fetchClinicalAttributes(List<String> studyIds, String projection) {
        
        return clinicalAttributeMapper.getClinicalAttributes(studyIds, projection, 0, 0, null, null);
	}

	@Override
	public BaseMeta fetchMetaClinicalAttributes(List<String> studyIds) {
        
        return clinicalAttributeMapper.getMetaClinicalAttributes(studyIds);
	}

    @Override
    public List<ClinicalAttributeCount> getClinicalAttributeCountsBySampleIds(List<String> studyIds, List<String> sampleIds) {

        return clinicalAttributeMapper.getClinicalAttributeCountsBySampleIds(studyIds, sampleIds);
    }

    @Override
    public List<ClinicalAttributeCount> getClinicalAttributeCountsBySampleListId(String sampleListId) {

        return clinicalAttributeMapper.getClinicalAttributeCountsBySampleListId(sampleListId);
    }

    @Override
    public List<ClinicalAttribute> getClinicalAttributesByStudyIdsAndAttributeIds(List<String> studyIds,
            List<String> attributeIds) {
        return clinicalAttributeMapper.getClinicalAttributesByStudyIdsAndAttributeIds(studyIds, attributeIds);
    }
}
