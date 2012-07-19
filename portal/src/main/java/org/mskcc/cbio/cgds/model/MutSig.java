package org.mskcc.cbio.cgds.model;

import org.mskcc.cgds.dao.DaoException;

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
}
