package org.mskcc.cgds.model;

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
    private int nVal;
    private int nVer;
    private int cpg;
    private int cAndG;
    private int aAndT;
    private int inDel;
    private String pValue;
    private String qValue;
    private Double adjustedQValue;

    public MutSig(int cancerType, CanonicalGene canonicalGene, int rank, 
            int numBasesCovered, int numMutations, int nVal, int nVer, int cpg,
                  int cAndG, int aAndT, int Indel, String pValue, String qValue,
                  Double adjustedQValue) {
        this.cancerType = cancerType;
        this.rank = rank;
        this.canonicalGene = canonicalGene;
        this.numBasesCovered = numBasesCovered;
        this.numMutations = numMutations;
        this.nVal = nVal;
        this.nVer = nVer;
        this.cpg = cpg;
        this.cAndG = cAndG;
        this.aAndT = aAndT;
        this.inDel = Indel;
        this.pValue = pValue;
        this.qValue = qValue;
        this.adjustedQValue = adjustedQValue;
    }

    public MutSig(int i, CanonicalGene canonicalGene, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, double v, double v1) {
        //To change body of created methods use File | Settings | File Templates.
    }

    /*
     * The following GET methods will return each specific data type in this Instance of MutSig
     * note: getCanonicalGene will return a CanonicalGene object. To extract the HugoGeneSymbol
     * or EntrezGeneID from the CanonicalGene, use the appropriate get methods on the Canonical
     * Gene object.
     */

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

    public int getnVal() {
        return nVal;
    }

    public int getnVer() {
        return nVer;
    }

    public int getCpG() {
        return cpg;
    }

    public int getCandG() {
        return cAndG;
    }

    public int getAandT() {
        return aAndT;
    }

    public int getIndel() {
        return inDel;
    }

    public String getpValue() {
        return pValue;
    }

    public String getqValue() {
        return qValue;
    }

    public Double getAdjustedQValue(){
        return adjustedQValue;
    }
}
