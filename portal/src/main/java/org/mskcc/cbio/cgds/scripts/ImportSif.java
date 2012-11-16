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

package org.mskcc.cbio.cgds.scripts;

import org.mskcc.cbio.cgds.util.ProgressMonitor;
import org.mskcc.cbio.cgds.util.ConsoleUtil;
import org.mskcc.cbio.cgds.util.FileUtil;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.MySQLbulkLoader;
import org.mskcc.cbio.cgds.dao.DaoGeneOptimized;
import org.mskcc.cbio.cgds.dao.DaoInteraction;
import org.mskcc.cbio.cgds.model.CanonicalGene;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Set;
import java.util.HashSet;

/**
 * Command Line to Import SIF Interactions.
 *
 * @author Ethan Cerami.
 */
public class ImportSif {
    private ProgressMonitor pMonitor;
    private File sifFile;
    private String dataSource;

    /**
     * Constructor.
     *
     * @param sifFile SIF File.
     * @param dataSource data source, e.g. "REACTOME"
     * @param pMonitor Progress Monitor.
     */
    public ImportSif(File sifFile, String dataSource, ProgressMonitor pMonitor) {
        this.sifFile = sifFile;
        this.dataSource = dataSource;
        this.pMonitor = pMonitor;
    }

    /**
     * Imports the Interaction Data.
     * @throws IOException IO Error.
     * @throws DaoException Database Error.
     */
    public void importData() throws IOException, DaoException {
        DaoInteraction daoInteraction = DaoInteraction.getInstance();
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        int numInteractionsSaved = 0;
        int numInteractionsNotSaved = 0;
        int numRedundantInteractionsSkipped = 0;
        Set<String> interactionSet = new HashSet<String>();

        MySQLbulkLoader.bulkLoadOn();
        FileReader reader = new FileReader(sifFile);
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
                String interactionType = parts[1];
                String geneBId = parts[2];

                CanonicalGene geneA = daoGeneOptimized.getNonAmbiguousGene(geneAId);
                CanonicalGene geneB = daoGeneOptimized.getNonAmbiguousGene(geneBId);

                //  Log genes that we cannot identify.
                if (geneA == null) {
                    pMonitor.logWarning("Cannot identify gene:  " + geneAId);
                }
                if (geneB == null) {
                    pMonitor.logWarning("Cannot identify gene:  " + geneBId);
                }

                if (geneA != null && geneB != null) {

                    String key = createKey(geneA, geneB, interactionType);
                    if (!interactionSet.contains(key)) {
                        //  SIF Interactions do not have experiment details or PMIDs.  So, we set to null.
                        daoInteraction.addInteraction(geneA, geneB, interactionType, dataSource,
                                null, null);
                        numInteractionsSaved++;
                        interactionSet.add(key);
                    } else {
                        numRedundantInteractionsSkipped++;
                    }
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
        pMonitor.setCurrentMessage("Total number of redundant interactions skipped:  "
                + numRedundantInteractionsSkipped);
    }

    /**
     * Creates an Interacton Key.
     * @param geneA Gene A.
     * @param geneB Gene B.
     * @param interactionType Interaction Type.
     * @return key.
     */
    private String createKey (CanonicalGene geneA, CanonicalGene geneB,
            String interactionType) {
        long idA = geneA.getEntrezGeneId();
        long idB = geneB.getEntrezGeneId();
        return new String(idA + ":" + interactionType + ":" + idB);
    }


    /**
     * Command Line Util.
     * @param args Command Line Arguments.
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("command line usage:  importSif.pl <sif.txt> <data_source>");
            System.exit(1);
        }
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        try {
            File geneFile = new File(args[0]);
            String dataSource = args[1];
            System.out.println("Reading interactions from:  " + geneFile.getAbsolutePath());
            int numLines = FileUtil.getNumLines(geneFile);
            System.out.println(" --> total number of lines:  " + numLines);
            pMonitor.setMaxValue(numLines);
            ImportSif parser = new ImportSif(geneFile, dataSource, pMonitor);
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
