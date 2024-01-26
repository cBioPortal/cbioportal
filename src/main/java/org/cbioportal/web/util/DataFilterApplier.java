package org.cbioportal.web.util;

import org.cbioportal.web.parameter.DataFilter;
import org.cbioportal.web.parameter.SampleIdentifier;

import java.util.List;

public interface DataFilterApplier<T extends DataFilter> {

    List<SampleIdentifier> apply(
        List<SampleIdentifier> sampleIdentifiers,
        List<T> dataFilters,
        boolean negateFilters
    );
}
