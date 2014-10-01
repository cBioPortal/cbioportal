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

package org.mskcc.cbio.portal.util;

import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.MutSig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.List;
import java.util.Properties;

import static org.mskcc.cbio.portal.dao.DaoMutSig.addMutSig;

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

		return getInternalId(cancerStudyIdentifier);
    }

    /**
     * Gets internal cancer study id by stable id.
     * @param cancerStudy String
     * @return int
     * @throws IOException
     * @throws DaoException
     */
    public static int getInternalId(final String cancerStudyIdentifier) throws IOException, DaoException
    {
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
            } else if (names[i].equalsIgnoreCase("gene")) {
                hugoField = i;
            } else if (names[i].equals("N") || names[i].equals("Nnon") ) {
                BasesCoveredField = i;
            } else if (names[i].equals("n") || names[i].equals("nnon") ) {
                numMutationsField = i;
            } else if (names[i].equalsIgnoreCase("p")) {
                PvalField = i;
            } else if (names[i].equalsIgnoreCase("q") || names[i].equalsIgnoreCase("q\n")) {
                QvalField = i;
            }
        }
        // end parse Column names

        // check to see if all fields are filled
        if (hugoField == -1
                || BasesCoveredField == -1
                || numMutationsField == -1
                || PvalField == -1
                || QvalField == -1) {
            throw new IOException("one or more of the fields [rank, hugoGeneSymbol, number of bases covered (N), " +
                    "number of mutations (n), p-value, q-value] are undefined");
        }

        // parse data
        int rank = 0;
        for (String line = buf.readLine();line != null;line = buf.readLine()) {

            if (pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
            
            MutSig mutSig = new MutSig();
            mutSig.setCancerType(internalId);
            
            DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

            String[] parts = line.split("\t");

            // -- load parameters for new MutSig object --
            try {
                if (rankField==-1) { // MutSigCV
                    rank++;
                } else {
                    rank = Integer.parseInt(parts[rankField]);
                }
                mutSig.setRank(rank);
            } catch (java.lang.NumberFormatException e) {
            }
            
            String hugoGeneSymbol = parts[hugoField];

            try {
                int numBasesCovered = Integer.parseInt(parts[BasesCoveredField]);
                mutSig.setNumBasesCovered(numBasesCovered);
            } catch (java.lang.NumberFormatException e) {
            }
            
            try {
                int numMutations = Integer.parseInt(parts[numMutationsField]);
                mutSig.setNumMutations(numMutations);
            } catch (java.lang.NumberFormatException e) {
            }
            
            // ignoring '<' sign
            try {
                float pValue = Float.valueOf(parts[PvalField].replace("<", ""));
                mutSig.setpValue(pValue);
            } catch (java.lang.NumberFormatException e) {
            }
            
            try {
                float qValue = Float.valueOf(parts[QvalField].replace("<", ""));
                // Ignore everything with high q-value,
                // specified by Ethan
                if (qValue >= 0.1) {
                    continue;
                }
                mutSig.setqValue(qValue);
            } catch (java.lang.NumberFormatException e) {
            }

            CanonicalGene gene = daoGene.getNonAmbiguousGene(hugoGeneSymbol);
            if (gene==null) {
                continue;
            }
            mutSig.setCanonicalGene(gene);

            // -- end load parameters for new MutSig object --

            loadedMutSigs += addMutSig(mutSig);

        }
        return loadedMutSigs;
    }
}
