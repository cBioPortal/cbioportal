package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.SampleIdentifier;
import org.cbioportal.model.StudyViewGenePanel;

import java.util.List;
import java.util.Set;

public interface StudyViewFilterMapper {
    Set<SampleIdentifier> getSampleIdentifiersForPanels(List<StudyViewGenePanel> genePanels);
}
