package org.cbioportal.service;

import org.cbioportal.model.SampleIdentifier;
import org.cbioportal.model.StudyViewGenePanel;
import org.cbioportal.persistence.StudyViewFilterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class StudyViewFilterServiceImpl implements StudyViewFilterService{
    
    @Autowired
    StudyViewFilterRepository repository;
    
    @Override
    public Set<SampleIdentifier> getSampleIdentifiersForPanels(List<StudyViewGenePanel> genePanels) {
        return repository.getSampleIdentifiersForPanels(genePanels);
    }
}
