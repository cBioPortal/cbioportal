package org.mskcc.portal.tool.bundle;

import org.mskcc.cgds.model.ExtendedMutation;
import org.mskcc.portal.model.ProfileData;
import org.mskcc.portal.util.ValueParser;
import org.mskcc.cgds.model.GeneticAlterationType;

import java.util.ArrayList;
import java.util.HashMap;

public class BundleHelper {

    public static boolean isCaseGermlineMutated (String caseId, String geneSymbol,
         HashMap<String, ArrayList<ExtendedMutation>> mutationMap) {
        ArrayList<ExtendedMutation> mutationList = mutationMap.get(geneSymbol);
        if (mutationList != null) {
            for (ExtendedMutation mutation:  mutationList) {
                if (mutation.getCaseId().equalsIgnoreCase(caseId)) {
                    if (mutation.getMutationStatus().equalsIgnoreCase("Germline")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isCaseSomaticallyMutated (String caseId, String geneSymbol,
         HashMap<String, ArrayList<ExtendedMutation>> mutationMap) {
        ArrayList<ExtendedMutation> mutationList = mutationMap.get(geneSymbol);
        if (mutationList != null) {
            for (ExtendedMutation mutation:  mutationList) {
                if (mutation.getCaseId().equalsIgnoreCase(caseId)) {
                    if (!mutation.getMutationStatus().equalsIgnoreCase("Germline")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isCaseEpigeneticallySilenced (String caseId, String geneSymbol,
         HashMap<String, ProfileData> binaryMap) {
        ProfileData pData = binaryMap.get(geneSymbol);
        if (pData != null) {
            ValueParser valueParser = pData.getValueParsed(geneSymbol, caseId, 0);
            String value = valueParser.getOriginalValue();
            if (value.equals("1")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean isCaseAmplified (String caseId, String geneSymbol,
         HashMap<String, ProfileData> cnaMap) {
        ProfileData pData = cnaMap.get(geneSymbol);
        if (pData != null) {
            String value = pData.getValue(geneSymbol, caseId);
            if (value.equalsIgnoreCase(GeneticAlterationType.AMPLIFICATION)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean isCaseHomozygouslyDeleted (String caseId, String geneSymbol,
         HashMap<String, ProfileData> cnaMap) {
        ProfileData pData = cnaMap.get(geneSymbol);
        if (pData != null) {
            String value = pData.getValue(geneSymbol, caseId);
            if (value.equalsIgnoreCase(GeneticAlterationType.HOMOZYGOUS_DELETION)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
