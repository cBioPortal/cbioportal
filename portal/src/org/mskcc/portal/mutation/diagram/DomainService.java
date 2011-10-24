package org.mskcc.portal.mutation.diagram;

import java.util.List;

/**
 * Domain service.
 */
public interface DomainService {

    /**
     * Return an immutable list of zero or more protein domains for the specified UniProt id.  The list will not be null.
     *
     * @param uniProtId UniProt id, must not be null
     * @return an immutable list of zero or more protein domains for the specified UniProt id
     */
    List<Domain> getDomains(String uniProtId);
}
