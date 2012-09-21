/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.cgds.scripts;

import org.mskcc.cbio.cgds.model.Gistic;
import org.mskcc.cbio.cgds.util.ConsoleUtil;
import org.mskcc.cbio.cgds.util.FileUtil;
import org.mskcc.cbio.cgds.util.GisticReader;
import org.mskcc.cbio.cgds.util.ProgressMonitor;
import org.mskcc.cbio.cgds.validate.ValidateGistic;
import org.mskcc.cbio.cgds.validate.validationException;

import java.io.*;

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

    public static boolean parseAmpDel(File gistic_file) throws validationException {

        boolean amp = gistic_file.getName().indexOf("amp") != -1 ? true : false;    // likely to be Amplified ROI
        boolean del = gistic_file.getName().indexOf("del") != -1 ? true : false;    // likely to be Deleted ROI

        return amp ? Gistic.AMPLIFIED : Gistic.DELETED;
    }

    // command line utility
    public static void main(String[] args) throws Exception {
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
        
        ampDel_table = parseAmpDel(tableFile);
        ampDel_nontable = parseAmpDel(nontableFile);
        try {
//            System.out.println(ampDel_nontable);
//            System.out.println(ampDel_table);
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
        int internalId = gisticReader.getCancerStudyInternalId(metadataFile);

        gisticReader.loadGistic(internalId, tableFile, nontableFile, ampDel_table);

        ConsoleUtil.showWarnings(pMonitor);
    }
}
