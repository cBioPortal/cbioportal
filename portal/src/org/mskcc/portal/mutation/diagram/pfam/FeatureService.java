package org.mskcc.portal.mutation.diagram.pfam;

import java.util.List;

/**
 * Feature (or sequence?) service.
 */
public interface FeatureService {

    /**
     * Return an immutable list of zero or more sequences for the specified UniProt id, populated
     * with sequence features such as regions, motifs, and markups.
     *
     * @param uniProtId UniProt id, must not be null
     * @return an immutable list of zero or more sequences for the specified UniProt id
     */
    List<Sequence> getFeatures(String uniProtId);
}
