package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.SampleIdentifier;
import org.cbioportal.model.StudyViewGenePanel;
import org.cbioportal.persistence.StudyViewFilterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public class StudyViewFilterMybatisRepository implements StudyViewFilterRepository {
    
    @Autowired
    StudyViewFilterMapper mapper;
    
    @Override
    public Set<SampleIdentifier> getSampleIdentifiersForPanels(List<StudyViewGenePanel> genePanels) {
        return mapper.getSampleIdentifiersForPanels(genePanels);
    }
}
