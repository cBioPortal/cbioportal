package org.mskcc.cgds.scripts;

import org.mskcc.cgds.dao.*;
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
 * @author Lennart Bastian
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

    //method responsible for parsing MutSig data, and adding individual 'MutSig' objects to CDGS database.

    public void importData() throws IOException, DaoException {
        MySQLbulkLoader.bulkLoadOff();
        FileReader reader = new FileReader(mutSigFile);
        BufferedReader buf = new BufferedReader(reader);
        int cancerType = loadProps();

        // parse Column names of a mutsig data file
        int rankColumn = 0;
        int hugoColumn = 0;
        int BasesCoveredColumn = 0;
        int numMutationsColumn = 0;
        int PvalColumn = 0;
        int QvalColumn = 0;

        String head = buf.readLine();
        String[] names = head.split("\t");
        int len = names.length;
        for (int i = 0; i < len ; i++)
        {
            if (names[i].equals("rank")) {
                rankColumn = i;
            }

            else if (names[i].equalsIgnoreCase("gene")) {
                hugoColumn = i;
            }

            else if (names[i].equals("N")) {
                BasesCoveredColumn = i;
            }
            
            else if (names[i].equals("n")) {
               numMutationsColumn = i;
            }

            else if (names[i].equalsIgnoreCase("p")) {
                PvalColumn = i;
            }

            else if (names[i].equalsIgnoreCase("q") || names[i].equalsIgnoreCase("q\n")) {
                QvalColumn = i;
            }

            // is this the right thing to do here?
            else {
                continue;
            }

        }
        // end parse Column names

        // parse data
        String line = buf.readLine();
        while (line != null) {

            if (pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
            DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
            
            String[] parts = line.split("\t");
            
            int rank = Integer.parseInt(parts[rankColumn]);
            String hugoGeneSymbol = parts[hugoColumn];
            int numBasesCovered = Integer.parseInt(parts[BasesCoveredColumn]);
            int numMutations = Integer.parseInt(parts[numMutationsColumn]);
            String pValue = parts[PvalColumn];
            String qValue = parts[QvalColumn];

            CanonicalGene gene = daoGene.getGene(hugoGeneSymbol);
            if (gene == null) {
                gene = new CanonicalGene(0, hugoGeneSymbol);
                pMonitor.logWarning("Invalid gene symbol:  " + hugoGeneSymbol);
            }

            // use 0 as a dummy value for nVal, nVer, cpg, aAndG, aAndT, indel, adjustedQValue
            MutSig mutSig = new MutSig(cancerType, gene, rank, numBasesCovered, numMutations, 0,
                    0, 0, 0, 0, 0, pValue, qValue, (double) 0);

            line = buf.readLine();
        }
    }

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

    //parses MutSig properties file and extracts CancerStudyID

    private int loadProps() throws IOException, DaoException {
        Properties props = new Properties();
        props.load(new FileInputStream(metaDataFile));
        String cancerStudyIdentifier = props.getProperty("cancer_study_identifier");
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyIdentifier);
        int cancerStudyID = cancerStudy.getInternalId();
        return cancerStudyID;
    }
}
