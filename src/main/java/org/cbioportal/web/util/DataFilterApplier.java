package org.cbioportal.web.util;

import java.util.List;
import org.cbioportal.web.parameter.DataFilter;
import org.cbioportal.web.parameter.SampleIdentifier;

public interface DataFilterApplier<T extends DataFilter> {

  List<SampleIdentifier> apply(
      List<SampleIdentifier> sampleIdentifiers, List<T> dataFilters, boolean negateFilters);
}
