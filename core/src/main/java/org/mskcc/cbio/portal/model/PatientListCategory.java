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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration for Patient List Controlled Vocabulary (CV) Category.
 */
public enum PatientListCategory {
    ALL_CASES_IN_STUDY("all_cases_in_study"),
    ALL_CASES_WITH_MUTATION_DATA("all_cases_with_mutation_data"),
    ALL_CASES_WITH_CNA_DATA("all_cases_with_cna_data"),
    ALL_CASES_WITH_LOG2_CNA_DNA("all_cases_with_log2_cna_data"),
    ALL_CASES_WITH_METHYLATION_DATA("all_cases_with_methylation_data"),
    ALL_CASES_WITH_MRNA_ARRAY_DATA("all_cases_with_mrna_array_data"),
    ALL_CASES_WITH_MRNA_RNA_SEQ_DATA("all_cases_with_mrna_rnaseq_data"),
    ALL_CASES_WITH_RPPA_DATA("all_cases_with_rppa_data"),
    ALL_CASES_WITH_MICRO_RNA_DATA("all_cases_with_microrna_data"),
    ALL_CASES_WITH_MUTATION_AND_CNA_DATA("all_cases_with_mutation_and_cna_data"),
    ALL_CASES_WITH_MUTATION_AND_CNA_AND_MRNA_DATA("all_cases_with_mutation_and_cna_and_mrna_data"),
    OTHER("other");

    // Init the look up map.
    private static final Map<String, PatientListCategory> lookup
            = new HashMap<String, PatientListCategory>();

    static {
        for(PatientListCategory c : EnumSet.allOf(PatientListCategory.class))  {
            lookup.put(c.getCategory(), c);
        }
    }

    private String category;

    private PatientListCategory(String category) {
        this.category = category;
    }

    public String getCategory() { return category; }

    /**
     * Gets the matching category by category name.
     * @param category  category name.
     * @return PatientListCategory Object.
     */
    public static PatientListCategory get(String category) {
        PatientListCategory match = lookup.get(category);
        if (match != null) {
            return match;
        } else {
            StringBuffer validOptions = new StringBuffer();
            for(PatientListCategory c : EnumSet.allOf(PatientListCategory.class))  {
                validOptions.append(c.getCategory() + ", ");
            }
            String validStr = validOptions.substring(0, validOptions.length()-2) + ".";
            throw new IllegalArgumentException("Invalid Patient List Category:  " + category
                + ".  Valid options are:  " + validStr);
        }
    }
}