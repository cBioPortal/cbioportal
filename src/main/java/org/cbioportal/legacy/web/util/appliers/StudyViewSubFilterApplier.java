package org.cbioportal.legacy.web.util.appliers;

import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;

import java.util.List;

public interface StudyViewSubFilterApplier {
    List<SampleIdentifier> filter(List<SampleIdentifier> toFilter, StudyViewFilter filters);
    
   boolean shouldApplyFilter(StudyViewFilter studyViewFilter);
}
