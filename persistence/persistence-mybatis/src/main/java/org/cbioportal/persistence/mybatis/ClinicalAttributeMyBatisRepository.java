package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalAttributeRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class ClinicalAttributeMyBatisRepository implements ClinicalAttributeRepository {

    @Autowired
    private ClinicalAttributeMapper clinicalAttributeMapper;
    @Autowired
    private OffsetCalculator offsetCalculator;

    @Override
    public List<ClinicalAttribute> getAllClinicalAttributes(String projection, Integer pageSize, Integer pageNumber,
                                                            String sortBy, String direction) {

        return clinicalAttributeMapper.getClinicalAttributes(null, projection, pageSize,
                offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
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
                offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
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
    public List<ClinicalAttribute> getAllClinicalAttributesInStudiesBySampleIds(List<String> studyIds,
            List<String> sampleIds, String projection, String sortBy, String direction) {

        return clinicalAttributeMapper.getAllClinicalAttributesInStudiesBySampleIds(studyIds, sampleIds, projection,
                sortBy, direction);

    }

    @Override
    public List<ClinicalAttribute> getAllClinicalAttributesInStudiesBySampleListId(String sampleListId,
            String projection, String sortBy, String direction) {

        return clinicalAttributeMapper.getAllClinicalAttributesInStudiesBySampleListId(sampleListId, projection, sortBy,
                direction);

    }
}
