/*
 * Copyright (c) 2016 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
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
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.model.*;


/**
 * Command Line Tool to update the status of a Single Cancer Study.
 */
public class UpdateCancerStudy {

    public static void main(String[] args) throws Exception {

        try {
  		  // check args
  	      if (args.length < 2) {
  	         System.out.println("command line usage:  updateCancerStudy " + "<study identifier> <status>");
  	         // an extra --noprogress option can be given to avoid the messages regarding memory usage and % complete
  	         //use 2 for command line syntax errors:
  	         System.exit(2);
  	      }
  	      ProgressMonitor.setConsoleModeAndParseShowProgress(args);
  	      
  	      String cancerStudyIdentifier = args[0];
  	      String cancerStudyStatus = args[1];
  	      //validate:
  	      DaoCancerStudy.Status status;
  	      try {
  	    	status = DaoCancerStudy.Status.valueOf(cancerStudyStatus);
  	      }
  	      catch (IllegalArgumentException ia) {
  	    	  throw new IllegalArgumentException("Invalid status parameter: " + cancerStudyStatus, ia);
  	      }

  	 	  SpringUtil.initDataSource();
  	      CancerStudy theCancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyIdentifier);
  	      if (theCancerStudy == null) {
  	          throw new IllegalArgumentException("cancer study identified by cancer_study_identifier '"
  	                   + cancerStudyIdentifier + "' not found in dbms or inaccessible to user.");
  	      }
  	      ProgressMonitor.setCurrentMessage("Updating study status to :  '" + status.name() + "' for study: " + cancerStudyIdentifier); 
  	      DaoCancerStudy.setStatus(status, cancerStudyIdentifier);
  	      
  	      ConsoleUtil.showMessages();
  	      System.out.println("Done.");
        }
        catch (Exception e) {
  	        ConsoleUtil.showWarnings();
  	        //exit with error status:
  	        System.err.println ("\nABORTED! Error:  " + e.getMessage());
  	        System.exit(1);
      }
    }
}
