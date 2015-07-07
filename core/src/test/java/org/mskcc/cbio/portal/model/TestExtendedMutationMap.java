/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.model;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;



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
