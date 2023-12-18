package org.cbioportal.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import org.cbioportal.model.CosmicMutation;
import org.cbioportal.service.CosmicCountService;
import org.cbioportal.persistence.CosmicCountRepository;

@Service
public class CosmicCountServiceImpl implements CosmicCountService {

    @Autowired
    private CosmicCountRepository cosmicCountRepository;

    @Override
    public List<CosmicMutation> fetchCosmicCountsByKeywords(List<String> keywords) {
        
	    return cosmicCountRepository.fetchCosmicCountsByKeywords(keywords);
    }
}
