package org.mskcc.cbio.portal.model;

import org.mskcc.cbio.cgds.model.ExtendedMutation;
import org.mskcc.cbio.cgds.model.CanonicalGene;
import org.mskcc.cbio.portal.model.ExtendedMutationMap;

import java.util.ArrayList;

import junit.framework.TestCase;

/**
 * Tests the Extended Mutation Map.
 *
 * @author Ethan Cerami.
 */
public class TestExtendedMutationMap extends TestCase {
    private static final String BRCA1 = "BRCA1";
    private static final String BRCA2 = "BRCA2";
    private static final String CASE_A = "A";

    public void test1() {
        CanonicalGene brca1 = new CanonicalGene(672, BRCA1);
        CanonicalGene brca2 = new CanonicalGene(675, BRCA2);
        ExtendedMutation mutation1 = createMutation1(brca1, CASE_A);
        ExtendedMutation mutation2 = createMutation1(brca1, CASE_A);
        ExtendedMutation mutation3 = createMutation1(brca2, CASE_A);

        ArrayList<ExtendedMutation> mutationList =
                createMutationList(mutation1, mutation2, mutation3);

        ArrayList<String> caseList = new ArrayList<String>();
        caseList.add(CASE_A);

        ExtendedMutationMap map = new ExtendedMutationMap(mutationList, caseList);
        ArrayList<ExtendedMutation> mutationReturnList = map.getExtendedMutations(BRCA1, CASE_A);
        assertEquals (2, mutationReturnList.size());

        // Try with mixed case
        mutationReturnList = map.getExtendedMutations("brCA1", CASE_A);
        assertEquals (2, mutationReturnList.size());

        mutationReturnList = map.getExtendedMutations(BRCA2, CASE_A);
        assertEquals (1, mutationReturnList.size());

        mutationReturnList = map.getExtendedMutations(BRCA1);
        assertEquals(2, mutationReturnList.size());

        assertEquals(2, map.getNumGenesWithExtendedMutations());
        assertEquals(2, map.getNumExtendedMutations(BRCA1));
    }

    private ArrayList<ExtendedMutation> createMutationList(ExtendedMutation mutation1,
            ExtendedMutation mutation2, ExtendedMutation mutation3) {
        ArrayList<ExtendedMutation> mutationList = new ArrayList<ExtendedMutation>();
        mutationList.add(mutation1);
        mutationList.add(mutation2);
        mutationList.add(mutation3);
        return mutationList;
    }

    private ExtendedMutation createMutation1(CanonicalGene gene, String caseId) {
        ExtendedMutation mutation2 = new ExtendedMutation();
        mutation2.setGene(gene);
        mutation2.setCaseId(caseId);
        mutation2.setAminoAcidChange("C22G");
        return mutation2;
    }

}
