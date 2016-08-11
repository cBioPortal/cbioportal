package org.cbioportal.persistence.mybatis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.cbioportal.model.CosmicCount;
import org.cbioportal.persistence.CosmicCountRepository;

@Repository
public class CosmicCountMyBatisRepository implements CosmicCountRepository {

    @Autowired
    CosmicCountMapper cosmicCountMapper;

    public List<CosmicCount> getCOSMICCountsByKeywords(List<String> keywords) {
	    return cosmicCountMapper.getCOSMICCountsByKeywords(keywords);
    }
}
