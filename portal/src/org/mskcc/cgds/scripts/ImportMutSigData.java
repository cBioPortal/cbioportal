package org.mskcc.cgds.scripts;

import org.mskcc.cgds.util.ConsoleUtil;
import org.mskcc.cgds.util.FileUtil;
import org.mskcc.cgds.util.MutSigReader;
import org.mskcc.cgds.util.ProgressMonitor;

import java.io.File;

/**
 * ImportMutSig is used to import the Broad Institutes MutSig data for different Cancer types
 * into our CGDS SQL database.
 * Command line users must specify a MutSig file, and properties file containing a CancerID.
 *
 * @author Lennart Bastian, Gideon Dresdner
 */

public class ImportMutSigData {
    private ProgressMonitor pMonitor;
    private File mutSigFile;
    private File metaDataFile;

    // command line utility
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("command line usage:  importMutSig.pl <Mutsig_file.txt> <MetaProperties.txt>");
            System.exit(1);
        }
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(false);

        File mutSigFile = new File(args[0]);
        File propertiesFile = new File(args[1]);

        System.out.println("Reading data from: " + mutSigFile.getAbsolutePath());
        System.out.println("Properties: " + propertiesFile.getAbsolutePath());

        int numLines = FileUtil.getNumLines(mutSigFile);
        System.out.println(" --> total number of lines:  " + numLines);
        pMonitor.setMaxValue(numLines);

        int internalId = MutSigReader.getInternalId(propertiesFile);
        MutSigReader.loadMutSig(internalId, mutSigFile, pMonitor);
        
        ConsoleUtil.showWarnings(pMonitor);
    }
}
