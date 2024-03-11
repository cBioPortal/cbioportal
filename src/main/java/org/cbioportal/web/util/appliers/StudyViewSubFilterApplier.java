package org.cbioportal.web.util.appliers;

import java.util.List;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;

public interface StudyViewSubFilterApplier {
  List<SampleIdentifier> filter(List<SampleIdentifier> toFilter, StudyViewFilter filters);

  boolean shouldApplyFilter(StudyViewFilter studyViewFilter);
}
