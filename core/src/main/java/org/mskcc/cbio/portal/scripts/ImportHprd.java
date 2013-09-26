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

package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.FileUtil;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoInteraction;
import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;

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
     * @throws org.mskcc.cbio.portal.dao.DaoException Database Error.
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
           MySQLbulkLoader.flushAll();
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
