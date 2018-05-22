package org.cbioportal.persistence.mybatis;

import java.util.List;

import org.cbioportal.model.FractionGenomeAltered;
import org.cbioportal.persistence.FractionGenomeAlteredRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class FractionGenomeAlteredMyBatisRepository implements FractionGenomeAlteredRepository {

    @Autowired
    private FractionGenomeAlteredMapper fractionGenomeAlteredMapper;

	@Override
	public List<FractionGenomeAltered> getFractionGenomeAltered(String studyId, String sampleListId) {
        
        return fractionGenomeAlteredMapper.getFractionGenomeAlteredBySampleListId(studyId, sampleListId);
	}

	@Override
	public List<FractionGenomeAltered> fetchFractionGenomeAltered(String studyId, List<String> sampleIds) {
        
        return fractionGenomeAlteredMapper.getFractionGenomeAltered(studyId, sampleIds);
	}
}
