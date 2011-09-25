package org.mskcc.portal.model;

import java.util.ArrayList;
import java.util.HashMap;
import org.mskcc.cgds.model.ExtendedMutation;

/**
 * A HashMap of ExtendedMutation Objects, indexed by Gene and Case ID.
 *
 * @author Ethan Cerami.
 */
public class ExtendedMutationMap {
    private static final String DELIMITER = ":";
    private HashMap <String, ArrayList<ExtendedMutation>> mutationCaseMap =
            new HashMap<String, ArrayList<ExtendedMutation>>();
    private HashMap <String, ArrayList<ExtendedMutation>> mutationMap =
            new HashMap<String, ArrayList<ExtendedMutation>>();
    private ArrayList<String> caseList;

    public ExtendedMutationMap (ArrayList<ExtendedMutation> mutationList,
            ArrayList<String> caseList) {
        this.caseList = caseList;
        for (ExtendedMutation mutation:  mutationList) {
            String key = getKey(mutation.getGeneSymbol(), mutation.getCaseId());
            appendToMap(key, mutation, mutationCaseMap);
            appendToMap(mutation.getGeneSymbol(), mutation, mutationMap);
        }
    }

    /**
     * Gets all Extended Mutations, associated with the specified Gene / Case ID combination.
     * @param geneSymbol    Gene Symbol.
     * @param caseId        Case ID.
     * @return ArrayList of ExtendedMutation Objects.
     */
    public ArrayList <ExtendedMutation> getExtendedMutations(String geneSymbol, String caseId) {
        String key = getKey(geneSymbol.toUpperCase(), caseId);
        return mutationCaseMap.get(key);
    }

    /**
     * Gets all Extended Mutations, associated with the specified Gene.
     * @param geneSymbol    Gene Symbol.
     * @return ArrayList of ExtendedMutation Objects.
     */
    public ArrayList <ExtendedMutation> getExtendedMutations(String geneSymbol) {
        return mutationMap.get(geneSymbol.toUpperCase());
    }

    public int getNumExtendedMutations(String geneSymbol) {
        ArrayList<ExtendedMutation> mutationList = mutationMap.get(geneSymbol.toUpperCase());
        if (mutationList == null) {
            return 0;
        } else {
            return mutationList.size();
        }
    }

    public int getNumGenesWithExtendedMutations() {
        return mutationMap.keySet().size();
    }

    public ArrayList<String> getCaseList() {
        return caseList;
    }

    private void appendToMap(String key, ExtendedMutation mutation,
            HashMap <String, ArrayList<ExtendedMutation>> map) {
        if (map.containsKey(key)) {
            ArrayList<ExtendedMutation> currentList = map.get(key);
            currentList.add(mutation);
        } else {
            ArrayList <ExtendedMutation> currentList = new ArrayList <ExtendedMutation> ();
            currentList.add(mutation);
            map.put(key, currentList);
        }
    }

    private String getKey(String geneSymbol, String caseId) {
        return geneSymbol + DELIMITER + caseId;
    }
}