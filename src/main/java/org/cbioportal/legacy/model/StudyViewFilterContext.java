package org.cbioportal.legacy.model;

import org.cbioportal.legacy.web.parameter.CustomSampleIdentifier;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;

import java.util.List;

public record StudyViewFilterContext(
    StudyViewFilter studyViewFilter,
    List<CustomSampleIdentifier> customDataFilterSamples,
    List<String> involvedCancerStudies
) {

}
