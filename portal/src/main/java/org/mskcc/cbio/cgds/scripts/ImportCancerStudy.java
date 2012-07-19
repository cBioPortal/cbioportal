package org.mskcc.cbio.cgds.scripts;

import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.util.ConsoleUtil;
import org.mskcc.cbio.cgds.util.ProgressMonitor;
import org.mskcc.cbio.cgds.util.CancerStudyReader;

import java.io.File;

/**
 * Command Line Tool to Import a Single Cancer Study.
 */
public class ImportCancerStudy {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("command line usage: importCancerStudy.pl <cancer_study.txt>");
            System.exit(1);
        }

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        File file = new File(args[0]);
        CancerStudy cancerStudy = CancerStudyReader.loadCancerStudy(file);
        System.out.println ("Loaded the following cancer study:  ");
        System.out.println ("ID:  " + cancerStudy.getInternalId());
        System.out.println ("Name:  " + cancerStudy.getName());
        System.out.println ("Description:  " + cancerStudy.getDescription());
        ConsoleUtil.showWarnings(pMonitor);
        System.err.println("Done.");
    }
}