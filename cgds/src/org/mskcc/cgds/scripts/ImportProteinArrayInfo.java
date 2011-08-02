
package org.mskcc.cgds.scripts;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.DaoProteinArrayInfo;
import org.mskcc.cgds.dao.DaoProteinArrayTarget;
import org.mskcc.cgds.dao.MySQLbulkLoader;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.ProteinArrayInfo;
import org.mskcc.cgds.model.ProteinArrayTarget;
import org.mskcc.cgds.util.ConsoleUtil;
import org.mskcc.cgds.util.FileUtil;
import org.mskcc.cgds.util.ProgressMonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author jj
 */
public class ImportProteinArrayInfo {
    private ProgressMonitor pMonitor;
    private File arrayInfoFile;
    
    public ImportProteinArrayInfo(File arrayInfoFile, ProgressMonitor pMonitor) {
        this.arrayInfoFile = arrayInfoFile;
        this.pMonitor = pMonitor;
    }
    
    public void importData() throws IOException, DaoException {
        MySQLbulkLoader.bulkLoadOff();
        DaoProteinArrayInfo daoPAI = DaoProteinArrayInfo.getInstance();
        DaoProteinArrayTarget daoPAT = DaoProteinArrayTarget.getInstance();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        
        FileReader reader = new FileReader(arrayInfoFile);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine(); // skip header line
        while ((line=buf.readLine()) != null) {
            if (pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
            
            String[] strs = line.split("\t");
            String arrayId = strs[6];
            String type = strs[7];
            String source = strs[5];
            boolean validated = strs[4].equals("(C)");
            ProteinArrayInfo pai = new ProteinArrayInfo(arrayId, type, source, validated);
            daoPAI.addProteinArrayInfo(pai);
            
            String position = strs[3];
            for (String symbol : strs[2].split("/")) {
                CanonicalGene gene = daoGene.getGene(symbol);
                if (gene==null) {
                    System.err.println(symbol+" not exist");
                    continue;
                }
                    
                long entrez = gene.getEntrezGeneId();
                ProteinArrayTarget pat = new ProteinArrayTarget(arrayId, entrez, position);
                daoPAT.addProteinArrayTarget(pat);
            }
            
        }
        if (MySQLbulkLoader.isBulkLoad()) {
            //daoMutSig.flushGenesToDatabase();
        }
    }
    
    public static void main(String[] args) throws Exception {
        DaoProteinArrayInfo daoPAI = DaoProteinArrayInfo.getInstance();
        DaoProteinArrayTarget daoPAT = DaoProteinArrayTarget.getInstance();
        daoPAI.deleteAllRecords();
        daoPAT.deleteAllRecords();
        if (args.length == 0) {
            System.out.println("command line usage:  importProteinArrayInfo.pl <RPPT_antibody_list.txt>");
            System.exit(1);
        }
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        File file = new File(args[0]);
        System.out.println("Reading data from:  " + file.getAbsolutePath());
        int numLines = FileUtil.getNumLines(file);
        System.out.println(" --> total number of lines:  " + numLines);
        pMonitor.setMaxValue(numLines);
        ImportProteinArrayInfo parser = new ImportProteinArrayInfo(file, pMonitor);
        parser.importData();
        ConsoleUtil.showWarnings(pMonitor);
        System.err.println("Done.");
    }
}
