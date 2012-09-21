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
    private static Logger logger = Logger.getLogger(ImportGisticData.class);

    public static boolean parseAmpDel(File gistic_file) throws validationException {

        boolean amp = gistic_file.getName().indexOf("amp") != -1 ? true : false;    // likely to be Amplified ROI
        boolean del = gistic_file.getName().indexOf("del") != -1 ? true : false;    // likely to be Deleted ROI

        return amp ? Gistic.AMPLIFIED : Gistic.DELETED;
    }

    // command line utility
    public static void main(String[] args) throws IOException, DaoException {

        if (args.length != 2) {
            System.out.printf("command line usage:  importGistic.pl <gistic-data-file.txt> <cancer-study-id>\n" +
                    "\tNote that gistic-data-file.txt must be a massaged file, it does not come straight from the Broad\n" +
                    "\tcancer-study-id e.g. 'tcga-gbm'");
            System.exit(1);
        }

        GisticReader gisticReader = new GisticReader();

        File gistic_f = new File(args[0]);
        int cancerStudyInternalId = gisticReader.getCancerStudyInternalId(args[1]);

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(false);

        System.out.println("Reading data from: " + gistic_f.getAbsolutePath());
        System.out.println("CancerStudyId: " + cancerStudyInternalId);

        int lines = FileUtil.getNumLines(gistic_f);
        System.out.println(" --> total number of lines: " + lines);
        pMonitor.setMaxValue(lines);

        ArrayList<Gistic> gistics = null;

        gistics = gisticReader.parse(gistic_f, cancerStudyInternalId);

        if (gistics == null) {
            System.out.println("Error: didn't get any data");
            System.exit(1);
        }

        // add to CGDS database
        for (Gistic g : gistics) {
            try {
                DaoGistic.addGistic(g);
            } catch (DaoException e) {
                logger.debug(e);
            }
        }

        ConsoleUtil.showWarnings(pMonitor);
    }
}
