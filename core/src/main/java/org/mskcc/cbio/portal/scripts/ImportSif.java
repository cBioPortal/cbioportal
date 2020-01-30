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
import java.util.*;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.util.*;

/**
 * Command Line to Import SIF Interactions.
 *
 * @author Ethan Cerami.
 */
public class ImportSif {
    private File sifFile;
    private String dataSource;

    /**
     * Constructor.
     *
     * @param sifFile SIF File.
     * @param dataSource data source, e.g. "REACTOME"
     */
    public ImportSif(File sifFile, String dataSource) {
        this.sifFile = sifFile;
        this.dataSource = dataSource;
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
            ProgressMonitor.incrementCurValue();
            ConsoleUtil.showProgress();
            if (!line.startsWith("#")) {
                String parts[] = line.split("\t");

                String geneAId = parts[0];
                String interactionType = parts[1];
                String geneBId = parts[2];

                CanonicalGene geneA = daoGeneOptimized.getNonAmbiguousGene(
                    geneAId,
                    true
                );
                CanonicalGene geneB = daoGeneOptimized.getNonAmbiguousGene(
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
                    String key = createKey(geneA, geneB, interactionType);
                    if (!interactionSet.contains(key)) {
                        //  SIF Interactions do not have experiment details or PMIDs.  So, we set to null.
                        daoInteraction.addInteraction(
                            geneA,
                            geneB,
                            interactionType,
                            dataSource,
                            null,
                            null
                        );
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
        ProgressMonitor.setCurrentMessage(
            "Total number of redundant interactions skipped:  " +
            numRedundantInteractionsSkipped
        );
    }

    /**
     * Creates an Interacton Key.
     * @param geneA Gene A.
     * @param geneB Gene B.
     * @param interactionType Interaction Type.
     * @return key.
     */
    private String createKey(
        CanonicalGene geneA,
        CanonicalGene geneB,
        String interactionType
    ) {
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
            System.out.println(
                "command line usage:  importSif.pl <sif.txt> <data_source>"
            );
            return;
        }
        ProgressMonitor.setConsoleMode(true);

        SpringUtil.initDataSource();

        try {
            File geneFile = new File(args[0]);
            String dataSource = args[1];
            System.out.println(
                "Reading interactions from:  " + geneFile.getAbsolutePath()
            );
            int numLines = FileUtil.getNumLines(geneFile);
            System.out.println(" --> total number of lines:  " + numLines);
            ProgressMonitor.setMaxValue(numLines);
            ImportSif parser = new ImportSif(geneFile, dataSource);
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
