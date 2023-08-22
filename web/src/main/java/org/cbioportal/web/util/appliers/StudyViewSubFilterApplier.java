package org.cbioportal.web.util.appliers;

import org.cbioportal.webparam.SampleIdentifier;
import org.cbioportal.webparam.StudyViewFilter;

import java.util.List;

public interface StudyViewSubFilterApplier {
    List<SampleIdentifier> filter(List<SampleIdentifier> toFilter, StudyViewFilter filters);
    
   boolean shouldApplyFilter(StudyViewFilter studyViewFilter);
}
