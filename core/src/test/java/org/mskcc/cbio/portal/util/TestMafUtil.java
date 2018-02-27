/*
 * Copyright (c) 2018 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.util;

import org.mskcc.cbio.portal.model.ExtendedMutation;
import org.mskcc.cbio.portal.model.ExtendedMutation.MutationEvent;

import java.util.*;
import org.junit.Assert;
import org.junit.Test;
import org.mskcc.cbio.maf.MafUtil;

/**
 *
 * @author ochoaa
 */
public class TestMafUtil {

    @Test
    public void testResolveTumorSeqAllele() {
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("Error messages:");

        int failCount = 0;
        for (ExtendedMutation record : makeMockExtendedMutationRecords()) {
            String resolvedAllele = MafUtil.resolveTumorSeqAllele(record.getReferenceAllele(),
                    record.getTumorSeqAllele1(), record.getTumorSeqAllele2());
            if (!resolvedAllele.equals(record.getTumorSeqAllele())) {
                failCount++;
                errorMessage.append("\n\tResolved tumor seq allele does not match expected value:  ")
                        .append(resolvedAllele).append(" != ").append(record.getTumorSeqAllele());
            }
        }
        if (failCount > 0) {
            Assert.fail(errorMessage.toString());
        }
    }

    /**
     * Returns mock data for testing MafUtil.resolveTumorSeqAllele(...).
     * referenceAllele, tumorSeqAllele1, tumorSeqAllele2 = input data
     * tumorSeqAllele = the expected allele to be resolved given a reference allele,
     *      tumor seq allele 1, and tumor seq allele 2
     * @return
     */
    private List<ExtendedMutation> makeMockExtendedMutationRecords() {
        List<ExtendedMutation> records = new ArrayList<>();
        String referenceAllele; String tumorSeqAllele;
        String tumorSeqAllele1; String tumorSeqAllele2;

        ExtendedMutation mut = new ExtendedMutation();
        MutationEvent me = new MutationEvent();

        // mock del variant
        referenceAllele = "AACG"; tumorSeqAllele = "-";
        tumorSeqAllele1 = "AACG"; tumorSeqAllele2 = "-";
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);

        referenceAllele = "AACG"; tumorSeqAllele = "-";
        tumorSeqAllele1 = "-"; tumorSeqAllele2 = "AACG";
        mut = new ExtendedMutation(); me = new MutationEvent();
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);

        // mock insertion variants
        referenceAllele = "-"; tumorSeqAllele = "ATGC";
        tumorSeqAllele1 = "ATGC"; tumorSeqAllele2 = "-";
        mut = new ExtendedMutation(); me = new MutationEvent();
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);

        referenceAllele = "-"; tumorSeqAllele = "ATGC";
        tumorSeqAllele1 = "-"; tumorSeqAllele2 = "ATGC";
        mut = new ExtendedMutation(); me = new MutationEvent();
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);

        referenceAllele = "-"; tumorSeqAllele = "C";
        tumorSeqAllele1 = "C"; tumorSeqAllele2 = "A";
        mut = new ExtendedMutation(); me = new MutationEvent();
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);

        // mock indel variants
        referenceAllele = "AACG"; tumorSeqAllele = "ATGC";
        tumorSeqAllele1 = "ATGC"; tumorSeqAllele2 = "TGCA";
        mut = new ExtendedMutation(); me = new MutationEvent();
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);

        referenceAllele = "AAGA"; tumorSeqAllele = "CA";
        tumorSeqAllele1 = "-"; tumorSeqAllele2 = "CA";
        mut = new ExtendedMutation(); me = new MutationEvent();
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);

        // mock cases where snps preferred over dels
        referenceAllele = "A"; tumorSeqAllele = "T";
        tumorSeqAllele1 = "T"; tumorSeqAllele2 = "C";
        mut = new ExtendedMutation(); me = new MutationEvent();
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);

        referenceAllele = "A"; tumorSeqAllele = "C";
        tumorSeqAllele1 = "A"; tumorSeqAllele2 = "C";
        mut = new ExtendedMutation(); me = new MutationEvent();
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);

        referenceAllele = "A"; tumorSeqAllele = "C";
        tumorSeqAllele1 = "-"; tumorSeqAllele2 = "C";
        mut = new ExtendedMutation(); me = new MutationEvent();
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);

        referenceAllele = "A"; tumorSeqAllele = "C";
        tumorSeqAllele1 = "C"; tumorSeqAllele2 = "-";
        mut = new ExtendedMutation(); me = new MutationEvent();
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);

        // test null cases
        referenceAllele = "G"; tumorSeqAllele = "-";
        tumorSeqAllele1 = null; tumorSeqAllele2 = "-";
        mut = new ExtendedMutation(); me = new MutationEvent();
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);
        
        referenceAllele = "G"; tumorSeqAllele = "A";
        tumorSeqAllele1 = "NA"; tumorSeqAllele2 = "A";
        mut = new ExtendedMutation(); me = new MutationEvent();
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);

        return records;
    }
}
