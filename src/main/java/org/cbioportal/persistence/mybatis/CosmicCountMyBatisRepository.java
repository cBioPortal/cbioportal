package org.cbioportal.persistence.mybatis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.cbioportal.model.CosmicMutation;
import org.cbioportal.persistence.CosmicCountRepository;

@Repository
public class CosmicCountMyBatisRepository implements CosmicCountRepository {

    @Autowired
    private CosmicCountMapper cosmicCountMapper;

    public List<CosmicMutation> fetchCosmicCountsByKeywords(List<String> keywords) {
        
	    return cosmicCountMapper.getCosmicCountsByKeywords(keywords);
    }
}
