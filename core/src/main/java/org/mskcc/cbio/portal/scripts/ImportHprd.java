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

package org.mskcc.cbio.portal.scripts;

import java.io.*;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.util.*;

/**
 * Command Line to Import HPRD Interactions.
 *
 * @author Ethan Cerami.
 */
public class ImportHprd {
    private File hprdFile;

    /**
     * Constructor.
     *
     * @param hprdFile SIF File.
     */
    public ImportHprd(File hprdFile) {
        this.hprdFile = hprdFile;
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
            ProgressMonitor.incrementCurValue();
            ConsoleUtil.showProgress();
            if (!line.startsWith("#")) {
                String parts[] = line.split("\t");

                String geneAId = parts[0];
                String geneBId = parts[3];
                String interactionType = "INTERACTS_WITH";
                String dataSource = "HPRD";
                String expTypes = parts[6];
                String pmids = parts[7];

                CanonicalGene geneA = daoGene.getNonAmbiguousGene(
                    geneAId,
                    true
                );
                CanonicalGene geneB = daoGene.getNonAmbiguousGene(
                    geneBId,
                    true
                );

                //  Log genes that we cannot identify.
                if (geneA == null) {
                    ProgressMonitor.logWarning(
                        "Cannot identify gene:  " + geneAId
                    );
                }
                if (geneB == null) {
                    ProgressMonitor.logWarning(
                        "Cannot identify gene:  " + geneBId
                    );
                }

                if (geneA != null && geneB != null) {
                    daoInteraction.addInteraction(
                        geneA,
                        geneB,
                        interactionType,
                        dataSource,
                        expTypes,
                        pmids
                    );
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
        ProgressMonitor.setCurrentMessage(
            "Total number of interactions saved:  " + numInteractionsSaved
        );
        ProgressMonitor.setCurrentMessage(
            "Total number of interactions not saved, due to " +
            "invalid gene IDs:  " +
            numInteractionsNotSaved
        );
    }

    /**
     * Command Line Util.
     * @param args Command Line Arguments.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("command line usage:  importHprd.pl <hprd.txt>");
            // an extra --noprogress option can be given to avoid the messages regarding memory usage and % complete
            return;
        }
        ProgressMonitor.setConsoleModeAndParseShowProgress(args);
        SpringUtil.initDataSource();

        try {
            File geneFile = new File(args[0]);
            System.out.println(
                "Reading interactions from:  " + geneFile.getAbsolutePath()
            );
            int numLines = FileUtil.getNumLines(geneFile);
            System.out.println(" --> total number of lines:  " + numLines);
            ProgressMonitor.setMaxValue(numLines);
            ImportHprd parser = new ImportHprd(geneFile);
            parser.importData();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DaoException e) {
            e.printStackTrace();
        } finally {
            ConsoleUtil.showWarnings();
            System.err.println("Done.");
        }
    }
}
