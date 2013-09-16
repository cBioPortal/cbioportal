/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.FileUtil;
import org.mskcc.cbio.portal.dao.*;

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