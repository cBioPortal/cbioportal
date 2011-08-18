package org.mskcc.portal.model;

import java.util.ArrayList;
import java.util.HashMap;
import org.mskcc.cgds.model.ExtendedMutation;

public class ExtendedMutationMap {
    HashMap <String, ArrayList<ExtendedMutation>> mutationMap =
            new HashMap<String, ArrayList<ExtendedMutation>>();

    public ExtendedMutationMap (ArrayList<ExtendedMutation> mutationList) {
        for (ExtendedMutation mutation:  mutationList) {
            String key = mutation.getGeneSymbol() + ":" + mutation.getCaseId();
            if (mutationMap.containsKey(key)) {
                ArrayList <ExtendedMutation> currentList = mutationMap.get(key);
                currentList.add(mutation);
            } else {
                ArrayList <ExtendedMutation> currentList = new ArrayList <ExtendedMutation> ();
                currentList.add(mutation);
                mutationMap.put(key, currentList);
            }
        }
    }

    public ArrayList <ExtendedMutation> getMutations (String geneSymbol, String caseId) {
        return mutationMap.get(geneSymbol + ":" + caseId);
    }

    public int getSize() {
        return mutationMap.size();
    }
}
