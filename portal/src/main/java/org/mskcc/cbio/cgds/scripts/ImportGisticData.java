package org.mskcc.cbio.cgds.scripts;

import java.io.*;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import org.mskcc.cbio.cgds.model.Gistic;
import org.mskcc.cbio.cgds.util.ConsoleUtil;
import org.mskcc.cbio.cgds.util.FileUtil;
import org.mskcc.cbio.cgds.util.GisticReader;
import org.mskcc.cbio.cgds.util.ProgressMonitor;
import org.mskcc.cbio.cgds.validate.ValidateGistic;
import org.mskcc.cbio.cgds.dao.DaoGistic;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.validate.validationException;


/**
 * Command line utility for importing Gistic data from files with names of the form:
 *      amp_genes.conf_99.txt
 *      del_genes.conf_99.txt
 * or,
 *      table_amp.conf_99.txt
 *      table_del.conf_99.txt
 */
public class ImportGisticData {
    private ProgressMonitor pMonitor;
    private File gisticTableFile;
    private File gistic_nonTableFile;
    private File metaDataFile;
//    private static Logger logger = Logger.getLogger(ImportGisticData.class);

    public static boolean parseAmpDel(File gistic_file) throws validationException {

        boolean amp = gistic_file.getName().indexOf("amp") != -1 ? true : false;    // likely to be Amplified ROI
        boolean del = gistic_file.getName().indexOf("del") != -1 ? true : false;    // likely to be Deleted ROI

        return amp ? Gistic.AMPLIFIED : Gistic.DELETED;
    }

    // command line utility
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.printf("command line usage:  importGistic.pl <CancerStudyMetaData.txt> <gistic_nontableFile.txt> <gistic_tableFile.txt>");
            System.exit(1);
        }

        File metadataFile = new File(args[0]);
        File nontableFile = new File(args[1]);
        File tableFile = new File(args[2]);

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        // try and help the user from making a mistake
        if ((nontableFile.getName().indexOf("table") != -1)
                || tableFile.getName().indexOf("table") == -1) {
            boolean i = true;
            while (i) {
                System.out.println("It appears that your non-table file has the word table in it." +
                        "Continue? (y/n)?");
                String yn = stdin.readLine();
                if (yn.equals("y")) {
                    break;
                }
                else if (yn.equals("n")) {
                    break;
                }
            }
                }

        // parse amp/del from filename
        boolean ampDel_table = false;
        boolean ampDel_nontable = false;

        try {
            ampDel_table = parseAmpDel(tableFile);
            ampDel_nontable = parseAmpDel(nontableFile);
            ValidateGistic.validateAmpdels(ampDel_nontable, ampDel_table);
        } catch (validationException e) {
            System.out.println("There was an error in parsing 'amplified' " +
                    "or 'deleted' from the gistic filenames: " + e);
            System.exit(1);
        }

        if (ampDel_nontable != ampDel_table) {
            System.out.println("It appears that your gistic files don't agree " +
                    "on whether they are both amplified or deleted.");
            System.exit(1);
        }

        boolean ampdel = ampDel_table;          // ampDel_table = ampDel_nontable
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(false);

        System.out.println("Reading data from: " + tableFile.getAbsolutePath());
        System.out.println("Reading data from: " + nontableFile.getAbsolutePath());
        System.out.println("Properties: " + metadataFile.getAbsolutePath());

        int numLines_table = FileUtil.getNumLines(tableFile);
        int numLines_nontable = FileUtil.getNumLines(nontableFile);
        System.out.println(" --> total number of lines:  " + numLines_table + "");
        String.format(" --> total number of lines: %d + %d = %d ",
                numLines_table,
                numLines_nontable,
                numLines_nontable + numLines_table);
        pMonitor.setMaxValue(numLines_nontable + numLines_table);

        GisticReader gisticReader = new GisticReader();
        ArrayList<Gistic> gistics = null;
        try {
            int cancerStudyInternalId = gisticReader.getCancerStudyInternalId(metadataFile);
            gistics = gisticReader.mergeAll(cancerStudyInternalId, tableFile, nontableFile, ampdel);
        } catch (Exception e) {     // catch all exceptions
            e.printStackTrace();
            System.out.println(e);
        }

        if (gistics == null) {
            System.out.println("Error in parsing and merging data from 2 files");
            System.exit(1);
        }

        // add to CGDS database
        System.out.println("ImportGisticData");
        for (Gistic g : gistics) {
            try {
                System.out.println(g);
                DaoGistic.addGistic(g);
            } catch (DaoException e) {
                //System.out.println(e);
                //logger.debug(e);
            }
        }

        ConsoleUtil.showWarnings(pMonitor);
    }
}
