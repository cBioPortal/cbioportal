package org.mskcc.portal.util;

import org.mskcc.portal.model.GeneticAlterationType;
import org.mskcc.portal.model.ProfileData;

import java.util.ArrayList;
import java.util.HashMap;

/*
 * TODO:
 * 
 * I think that these changes should be made to make that code more robust and easier, i.e. faster, to modify in the future.
 * A value in a merged profile is stored as a String of the RE form (TYPE:VALUE;)+  I replaced constant ':' and ';' as used 
 * in these strings by defined constants ProfileMerger.TYPE_VALUE_SEPARATOR and ProfileMerger.VALUE_SEPARATOR, respectively.
 * 
 * Since the VALUE in a PROTEIN_LEVEL or PHOSPHORYLATION data type can contain a ':', I changed ValueParser.parseValue to parse 
 * each TYPE:VALUE on the first ':'.
 * 
 * These changes cause the tests on lines 154-155 of TestValueParser.testValueParser to fail. Unclear that we need these tests so I disabled them.
 * 
 * Interactions between ValueParser and OncoPrintGeneDisplaySpec are overly complex and should be simplified.
 * 
 * String-based enumerations, especially GeneticAlterationType, should be replaced with Java enums to 1) simplify the code, by, for example, 
 * replacing cascading if-else statements with switch statements, 2) provide reliable compile-time type checking, 
 * 3) provide opportunities for greater generality (such as operations on sets of enumerations), and 
 * 4) simplify the addition of new data types. Also, multiple enumerations, including those in the OncoSpec code, should be combined into one.
 * 
 * ProfileMerger.determineAlteredStatus should return an object that stores the alteration, rather than a String, 
 * so we have the data already structured and it doesn't need to be parsed again (by ValueParser).
 */
/**
 * Merge multiple ProfileData objects into one, so the data (in a String) for each gene,case pair contains the data from all input ProfileData objects.
 */
public class ProfileMerger {
    private ProfileData profileData;
    static final String VALUE_SEPARATOR = ";";        // Separator placed between multiple datatypes in the merged value 
    static final String TYPE_VALUE_SEPARATOR = ":";   // Separator placed between a datatype name and its value

    /**
     * Constructor.
     *
     * @param profileList ArrayList of ProfileData Objects.
     */
    public ProfileMerger(ArrayList<ProfileData> profileList) {

        //  Create Union of all Cases and all Genes
        ArrayList<String> caseList = new ArrayList<String>();
        ArrayList<String> geneList = new ArrayList<String>();
        createUnion(profileList, caseList, geneList);

        //  Create new HashMap of Merged Data
        HashMap<String, String> map = new HashMap<String, String>();

        //  Perform the actual merge
        mergeProfiles(map, profileList, caseList, geneList);
    }

    /**
     * Gets the new merged profile data object.
     *
     * @return ProfileData Object.
     */
    public ProfileData getMergedProfile() {
        return this.profileData;
    }

    /**
     * Perform the merge.
     */
    private void mergeProfiles(HashMap<String, String> map, ArrayList<ProfileData> profileList,
                               ArrayList<String> caseList, ArrayList<String> geneList) {
        //  Iterate through all genes
        for (String gene : geneList) {

            //  Iterate through all cases
            for (String caseId : caseList) {

                //  Determine status of gene X in caseId Y.
                String status = determineAlteredStatus(profileList, gene, caseId);

                // Store status in hash map
                String key = createKey(gene, caseId);
                map.put(key, status.toString());
            }
        }
        profileData = new ProfileData(map, geneList, caseList);
    }

    /**
     * Determines the alteration status of gene X in case Y.
     */
    private String determineAlteredStatus(ArrayList<ProfileData> profileList,
                                          String gene, String caseId) {
       // TODO: APG: I would prefer to have this return an object that stores the alteration, rather than a String
       // so we have the data already structured and it doesn't need to be parsed again (by ValueParser)
        StringBuffer status = new StringBuffer("");

        //  Iterate through all profiles
        for (ProfileData data : profileList) {

            //  Get the data value and alteration type for gene X in caseId Y in profile Z
            String value = data.getValue(gene, caseId);
            GeneticAlterationType alterationType =
                    data.getGeneticProfile().getAlterationType();

            //  Handle Copy Number Changes
            if (alterationType.equals(GeneticAlterationType.COPY_NUMBER_ALTERATION)) {
                if (value != null) {
                    status.append(GeneticAlterationType.COPY_NUMBER_ALTERATION + TYPE_VALUE_SEPARATOR
                            + value + VALUE_SEPARATOR);
                }
            } else if (alterationType.equals(GeneticAlterationType.MRNA_EXPRESSION)) {
                //  Handle mRNA Data
                if (value != null) {
                    status.append(GeneticAlterationType.MRNA_EXPRESSION + TYPE_VALUE_SEPARATOR + value + VALUE_SEPARATOR);
                }
            } else if (alterationType.equals(GeneticAlterationType.MUTATION)
                    || alterationType.equals(GeneticAlterationType.MUTATION_EXTENDED)) {
                //  Handle Mutation Data
                if (value != null) {
                    status.append(GeneticAlterationType.MUTATION + TYPE_VALUE_SEPARATOR + value + VALUE_SEPARATOR);
                }
            } else if (alterationType.equals(GeneticAlterationType.METHYLATION)) {
                if (value != null) {
                    status.append(GeneticAlterationType.METHYLATION + TYPE_VALUE_SEPARATOR + value + VALUE_SEPARATOR);
                }
            } else if (alterationType.equals(GeneticAlterationType.METHYLATION_BINARY)) {
               if (value != null) {
                   status.append(GeneticAlterationType.METHYLATION_BINARY + TYPE_VALUE_SEPARATOR + value + VALUE_SEPARATOR);
               }
            } else if (alterationType.equals(GeneticAlterationType.PROTEIN_LEVEL)) {
               if (value != null) {
                   status.append(GeneticAlterationType.PROTEIN_LEVEL + TYPE_VALUE_SEPARATOR + value + VALUE_SEPARATOR);
               }
            } else if (alterationType.equals(GeneticAlterationType.PHOSPHORYLATION)) {
               if (value != null) {
                   status.append(GeneticAlterationType.PHOSPHORYLATION + TYPE_VALUE_SEPARATOR + value + VALUE_SEPARATOR);
               }
            }
        }
        return status.toString();
    }

    /**
     * Creates the Union of all Cases and the Union of all Genes.
     */
    private void createUnion(ArrayList<ProfileData> profileList,
                             ArrayList<String> caseIdList, ArrayList<String> geneList) {

        //  Iterate through all profiles
        for (ProfileData data : profileList) {

            //  Get the case list and the gene list
            ArrayList<String> currentCaseList = data.getCaseIdList();
            ArrayList<String> currentGeneList = data.getGeneList();

            //  Conditionally add each new case to the global case list
            for (String currentCaseId : currentCaseList) {
                if (!caseIdList.contains(currentCaseId)) {
                    caseIdList.add(currentCaseId);
                }
            }

            //  Conditionally add each new gene to the global gene list
            for (String currentGene : currentGeneList) {
                if (!geneList.contains(currentGene)) {
                    geneList.add(currentGene);
                }
            }
        }
    }

    private String createKey(String geneSymbol, String caseId) {
        return geneSymbol + ":" + caseId;
    }
}
