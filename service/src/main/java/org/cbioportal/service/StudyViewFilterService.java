package org.cbioportal.service;

import org.cbioportal.model.SampleIdentifier;
import org.cbioportal.model.StudyViewGenePanel;

import java.util.List;
import java.util.Set;

public interface StudyViewFilterService {
    Set<SampleIdentifier> getSampleIdentifiersForPanels(List<StudyViewGenePanel> studyIds);
}
