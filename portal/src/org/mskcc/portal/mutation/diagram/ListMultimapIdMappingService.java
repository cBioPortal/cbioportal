package org.mskcc.portal.mutation.diagram;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.inject.Inject;

/**
 * Implementation of IdMappingService backed by a list multimap of UniProt ids keyed by HUGO gene symbols.
 */
public final class ListMultimapIdMappingService implements IdMappingService {
    private final ListMultimap<String, String> uniProtIds;

    /**
     * Create a new multimap id mapping service with the specified list multimap of UniProt
     * ids keyed by HUGO gene symbols.  The list multimap of UniProt ids keyed by HUGO gene
     * symbols will be copied defensively into this class.
     *
     * @param uniProtIds list multimap of UniProt ids keyed by HUGO gene symbol, must not be null
     */
    @Inject
    public ListMultimapIdMappingService(final ListMultimap<String, String> uniProtIds) {
        this.uniProtIds = ImmutableListMultimap.copyOf(uniProtIds);
    }

    /** {@inheritDoc} */
    public List<String> getUniProtIds(final String hugoGeneSymbol) {
        checkNotNull(hugoGeneSymbol, "hugoGeneSymbol must not be null");
        return uniProtIds.get(hugoGeneSymbol);
    }
}