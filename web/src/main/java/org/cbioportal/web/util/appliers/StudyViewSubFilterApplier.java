package org.cbioportal.web.util.appliers;

import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;

import java.util.List;

public interface StudyViewSubFilterApplier {
    List<SampleIdentifier> filter(List<SampleIdentifier> toFilter, StudyViewFilter filters);
    
   boolean shouldApplyFilter(StudyViewFilter studyViewFilter);
}
