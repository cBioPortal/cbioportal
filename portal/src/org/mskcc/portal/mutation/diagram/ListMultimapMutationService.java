package org.mskcc.portal.mutation.diagram;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

import com.google.inject.Inject;

/**
 * Implementation of MutationService backed by a ListMultimap.
 */
public final class ListMultimapMutationService implements MutationService {
    private static final long serialVersionUID = 1L;
    private final ListMultimap<String, Mutation> mutations;

    /**
     * Create a new list multimap mutation service with the specified mutations.  The
     * mutations will be copied defensively into this class.
     *
     * @param mutations mutations, must not be null
     */
    @Inject
    public ListMultimapMutationService(final ListMultimap<String, Mutation> mutations) {
        this.mutations = ImmutableListMultimap.copyOf(mutations);
    }

    /** {@inheritDoc} */
    public List<Mutation> getMutations(final String hugoGeneSymbol) {
        checkNotNull(hugoGeneSymbol);
        return mutations.get(hugoGeneSymbol);
    }
}
