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

import org.mskcc.cbio.portal.util.*;

import java.io.File;

/**
 * ImportMutSig is used to import the Broad Institute's MutSig data into our CGDS SQL database.
 * Command line users must specify a MutSig file, and properties file containing a CancerID (InternalId)
 *
 * @author Lennart Bastian, Gideon Dresdner
 */

public class ImportMutSigData {
    private ProgressMonitor pMonitor;
    private File mutSigFile;
    private File metaDataFile;

    // command line utility
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("command line usage:  importMutSig.pl <Mutsig_file.txt> <cancer-study-identifier>");
            return;
        }

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(false);
		SpringUtil.initDataSource();

        File mutSigFile = new File(args[0]);
        System.out.println("Reading data from: " + mutSigFile.getAbsolutePath());
        int numLines = FileUtil.getNumLines(mutSigFile);
        System.out.println(" --> total number of lines:  " + numLines);
        pMonitor.setMaxValue(numLines);

        int internalId = MutSigReader.getInternalId(args[1]);
        MutSigReader.loadMutSig(internalId, mutSigFile, pMonitor);

        ConsoleUtil.showWarnings(pMonitor);
    }
}
