/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.scripts;

import java.io.*;
import java.util.*;
import joptsimple.OptionSet;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGistic;
import org.mskcc.cbio.portal.model.Gistic;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.FileUtil;
import org.mskcc.cbio.portal.util.GisticReader;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.validate.validationException;


/**
 * Command line utility for importing (amp/del) Gistic data 
 */
public class ImportGisticData extends ConsoleRunnable {

    public void run () {
        try {
	    	String description = "Import GISTIC data.\n" +
                    " Note that gistic-data-file.txt must be a massaged file, it does not come straight from the Broad";
	    	
	    	OptionSet options = ConsoleUtil.parseStandardDataAndStudyOptions(args, description);
		    String dataFile = (String) options.valueOf("data");
		    String studyId = (String) options.valueOf("study");
	
	        File gistic_f = new File(dataFile);
	        int cancerStudyInternalId = ValidationUtils.getInternalStudyId(studyId);
	
	        ProgressMonitor.setCurrentMessage(
	                "Reading data from: " + gistic_f.getAbsolutePath());
	        ProgressMonitor.setCurrentMessage(
	                "CancerStudyId: " + cancerStudyInternalId);
	
	        int lines = FileUtil.getNumLines(gistic_f);
	        ProgressMonitor.setCurrentMessage(
	                " --> total number of lines: " + lines);
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
        } catch (RuntimeException e) {
            throw e;
        } catch (IOException|DaoException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Makes an instance to run with the given command line arguments.
     *
     * @param args  the command line arguments to be used
     */
    public ImportGisticData(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args  the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportGisticData(args);
        runner.runInConsole();
    }
}
