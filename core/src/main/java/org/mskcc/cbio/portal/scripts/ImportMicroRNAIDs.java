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
 * Command Line Tool to Import Background Gene Data.
 */
public class ImportMicroRNAIDs {

    public static void importData(File geneFile)
        throws IOException, DaoException {
        MySQLbulkLoader.bulkLoadOff();
        FileReader reader = new FileReader(geneFile);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine(); // skip first line
        SpringUtil.initDataSource();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

        List<CanonicalGene> mirnas = new ArrayList<CanonicalGene>();

        while ((line = buf.readLine()) != null) {
            ProgressMonitor.incrementCurValue();
            ConsoleUtil.showProgress();
            if (!line.startsWith("#")) {
                String parts[] = line.split("\t");

                String geneSymbol = parts[2];

                Set<String> aliases = new HashSet<String>();
                setAliases(parts[0], aliases);

                if (!parts[0].equalsIgnoreCase(parts[1])) {
                    setAliases(parts[1], aliases);
                }

                CanonicalGene mirna = new CanonicalGene(geneSymbol, aliases);
                mirna.setType(CanonicalGene.MIRNA_TYPE);
                mirnas.add(mirna);
            }
        }

        for (CanonicalGene mirna : mirnas) {
            daoGene.addGene(mirna);
        }
    }

    private static void setAliases(String hsa, Set<String> aliases) {
        aliases.add(hsa);
        if (hsa.startsWith("hsa-")) {
            String mir = hsa.substring(4).toUpperCase();
            aliases.add(mir);
            aliases.add(getHUGOInNCBIFile(mir));
        }
    }

    private static String getHUGOInNCBIFile(String mir) {
        StringBuilder sb = new StringBuilder();
        sb.append("MIR");
        if (mir.startsWith("LET")) {
            sb.append("LET");
        }

        int ix = mir.indexOf("-");
        sb.append(mir.substring(ix + 1));
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        System.err.println("This script will be called from ImportGeneData");
        //        if (args.length == 0) {
        //            System.out.println("command line usage:  importMicroRNAIDs.pl <microrna.txt>");
        //            return;
        //        }
        //        ProgressMonitor.setConsoleMode(true);
        //
        //        File geneFile = new File(args[0]);
        //        System.out.println("Reading data from:  " + geneFile.getAbsolutePath());
        //        int numLines = FileUtil.getNumLines(geneFile);
        //        System.out.println(" --> total number of lines:  " + numLines);
        //        ProgressMonitor.setMaxValue(numLines);
        //        ImportMicroRNAIDs.importData(geneFile);
        //        ConsoleUtil.showWarnings();
        //        System.err.println("Done.");
    }
}
