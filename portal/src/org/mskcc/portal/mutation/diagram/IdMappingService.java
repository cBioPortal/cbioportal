package org.mskcc.portal.mutation.diagram;

import java.io.Serializable;

/**
 * Id mapping service.
 */
public interface IdMappingService extends Serializable {

    /**
     * Return the UniProt id for the specified HUGO gene symbol, or <code>null</code> if no such id exists
     *
     * @param hugoGeneSymbol HUGO gene symbol, must not be null
     * @return the UniProt id for the specified HUGO gene symbol, or <code>null</code> if no such id exists
     */
    String getUniProtId(String hugoGeneSymbol);
}
