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
 * Command Line Tool to Import Sanger Cancer Gene Census Data.
 */
public class ImportSangerCensusData {
    private File censusFile;

    public ImportSangerCensusData(File censusFile) {
        this.censusFile = censusFile;
    }

    public void importData() throws IOException, DaoException {
        MySQLbulkLoader.bulkLoadOn();
        DaoSangerCensus daoCensus = DaoSangerCensus.getInstance();
        daoCensus.deleteAllRecords();
        FileReader reader = new FileReader(censusFile);
        BufferedReader buf = new BufferedReader(reader);
        buf.readLine(); //  Skip Header Line
        String line = buf.readLine();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        while (line != null) {
            ProgressMonitor.incrementCurValue();
            ConsoleUtil.showProgress();
            String parts[] = line.split(",");
            long entrezGeneId = Long.parseLong(parts[2]);
            CanonicalGene gene = daoGene.getGene(entrezGeneId);
            if (gene != null) {
                boolean cancerSomaticMutation = getBoolean(parts, 5);
                boolean cancerGermlineMutation = getBoolean(parts, 6);
                String tumorTypesSomaticMutation = getColumn(parts, 7);
                String tumorTypesGermlineMutation = getColumn(parts, 8);
                String cancerSyndrome = getColumn(parts, 9);
                String tissueType = getColumn(parts, 10);
                String mutationType = getColumn(parts, 12);
                String translocationPartner = getColumn(parts, 13);
                boolean otherGermlineMutation = getBoolean(parts, 14);
                String otherDisease = getColumn(parts, 15);
                daoCensus.addGene(
                    gene,
                    cancerSomaticMutation,
                    cancerGermlineMutation,
                    tumorTypesSomaticMutation,
                    tumorTypesGermlineMutation,
                    cancerSyndrome,
                    tissueType,
                    mutationType,
                    translocationPartner,
                    otherGermlineMutation,
                    otherDisease
                );
            } else {
                ProgressMonitor.setCurrentMessage(
                    "Cannot identify gene:  " + entrezGeneId
                );
            }
            line = buf.readLine();
        }
    }

    private String getColumn(String parts[], int index) {
        try {
            String part = parts[index];
            return part.replaceAll("\"", "");
        } catch (ArrayIndexOutOfBoundsException e) {
            return "";
        }
    }

    private boolean getBoolean(String parts[], int index) {
        String value = getColumn(parts, index);
        if (value != null && value.equalsIgnoreCase("yes")) {
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println(
                "command line usage:  importSangerCensus.pl <sanger.txt>"
            );
            return;
        }
        ProgressMonitor.setConsoleMode(true);

        SpringUtil.initDataSource();

        File geneFile = new File(args[0]);
        System.out.println("Reading data from:  " + geneFile.getAbsolutePath());
        int numLines = FileUtil.getNumLines(geneFile);
        System.out.println(" --> total number of lines:  " + numLines);
        ProgressMonitor.setMaxValue(numLines);
        ImportSangerCensusData parser = new ImportSangerCensusData(geneFile);
        parser.importData();
        ConsoleUtil.showWarnings();
        System.err.println("Done.");
    }
}
