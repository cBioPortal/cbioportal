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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Command Line Tool to Import Background Gene Data.
 */
public class ImportMicroRNAIDs {
    private ProgressMonitor pMonitor;
    private File geneFile;

    public ImportMicroRNAIDs(File geneFile, ProgressMonitor pMonitor) {
        this.geneFile = geneFile;
        this.pMonitor = pMonitor;
    }

    public void importData() throws IOException, DaoException {
        boolean isBulkLoadOn = MySQLbulkLoader.isBulkLoad();
        MySQLbulkLoader.bulkLoadOff();
        FileReader reader = new FileReader(geneFile);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine(); // skip first line
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        
        int faceEntrezId = -1;
        
        List<CanonicalGene> mirnas = new ArrayList<CanonicalGene>();
        
        while ((line=buf.readLine()) != null) {
            if (pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
            if (!line.startsWith("#")) {
                String parts[] = line.split("\t");
                
                String geneSymbol = parts[2];
                Set<String> aliases = new HashSet<String>();
                aliases.add(parts[0]);
                if (!parts[0].equalsIgnoreCase(parts[1])) {
                    aliases.add(parts[1]);
                }
                
                CanonicalGene mirna = new CanonicalGene(faceEntrezId--, geneSymbol,
                        aliases);
                mirnas.add(mirna);
            }
        }
        
        removePreviousMicroRNARecord(daoGene, mirnas);
        
        for (CanonicalGene mirna : mirnas) {
            daoGene.addGene(mirna);
        }
        
        if (isBulkLoadOn) {
           MySQLbulkLoader.bulkLoadOff();
        }        
    }
    
    /**
     * 
     * @param daoGene
     * @param id
     * @param mirnas 
     */
    private void removePreviousMicroRNARecord(DaoGeneOptimized daoGene, List<CanonicalGene> mirnas) {
        for (CanonicalGene mirna : mirnas) {
            for (String mirnaid : mirna.getAliases()) {
                List<CanonicalGene> pres = new ArrayList<CanonicalGene>(daoGene.guessGene(mirnaid));
                for (CanonicalGene pre : pres) {
                    try {
                        daoGene.deleteGene(pre);
                    } catch (DaoException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
    }


    public static void main(String[] args) throws Exception {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        daoGene.deleteAllRecords();
        if (args.length == 0) {
            System.out.println("command line usage:  importMicroRNAIDs.pl <microrna.txt>");
            System.exit(1);
        }
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        File geneFile = new File(args[0]);
        System.out.println("Reading data from:  " + geneFile.getAbsolutePath());
        int numLines = FileUtil.getNumLines(geneFile);
        System.out.println(" --> total number of lines:  " + numLines);
        pMonitor.setMaxValue(numLines);
        ImportMicroRNAIDs parser = new ImportMicroRNAIDs(geneFile, pMonitor);
        parser.importData();
        ConsoleUtil.showWarnings(pMonitor);
        System.err.println("Done.");
    }
}
