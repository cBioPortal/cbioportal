/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.service.EntityService;

import java.io.File;

/**
 * Command Line Tool to Import a Single Cancer Study.
 */
public class ImportCancerStudy {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("command line usage: importCancerStudy.pl <cancer_study.txt>");
            return;
        }

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        File file = new File(args[0]);
        CancerStudy cancerStudy = CancerStudyReader.loadCancerStudy(file);
        ImportDataUtil.entityService.insertCancerStudyEntity(cancerStudy.getCancerStudyStableId());
        System.out.println ("Loaded the following cancer study:  ");
        System.out.println ("ID:  " + cancerStudy.getInternalId());
        System.out.println ("Name:  " + cancerStudy.getName());
        System.out.println ("Description:  " + cancerStudy.getDescription());
        ConsoleUtil.showWarnings(pMonitor);
        System.err.println("Done.");
    }
}