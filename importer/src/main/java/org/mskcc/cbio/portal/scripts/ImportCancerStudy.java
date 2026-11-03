/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.scripts;

import java.io.File;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CancerStudyTags;
import org.mskcc.cbio.portal.util.CancerStudyReader;
import org.mskcc.cbio.portal.util.CancerStudyTagsReader;
import org.mskcc.cbio.portal.util.ProgressMonitor;

/**
 * Command Line Tool to Import a Single Cancer Study.
 */
public class ImportCancerStudy extends ConsoleRunnable {
    
    public void run() {
        try {
            if (args.length < 1) {
                // an extra --noprogress option can be given to avoid the messages regarding memory usage and % complete
                throw new UsageException(
                        "importCancerStudy.pl",
                        null,
                        "<cancer_study.txt>");
            }
            
            File file = new File(args[0]);
            CancerStudy cancerStudy = CancerStudyReader.loadCancerStudy(file);
            CancerStudyTags cancerStudyTags = CancerStudyTagsReader.loadCancerStudyTags(file, cancerStudy);
            String message = "Loaded the following cancer study:" +
                "\n --> Study ID:  " + cancerStudy.getInternalId() +
                "\n --> Name:  " + cancerStudy.getName() +
                "\n --> Description:  " + cancerStudy.getDescription();

            if (cancerStudyTags != null) {
                message += "\n --> Study Tags:  " + cancerStudyTags.getTags();
            }
            ProgressMonitor.setCurrentMessage(message);
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Makes an instance to run with the given command line arguments.
     *
     * @param args  the command line arguments to be used
     */
    public ImportCancerStudy(String[] args) {
        super(args);
    }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args  the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new ImportCancerStudy(args);
        runner.runInConsole();
    }
}
