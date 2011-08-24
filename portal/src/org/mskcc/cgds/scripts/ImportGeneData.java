package org.mskcc.cgds.scripts;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.MySQLbulkLoader;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.util.ConsoleUtil;
import org.mskcc.cgds.util.FileUtil;
import org.mskcc.cgds.util.ProgressMonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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
                CanonicalGene gene = new CanonicalGene(entrezGeneId, geneSymbol);
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
