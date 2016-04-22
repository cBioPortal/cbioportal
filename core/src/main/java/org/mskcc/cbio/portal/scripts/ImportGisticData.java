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

import org.mskcc.cbio.portal.model.Gistic;
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.validate.validationException;

import joptsimple.OptionSet;


/**
 * Command line utility for importing (amp/del) Gistic data 
 */
public class ImportGisticData {

    public static void main(String[] args) throws IOException, DaoException {
    	try {
	    	ProgressMonitor.setConsoleModeAndParseShowProgress(args);
	    	
	    	String description = "Import GISTIC data.\n" +
                    " Note that gistic-data-file.txt must be a massaged file, it does not come straight from the Broad";
	    	
	    	OptionSet options = ConsoleUtil.parseStandardDataAndStudyOptions(args, description);
		    String dataFile = (String) options.valueOf("data");
		    String studyId = (String) options.valueOf("study");
	
			SpringUtil.initDataSource();
	        
	        File gistic_f = new File(dataFile);
	        int cancerStudyInternalId = ValidationUtils.getInternalStudyId(studyId);
	
	        System.out.println("Reading data from: " + gistic_f.getAbsolutePath());
	        System.out.println("CancerStudyId: " + cancerStudyInternalId);
	
	        int lines = FileUtil.getNumLines(gistic_f);
	        System.out.println(" --> total number of lines: " + lines);
	        ProgressMonitor.setMaxValue(lines);
	
	        GisticReader gisticReader = new GisticReader();
	        ArrayList<Gistic> gistics = gisticReader.parse(gistic_f, cancerStudyInternalId);
	
	        if (gistics == null) {
	            throw new RuntimeException("Error: didn't get any data");
	        }
	
	        // add to CGDS database
	        for (Gistic g : gistics) {
	            try {
	                DaoGistic.addGistic(g);
	            } catch (validationException e) {
	                // only catching validationException, not DaoException
	                ProgressMonitor.logWarning("Error: " + e.getMessage() + ". Skipping record.");
	            } 
	        }
	        //Finish with any logged messages and/or warnings:
	        ConsoleUtil.showMessages();
	        
    	} catch (Exception e) {
	    	ConsoleUtil.showWarnings();
	    	//exit with error status:
    		System.err.println ("\nABORTED! Error:  " + e.getMessage());
	    	System.exit(1);
	    }
    	
    }
}
