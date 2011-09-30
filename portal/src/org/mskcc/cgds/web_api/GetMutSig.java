package org.mskcc.cgds.web_api;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoMutSig;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.MutSig;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * @author Lennart Bastian
 */
public class GetMutSig {

    private GetMutSig() {
    }

    /*
    * GetMutSig uses DaoMutSig to retrieve all MutSigs of a specific
    * cancer study from the database, in an Arraylist.
    * One option is to input only a CancerStudyId and retrieve
    * the entire Gene List.
    * @throws DaoException Database Error
    * @returns StringBuffer - MutSig Table
    */

    public static StringBuffer GetAMutSig(int cancerStudy)
            throws DaoException {
        StringBuffer toReturn = header(new StringBuffer());
        DaoMutSig daoMutSig = DaoMutSig.getInstance();
        ArrayList<MutSig> mutSigList = daoMutSig.getAllMutSig(cancerStudy);
        for (int i = 0; i < mutSigList.size(); i++) {
            toReturn.append(parseMutSig(mutSigList.get(i)));
        }
        return toReturn;
    }

    /*
    * The second option is to input either a Gene List (foo = false) of a Q Value Threshold (foo = true)
    * to retrieve only a specific number of Genes.
    */

    public static StringBuffer GetAMutSig(int cancerStudy, String q_Value_or_Gene_List, Boolean qOrGene)
            throws DaoException, NumberFormatException {
        StringBuffer toReturn = header(new StringBuffer());
        //code for Q Value Threshold
        if (qOrGene) {
            DaoMutSig daoMutSig = DaoMutSig.getInstance();
            ArrayList<MutSig> mutSigList = daoMutSig.getAllMutSig(cancerStudy, Double.parseDouble(q_Value_or_Gene_List));
            for (int i = 0; i < mutSigList.size(); i++) {
            toReturn.append(parseMutSig(mutSigList.get(i)));
            }
            //code for Gene List
        } else if (!qOrGene) {
            Pattern p = Pattern.compile("[,\\s]+");
            String genes[] = p.split(q_Value_or_Gene_List);
            for (String gene : genes) {
                gene = gene.trim();
                if (gene.length() == 0) continue;
                MutSig mutSig = DaoMutSig.getMutSig(gene, cancerStudy);
                toReturn.append(parseMutSig(mutSig));
            }
        }
        return toReturn;
    }

    /*
    * Splits of each individual MutSig and returns as String
    */

    private static String parseMutSig(MutSig mutSig) {
        String toReturn = "";
        toReturn += Integer.toString(mutSig.getCancerType()) + "\t";
        CanonicalGene gene = mutSig.getCanonicalGene();
        toReturn += Long.toString(gene.getEntrezGeneId()) + "\t";
        toReturn += gene.getHugoGeneSymbolAllCaps() + "\t";
        toReturn += Integer.toString(mutSig.getRank()) + "\t";
        toReturn += Integer.toString(mutSig.getNumBasesCovered()) + "\t";
        toReturn += Integer.toString(mutSig.getNumMutations()) + "\t";
        toReturn += Integer.toString(mutSig.getnVal()) + "\t";
        toReturn += Integer.toString(mutSig.getnVer()) + "\t";
        toReturn += Integer.toString(mutSig.getCpG()) + "\t";
        toReturn += Integer.toString(mutSig.getCandG()) + "\t";
        toReturn += Integer.toString(mutSig.getAandT()) + "\t";
        toReturn += Integer.toString(mutSig.getIndel()) + "\t";
        toReturn += mutSig.getpValue() + "\t";
        toReturn += mutSig.getqValue() + "\n";
        return toReturn;
    }

    private static StringBuffer header(StringBuffer stringBuffer){
        stringBuffer.append("Cancer\tEntrez\tHugo\tRank\tN\tn\tnVal\tnVer\tCpG\tC+G\tA+T\tINDEL\tp\tq\n");
        return stringBuffer;
    }
}
