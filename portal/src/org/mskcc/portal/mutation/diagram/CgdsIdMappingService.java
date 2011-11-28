package org.mskcc.portal.mutation.diagram;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.List;

/**
 * Implementation of IdMappingService that reads from the CGDS data source.
 */
public final class CgdsIdMappingService implements IdMappingService {

    /** {@inheritDoc} */
    public List<String> getUniProtIds(final String hugoGeneSymbol) {
        checkNotNull(hugoGeneSymbol, "hugoGeneSymbol must not be null");
        return Collections.emptyList();
    }
}