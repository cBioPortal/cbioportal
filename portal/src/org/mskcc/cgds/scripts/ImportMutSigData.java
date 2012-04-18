package org.mskcc.cgds.scripts;

import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.MySQLbulkLoader;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.MutSig;
import org.mskcc.cgds.util.ConsoleUtil;
import org.mskcc.cgds.util.FileUtil;
import org.mskcc.cgds.util.ProgressMonitor;

import java.io.*;
import java.util.Properties;

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

    public ImportMutSigData(File mutSigFile, File metaDataFile, ProgressMonitor pMonitor) {
        this.mutSigFile = mutSigFile;
        this.pMonitor = pMonitor;
        this.metaDataFile = metaDataFile;
    }

    // method responsible for parsing MutSig data,
    // adds individual 'MutSig' objects to CDGS database.

    public void importData() throws IOException, DaoException {
        MySQLbulkLoader.bulkLoadOff();
        FileReader reader = new FileReader(mutSigFile);
        BufferedReader buf = new BufferedReader(reader);
        int cancerType = loadProps();

        // parse field names of a mutsig data file
        int rankField = 0;
        int hugoField = 0;
        int BasesCoveredField = 0;
        int numMutationsField = 0;
        int PvalField = 0;
        int QvalField = 0;

        String head = buf.readLine();
        String[] names = head.split("\t");
        int len = names.length;
        for (int i = 0; i < len ; i++)
        {
            if (names[i].equals("rank")) {
                rankField = i;
            }

            if (names[i].equalsIgnoreCase("gene")) {
                hugoField = i;
            }

            if (names[i].equals("N")) {
                BasesCoveredField = i;
            }
            
            if (names[i].equals("n")) {
               numMutationsField = i;
            }

            if (names[i].equalsIgnoreCase("p")) {
                PvalField = i;
            }

            if (names[i].equalsIgnoreCase("q") || names[i].equalsIgnoreCase("q\n")) {
                QvalField = i;
            }
        }
        // end parse Column names
        
        // check to see if all fields are filled
        if (rankField == 0
                ^ hugoField == 0
                ^ BasesCoveredField == 0
                ^ numMutationsField == 0
                ^ PvalField == 0
                ^ QvalField == 0)
        {
            // *************** how to print out an error message?
            System.exit(1);
        }


        // parse data
        String line = buf.readLine();
        while (line != null) {

            if (pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
            DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

            String[] parts = line.split("\t");

            int rank = Integer.parseInt(parts[rankField]);
            String hugoGeneSymbol = parts[hugoField];
            int numBasesCovered = Integer.parseInt(parts[BasesCoveredField]);
            int numMutations = Integer.parseInt(parts[numMutationsField]);
            String pValue = parts[PvalField];
            String qValue = parts[QvalField];

            CanonicalGene gene = daoGene.getGene(hugoGeneSymbol);
            if (gene == null) {
                gene = new CanonicalGene(0, hugoGeneSymbol);
                pMonitor.logWarning("Invalid gene symbol:  " + hugoGeneSymbol);
            }

            MutSig mutSig = new MutSig(cancerType, gene, rank, numBasesCovered, numMutations, pValue, qValue);
            line = buf.readLine();
        }
    }

    //parses MutSig properties file and extracts CancerStudyID

    // should loadProps() be private?  What harm is there to making it public?
    public int loadProps() throws IOException, DaoException {
        Properties props = new Properties();
        props.load(new FileInputStream(metaDataFile));
        String cancerStudyIdentifier = props.getProperty("cancer_study_identifier");
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyIdentifier);

        int cancerStudyID = cancerStudy.getInternalId();
        return cancerStudyID;
    }


    // command line utility
    
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("command line usage:  importMutSig.pl <Mutsig_file.txt> <MetaProperties.txt>");
            System.exit(1);
        }
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        File mutSigFile = new File(args[0]);
        File propertiesFile = new File(args[1]);

        System.out.println("Reading data from: " + mutSigFile.getAbsolutePath());
        System.out.println("Properties: " + propertiesFile.getAbsolutePath());

        int numLines = FileUtil.getNumLines(mutSigFile);
        System.out.println(" --> total number of lines:  " + numLines);
        pMonitor.setMaxValue(numLines);

        ImportMutSigData parser = new ImportMutSigData(mutSigFile, propertiesFile, pMonitor);
        parser.importData();
        ConsoleUtil.showWarnings(pMonitor);
    }
}
