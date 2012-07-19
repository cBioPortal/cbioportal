package org.mskcc.portal.mapback;

import java.util.ArrayList;

/**
 * Encapsulates Gene Mapping Information, for mapping between GenBank Nucleotide
 * position and global chromosomal location.
 *
 * @author Ethan Cerami.
 */
public abstract class MapBackGene {

    /**
     * Gets the complete list of local to global mappings.
     * @return ArrayList of Mapping Objects.
     */
    public abstract ArrayList<Mapping> getMappingList();

    /**
     * Is a Forward Strand Gene.
     * @return true or false.
     */
    public abstract boolean isForwardStrand();

    /**
     * Gets DNA of the Gene (used for debugging purposes).
     * @return DNA Sequence.
     */
    public abstract String getFullDna();
}
