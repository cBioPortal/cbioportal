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

import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.CanonicalGene;

import java.io.*;

/**
 * Command Line to Import HPRD Interactions.
 *
 * @author Ethan Cerami.
 */
public class ImportPathwayCommonsExtSif {
    private File sifFile;

    /**
     * Constructor.
     *
     * @param sifFile SIF File.
     */
    public ImportPathwayCommonsExtSif(File sifFile) {
        this.sifFile = sifFile;
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
        FileReader reader = new FileReader(sifFile);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine(); // skip the first line
        while ((line = buf.readLine()) != null) {
            ProgressMonitor.incrementCurValue();
            ConsoleUtil.showProgress();
            
            String parts[] = line.split("\t");
            
            if (parts.length<4) {
                continue;
            }

            String geneAId = parts[0];

            CanonicalGene geneA = daoGene.getNonAmbiguousGene(geneAId, true);
            if (geneA != null) {
                String geneBId = parts[2];
                CanonicalGene geneB = daoGene.getNonAmbiguousGene(geneBId, true);

                if (geneB != null) {
                    String interactionType = parts[1];
                    String dataSource = parts[3];
                    String pmids = parts.length<=4 ? null : parts[4].replaceAll(";", ",");
                    String expTypes = null;

                    daoInteraction.addInteraction(geneA, geneB, interactionType, dataSource,
                            expTypes, pmids);

                    numInteractionsSaved++;
                } else {
                    numInteractionsNotSaved++;
                }
            } else {
                numInteractionsNotSaved++;
            }
        }

        //  Flush database
        if (MySQLbulkLoader.isBulkLoad()) {
           MySQLbulkLoader.flushAll();
        }
        ProgressMonitor.setCurrentMessage("Total number of interactions saved:  " + numInteractionsSaved);
        ProgressMonitor.setCurrentMessage("Total number of interactions not saved, due to " +
                "invalid gene IDs:  " + numInteractionsNotSaved);
    }

    /**
     * Command Line Util.
     * @param args Command Line Arguments.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("command line usage:  importHprd.pl <sif.txt>");
            return;
        }
        ProgressMonitor.setConsoleMode(true);
		SpringUtil.initDataSource();

        try {
            File sifFile = new File(args[0]);
            System.out.println("Reading interactions from:  " + sifFile.getAbsolutePath());
            int numLines = FileUtil.getNumLines(sifFile);
            System.out.println(" --> total number of lines:  " + numLines);
            ProgressMonitor.setMaxValue(numLines);
            ImportPathwayCommonsExtSif parser = new ImportPathwayCommonsExtSif(sifFile);
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
