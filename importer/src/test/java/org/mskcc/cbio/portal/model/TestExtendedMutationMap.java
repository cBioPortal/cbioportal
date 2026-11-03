/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.model;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Tests the Extended Mutation Map.
 *
 * @author Ethan Cerami.
 */
public class TestExtendedMutationMap {
    private static final String BRCA1 = "BRCA1";
    private static final String BRCA2 = "BRCA2";
    private static final Integer SAMPLE_A = 1;

    @Test
    public void test1() {
        CanonicalGene brca1 = new CanonicalGene(672, BRCA1);
        CanonicalGene brca2 = new CanonicalGene(675, BRCA2);
        ExtendedMutation mutation1 = createMutation1(brca1, SAMPLE_A);
        ExtendedMutation mutation2 = createMutation1(brca1, SAMPLE_A);
        ExtendedMutation mutation3 = createMutation1(brca2, SAMPLE_A);

        ArrayList<ExtendedMutation> mutationList =
                createMutationList(mutation1, mutation2, mutation3);

        ArrayList<Integer> sampleList = new ArrayList<Integer>();
        sampleList.add(SAMPLE_A);

        ExtendedMutationMap map = new ExtendedMutationMap(mutationList, sampleList);
        ArrayList<ExtendedMutation> mutationReturnList = map.getExtendedMutations(BRCA1, SAMPLE_A);
        assertEquals (2, mutationReturnList.size());

        // Try with mixed sample
        mutationReturnList = map.getExtendedMutations("brCA1", SAMPLE_A);
        assertEquals (2, mutationReturnList.size());

        mutationReturnList = map.getExtendedMutations(BRCA2, SAMPLE_A);
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

    private ExtendedMutation createMutation1(CanonicalGene gene, Integer sampleId) {
        ExtendedMutation mutation2 = new ExtendedMutation();
        mutation2.setGene(gene);
        mutation2.setSampleId(sampleId);
        mutation2.setProteinChange("C22G");
        return mutation2;
    }

}
