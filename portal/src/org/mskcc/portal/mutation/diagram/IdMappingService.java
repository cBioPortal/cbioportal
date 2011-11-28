package org.mskcc.portal.mutation.diagram;

import java.util.List;

/**
 * Id mapping service.
 */
public interface IdMappingService {

    /**
     * Return a list of UniProt ids for the specified HUGO gene symbol, or an empty list if no such ids exist.
     *
     * @param hugoGeneSymbol HUGO gene symbol, must not be null
     * @return a list of UniProt ids for the specified HUGO gene symbol, or an empty list if no such ids exist
     */
    List<String> getUniProtIds(String hugoGeneSymbol);
}
