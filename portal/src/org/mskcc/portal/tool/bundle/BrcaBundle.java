package org.mskcc.portal.tool.bundle;

import org.mskcc.portal.model.ProfileData;
import org.mskcc.cgds.model.ExtendedMutation;
import org.mskcc.cgds.model.GeneticAlterationType;

import java.util.ArrayList;
import java.util.HashMap;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The Main BRCA/HR Bundle.
 *
 * @author Ethan Cerami.
 */
class BrcaBundle extends Bundle {
    private String MUTATION_DATA_PROFILE = "ova_mutations_next_gen";
    private String METHYLATION_BINARY_PROFILE = "ova_meth_binary";
    private String GISTIC_PROFILE = "ova_gistic";
    private String TAB = "\t";

    private ArrayList<GeneRequest> geneList;
    private FileWriter writer;
    
    private int numCases;
    private int numEmsyAltered = 0;
    private int numBrca1EpigeneticallySilenced = 0;
    private int numBrcaSomaticAltered = 0;
    private int numBrcaMutated = 0;
    private int numBrcaGermlineAltered = 0;
    private int numPtenAltered = 0;
    private int numRad51CAltered = 0;

    public static int NO_MUTATION = 0;
    public static int SOMATIC_MUTATION = 1;
    public static int GERMLINE_MUTATION =2;
    private int startLowFrequencyGenes = -1;
    private String fileName = "hr_altered.txt";

    /**
     * Constructor.
     */
    public BrcaBundle () {
        try {
            writer = new FileWriter(fileName);
            writer.write ("CASE_ID" + TAB + "BRCA1_GERMLINE" + TAB + "BRCA1_SOMATIC" + TAB
                    + "BRCA2_GERMLINE" + TAB + "BRCA2_SOMATIC" + TAB + "BRCA1_SILENCED"
                    + TAB + "EMSY_ALTERED" + TAB + "PTEN_ALTERED" + TAB + "RAD51_SILENCED"
                    + TAB + "OTHER_HR_MUTATED" + TAB + "HR_ALTERED\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the Genes of Interest.
     *
     * Each Gene is Tied to a Specific Genomic Profile.
     *
     * @return ArrayList of GeneRequest Objects.
     */
    public ArrayList<GeneRequest> getGeneRequestList() {
        geneList = new ArrayList<GeneRequest>();
        geneList.add(new GeneRequest("BRCA1", MUTATION_DATA_PROFILE, GeneticAlterationType.MUTATION_EXTENDED));
        geneList.add(new GeneRequest("BRCA2", MUTATION_DATA_PROFILE, GeneticAlterationType.MUTATION_EXTENDED));

        geneList.add(new GeneRequest("BRCA1", GISTIC_PROFILE, GeneticAlterationType.COPY_NUMBER_ALTERATION));
        geneList.add(new GeneRequest("BRCA2", GISTIC_PROFILE, GeneticAlterationType.COPY_NUMBER_ALTERATION));

        geneList.add(new GeneRequest("BRCA1", METHYLATION_BINARY_PROFILE, GeneticAlterationType.METHYLATION_BINARY));
        geneList.add(new GeneRequest("C11ORF30", MUTATION_DATA_PROFILE, GeneticAlterationType.MUTATION_EXTENDED));
        geneList.add(new GeneRequest("C11ORF30", GISTIC_PROFILE, GeneticAlterationType.COPY_NUMBER_ALTERATION));
        geneList.add(new GeneRequest("PTEN", MUTATION_DATA_PROFILE, GeneticAlterationType.MUTATION_EXTENDED));
        geneList.add(new GeneRequest("PTEN", GISTIC_PROFILE, GeneticAlterationType.COPY_NUMBER_ALTERATION));
        geneList.add(new GeneRequest("RAD51C", METHYLATION_BINARY_PROFILE, GeneticAlterationType.METHYLATION_BINARY));

        startLowFrequencyGenes = 10;
        geneList.add(new GeneRequest("ATM", MUTATION_DATA_PROFILE, GeneticAlterationType.MUTATION_EXTENDED));
        geneList.add(new GeneRequest("ATR", MUTATION_DATA_PROFILE, GeneticAlterationType.MUTATION_EXTENDED));
        geneList.add(new GeneRequest("PALB2", MUTATION_DATA_PROFILE, GeneticAlterationType.MUTATION_EXTENDED));
        geneList.add(new GeneRequest("FANCA", MUTATION_DATA_PROFILE, GeneticAlterationType.MUTATION_EXTENDED));
        geneList.add(new GeneRequest("FANCC", MUTATION_DATA_PROFILE, GeneticAlterationType.MUTATION_EXTENDED));
        geneList.add(new GeneRequest("FANCI", MUTATION_DATA_PROFILE, GeneticAlterationType.MUTATION_EXTENDED));
        geneList.add(new GeneRequest("FANCL", MUTATION_DATA_PROFILE, GeneticAlterationType.MUTATION_EXTENDED));
        geneList.add(new GeneRequest("FANCD2", MUTATION_DATA_PROFILE, GeneticAlterationType.MUTATION_EXTENDED));
        geneList.add(new GeneRequest("FANCE", MUTATION_DATA_PROFILE, GeneticAlterationType.MUTATION_EXTENDED));
        geneList.add(new GeneRequest("FANCG", MUTATION_DATA_PROFILE, GeneticAlterationType.MUTATION_EXTENDED));
        geneList.add(new GeneRequest("FANCM", MUTATION_DATA_PROFILE, GeneticAlterationType.MUTATION_EXTENDED));

        return geneList;
    }

    /**
     * Gets the Case Set of Interest.
     * @return Case Set ID.
     */
    public String getCaseSetId() {
        return "ova_sequenced";
        //return "ova_95_bra";
    }

    /**
     * Determines whether the case is altered in any of the genes of interest.  If so, we
     * "accept" it in a specific bin number and display it in the fingerprint.
     *
     * @param caseId                Current Case Id.
     * @param mutationMap           All Mutations (germline and somatic)
     * @param cnaMap                All CNA Events
     * @param binaryMethylationMap  All Binary Methylation Events.
     * @return integer value
     */
    public int binAccept (String caseId,
       HashMap<String, ArrayList<ExtendedMutation>> mutationMap,
       HashMap <String, ProfileData> cnaMap,
       HashMap <String, ProfileData> binaryMethylationMap) {

        //  Keep track of multiple variables to output to hr matrix file
        boolean brca1Germline = false;
        boolean brca1Somatic = false;
        boolean brca2Germline = false;
        boolean brca2Somatic = false;
        boolean brca1Silenced = false;
        boolean emsyAltered = false;
        boolean ptenAltered = false;
        boolean rad51Silenced = false;
        boolean otherHrMutated = false;
        boolean hrAltered = false;

        numCases += 1;
        int accept = 0;

        //  Check for BRCA1 Events
        if (BundleHelper.isCaseGermlineMutated(caseId, "BRCA1", mutationMap)) {
            numBrcaGermlineAltered++;
            brca1Germline = true;
            accept = 1;
        }
        if (BundleHelper.isCaseSomaticallyMutated(caseId, "BRCA1", mutationMap)) {
            numBrcaSomaticAltered++;
            brca1Somatic = true;
            if (accept == 0) {
                accept = 2;
            }
        }

        //  Check for BRCA2 Events
        if (BundleHelper.isCaseGermlineMutated(caseId, "BRCA2", mutationMap)) {
            numBrcaGermlineAltered++;
            brca2Germline = true;
            if (accept == 0) {
                accept = 3;
            }
        }
        if (BundleHelper.isCaseSomaticallyMutated(caseId, "BRCA2", mutationMap)) {
            numBrcaSomaticAltered++;
            brca2Somatic = true;
            if (accept == 0) {
                accept = 4;
            }
        }

        //  Check for BRCA1 Epigenetic Silencing
        if (BundleHelper.isCaseEpigeneticallySilenced (caseId, "BRCA1", binaryMethylationMap)) {
            numBrca1EpigeneticallySilenced++;
            brca1Silenced = true;
            if (accept == 0) {
                accept = 5;
            }
        }

        //  Check for EMSY Alterations
        if (BundleHelper.isCaseSomaticallyMutated(caseId, "C11ORF30", mutationMap)) {
            numEmsyAltered++;
            emsyAltered = true;
            if (accept == 0) {
                accept = 6;
            }
        }
        if (BundleHelper.isCaseAmplified(caseId, "C11ORF30", cnaMap)) {
            numEmsyAltered++;
            emsyAltered = true;
            if (accept == 0) {
                accept = 7;
            }
        }

        //  Check for PTEN Alterations
        if (BundleHelper.isCaseSomaticallyMutated(caseId, "PTEN", mutationMap)) {
            numPtenAltered++;
            ptenAltered = true;
            if (accept == 0) {
                accept = 8;
            }
        }
        if (BundleHelper.isCaseHomozygouslyDeleted(caseId, "PTEN", cnaMap)) {
            numPtenAltered++;
            ptenAltered = true;
            if (accept == 0) {
                accept = 9;
            }
        }

        //  Check for RAD51C
        if (BundleHelper.isCaseEpigeneticallySilenced (caseId, "RAD51C", binaryMethylationMap)) {
            numRad51CAltered++;
            rad51Silenced = true;
            if (accept == 0) {
                accept = 10;
            }
        }

        //  Check all other mutated genes
        if (startLowFrequencyGenes > 0) {
            for (int i=startLowFrequencyGenes; i<geneList.size(); i++) {
                GeneRequest geneRequest = geneList.get(i);
                if (BundleHelper.isCaseSomaticallyMutated(caseId, geneRequest.getGeneSymbol(), mutationMap)){
                    otherHrMutated = true;
                    if (accept ==0) {
                        accept = i + 1000;
                    }
                }
            }
        }

        //  Record whether HR is putatively altered
        if (accept > 0) {
            hrAltered = true;
        }

        //  Write out the case
        writeCase (caseId, brca1Germline, brca1Somatic, brca2Germline, brca2Somatic, brca1Silenced,
                emsyAltered, ptenAltered, rad51Silenced, otherHrMutated, hrAltered);

        if (brca1Germline || brca2Germline || brca1Somatic || brca2Somatic) {
            numBrcaMutated++;
        }

        // All else, return 0/false.
        return accept;
    }

    /**
     * Writes HR Alteration Data to HR Matrix File.
     */
    private void writeCase (String caseId, boolean brca1Germline, boolean brca1Somatic,
                            boolean brca2Germline, boolean brca2Somatic, boolean brca1Silenced,
                            boolean emsyAltered, boolean ptenAltered, boolean rad51Silenced,
                            boolean otherHrMutated, boolean hrAltered) {
        try {
            String str = caseId + TAB + brca1Germline + TAB + brca1Somatic + TAB + brca2Germline
                    + TAB + brca2Somatic + TAB + brca1Silenced + TAB + emsyAltered + TAB
                    + ptenAltered + TAB + rad51Silenced + TAB + otherHrMutated + TAB
                    + hrAltered + "\n";
            writer.write(str.toUpperCase());
        } catch (IOException e) {
        }
    }

    /**
     * Gets Text Summary of What we Just Found Out.
     */
    public String getSummary() {
        StringBuffer buf = new StringBuffer();
        buf.append ("Total number of cases is:  " + numCases + "\n");
        buf.append (summarize ("BRCA1 / BRCA2 Germline Mutation", numBrcaGermlineAltered, numCases));
        buf.append (summarize ("BRCA1 / BRCA2 Somatic Mutation", numBrcaSomaticAltered, numCases));
        buf.append (summarize ("BRCA Mutation", numBrcaMutated, numCases));
        buf.append (summarize ("EMSY Altered", numEmsyAltered, numCases));
        buf.append (summarize ("BRCA1 Epigenetically Silenced", numBrca1EpigeneticallySilenced, numCases));
        buf.append (summarize ("PTEN Altered", numPtenAltered, numCases));
        buf.append (summarize ("RAD51C Altered", numRad51CAltered, numCases));

        buf.append ("\n\nHR Matrix written to:  " + fileName);
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buf.toString();
    }

    /**
     * Percent Summary Text.
     */
    private String summarize (String start, int numCasesAffected, int totalN) {
        double percent = 100.0 * (numCasesAffected / (float) numCases);
        NumberFormat formatter = new DecimalFormat("#0.00");
        return (start + ":  " + numCasesAffected
                + " [" + formatter.format(percent) + "%]\n");
    }

}
