/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.mapback;

import java.util.ArrayList;

/**
 * Maps Back from a global chromosomal location to a local nucleotide position, based
 * on a GenBank Record.
 *
 * @author Ethan Cerami.
 */
public class MapBack {
    private final MapBackGene mapBackGene;
    private final ArrayList<Mapping> mappingList;
    private final long globalMutationLocation;
    private long ntPosition = -1;

    /**
     * Constructor.
     *
     * @param mapBackGene       MapBackGene Object.
     * @param globalLocation    Global chromosomal location.
     */
    public MapBack(MapBackGene mapBackGene, long globalLocation) {
        this.mapBackGene = mapBackGene;
        this.mappingList = mapBackGene.getMappingList();
        this.globalMutationLocation = globalLocation;
        mapBack();
    }

    private void mapBack() {
        Mapping lastMapping = mappingList.get(mappingList.size()-1);
        long sequenceLen = lastMapping.getQStart() + lastMapping.getBlockSize();

        Mapping matchedMapping = null;
        for (Mapping mapping : mappingList) {
            if (globalMutationLocation > mapping.getTStart()
                    && globalMutationLocation < mapping.getTStop()) {
                matchedMapping = mapping;
            }
        }

        if (matchedMapping != null) {
            long offset = globalMutationLocation - matchedMapping.getTStart();
            long plusQuery = matchedMapping.getQStart() + offset;
            ntPosition = plusQuery;
            if (!mapBackGene.isForwardStrand()) {
                ntPosition = sequenceLen - plusQuery +1;
            }
        }
    }

    public long getNtPositionWhereMutationOccurs() {
        return ntPosition;
    }

    public char getBpWhereMutationOccurs() {
        return mapBackGene.getFullDna().charAt((int) ntPosition-1);
    }
}