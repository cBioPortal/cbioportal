package org.cbioportal.service.impl;

import java.util.List;
import org.cbioportal.model.CosmicMutation;
import org.cbioportal.persistence.CosmicCountRepository;
import org.cbioportal.service.CosmicCountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CosmicCountServiceImpl implements CosmicCountService {
    @Autowired
    private CosmicCountRepository cosmicCountRepository;

    @Override
    public List<CosmicMutation> fetchCosmicCountsByKeywords(
        List<String> keywords
    ) {
        return cosmicCountRepository.fetchCosmicCountsByKeywords(keywords);
    }
}
