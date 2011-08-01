package org.mskcc.cgds.model;

import org.mskcc.cgds.dao.DaoException;

/*
 * We use MutSig as an object to simplify the process of adding and getting
 * data from the mut_sig database in CGDS.
 * The MutSig object takes a total of 13 parameters, 12 of which correspond
 * directly to MutSig data collumns, and one of which is a CanonicalGene Object,
 * containing a HugeGeneSymbol and EntrezGeneID.
 * This simplifies the process of switching back between the two, and ensuring
 * a stable system in which gene IDs do not fluctuate.
 */

public class MutSig {
    private int cancerType;
    private int rank;
    private CanonicalGene canonicalGene;
    private int N;
    private int n;
    private int nVal;
    private int nVer;
    private int CpG;
    private int CandG;
    private int AandT;
    private int Indel;
    private String pValue;
    private String qValue;

    public MutSig(int cancerType, CanonicalGene canonicalGene, int rank, int N, int n, int nVal, int nVer, int CpG,
                  int CandG, int AandT, int Indel, String pValue, String qValue) {
        this.cancerType = cancerType;
        this.rank = rank;
        this.canonicalGene = canonicalGene;
        this.N = N;
        this.n = n;
        this.nVal = nVal;
        this.nVer = nVer;
        this.CpG = CpG;
        this.CandG = CandG;
        this.AandT = AandT;
        this.Indel = Indel;
        this.pValue = pValue;
        this.qValue = qValue;
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

    public int getN() {
        return N;
    }

    public int getn() {
        return n;
    }

    public int getnVal() {
        return nVal;
    }

    public int getnVer() {
        return nVer;
    }

    public int getCpG() {
        return CpG;
    }

    public int getCandG() {
        return CandG;
    }

    public int getAandT() {
        return AandT;
    }

    public int getIndel() {
        return Indel;
    }

    public String getpValue() {
        return pValue;
    }

    public String getqValue() {
        return qValue;
    }
}
