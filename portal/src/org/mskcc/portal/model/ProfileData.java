package org.mskcc.portal.model;

import org.mskcc.portal.util.ValueParser;
import org.mskcc.cgds.model.GeneticProfile;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Encapsulates Genetic Profile Data.
 * Stores the properties of each gene in each case.
 * also stores lists of the genes and cases
 */
public class ProfileData {
    private String[][] matrix;
    private GeneticProfile geneticProfile;
    
    // primary store of profile's data:
    private HashMap<String, String> mapFromGeneAndCaseToGeneProperties = new HashMap<String, String>();
    private ArrayList<String> caseIdList = new ArrayList<String>();
    private ArrayList<String> geneList = new ArrayList<String>();

    /**
     * Constructor.
     *
     * @param geneticProfile GeneticProfile Object.
     * @param matrix         2D Matrix of Data
     */
    public ProfileData(GeneticProfile geneticProfile, String[][] matrix) {
        this.geneticProfile = geneticProfile;
        this.matrix = matrix;
        processMatrix();
    }

    /**
     * Constructor.
     *
     * @param hashMap    HashMap of Data.
     * @param geneList   List of Genes.
     * @param caseIdList List of Case Ids.
     */
    public ProfileData(HashMap<String, String> hashMap,
                       ArrayList<String> geneList, ArrayList<String> caseIdList) {
        this.mapFromGeneAndCaseToGeneProperties = hashMap;
        this.geneList = geneList;
        this.caseIdList = caseIdList;
    }

    /**
     * Gets the Data Matrix.
     *
     * @return 2D Matrix of Data.
     */
    public String[][] getMatrix() {
        return matrix;
    }

    /**
     * Gets the Genetic Profile.
     *
     * @return Genetic Profile Object.
     */
    public GeneticProfile getGeneticProfile() {
        return geneticProfile;
    }

    /**
     * Gets the value of gene X in case Y.
     *
     * @param geneSymbol Gene Symbol.
     * @param caseId     Case ID.
     * @return value.
     */
    public String getValue(String geneSymbol, String caseId) {
        String key = createKey(geneSymbol, caseId);
        return mapFromGeneAndCaseToGeneProperties.get(key);
    }

    /**
     * Gets the value of gene X in case Y.
     *
     * @param geneSymbol Gene Symbol.
     * @param caseId     Case ID.
     * @return value.
     */
    public ValueParser getValueParsed(String geneSymbol, String caseId, double zScoreThreshold) {
        String key = createKey(geneSymbol, caseId);
        String value = mapFromGeneAndCaseToGeneProperties.get(key);
        if (value != null) {
            return new ValueParser (value, zScoreThreshold);
        }
        return null;
    }

    /**
     * Gets list of case Ids.
     *
     * @return ArrayList of Case IDs.
     */
    public ArrayList<String> getCaseIdList() {
        return caseIdList;
    }

    /**
     * Gets list of gene symbols.
     *
     * @return ArrayList of Gene Symbols.
     */
    public ArrayList<String> getGeneList() {
        return geneList;
    }

    /**
     * Process the data matrix.
     */
    private void processMatrix() {
        //  First, extract the case IDs
        if (matrix[0].length > 0) {
            for (int cols = 2; cols < matrix[0].length; cols++) {
                String caseId = matrix[0][cols];
                caseIdList.add(caseId);
            }
        }

        //  Then, extract the gene list
        if (matrix.length > 0) {
            for (int rows = 1; rows < matrix.length; rows++) {
                String geneSymbol = matrix[rows][1];
                geneList.add(geneSymbol);
            }
        }

        //  Then, store to hashtable for quick look up
        for (int rows = 1; rows < matrix.length; rows++) {
            for (int cols = 2; cols < matrix[0].length; cols++) {
                String value = matrix[rows][cols];
                String caseId = matrix[0][cols];
                String geneSymbol = matrix[rows][1];
                String key = createKey(geneSymbol, caseId);
                mapFromGeneAndCaseToGeneProperties.put(key, value);
            }
        }
    }

    /**
     * Create gene + case ID key.
     *
     * @param geneSymbol gene symbol.
     * @param caseId     case ID.
     * @return hash key.
     */
    private String createKey(String geneSymbol, String caseId) {
        return geneSymbol + ":" + caseId;
    }
}
