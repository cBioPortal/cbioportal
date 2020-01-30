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

package org.mskcc.cbio.portal.util;

import static org.mskcc.cbio.portal.dao.DaoMutSig.addMutSig;

import java.io.*;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.MutSig;

/*
 * Reads and loads a MutSig file.
 * Requires a "properties" file for the Cancer Study associated with the MutSig file.

 * @author Lennart Bastian
 * @author Gideon Dresdner
 *
 */

public class MutSigReader {
    public static final int HIGH_Q_VALUE = -1;

    /**
     * Adds MutSigs to CDGS database.
     * @param internalId        CancerStudy database record
     * @param mutSigFile        MutSigFile
     * @return                  number of MutSig records loaded
     * @throws IOException
     * @throws DaoException
     */
    public static int loadMutSig(int internalId, File mutSigFile)
        throws IOException, DaoException {
        int loadedMutSigs = 0;
        MySQLbulkLoader.bulkLoadOff();
        FileReader reader = new FileReader(mutSigFile);
        BufferedReader buf = new BufferedReader(reader);
        try {
            // parse field names of a mutsig data file
            int rankField = -1;
            int hugoField = -1;
            int BasesCoveredField = -1;
            int numMutationsField = -1;
            int PvalField = -1;
            int QvalField = -1;

            String head = buf.readLine();
            String[] names = head.split("\t");
            int len = names.length;
            for (int i = 0; i < len; i++) {
                if (names[i].equals("rank")) {
                    rankField = i;
                } else if (names[i].equalsIgnoreCase("gene")) {
                    hugoField = i;
                } else if (names[i].equals("N") || names[i].equals("Nnon")) {
                    BasesCoveredField = i;
                } else if (names[i].equals("n") || names[i].equals("nnon")) {
                    numMutationsField = i;
                } else if (names[i].equalsIgnoreCase("p")) {
                    PvalField = i;
                } else if (
                    names[i].equalsIgnoreCase("q") ||
                    names[i].equalsIgnoreCase("q\n")
                ) {
                    QvalField = i;
                }
            }
            // end parse Column names

            // check to see if all fields are filled
            if (
                hugoField == -1 ||
                BasesCoveredField == -1 ||
                numMutationsField == -1 ||
                PvalField == -1 ||
                QvalField == -1
            ) {
                throw new IOException(
                    "one or more of the fields [rank, hugoGeneSymbol, number of bases covered (N), " +
                    "number of mutations (n), p-value, q-value] are undefined"
                );
            }

            // parse data
            int rank = 0;
            for (
                String line = buf.readLine();
                line != null;
                line = buf.readLine()
            ) {
                ProgressMonitor.incrementCurValue();
                ConsoleUtil.showProgress();

                MutSig mutSig = new MutSig();
                mutSig.setCancerType(internalId);

                DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

                String[] parts = line.split("\t");

                // -- load parameters for new MutSig object --
                try {
                    if (rankField == -1) { // MutSigCV
                        rank++;
                    } else {
                        rank = Integer.parseInt(parts[rankField]);
                    }
                    mutSig.setRank(rank);
                } catch (java.lang.NumberFormatException e) {
                    //should not occur anymore:
                    throw e;
                }

                String hugoGeneSymbol = parts[hugoField];

                try {
                    int numBasesCovered = Integer.parseInt(
                        parts[BasesCoveredField]
                    );
                    mutSig.setNumBasesCovered(numBasesCovered);
                } catch (java.lang.NumberFormatException e) {
                    //should not occur anymore:
                    throw e;
                }

                try {
                    int numMutations = Integer.parseInt(
                        parts[numMutationsField]
                    );
                    mutSig.setNumMutations(numMutations);
                } catch (java.lang.NumberFormatException e) {
                    //should not occur anymore:
                    throw e;
                }

                // ignoring '<' sign
                try {
                    float pValue = Float.valueOf(
                        parts[PvalField].replace("<", "")
                    );
                    mutSig.setpValue(pValue);
                } catch (java.lang.NumberFormatException e) {
                    //should not occur anymore:
                    throw e;
                }

                try {
                    float qValue = Float.valueOf(
                        parts[QvalField].replace("<", "")
                    );
                    // Ignore everything with high q-value,
                    // specified by Ethan
                    if (qValue >= 0.1) {
                        ProgressMonitor.logWarning(
                            "Filtered out item with qValue >= 0.1"
                        ); //Because this message is static, it will be grouped and shown only once if it occurs many times
                        continue;
                    }
                    mutSig.setqValue(qValue);
                } catch (java.lang.NumberFormatException e) {
                    //should not occur anymore:
                    throw e;
                }

                CanonicalGene gene = daoGene.getNonAmbiguousGene(
                    hugoGeneSymbol
                );
                if (gene == null) {
                    ProgressMonitor.logWarning(
                        "Gene " +
                        gene +
                        " not found or was ambiguous. Skipping this gene."
                    );
                    continue;
                }
                mutSig.setCanonicalGene(gene);

                // -- end load parameters for new MutSig object --

                loadedMutSigs += addMutSig(mutSig);
            }
            return loadedMutSigs;
        } finally {
            buf.close();
        }
    }
}
