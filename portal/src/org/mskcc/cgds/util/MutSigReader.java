package org.mskcc.cgds.util;

import org.mskcc.cgds.dao.*;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.MutSig;

import java.io.*;
import java.util.Properties;

import static org.mskcc.cgds.dao.DaoMutSig.*;

/*
 * Reads and loads a MutSig file.
 * Requires a "properties" file for the Cancer Study associated with the MutSig file.

 * @author Lennart Bastian
 * @author Gideon Dresdner
 *
 */

public class MutSigReader {

    // look up the internalId for a cancer_study_identifier from a properties file
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

    // adds MutSigs to CDGS database.
    // @return total number of rows added to database
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

            int rank = Integer.parseInt(parts[rankField]);
            String hugoGeneSymbol = parts[hugoField];
            int numBasesCovered = Integer.parseInt(parts[BasesCoveredField]);
            int numMutations = Integer.parseInt(parts[numMutationsField]);
            String pValue = parts[PvalField];
            String qValue = parts[QvalField];

            CanonicalGene gene = daoGene.getGene(hugoGeneSymbol);
            if (gene == null) {
                gene = new CanonicalGene(0, hugoGeneSymbol);
                pMonitor.logWarning("Invalid gene symbol:  " + hugoGeneSymbol);
            }

            MutSig mutSig = new MutSig(internalId, gene, rank, numBasesCovered, numMutations, pValue, qValue);
            loadedMutSigs += addMutSig(mutSig);

            line = buf.readLine();
        }
        return loadedMutSigs;
    }
}
