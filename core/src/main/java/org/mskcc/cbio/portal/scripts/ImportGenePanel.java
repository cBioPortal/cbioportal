/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.FileUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.model.GenePanelData;
import org.mskcc.cbio.portal.model.GenePanelListData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.BitSet;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGenePanel;

/**
 *
 * @author dongli
 */
public class ImportGenePanel {        
    public static void importData(ProgressMonitor pMonitor, File geneFile) throws IOException, DaoException {
        MySQLbulkLoader.bulkLoadOn();
        FileReader reader = new FileReader(geneFile);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine();

        String studyId="empty";
        String stableId = "empty";
        String decription = "empty";
        while (!line.startsWith("gene_list")) {
            if (pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
            if (line.startsWith("cancer_study")) {
                    String parts[] = line.split(":");
                    studyId = parts[1];
            }
            
            if (line.startsWith("stable_id")) {
                    String parts[] = line.split(":");
                    stableId = parts[1];
                }
            
            if (line.startsWith("gene_panel")) {
                    String parts[] = line.split(":");
                    decription = parts[1];
                }
            
            line = buf.readLine();
        }
        String Parts[] = line.split(":");
        String panelList[] = Parts[1].split("\t");
        int maxListID = DaoGenePanel.getMaxListId();
        if(studyId != "empty" || stableId != "empty" || decription != "empty")
        {
            GenePanelData genepenal = new GenePanelData(maxListID+1,stableId,studyId,decription);
            DaoGenePanel.addGenePanel(genepenal);
        }
        
        long geneId;
        
        if(panelList.length > 0)     
        {
            DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
            for(int i=0; i<panelList.length;i++)
            {
                geneId = daoGene.getNonAmbiguousGene(panelList[i].replaceAll("\\s+","")).getEntrezGeneId();
                GenePanelListData genepenallist = new GenePanelListData(maxListID+1,geneId);
                DaoGenePanel.addGenePanelList(genepenallist);
            }
        }
        reader.close();
        if (MySQLbulkLoader.isBulkLoad()) {
           MySQLbulkLoader.flushAll();
        }        
    }
        
        public static void main(String[] args) throws Exception {
            DaoGenePanel.deleteAllGenePanelRecords();
//            if (args.length == 0) {
//                System.out.println("command line usage:  importGenes.pl <ncbi_genes.txt> <supp-genes.txt> <microrna.txt> <all_exon_loci.bed>");
//                return;
//            }
            ProgressMonitor pMonitor = new ProgressMonitor();
            pMonitor.setConsoleMode(true);

            String filenames[] = {"im3_gene_panel.txt","im5_gene_panel.txt"};

            for(int i=0; i<filenames.length;i++)
            {
                File geneFile = new File(filenames[i]);
                System.out.println("Reading gene data from:  " + geneFile.getAbsolutePath());
                int numLines = FileUtil.getNumLines(geneFile);
                System.out.println(" --> total number of lines:  " + numLines);
                pMonitor.setMaxValue(numLines);
                ImportGenePanel.importData(pMonitor, geneFile);
                ConsoleUtil.showWarnings(pMonitor);
                System.err.println("Done.");
            }
    }
}
