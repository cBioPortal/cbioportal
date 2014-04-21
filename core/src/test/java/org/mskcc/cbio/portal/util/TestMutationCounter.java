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

package org.mskcc.cbio.portal.util;

import junit.framework.TestCase;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.ExtendedMutation;
import org.mskcc.cbio.portal.model.ExtendedMutationMap;

import java.util.ArrayList;

/**
 * Tests the Mutation Counder Class.
 *
 * @author Ethan Cerami.
 */
public class TestMutationCounter extends TestCase {
    private static final String BRCA1 = "BRCA1";
    private static final String BRCA2 = "BRCA2";
    private static final String CASE_A = "A";
    private static final String CASE_B = "B";
    private static final String CASE_C = "C";
    private static final String CASE_D = "D";

    public void test1() {
        CanonicalGene brca1 = new CanonicalGene(672, BRCA1);
        CanonicalGene brca2 = new CanonicalGene(675, BRCA2);

        //  By default, all these mutations are somatic
        ExtendedMutation mutation1 = createMutation1(brca1, CASE_A);
        ExtendedMutation mutation2 = createMutation1(brca1, CASE_A);
        ExtendedMutation mutation3 = createMutation1(brca2, CASE_A);

        ArrayList<ExtendedMutation> mutationList =
                createMutationList(mutation1, mutation2, mutation3);

        ArrayList<String> caseList = new ArrayList<String>();
        caseList.add(CASE_A);
        caseList.add(CASE_B);

        ExtendedMutationMap mutationMap = new ExtendedMutationMap(mutationList, caseList);
        MutationCounter mutationCounter = new MutationCounter (BRCA1, mutationMap);
        assertEquals (0.5, mutationCounter.getMutationRate(), 0.01);
        assertEquals (0.5, mutationCounter.getSomaticMutationRate(), 0.01);
        assertEquals (0.0, mutationCounter.getGermlineMutationRate(), 0.01);

        assertEquals ("[Somatic Mutation Rate:  50%]", mutationCounter.getTextSummary());
    }

    public void test2() {
        CanonicalGene brca1 = new CanonicalGene(672, BRCA1);
        CanonicalGene brca2 = new CanonicalGene(675, BRCA2);

        //  Case A will contain a germline and somatic mutation
        ExtendedMutation mutation1 = createMutation1(brca1, CASE_A);
        mutation1.setMutationStatus("germline");
        ExtendedMutation mutation2 = createMutation1(brca1, CASE_A);
        ExtendedMutation mutation3 = createMutation1(brca2, CASE_A);

        ArrayList<ExtendedMutation> mutationList =
                createMutationList(mutation1, mutation2, mutation3);

        ArrayList<String> caseList = new ArrayList<String>();
        caseList.add(CASE_A);
        caseList.add(CASE_B);

        ExtendedMutationMap mutationMap = new ExtendedMutationMap(mutationList, caseList);
        MutationCounter mutationCounter = new MutationCounter (BRCA1, mutationMap);
        assertEquals (0.5, mutationCounter.getMutationRate(), 0.01);
        assertEquals (0.5, mutationCounter.getSomaticMutationRate(), 0.01);
        assertEquals (0.5, mutationCounter.getGermlineMutationRate(), 0.01);
        assertEquals ("[Germline Mutation Rate:  50%, Somatic Mutation Rate:  50%]",
                mutationCounter.getTextSummary());
    }

    public void test3() {
        CanonicalGene brca1 = new CanonicalGene(672, BRCA1);
        CanonicalGene brca2 = new CanonicalGene(675, BRCA2);

        //  By default, all these mutations are somatic
        ExtendedMutation mutation1 = createMutation1(brca1, CASE_A);
        ExtendedMutation mutation2 = createMutation1(brca1, CASE_A);
        ExtendedMutation mutation3 = createMutation1(brca2, CASE_A);

        ArrayList<ExtendedMutation> mutationList =
                createMutationList(mutation1, mutation2, mutation3);

        ArrayList<String> caseList = new ArrayList<String>();
        caseList.add(CASE_A);
        caseList.add(CASE_B);
        caseList.add(CASE_C);
        caseList.add(CASE_D);

        ExtendedMutationMap mutationMap = new ExtendedMutationMap(mutationList, caseList);
        MutationCounter mutationCounter = new MutationCounter (BRCA1, mutationMap);
        assertEquals (0.25, mutationCounter.getMutationRate(), 0.01);
        assertEquals (0.25, mutationCounter.getSomaticMutationRate(), 0.01);
        assertEquals (0.0, mutationCounter.getGermlineMutationRate(), 0.01);
        assertEquals ("[Somatic Mutation Rate:  25%]", mutationCounter.getTextSummary());
    }

    private ArrayList<ExtendedMutation> createMutationList(ExtendedMutation
            mutation1, ExtendedMutation mutation2, ExtendedMutation mutation3) {
        ArrayList<ExtendedMutation> mutationList = new ArrayList<ExtendedMutation>();
        mutationList.add(mutation1);
        mutationList.add(mutation2);
        mutationList.add(mutation3);
        return mutationList;
    }

    private ExtendedMutation createMutation1(CanonicalGene
            gene, String caseId) {
        ExtendedMutation mutation2 = new ExtendedMutation();
        mutation2.setGene(gene);
        mutation2.setCaseId(caseId);
        mutation2.setProteinChange("C22G");
        return mutation2;
    }
}
