
package org.mskcc.cgds.scripts;

import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.DaoProteinArrayInfo;
import org.mskcc.cgds.dao.DaoProteinArrayTarget;
import org.mskcc.cgds.dao.MySQLbulkLoader;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.ProteinArrayInfo;
import org.mskcc.cgds.util.ConsoleUtil;
import org.mskcc.cgds.util.FileUtil;
import org.mskcc.cgds.util.ProgressMonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.Collections;

/**
 * Import protein array antibody information into database.
 * @author jj
 */
public class ImportProteinArrayInfo {
    private ProgressMonitor pMonitor;
    private File arrayInfoFile;
    
    public ImportProteinArrayInfo(File arrayInfoFile, ProgressMonitor pMonitor) {
        this.arrayInfoFile = arrayInfoFile;
        this.pMonitor = pMonitor;
    }
    
    /**
     * Import protein array antibody information. Antibodies that already exist 
     * in the database (based on array id) will be skipped.
     * @throws IOException
     * @throws DaoException 
     */
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
            if (strs.length<5) {
                System.err.println("wrong format: "+line);
            }
            
            for (String arrayId : strs[0].split("/")) {
                if (daoPAI.getProteinArrayInfo(arrayId)!=null) {
                    continue;
                }

                String type = strs[4];
                String source = null;
                String symbols = strs[2];
                String position = strs[3];
                boolean validated = true;
                ProteinArrayInfo pai = new ProteinArrayInfo(arrayId, type, source, 
                        symbols, position, validated, null);

                daoPAI.addProteinArrayInfo(pai);

                for (String symbol : symbols.split("/")) {
                    CanonicalGene gene = daoGene.getGene(symbol);
                    if (gene==null) {
                        System.err.println(symbol+" not exist");
                        continue;
                    }

                    long entrez = gene.getEntrezGeneId();
                    daoPAT.addProteinArrayTarget(arrayId, entrez);
                }
            }
            
        }
        if (MySQLbulkLoader.isBulkLoad()) {
            //daoMutSig.flushGenesToDatabase();
        }
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("command line usage:  importProteinArrayInfo.pl <RPPT_antibody_list.txt>");
            System.exit(1);
        }
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);
        
        //int cancerStudyId = DaoCancerStudy.getCancerStudyByStableId(args[1]).getInternalId();

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
