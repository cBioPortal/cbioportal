package org.mskcc.portal.mutation.diagram;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of IdMappingService that reads from the CGDS data source.
 */
public final class CgdsIdMappingService implements IdMappingService {
    private static final long serialVersionUID = 1L;

    /** {@inheritDoc} */
    public String getUniProtId(final String hugoGeneSymbol) {
        checkNotNull(hugoGeneSymbol, "hugoGeneSymbol must not be null");
        return null;
    }
}