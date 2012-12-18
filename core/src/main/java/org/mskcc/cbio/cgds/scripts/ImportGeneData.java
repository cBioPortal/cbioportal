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

import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneOptimized;
import org.mskcc.cbio.cgds.dao.MySQLbulkLoader;
import org.mskcc.cbio.cgds.model.CanonicalGene;
import org.mskcc.cbio.cgds.util.ConsoleUtil;
import org.mskcc.cbio.cgds.util.FileUtil;
import org.mskcc.cbio.cgds.util.ProgressMonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Command Line Tool to Import Background Gene Data.
 */
public class ImportGeneData {
    private ProgressMonitor pMonitor;
    private File geneFile;

    public ImportGeneData(File geneFile, ProgressMonitor pMonitor) {
        this.geneFile = geneFile;
        this.pMonitor = pMonitor;
    }

    public void importData() throws IOException, DaoException {
        MySQLbulkLoader.bulkLoadOn();
        FileReader reader = new FileReader(geneFile);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        while (line != null) {
            if (pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
            if (!line.startsWith("#")) {
                String parts[] = line.split("\t");
                int entrezGeneId = Integer.parseInt(parts[1]);
                String geneSymbol = parts[2];
                Set<String> aliases;
                if (parts[3].equals("-")) {
                    aliases = Collections.emptySet();
                } else {
                    aliases = new HashSet<String>(Arrays.asList(parts[3].split("\\|")));
                }
                
                CanonicalGene gene = new CanonicalGene(entrezGeneId, geneSymbol,
                        aliases);
                daoGene.addGene(gene);
            }
            line = buf.readLine();
        }
        if (MySQLbulkLoader.isBulkLoad()) {
           daoGene.flushGenesToDatabase();
        }        
    }


    public static void main(String[] args) throws Exception {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        daoGene.deleteAllRecords();
        if (args.length == 0) {
            System.out.println("command line usage:  importGenes.pl <ncbi_genes.txt>");
            System.exit(1);
        }
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        File geneFile = new File(args[0]);
        System.out.println("Reading data from:  " + geneFile.getAbsolutePath());
        int numLines = FileUtil.getNumLines(geneFile);
        System.out.println(" --> total number of lines:  " + numLines);
        pMonitor.setMaxValue(numLines);
        ImportGeneData parser = new ImportGeneData(geneFile, pMonitor);
        parser.importData();
        ConsoleUtil.showWarnings(pMonitor);
        System.err.println("Done.");
    }
}
