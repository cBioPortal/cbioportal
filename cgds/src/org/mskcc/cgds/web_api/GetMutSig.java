package org.mskcc.cgds.web_api;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoMutSig;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.MutSig;

import java.util.ArrayList;

/**
 * @author Lennart Bastian
 */
public class GetMutSig {

    private GetMutSig() {
    }
    /*
     * GetMutSig uses DaoMutSig to retrieve all MutSigs from Database
     * in an Arraylist
     * @throws DaoException Database Error
     * @returns String - MutSig Table
     */
    public static String GetMutSig(int cancerStudy)
            throws DaoException {
        String toReturn = "";
        DaoMutSig daoMutSig = DaoMutSig.getInstance();
        ArrayList<MutSig> mutSigList = daoMutSig.getAllMutSig(cancerStudy);
        for (int i = 0; i < mutSigList.size(); i++) {
            toReturn += parseMutSig(mutSigList.get(i));
        }
        return toReturn;
    }

    /*
     * Splits of each individual MutSig and returns as String
     */

    private static String parseMutSig(MutSig mutSig) {
        String toReturn = "";
        toReturn += Integer.toString(mutSig.getCancerType());
        toReturn += "\t";
        CanonicalGene gene = mutSig.getCanonicalGene();
        toReturn += Long.toString(gene.getEntrezGeneId());
        toReturn += "\t";
        toReturn += Integer.toString(mutSig.getRank());
        toReturn += "\t";
        toReturn += Integer.toString(mutSig.getN());
        toReturn += "\t";
        toReturn += Integer.toString(mutSig.getn());
        toReturn += "\t";
        toReturn += Integer.toString(mutSig.getnVal());
        toReturn += "\t";
        toReturn += Integer.toString(mutSig.getnVer());
        toReturn += "\t";
        toReturn += Integer.toString(mutSig.getCpG());
        toReturn += "\t";
        toReturn += Integer.toString(mutSig.getCandG());
        toReturn += "\t";
        toReturn += Integer.toString(mutSig.getAandT());
        toReturn += "\t";
        toReturn += Integer.toString(mutSig.getIndel());
        toReturn += "\t";
        toReturn += mutSig.getpValue();
        toReturn += "\t";
        toReturn += mutSig.getqValue();
        toReturn += "\n";
        return toReturn;
    }
}


