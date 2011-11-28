package org.mskcc.portal.mutation.diagram;

import java.util.List;

/**
 * Domain service.
 */
public interface DomainService {

    /**
     * Return the length of the primary transcript associated with the specified UniProt id.
     *
     * @param uniProtId UniProt id, must not be null
     * @return the length of the primary transcript association with the specified UniProt id, or
     *    <code>-1</code> if no such transcript exists
     */
    //int getLength(String uniProtId);

    /**
     * Return an immutable list of zero or more protein domains for the specified UniProt id.  The list will not be null.
     *
     * @param uniProtId UniProt id, must not be null
     * @return an immutable list of zero or more protein domains for the specified UniProt id
     */
    List<Domain> getDomains(String uniProtId);
}
