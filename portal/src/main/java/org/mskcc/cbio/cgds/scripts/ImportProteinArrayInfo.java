
package org.mskcc.cgds.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.DaoProteinArrayInfo;
import org.mskcc.cgds.dao.DaoProteinArrayTarget;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.ProteinArrayInfo;
import org.mskcc.cgds.util.ConsoleUtil;
import org.mskcc.cgds.util.FileUtil;
import org.mskcc.cgds.util.ProgressMonitor;

/**
 * Import protein array antibody information into database.
 * @author jj
 */
public class ImportProteinArrayInfo {
    private ProgressMonitor pMonitor;
    private File arrayInfoFile;
    private boolean overwrite = false;
    
    public ImportProteinArrayInfo(File arrayInfoFile, boolean overwrite, ProgressMonitor pMonitor) {
        this.arrayInfoFile = arrayInfoFile;
        this.pMonitor = pMonitor;
        this.overwrite = overwrite;
    }
    
    /**
     * Import protein array antibody information. Antibodies that already exist 
     * in the database (based on array id) will be skipped.
     * @throws IOException
     * @throws DaoException 
     */
    public void importData() throws IOException, DaoException {
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

            String type = strs[4];
            String source = null;
            String symbols = strs[2];
            String position = strs[3];
            boolean validated = true;
            
            for (String arrayId : strs[0].split("/")) {
                if (daoPAI.getProteinArrayInfo(arrayId)!=null) {
                    if (overwrite) {
                        daoPAI.deleteProteinArrayInfo(arrayId);
                        daoPAT.deleteProteinArrayTarget(arrayId);
                    } else {
                        continue;
                    }
                }
                ProteinArrayInfo pai = new ProteinArrayInfo(arrayId, type, source, 
                        symbols, position, validated, null);

                daoPAI.addProteinArrayInfo(pai);

                for (String symbol : symbols.split("/")) {
                    CanonicalGene gene = daoGene.getNonAmbiguousGene(symbol);
                    if (gene==null) {
                        System.err.println(symbol+" not exist");
                        continue;
                    }

                    long entrez = gene.getEntrezGeneId();
                    daoPAT.addProteinArrayTarget(arrayId, entrez);
                }
            }
                
            if (type.equalsIgnoreCase("phosphorylation")) {
                importPhosphoGene(strs[1], strs[2], strs[0]);
            }
            
        }
    }
    
    private void importPhosphoGene(String phosphoSymbol,
            String geneSymbols, String arrayIds) throws DaoException {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        CanonicalGene existingGene = daoGene.getGene(phosphoSymbol);
        if (existingGene!=null) {
            if (overwrite) {
                daoGene.deleteGene(existingGene);
            } else {
                System.err.println(phosphoSymbol+" exists.");
                return;
            }
        }

        Set<String> aliases = new HashSet<String>();
        aliases.add("phosphoprotein");
        for (String gene : geneSymbols.split("/")) {
            aliases.add("phospho"+gene);
        }
        
        for (String arrayId : arrayIds.split("/")) {
            aliases.add(arrayId);
        }

        CanonicalGene phosphoGene = new CanonicalGene(phosphoSymbol, aliases);
        daoGene.addGene(phosphoGene);
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("command line usage:  importProteinArrayInfo.pl <RPPT_antibody_list.txt>");
            System.exit(1);
        }
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);
        
        //int cancerStudyId = DaoCancerStudy.getCancerStudyByStableId(args[1]).getInternalId();

        boolean overwrite = false;
        if (args.length>1) {
            overwrite = args[1].equalsIgnoreCase("overwrite");
        }
        
        File file = new File(args[0]);
        System.out.println("Reading data from:  " + file.getAbsolutePath());
        int numLines = FileUtil.getNumLines(file);
        System.out.println(" --> total number of lines:  " + numLines);
        pMonitor.setMaxValue(numLines);
        ImportProteinArrayInfo parser = new ImportProteinArrayInfo(file, overwrite, pMonitor);
        parser.importData();
        ConsoleUtil.showWarnings(pMonitor);
        System.err.println("Done.");
    }
}
