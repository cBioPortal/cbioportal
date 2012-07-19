package org.mskcc.cbio.portal.mapback;

import java.util.ArrayList;

/**
 * Maps Back from a global chromosomal location to a local nucleotide position, based
 * on a GenBank Record.
 *
 * @author Ethan Cerami.
 */
public class MapBack {
    private MapBackGene mapBackGene;
    private ArrayList<Mapping> mappingList;
    private long globalMutationLocation;
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
        for (int i =0; i< mappingList.size(); i++) {
            Mapping mapping = mappingList.get(i);
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