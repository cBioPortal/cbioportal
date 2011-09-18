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
    private HashMap <String, ArrayList<ExtendedMutation>> mutationMap =
            new HashMap<String, ArrayList<ExtendedMutation>>();
    private ArrayList<String> caseList;

    public ExtendedMutationMap (ArrayList<ExtendedMutation> mutationList,
            ArrayList<String> caseList) {
        this.caseList = caseList;
        for (ExtendedMutation mutation:  mutationList) {
            String key = getKey(mutation.getGeneSymbol(), mutation.getCaseId());
            appendToMap(key, mutation);
        }
    }

    /**
     * Gets all Mutations, associated with the specified Gene / Case ID combination.
     * @param geneSymbol    Gene Symbol.
     * @param caseId        Case ID.
     * @return ArrayList of ExtendedMutation Objects.
     */
    public ArrayList <ExtendedMutation> getMutations (String geneSymbol, String caseId) {
        String key = getKey(geneSymbol.toUpperCase(), caseId);
        return mutationMap.get(key);
    }

    public ArrayList<String> getCaseList() {
        return caseList;
    }

    private void appendToMap(String key, ExtendedMutation mutation) {
        if (mutationMap.containsKey(key)) {
            ArrayList<ExtendedMutation> currentList = mutationMap.get(key);
            currentList.add(mutation);
        } else {
            ArrayList <ExtendedMutation> currentList = new ArrayList <ExtendedMutation> ();
            currentList.add(mutation);
            mutationMap.put(key, currentList);
        }
    }

    private String getKey(String geneSymbol, String caseId) {
        return geneSymbol + DELIMITER + caseId;
    }
}