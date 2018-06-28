package org.cbioportal.service.impl;

import org.cbioportal.model.FractionGenomeAltered;
import org.cbioportal.persistence.FractionGenomeAlteredRepository;
import org.cbioportal.service.FractionGenomeAlteredService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FractionGenomeAlteredServiceImpl implements FractionGenomeAlteredService {

    @Autowired
    private FractionGenomeAlteredRepository fractionGenomeAlteredRepository;

    @Override
    public List<FractionGenomeAltered> getFractionGenomeAltered(String studyId, String sampleListId) {

        return fractionGenomeAlteredRepository.getFractionGenomeAltered(studyId, sampleListId);
    }

    @Override
    public List<FractionGenomeAltered> fetchFractionGenomeAltered(String studyId, List<String> sampleIds) {
        
        return fractionGenomeAlteredRepository.fetchFractionGenomeAltered(studyId, sampleIds);
    }
}
