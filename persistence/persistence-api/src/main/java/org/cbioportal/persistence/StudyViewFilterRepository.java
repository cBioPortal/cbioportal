package org.cbioportal.persistence;

import org.cbioportal.model.SampleIdentifier;
import org.cbioportal.model.StudyViewGenePanel;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Set;

public interface StudyViewFilterRepository {
    
    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    Set<SampleIdentifier> getSampleIdentifiersForPanels(List<StudyViewGenePanel> genePanels);
}
