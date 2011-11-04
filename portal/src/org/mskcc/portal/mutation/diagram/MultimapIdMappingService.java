package org.mskcc.portal.mutation.diagram;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import com.google.inject.Inject;

/**
 * Implementation of IdMappingService backed by a multimap of UniProt ids keyed by HUGO gene symbols.
 */
public final class MultimapIdMappingService implements IdMappingService {
    private final Multimap<String, String> uniProtIds;

    /**
     * Create a new multimap id mapping service with the specified multimap of UniProt
     * ids keyed by HUGO gene symbols.  The multimap of UniProt ids keyed by HUGO gene
     * symbols will be copied defensively into this class.
     *
     * @param uniProtIds multimap of UniProt ids keyed by HUGO gene symbol, must not be null
     */
    @Inject
    public MultimapIdMappingService(final Multimap<String, String> uniProtIds) {
        this.uniProtIds = ImmutableMultimap.copyOf(uniProtIds);
    }

    /** {@inheritDoc} */
    public String getUniProtId(final String hugoGeneSymbol) {
        checkNotNull(hugoGeneSymbol, "hugoGeneSymbol must not be null");
        Collection<String> match = uniProtIds.get(hugoGeneSymbol);
        return match.isEmpty() ? null : match.iterator().next();
    }
}