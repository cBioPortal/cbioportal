/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.util.ProgressMonitor;

/**
 * Command Line Tool to Remove a Single Cancer Study.
 */
public class RemoveCancerStudy extends ConsoleRunnable {

    public void run() {
    	try {
	        if (args.length < 1) {
	            // an extra --noprogress option can be given to avoid the messages regarding memory usage and % complete
                throw new UsageException(
                        "removeCancerStudy",
                        null,
                        "<cancer_study_identifier>");
	        }
	        String cancerStudyIdentifier = args[0];

            ProgressMonitor.setCurrentMessage(
                    "Checking if Cancer study with identifier " +
                    cancerStudyIdentifier +
                    " already exists before removing...");
	        if (DaoCancerStudy.doesCancerStudyExistByStableId(cancerStudyIdentifier)) {
	            ProgressMonitor.setCurrentMessage(
	                    "Cancer study with identifier " +
	                    cancerStudyIdentifier +
	                    " found in database, removing...");
	            DaoCancerStudy.deleteCancerStudy(cancerStudyIdentifier);
	        }
	        else {
	            ProgressMonitor.setCurrentMessage(
	                    "Cancer study with identifier " +
	                    cancerStudyIdentifier +
	                    " does not exist the the database, not removing...");
	        }
        } catch (RuntimeException e) {
            throw e;
        } catch (DaoException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Makes an instance to run with the given command line arguments.
     *
     * @param args  the command line arguments to be used
     */
    public RemoveCancerStudy(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args  the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new RemoveCancerStudy(args);
        runner.runInConsole();
    }
}
