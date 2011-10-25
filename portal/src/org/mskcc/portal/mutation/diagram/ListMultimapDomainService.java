package org.mskcc.portal.mutation.diagram;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Implementation of DomainService backed by a ListMultimap.
 */
public final class ListMultimapDomainService implements DomainService {
    private static final long serialVersionUID = 1L;
    private final ListMultimap<String, Domain> domains;

    /**
     * Create a new list multimap domain service with the specified domains.  The
     * domains will be copied defensively into this class.
     *
     * @param domains domains, must not be null
     */
    public ListMultimapDomainService(final ListMultimap<String, Domain> domains) {
        this.domains = ImmutableListMultimap.copyOf(domains);
    }

    /** {@inheritDoc} */
    public List<Domain> getDomains(final String uniProtId) {
        checkNotNull(uniProtId, "uniProdId must not be null");
        return domains.get(uniProtId);
    }
}
