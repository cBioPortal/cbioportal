package org.cbioportal.web.util.appliers;

import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;

import java.util.List;

public abstract class StudyViewSubFilterApplier {
    public abstract List<SampleIdentifier> filter(List<SampleIdentifier> toFilter, StudyViewFilter filters);
    
    public abstract boolean shouldApplyFilter(StudyViewFilter studyViewFilter);
}
