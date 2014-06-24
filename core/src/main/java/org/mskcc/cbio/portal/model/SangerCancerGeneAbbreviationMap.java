/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.model;

import java.util.HashMap;

/**
 * Abbreviation Map for the Sanger Cancer Gene Census.
 */
public class SangerCancerGeneAbbreviationMap {
    private HashMap<String, String> abbreviationMap = new HashMap<String, String>();
    private static SangerCancerGeneAbbreviationMap instance;

    public static SangerCancerGeneAbbreviationMap getInstance() {
        if (instance == null) {
            instance = new SangerCancerGeneAbbreviationMap();
        }
        return instance;
    }

    public String getTranslation(String abbreviation) {
        return abbreviationMap.get(abbreviation); 
    }

    private SangerCancerGeneAbbreviationMap () {
        initMap();
    }

    private void initMap() {
        abbreviationMap.put("A", "amplification");
        abbreviationMap.put("AEL", "acute eosinophilic leukemia");
        abbreviationMap.put("AL", "acute leukemia");
        abbreviationMap.put("ALCL", "anaplastic large-cell lymphoma");
        abbreviationMap.put("ALL", "acute lymphocytic leukemia");
        abbreviationMap.put("AML", "acute myelogenous leukemia");
        abbreviationMap.put("AML*", "acute myelogenous leukemia (primarily treatment associated)");
        abbreviationMap.put("APL", "acute promyelocytic leukemia");
        abbreviationMap.put("B-ALL", "B-cell acute lymphocytic leukaemia");
        abbreviationMap.put("B-CLL", "B-cell Lymphocytic leukemia");
        abbreviationMap.put("B-NHL", "B-cell Non-Hodgkin Lymphoma");
        abbreviationMap.put("CLL", "chronic lymphatic leukemia");
        abbreviationMap.put("CML", "chronic myeloid leukemia");
        abbreviationMap.put("CMML", "chronic myelomonocytic leukemia");
        abbreviationMap.put("CNS", "central nervous system");
        abbreviationMap.put("D", "large deletion");
        abbreviationMap.put("DFSP", "dermatofibrosarcoma protuberans");
        abbreviationMap.put("DLBCL", "diffuse large B-cell lymphoma");
        abbreviationMap.put("DLCL", "diffuse large-cell lymphoma");
        abbreviationMap.put("Dom", "dominant");
        abbreviationMap.put("E", "epithelial");
        abbreviationMap.put("F", "frameshift");
        abbreviationMap.put("GIST", "gastrointestinal stromal tumour");
        abbreviationMap.put("JMML", "juvenile myelomonocytic leukemia");
        abbreviationMap.put("L", "leukaemia/lymphoma");
        abbreviationMap.put("M", "mesenchymal");
        abbreviationMap.put("MALT", "mucosa-associated lymphoid tissue lymphoma");
        abbreviationMap.put("MDS", "myelodysplastic syndrome");
        abbreviationMap.put("Mis", "missense");
        abbreviationMap.put("MLCLS", "mediastinal large cell lymphoma with sclerosis");
        abbreviationMap.put("MM", "multiple myeloma");
        abbreviationMap.put("MPD", "Myeloproliferative disorder");
        abbreviationMap.put("N", "nonsense");
        abbreviationMap.put("NHL", "non-Hodgkin lymphoma");
        abbreviationMap.put("NK/T", "natural killer T cell");
        abbreviationMap.put("NSCLC", "non small cell lung cancer");
        abbreviationMap.put("O", "other");
        abbreviationMap.put("PMBL", "primary mediastinal B-cell lymphoma");
        abbreviationMap.put("pre-B All", "pre-B-cell acute lymphoblastic leukaemia");
        abbreviationMap.put("Rec", "reccesive");
        abbreviationMap.put("S", "splice site");
        abbreviationMap.put("T", "translocation");
        abbreviationMap.put("T-ALL", "T-cell acute lymphoblastic leukemia");
        abbreviationMap.put("T-CLL", "T-cell chronic lymphocytic leukaemia");
        abbreviationMap.put("TGCT", "testicular germ cell tumour");
        abbreviationMap.put("T-PLL", "T cell prolymphocytic leukaemia");
    }
}
