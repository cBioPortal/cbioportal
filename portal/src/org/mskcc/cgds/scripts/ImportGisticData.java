package org.mskcc.cgds.scripts;

import org.mskcc.cgds.util.ConsoleUtil;
import org.mskcc.cgds.util.FileUtil;
import org.mskcc.cgds.util.GisticReader;
import org.mskcc.cgds.util.ProgressMonitor;

import java.io.File;

/**
 * Command line utility for importing Gistic data from files with names of the form:
 *      amp_genes.conf_99.txt
 *      del_genes.conf_99.txt
 */
public class ImportGisticData {
    private ProgressMonitor pMonitor;
    private File mutSigFile;
    private File metaDataFile;

    // command line utility
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.printf("command line usage:  importGistic.pl <gisticFile.txt> <CancerStudyMetaData.txt>\n " +
                    "where gisticFile.txt is of the form <amp_genes.conf_99.txt> or <del_genes.conf_99.txt>");
            System.exit(1);
        }

        // parse table from filename

        // parse amp/del from filename
        
        boolean ampDel = false;

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(false);

        File gisticFile = new File(args[0]);
        File cancerStudyMetaData = new File(args[1]);

        System.out.println("Reading data from: " + gisticFile.getAbsolutePath());
        System.out.println("Properties: " + cancerStudyMetaData.getAbsolutePath());

        int numLines = FileUtil.getNumLines(gisticFile);
        System.out.println(" --> total number of lines:  " + numLines);
        pMonitor.setMaxValue(numLines);

        GisticReader gisticReader = new GisticReader();
        int internalId = gisticReader.getCancerStudyInternalId(cancerStudyMetaData);
//        GisticReader.loadGistic(internalId, ampDel, gisticFile, pMonitor);

        ConsoleUtil.showWarnings(pMonitor);
    }
}
