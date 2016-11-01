package org.cbioportal.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import org.cbioportal.model.CosmicCount;
import org.cbioportal.service.CosmicCountService;
import org.cbioportal.persistence.CosmicCountRepository;

@Service
public class CosmicCountServiceImpl implements CosmicCountService {

    @Autowired
    private CosmicCountRepository cosmicCountRepository;

    @Override
    public List<CosmicCount> getCOSMICCountsByKeywords(List<String> keywords) {
	    return cosmicCountRepository.getCOSMICCountsByKeywords(keywords);
    }
}
