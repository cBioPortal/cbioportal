package org.cbioportal.model;

import org.cbioportal.web.parameter.CustomSampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;

import java.util.List;

public record StudyViewFilterContext(
    StudyViewFilter studyViewFilter,
    List<CustomSampleIdentifier> customDataFilterSamples,
    List<String> involvedCancerStudies
) {

}
