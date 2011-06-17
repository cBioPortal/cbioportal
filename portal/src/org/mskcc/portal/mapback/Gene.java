package org.mskcc.portal.mapback;

import java.util.ArrayList;

public abstract class Gene {

    public abstract ArrayList<Mapping> getMappingList();

    public abstract boolean isForwardStrand();
    public abstract String getFullDna();
}
