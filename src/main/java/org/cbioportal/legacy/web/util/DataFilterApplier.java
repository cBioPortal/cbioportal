package org.cbioportal.legacy.web.util;

import java.util.List;
import org.cbioportal.legacy.web.parameter.DataFilter;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;

public interface DataFilterApplier<T extends DataFilter> {

  List<SampleIdentifier> apply(
      List<SampleIdentifier> sampleIdentifiers, List<T> dataFilters, boolean negateFilters);
}
