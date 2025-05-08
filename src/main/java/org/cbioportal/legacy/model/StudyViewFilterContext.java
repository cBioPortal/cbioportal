package org.cbioportal.legacy.model;

import java.util.List;
import org.cbioportal.legacy.web.parameter.CustomSampleIdentifier;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;

public record StudyViewFilterContext(
    StudyViewFilter studyViewFilter,
    List<CustomSampleIdentifier> customDataFilterSamples,
    List<String> involvedCancerStudies) {}
