/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.scripts;

import java.io.*;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import org.mskcc.cbio.portal.model.Gistic;
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.validate.validationException;


/**
 * Command line utility for importing Gistic data from files with names of the form:
 *      amp_genes.conf_99.txt
 *      del_genes.conf_99.txt
 * or,
 *      table_amp.conf_99.txt
 *      table_del.conf_99.txt
 */
public class ImportGisticData {
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
                    "\t <gistic-data-file.txt> Note that gistic-data-file.txt must be a massaged file, it does not come straight from the Broad\n" +
                    "\t <cancer-study-id> e.g. 'tcga_gbm'");
            return;
        }
		SpringUtil.initDataSource();
        GisticReader gisticReader = new GisticReader();

        File gistic_f = new File(args[0]);
        int cancerStudyInternalId = gisticReader.getCancerStudyInternalId(args[1]);

        ProgressMonitor.setConsoleMode(false);

        System.out.println("Reading data from: " + gistic_f.getAbsolutePath());
        System.out.println("CancerStudyId: " + cancerStudyInternalId);

        int lines = FileUtil.getNumLines(gistic_f);
        System.out.println(" --> total number of lines: " + lines);
        ProgressMonitor.setMaxValue(lines);

        ArrayList<Gistic> gistics = null;

        gistics = gisticReader.parse(gistic_f, cancerStudyInternalId);

        if (gistics == null) {
            System.out.println("Error: didn't get any data");
            return;
        }

        // add to CGDS database
        for (Gistic g : gistics) {
            try {
                DaoGistic.addGistic(g);
            } catch (validationException e) {
                // only catching validationException, not DaoException
                logger.debug(e);
            } catch (DaoException e) {
                System.err.println(e);
            }
        }
        ConsoleUtil.showWarnings();
    }
}
