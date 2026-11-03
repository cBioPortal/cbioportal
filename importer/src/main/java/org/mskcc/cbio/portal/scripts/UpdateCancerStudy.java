/*
 * Copyright (c) 2016 The Hyve B.V.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.util.ProgressMonitor;

/**
 * Command Line Tool to update the status of a Single Cancer Study.
 */
public class UpdateCancerStudy extends ConsoleRunnable {
    public void run() {
        try {
  		  // check args
          String progName = "updateCancerStudy";
          String argSpec = "<study identifier> <status>";
  	      if (args.length < 2) {
  	         // an extra --noprogress option can be given to avoid the messages regarding memory usage and % complete
             throw new UsageException(
                     progName,
                     null,
                     argSpec);
  	      }

  	      String cancerStudyIdentifier = args[0];
  	      String cancerStudyStatus = args[1];
  	      //validate:
  	      DaoCancerStudy.Status status;
  	      try {
  	    	status = DaoCancerStudy.Status.valueOf(cancerStudyStatus);
  	      }
  	      catch (IllegalArgumentException ia) {
              throw new UsageException(progName, null, argSpec,
                      "Invalid study status parameter: " + cancerStudyStatus);
  	      }

  	      CancerStudy theCancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyIdentifier);
  	      if (theCancerStudy == null) {
  	          throw new IllegalArgumentException("cancer study identified by cancer_study_identifier '"
  	                   + cancerStudyIdentifier + "' not found in dbms or inaccessible to user.");
  	      }
  	      ProgressMonitor.setCurrentMessage("Updating study status to :  '" + status.name() + "' for study: " + cancerStudyIdentifier); 
  	      DaoCancerStudy.setStatus(status, cancerStudyIdentifier);
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
    public UpdateCancerStudy(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args  the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new UpdateCancerStudy(args);
        runner.runInConsole();
    }
}
