package org.mskcc.cbio.cgds.scripts;

import org.mskcc.cbio.cgds.util.ProgressMonitor;
import org.mskcc.cbio.cgds.util.FileUtil;
import org.mskcc.cbio.cgds.util.ConsoleUtil;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoDrug;
import org.mskcc.cbio.cgds.dao.DaoGeneOptimized;
import org.mskcc.cbio.cgds.model.CanonicalGene;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;

/**
 * Command Line tool to import background drug information.
 */
public class ImportDrugs {
    private ProgressMonitor pMonitor;
    private File file;
    private static final String NA = "N/A";

    public ImportDrugs(File file, ProgressMonitor pMonitor) {
        this.file = file;
        this.pMonitor = pMonitor;
    }

    public void importData() throws IOException, DaoException {
        FileReader reader = new FileReader(file);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine();
        DaoDrug daoDrug = DaoDrug.getInstance();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

        while (line != null) {
            if (pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
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
            System.out.println("command line usage:  importDrugs.pl <XXXX.txt>");
            System.exit(1);
        }
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        File file = new File(args[0]);
        System.out.println("Reading drug data from:  " + file.getAbsolutePath());
        int numLines = FileUtil.getNumLines(file);
        System.out.println(" --> total number of lines:  " + numLines);
        pMonitor.setMaxValue(numLines);
        ImportDrugs parser = new ImportDrugs(file, pMonitor);
        parser.importData();
        ConsoleUtil.showWarnings(pMonitor);
        System.err.println("Done.");
    }
}
