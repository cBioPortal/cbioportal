package org.mskcc.portal.mutation.diagram;

import java.util.List;

/**
 * Mutation service.
 */
public interface MutationService {

    /**
     * Return an immutable list of zero or more mutations for the specified HUGO gene symbol.  The list will not be null.
     *
     * @param hugoGeneSymbol HUGO gene symbol, must not be null
     * @return an immutable list of zero or more mutations for the specified HUGO gene symbol
     */
    List<Mutation> getMutations(String hugoGeneSymbol);
}
