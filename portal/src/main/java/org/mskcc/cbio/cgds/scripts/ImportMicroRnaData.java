package org.mskcc.cgds.scripts;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoMicroRna;
import org.mskcc.cgds.dao.MySQLbulkLoader;
import org.mskcc.cgds.util.ConsoleUtil;
import org.mskcc.cgds.util.FileUtil;
import org.mskcc.cgds.util.ProgressMonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Command Line Tool to Import MicroRNA Data.
 */
public class ImportMicroRnaData {
    private ProgressMonitor pMonitor;
    private File geneFile;

    public ImportMicroRnaData(File geneFile, ProgressMonitor pMonitor) {
        this.geneFile = geneFile;
        this.pMonitor = pMonitor;
    }

    public void importData() throws IOException, DaoException {
        FileReader reader = new FileReader(geneFile);
        BufferedReader buf = new BufferedReader(reader);

        String line = buf.readLine();   //  Skip header
        line = buf.readLine();
        DaoMicroRna daoMicroRna = new DaoMicroRna();
        while (line != null) {
            if (pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
            if (!line.startsWith("#")) {
                String parts[] = line.split("\t");
                String id = parts[0];
                String variantId = parts[1];
                daoMicroRna.addMicroRna(id, variantId);
            }
            line = buf.readLine();
        }
        if (MySQLbulkLoader.isBulkLoad()) {
           daoMicroRna.flushMicroRna();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("command line usage:  importMicroRna.pl <micro_rna.txt>");
            System.exit(1);
        }
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        File microRnaFile = new File(args[0]);
        System.out.println("Reading data from:  " + microRnaFile.getAbsolutePath());
        int numLines = FileUtil.getNumLines(microRnaFile);
        System.out.println(" --> total number of lines:  " + numLines);
        pMonitor.setMaxValue(numLines);
        ImportMicroRnaData parser = new ImportMicroRnaData(microRnaFile, pMonitor);
        parser.importData();
        ConsoleUtil.showWarnings(pMonitor);
        System.err.println("Done.");
    }
}