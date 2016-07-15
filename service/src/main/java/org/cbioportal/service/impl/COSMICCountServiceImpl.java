package org.cbioportal.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import org.cbioportal.model.COSMICCount;
import org.cbioportal.persistence.COSMICCountRepository;
import org.cbioportal.service.COSMICCountService;

@Service
public class COSMICCountServiceImpl implements COSMICCountService {

    @Autowired
    private COSMICCountRepository cosmicCountRepository;

    @Override
    public List<COSMICCount> getCOSMICCountsByKeywords(List<String> keywords) {
	    return cosmicCountRepository.getCOSMICCountsByKeywords(keywords);
    }
}
