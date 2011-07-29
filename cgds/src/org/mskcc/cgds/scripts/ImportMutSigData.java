package org.mskcc.cgds.scripts;

/**
 * Created by IntelliJ IDEA.
 * User: lennartbastian
 * Date: 22/07/2011
 * Time: 11:15
 * To change this template use File | Settings | File Templates.
 */

import org.mskcc.cgds.dao.*;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.MutSig;
import org.mskcc.cgds.util.ConsoleUtil;
import org.mskcc.cgds.util.FileUtil;
import org.mskcc.cgds.util.ProgressMonitor;

import java.util.*;

import java.io.*;


public class ImportMutSigData {
    private ProgressMonitor pMonitor;
    private File mutSigFile;
    private File metaDataFile;

    public ImportMutSigData(File mutSigFile, File metaDataFile, ProgressMonitor pMonitor) {
        this.mutSigFile = mutSigFile;
        this.pMonitor = pMonitor;
        this.metaDataFile = metaDataFile;
    }

    public void importData() throws IOException, DaoException {
        //MySQLbulkLoader.bulkLoadOn();
        FileReader reader = new FileReader(mutSigFile);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine();
        System.err.println("Importing Data");
        int cancerType = loadProps(metaDataFile);
        //DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        while (line != null) {

            if (pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
            DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
            if (!line.startsWith("rank"))
                if (!line.startsWith("#")) {
                    String parts[] = line.split("\t");
                    int rank = Integer.parseInt(parts[0]);
                    String hugoGeneSymbol = parts[1];
                    int N = Integer.parseInt(parts[2]);
                    int n = Integer.parseInt(parts[3]);
                    int nVal = Integer.parseInt(parts[4]);
                    int nVer = Integer.parseInt(parts[5]);
                    int CpG = Integer.parseInt(parts[6]);
                    int CandG = Integer.parseInt(parts[7]);
                    int AandT = Integer.parseInt(parts[8]);
                    int Indel = Integer.parseInt(parts[9]);
                    String pValue = parts[10];
                    String qValue = parts[11];
                    CanonicalGene gene = daoGene.getGene(hugoGeneSymbol);
                    MutSig mutSig = new MutSig(cancerType, gene, rank, N, n, nVal, nVer, CpG, CandG, AandT, Indel, pValue, qValue);
                    DaoMutSig.addMutSig(mutSig);
                }
            line = buf.readLine();
        }
        /*
        if (MySQLbulkLoader.isBulkLoad()) {
            //daoMutSig.flushGenesToDatabase();
        }
        */
    }

    public static void main(String[] args) throws Exception {
        //DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        //daoGene.deleteAllRecords();
        if (args.length < 2) {
            System.out.println("command line usage:  importMutSig.pl <ncbi_genes.txt> <MetaProperties.txt>");
            System.exit(1);
        }
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        File mutSigFile = new File(args[0]);
        File propertiesFile = new File(args[1]);
        System.out.println("Reading data from:  " + mutSigFile.getAbsolutePath());
        int numLines = FileUtil.getNumLines(mutSigFile);
        System.out.println(" --> total number of lines:  " + numLines);
        pMonitor.setMaxValue(numLines);
        ImportMutSigData parser = new ImportMutSigData(mutSigFile, propertiesFile, pMonitor);
        parser.importData();
        ConsoleUtil.showWarnings(pMonitor);
        System.err.println("Done.");
    }

    public int loadProps(File file) throws IOException, DaoException {
        Properties props = new Properties();
        props.load(new FileInputStream(file));
        String cancer_study_identifier;
        cancer_study_identifier = props.getProperty("cancer_study_identifier");
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancer_study_identifier);
        int cancerStudyID = cancerStudy.getStudyId();
        return cancerStudyID;
    }


}


