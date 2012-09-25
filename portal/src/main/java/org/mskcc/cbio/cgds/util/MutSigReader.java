/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.cgds.util;

import org.mskcc.cbio.cgds.dao.DaoCancerStudy;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneOptimized;
import org.mskcc.cbio.cgds.dao.MySQLbulkLoader;
import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.model.CanonicalGene;
import org.mskcc.cbio.cgds.model.MutSig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.List;
import java.util.Properties;

import static org.mskcc.cbio.cgds.dao.DaoMutSig.addMutSig;

/*
 * Reads and loads a MutSig file.
 * Requires a "properties" file for the Cancer Study associated with the MutSig file.

 * @author Lennart Bastian
 * @author Gideon Dresdner
 *
 */

public class MutSigReader {
    public static final int HIGH_Q_VALUE = -1;
    private static Log log = LogFactory.getLog(MutSigReader.class);

    // look up the internalId for a cancer_study_identifier from a properties file

    /**
     * Look up CancerStudy Id, internal databse record
     * @param props         Properties file
     * @return              CancerStudyId
     * @throws IOException
     * @throws DaoException
     */
    public static int getInternalId(File props) throws IOException, DaoException
    {

        Properties properties = new Properties();
        properties.load(new FileInputStream(props));

        String cancerStudyIdentifier = properties.getProperty("cancer_study_identifier");

        if (cancerStudyIdentifier == null) {
            throw new IllegalArgumentException("cancer_study_identifier is not specified.");
        }

        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyIdentifier);

        if (cancerStudy == null) {
            throw new DaoException("no CancerStudy associated with \""
                    + cancerStudyIdentifier + "\" cancer_study_identifier");
        }

        return cancerStudy.getInternalId();
    }

    /**
     * Adds MutSigs to CDGS database.
     * @param internalId        CancerStudy database record
     * @param mutSigFile        MutSigFile
     * @param pMonitor          pMonitor
     * @return                  number of MutSig records loaded
     * @throws IOException
     * @throws DaoException
     */
    public static int loadMutSig(int internalId, File mutSigFile, ProgressMonitor pMonitor) throws IOException, DaoException {
        int loadedMutSigs = 0;
        MySQLbulkLoader.bulkLoadOff();
        FileReader reader = new FileReader(mutSigFile);
        BufferedReader buf = new BufferedReader(reader);

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
        for (int i = 0; i < len ; i++)
        {
            if (names[i].equals("rank")) {
                rankField = i;
            }

            if (names[i].equalsIgnoreCase("gene")) {
                hugoField = i;
            }

            if (names[i].equals("N")) {
                BasesCoveredField = i;
            }

            if (names[i].equals("n")) {
                numMutationsField = i;
            }

            if (names[i].equalsIgnoreCase("p")) {
                PvalField = i;
            }

            if (names[i].equalsIgnoreCase("q") || names[i].equalsIgnoreCase("q\n")) {
                QvalField = i;
            }
        }
        // end parse Column names

        // check to see if all fields are filled
        if (rankField == -1
                || hugoField == -1
                || BasesCoveredField == -1
                || numMutationsField == -1
                || PvalField == -1
                || QvalField == -1) {
            throw new IOException("one or more of the fields [rank, hugoGeneSymbol, number of bases covered (N), " +
                    "number of mutations (n), p-value, q-value] are undefined");
        }

        // parse data
        String line = buf.readLine();
        while (line != null) {

            if (pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
            DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

            String[] parts = line.split("\t");

            // -- load parameters for new MutSig object --
            int rank = Integer.parseInt(parts[rankField]);
            String hugoGeneSymbol = parts[hugoField];
            int numBasesCovered = Integer.parseInt(parts[BasesCoveredField]);
            int numMutations = Integer.parseInt(parts[numMutationsField]);

            // ignoring '<' sign
            float pValue = Float.valueOf(parts[PvalField].replace("<", ""));
            float qValue = Float.valueOf(parts[QvalField].replace("<", ""));

            // Ignore everything with high q-value,
            // specified by Ethan
            if (qValue >= 0.1) {
                line = buf.readLine();
                continue;
            }

            List<CanonicalGene> genes;
            genes = daoGene.guessGene(hugoGeneSymbol);

            // there should only be one EntrezId for any given HugoGeneSymbol
            CanonicalGene gene;
            if (genes.size() == 0) {
                if (log.isWarnEnabled()) {
                    log.warn("Cannot find CanonicalGene for HugoGeneSymbol: " + hugoGeneSymbol
                    + ". Set EntrezId = 0");
                }

                gene = new CanonicalGene(0, hugoGeneSymbol);
            }

            else if (genes.size() > 1 && log.isWarnEnabled()) {
                log.warn("Found more than one CanonicalGenes for HugoGeneSymbol: " + hugoGeneSymbol
                        + ". Chose the first one by default");
                gene = genes.get(0);
            }
            
            else {  // there is one and only one EntrezId for a given HUGO symbol
                gene = genes.get(0);
            }

            // -- end load parameters for new MutSig object --

            MutSig mutSig = new MutSig(internalId, gene, rank, numBasesCovered, numMutations, pValue, qValue);
            loadedMutSigs += addMutSig(mutSig);

            line = buf.readLine();
        }
        return loadedMutSigs;
    }
}
