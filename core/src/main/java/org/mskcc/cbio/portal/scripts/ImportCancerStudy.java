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
import org.mskcc.cbio.portal.model.*;

import java.io.File;

/**
 * Command Line Tool to Import a Single Cancer Study.
 */
public class ImportCancerStudy {

    public static void main(String[] args) throws Exception {
    	try {
	        if (args.length < 1) {
	            System.out.println("command line usage: importCancerStudy.pl <cancer_study.txt>");
	            // an extra --noprogress option can be given to avoid the messages regarding memory usage and % complete
	            //use 2 for command line syntax errors:
	            System.exit(2);
	        }
	
	        ProgressMonitor.setConsoleModeAndParseShowProgress(args);
	
	        File file = new File(args[0]);
			SpringUtil.initDataSource();
	        CancerStudy cancerStudy = CancerStudyReader.loadCancerStudy(file);
	        System.out.println ("Loaded the following cancer study:  ");
	        System.out.println (" --> Study ID:  " + cancerStudy.getInternalId());
	        System.out.println (" --> Name:  " + cancerStudy.getName());
	        System.out.println (" --> Description:  " + cancerStudy.getDescription());
	        ConsoleUtil.showMessages();
	        System.out.println("Done.");
    	}
    	catch (Exception e) {
	        ConsoleUtil.showWarnings();
	        //exit with error status:
	        System.err.println ("\nABORTED! Error:  " + e.getMessage());
	        if (e.getMessage() == null)
	        	e.printStackTrace();
	        System.exit(1);
        }
    }
}
