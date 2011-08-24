package org.mskcc.portal.util;

import org.mskcc.portal.model.ExtendedMutationMap;
import org.mskcc.cgds.model.ExtendedMutation;

import java.util.ArrayList;

public class MutationCounter {
    private int numSomaticMutations = 0;
    private int numGermlineMutations = 0;
    private int numMutations = 0;

    private int totalNumCases;

    public MutationCounter (String gene, ExtendedMutationMap mutationMap,
            ArrayList<String> caseList) {
        totalNumCases = caseList.size();
        for (String caseId:  caseList) {
            ArrayList<ExtendedMutation> mutationList =
                mutationMap.getMutations(gene, caseId);
            boolean somaticMutated = false;
            boolean germlineMutated = false;
            if (mutationList != null && mutationList.size() > 0) {
                for (ExtendedMutation mutation:  mutationList) {
                    if (mutation.getMutationStatus().equalsIgnoreCase("somatic")
                            || mutation.getMutationStatus().equalsIgnoreCase("valid")
                            || mutation.getMutationStatus().equalsIgnoreCase("unknown")) {
                        somaticMutated = true;
                    } else if (mutation.getMutationStatus().equalsIgnoreCase("germline")) {
                        germlineMutated = true;
                    }
                }
                numMutations++;
            }
            if (somaticMutated) {
                numSomaticMutations++;
            }
            if (germlineMutated) {
                numGermlineMutations++;
            }
        }
    }

    public double getSomaticMutationRate() {
        return numSomaticMutations / (float) totalNumCases;
    }

    public double getGermlineMutationRate() {
        return numGermlineMutations / (float) totalNumCases;
    }

    public double getMutationRate() {
        return numMutations / (float) totalNumCases;
    }
}
