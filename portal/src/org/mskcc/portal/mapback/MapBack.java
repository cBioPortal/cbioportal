package org.mskcc.portal.mapback;

import java.util.ArrayList;

public class MapBack {
    private Gene gene;
    private ArrayList<Mapping> mappingList;
    private long globalMutationLocation;
    private long ntPosition = -1;

    public MapBack(Gene gene, long globalMutationLocation) {
        this.gene = gene;
        this.mappingList = gene.getMappingList();
        this.globalMutationLocation = globalMutationLocation;
        mapBack();
    }

    private void mapBack() {
        Mapping lastMapping = mappingList.get(mappingList.size()-1);
        long sequenceLen = lastMapping.getQStart() + lastMapping.getBlockSize();

        Mapping matchedMapping = null;
        for (int i =0; i< mappingList.size(); i++) {
            Mapping mapping = mappingList.get(i);
            if (globalMutationLocation > mapping.getTStart() && globalMutationLocation < mapping.getTStop()) {
                matchedMapping = mapping;
            }
        }

        if (matchedMapping != null) {
            long offset = globalMutationLocation - matchedMapping.getTStart();
            long plusQuery = matchedMapping.getQStart() + offset;
            //System.out.println ("offset:  " + offset);
            //System.out.println ("qStart:  " + matchedMapping.getQStart());
            ntPosition = plusQuery;
            if (!gene.isForwardStrand()) {
                ntPosition = sequenceLen - plusQuery +1;
            }
            //System.out.println ("Match occurs in mapping:  " + ntPosition);
        }
    }

    public long getNtPositionWhereMutationOccurs() {
        return ntPosition;
    }

    public char getBpWhereMutationOccurs() {
        return gene.getFullDna().charAt((int) ntPosition-1);
    }

}
