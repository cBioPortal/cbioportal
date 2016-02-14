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

import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoMicroRna;
import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.FileUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Command Line Tool to Import MicroRNA Data.
 */
public class ImportMicroRnaData {
    private File geneFile;

    public ImportMicroRnaData(File geneFile) {
        this.geneFile = geneFile;
    }

    public void importData() throws IOException, DaoException {
        FileReader reader = new FileReader(geneFile);
        BufferedReader buf = new BufferedReader(reader);

        String line = buf.readLine();   //  Skip header
        line = buf.readLine();
        DaoMicroRna daoMicroRna = new DaoMicroRna();
        while (line != null) {
            ProgressMonitor.incrementCurValue();
            ConsoleUtil.showProgress();
            if (!line.startsWith("#")) {
                String parts[] = line.split("\t");
                String id = parts[0];
                String variantId = parts[1];
                daoMicroRna.addMicroRna(id, variantId);
            }
            line = buf.readLine();
        }
        if (MySQLbulkLoader.isBulkLoad()) {
           MySQLbulkLoader.flushAll();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("command line usage:  importMicroRna.pl <micro_rna.txt>");
            return;
        }
        ProgressMonitor.setConsoleMode(true);

        File microRnaFile = new File(args[0]);
        System.out.println("Reading data from:  " + microRnaFile.getAbsolutePath());
        int numLines = FileUtil.getNumLines(microRnaFile);
        System.out.println(" --> total number of lines:  " + numLines);
        ProgressMonitor.setMaxValue(numLines);
        ImportMicroRnaData parser = new ImportMicroRnaData(microRnaFile);
        parser.importData();
        ConsoleUtil.showWarnings();
        System.err.println("Done.");
    }
}