package org.mskcc.cgds.model;

import org.mskcc.cgds.dao.DaoException;


/**
 * Created by IntelliJ IDEA.
 * User: lennartbastian
 * Date: 22/07/2011
 * Time: 15:50
 * To change this template use File | Settings | File Templates.
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

    public MutSig(int cancerType, CanonicalGene canonicalGene, int rank, int N, int n, int nVal, int nVer, int CpG, int CandG, int AandT, int Indel,
                  String pValue, String qValue){
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

    public MutSig getInstance() throws DaoException {
        return this;
    }

    public int getCancerType(){
        return cancerType;
    }

    public int getRank(){
        return rank;
    }

    public CanonicalGene getCanonicalGene(){
        return canonicalGene;
    }

    public int getN(){
        return N;
    }

    public int getn(){
        return n;
    }

    public int getnVal(){
        return nVal;
    }

    public int getnVer(){
        return nVer;
    }

    public int getCpG(){
        return CpG;
    }

    public int getCandG(){
        return CandG;
    }

    public int getAandT(){
        return AandT;
    }

    public int getIndel(){
        return Indel;
    }

    public String getpValue(){
        return pValue;
    }

    public String getqValue(){
        return qValue;
    }
}
