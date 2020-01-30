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
 * Command Line tool to import background drug information.
 */
public class ImportDrugs {
    private File file;
    private static final String NA = "N/A";

    public ImportDrugs(File file) {
        this.file = file;
    }

    public void importData() throws IOException, DaoException {
        FileReader reader = new FileReader(file);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine();
        DaoDrug daoDrug = DaoDrug.getInstance();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

        while (line != null) {
            ProgressMonitor.incrementCurValue();
            ConsoleUtil.showProgress();
            if (!line.startsWith("#") && line.trim().length() > 0) {
                line = line.trim();
                String parts[] = line.split("\t");
                String geneSymbol = parts[0];
                String drugType = parts[1];
                String id = parts[2];

                //  Load up the specified genes from the master table
                CanonicalGene gene = daoGene.getGene(geneSymbol);
            }
            line = buf.readLine();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println(
                "command line usage:  importDrugs.pl <XXXX.txt>"
            );
            return;
        }
        ProgressMonitor.setConsoleMode(true);
        SpringUtil.initDataSource();

        File file = new File(args[0]);
        System.out.println(
            "Reading drug data from:  " + file.getAbsolutePath()
        );
        int numLines = FileUtil.getNumLines(file);
        System.out.println(" --> total number of lines:  " + numLines);
        ProgressMonitor.setMaxValue(numLines);
        ImportDrugs parser = new ImportDrugs(file);
        parser.importData();
        ConsoleUtil.showWarnings();
        System.err.println("Done.");
    }
}
