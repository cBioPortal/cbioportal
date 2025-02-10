package org.cbioportal.legacy.web.util;

import org.cbioportal.legacy.web.parameter.DataFilter;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;

import java.util.List;

public interface DataFilterApplier<T extends DataFilter> {

    List<SampleIdentifier> apply(
        List<SampleIdentifier> sampleIdentifiers,
        List<T> dataFilters,
        boolean negateFilters
    );
}
