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

package org.mskcc.cbio.portal.model;

import org.mskcc.cbio.portal.dao.DaoException;

/**
 * @author Lennart Bastian
 * We use MutSig as an object to simplify the process of adding and getting
 * data from the mut_sig database in CGDS.
 * The MutSig object takes a total of 13 parameters, 12 of which correspond
 * directly to MutSig data columns, and one of which is a CanonicalGene Object,
 * containing a HugeGeneSymbol and EntrezGeneID.
 * This simplifies the process of switching back between the two, and ensuring
 * a stable system in which gene IDs do not fluctuate.
 */

public class MutSig {
    private int cancerType;
    private int rank;
    private CanonicalGene canonicalGene;
    private int numBasesCovered;
    private int numMutations;
    private float pValue;
    private float qValue;
    
    public MutSig() {
        
    }

    public MutSig(int cancerType, CanonicalGene canonicalGene, int rank,
            int numBasesCovered, int numMutations, float pValue, float qValue) {
        this.cancerType = cancerType;
        this.rank = rank;
        this.canonicalGene = canonicalGene;
        this.numBasesCovered = numBasesCovered;
        this.numMutations = numMutations;
        this.pValue = pValue;
        this.qValue = qValue;
    }
    // ignoring fields :
    //      nVal, nVer, cpg, cAndG, aAndT, inDel, adjustedQValue


//  note: getCanonicalGene will return a CanonicalGene object.

    public MutSig getInstance() throws DaoException {
        return this;
    }

    public int getCancerType() {
        return cancerType;
    }

    public int getRank() {
        return rank;
    }

    public CanonicalGene getCanonicalGene() {
        return canonicalGene;
    }

    public int getNumBasesCovered() {
        return numBasesCovered;
    }

    public int getNumMutations() {
        return numMutations;
    }

    public float getpValue() {
        return pValue;
    }

    public float getqValue() {
        return qValue;
    }

    public void setCancerType(int cancerType) {
        this.cancerType = cancerType;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setCanonicalGene(CanonicalGene canonicalGene) {
        this.canonicalGene = canonicalGene;
    }

    public void setNumBasesCovered(int numBasesCovered) {
        this.numBasesCovered = numBasesCovered;
    }

    public void setNumMutations(int numMutations) {
        this.numMutations = numMutations;
    }

    public void setpValue(float pValue) {
        this.pValue = pValue;
    }

    public void setqValue(float qValue) {
        this.qValue = qValue;
    }

    public String toString() {
        return  String.format("[canonicalGene: %s, numBasesCovered: %d, numMutations: %d, qValue: %f]",
                        this.getCanonicalGene(), this.numBasesCovered, this.numMutations, this.qValue);
    }
}
