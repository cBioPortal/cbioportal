/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mskcc.cbio.portal.web_api;

import java.util.ArrayList;
import java.util.regex.Pattern;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoMutSig;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.MutSig;

/**
 * @author Lennart Bastian
 */
public class GetMutSig {

    private GetMutSig() {}

    /*
     * GetMutSig uses DaoMutSig to retrieve all MutSigs of a specific
     * cancer study from the database, in an Arraylist.
     * One option is to input only a CancerStudyId and retrieve
     * the entire Gene List.
     * @throws DaoException Database Error
     * @returns StringBuffer - MutSig Table
     */

    public static StringBuffer getMutSig(int cancerStudy) throws DaoException {
        StringBuffer toReturn = header(new StringBuffer());
        ArrayList<MutSig> mutSigList = DaoMutSig.getAllMutSig(cancerStudy);
        for (int i = 0; i < mutSigList.size(); i++) {
            toReturn.append(parseMutSig(mutSigList.get(i)));
        }
        return toReturn;
    }

    /*
     * The second option is to input either a Gene List (foo = false) of a Q Value Threshold (foo = true)
     * to retrieve only a specific number of Genes.
     */

    public static StringBuffer getMutSig(
        int cancerStudy,
        String qValueOrGeneList,
        Boolean qOrGene
    )
        throws DaoException {
        StringBuffer toReturn = header(new StringBuffer());
        //code for Q Value Threshold
        if (qOrGene) {
            ArrayList<MutSig> mutSigList = DaoMutSig.getAllMutSig(
                cancerStudy,
                Double.parseDouble(qValueOrGeneList)
            );
            for (int i = 0; i < mutSigList.size(); i++) {
                toReturn.append(parseMutSig(mutSigList.get(i)));
            }
            //code for Gene List
        } else if (!qOrGene) {
            Pattern p = Pattern.compile("[,\\s]+");
            String genes[] = p.split(qValueOrGeneList);
            for (String gene : genes) {
                gene = gene.trim();
                if (gene.length() == 0) {
                    continue;
                }
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
        //        toReturn += Integer.toString(mutSig.getnVal()) + "\t";
        //        toReturn += Integer.toString(mutSig.getnVer()) + "\t";
        //        toReturn += Integer.toString(mutSig.getCpG()) + "\t";
        //        toReturn += Integer.toString(mutSig.getCandG()) + "\t";
        //        toReturn += Integer.toString(mutSig.getAandT()) + "\t";
        //        toReturn += Integer.toString(mutSig.getIndel()) + "\t";
        toReturn += Float.toString(mutSig.getpValue()) + "\t";
        toReturn += Float.toString(mutSig.getqValue()) + "\n";
        return toReturn;
    }

    private static StringBuffer header(StringBuffer stringBuffer) {
        stringBuffer.append(
            "Cancer\tEntrez\tHugo\tRank\tN\tn\tnVal\tnVer\tCpG\tC+G\tA+T\tINDEL\tp\tq\n"
        );
        return stringBuffer;
    }
}
