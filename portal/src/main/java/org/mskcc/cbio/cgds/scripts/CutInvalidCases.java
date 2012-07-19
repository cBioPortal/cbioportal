package org.mskcc.cgds.scripts;

import org.mskcc.cgds.util.ProgressMonitor;
import org.mskcc.cgds.util.ConsoleUtil;
import org.mskcc.cgds.util.FileUtil;
import org.mskcc.cgds.dao.*;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.HashSet;

/**
 * Command Line Tool to Remove Exclused Cases.
 */
public class CutInvalidCases {

    private ProgressMonitor pMonitor;
    private File caseExclusionFile;
    private File dataFile;

    public CutInvalidCases(File caseExclusionFile, File dataFile, ProgressMonitor pMonitor) {
        this.caseExclusionFile = caseExclusionFile;
        this.dataFile = dataFile;
        this.pMonitor = pMonitor;
    }

    public String process() throws IOException, DaoException {
        StringBuffer revisedText = new StringBuffer();
        HashSet excludedCaseSet = getExcludedCases();
        FileReader reader = new FileReader(dataFile);
        BufferedReader buf = new BufferedReader(reader);
        String headerLine = buf.readLine();
        String parts[] = headerLine.split("\t");
        boolean includeColumn[] = new boolean [parts.length];

        //  Mark all columns for inclusion / exclusion
        pMonitor.setCurrentMessage("Total number of columns to process:  " + parts.length);
        for (int i = 0; i < parts.length; i++) {
            String colHeading = parts[i].trim();
            if (excludedCaseSet.contains(colHeading)) {
                includeColumn[i] = false;
                pMonitor.setCurrentMessage ("Marking for exclusion, col #" + i
                        + ", Case ID:  " + colHeading);
            } else {
                includeColumn[i] = true;
                revisedText.append(colHeading);
                if (i < parts.length-1) {
                    revisedText.append ("\t");
                }
            }
        }
        revisedText.append("\n");

        String line = buf.readLine();
        while (line != null) {
            if (pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
            parts = line.split("\t");
            for (int i=0; i<parts.length; i++) {
                if (includeColumn[i]) {
                    revisedText.append (parts[i]);
                    if (i < parts.length-1) {
                        revisedText.append ("\t");
                    }
                }
            }
            line = buf.readLine();
            revisedText.append("\n");
        }
        return revisedText.toString();
    }

    private HashSet getExcludedCases() throws IOException {
        FileReader reader = new FileReader(caseExclusionFile);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine();

        HashSet excludedCaseSet = new HashSet();
        while (line != null) {
            if (!line.startsWith("#") && line.trim().length() > 0) {
                String parts[] = line.split("\t");
                excludedCaseSet.add(parts[0]);
            }
            line = buf.readLine();
        }
        return excludedCaseSet;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("command line usage:  cutInvalidCases.pl " +
                    "<cases_excluded.txt> <data_file.txt>");
            System.exit(1);
        }

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);
        File casesExcludedFile = new File (args[0]);
        File dataFile = new File(args[1]);


        System.err.println("Reading data from:  " + dataFile.getAbsolutePath());
        int numLines = FileUtil.getNumLines(dataFile);
        pMonitor.setMaxValue(numLines);
        CutInvalidCases parser = new CutInvalidCases(casesExcludedFile,
                dataFile, pMonitor);
        String out = parser.process();
        System.out.print (out);
        ConsoleUtil.showWarnings(pMonitor);
        System.err.println("Done.");
    }
}