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
