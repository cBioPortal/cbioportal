package org.mskcc.cgds.scripts;

import org.mskcc.cgds.util.ProgressMonitor;
import org.mskcc.cgds.util.ConsoleUtil;
import org.mskcc.cgds.util.FileUtil;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoInteraction;
import org.mskcc.cgds.dao.MySQLbulkLoader;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.model.CanonicalGene;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;

/**
 * Command Line to Import HPRD Interactions.
 *
 * @author Ethan Cerami.
 */
public class ImportHprd {
    private ProgressMonitor pMonitor;
    private File hprdFile;

    /**
     * Constructor.
     *
     * @param hprdFile SIF File.
     * @param pMonitor Progress Monitor.
     */
    public ImportHprd(File hprdFile, ProgressMonitor pMonitor) {
        this.hprdFile = hprdFile;
        this.pMonitor = pMonitor;
    }

    /**
     * Imports the Interaction Data.
     * @throws java.io.IOException IO Error.
     * @throws org.mskcc.cgds.dao.DaoException Database Error.
     */
    public void importData() throws IOException, DaoException {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        DaoInteraction daoInteraction = DaoInteraction.getInstance();
        int numInteractionsSaved = 0;
        int numInteractionsNotSaved = 0;

        MySQLbulkLoader.bulkLoadOn();
        FileReader reader = new FileReader(hprdFile);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine();
        while (line != null) {
            if (pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
            if (!line.startsWith("#")) {
                String parts[] = line.split("\t");

                String geneAId = parts[0];
                String geneBId = parts[3];
                String interactionType = "INTERACTS_WITH";
                String dataSource = "HPRD";
                String expTypes = parts[6];
                String pmids = parts[7];

                CanonicalGene geneA = daoGene.getNonAmbiguousGene(geneAId);
                CanonicalGene geneB = daoGene.getNonAmbiguousGene(geneBId);

                //  Log genes that we cannot identify.
                if (geneA == null) {
                    pMonitor.logWarning("Cannot identify gene:  " + geneAId);
                }
                if (geneB == null) {
                    pMonitor.logWarning("Cannot identify gene:  " + geneBId);
                }

                if (geneA != null && geneB != null) {
                    daoInteraction.addInteraction(geneA, geneB, interactionType, dataSource,
                            expTypes, pmids);
                    numInteractionsSaved++;
                } else {
                    numInteractionsNotSaved++;
                }
            }
            line = buf.readLine();
        }

        //  Flush database
        if (MySQLbulkLoader.isBulkLoad()) {
           daoInteraction.flushToDatabase();
        }
        pMonitor.setCurrentMessage("Total number of interactions saved:  " + numInteractionsSaved);
        pMonitor.setCurrentMessage("Total number of interactions not saved, due to " +
                "invalid gene IDs:  " + numInteractionsNotSaved);
    }

    /**
     * Command Line Util.
     * @param args Command Line Arguments.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("command line usage:  importHprd.pl <hprd.txt>");
            System.exit(1);
        }
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        try {
            File geneFile = new File(args[0]);
            System.out.println("Reading interactions from:  " + geneFile.getAbsolutePath());
            int numLines = FileUtil.getNumLines(geneFile);
            System.out.println(" --> total number of lines:  " + numLines);
            pMonitor.setMaxValue(numLines);
            ImportHprd parser = new ImportHprd(geneFile, pMonitor);
            parser.importData();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DaoException e) {
            e.printStackTrace();
        } finally {
            ConsoleUtil.showWarnings(pMonitor);
            System.err.println("Done.");
        }
    }
}
