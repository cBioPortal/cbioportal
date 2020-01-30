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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import org.junit.Assert;
import org.junit.Test;
import org.mskcc.cbio.maf.MafRecord;
import org.mskcc.cbio.maf.MafUtil;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.model.AlleleSpecificCopyNumber;
import org.mskcc.cbio.portal.model.ExtendedMutation;
import org.mskcc.cbio.portal.model.ExtendedMutation.MutationEvent;
import org.mskcc.cbio.portal.model.GeneticProfile;

/**
 *
 * @author ochoaa
 */
public class TestMafUtil {
    private final String ASCN_NAMESPACE = "ascn";
    private final Set<String> VALID_NAMESPACES = new LinkedHashSet<>(
        Arrays.asList("namespace1", "namespace2")
    );
    private final String INVALID_NAMESPACE = "invalid_namespace";

    @Test
    public void testResolveTumorSeqAllele() {
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("Error messages:");

        int failCount = 0;
        for (ExtendedMutation record : makeMockExtendedMutationRecords()) {
            String resolvedAllele = MafUtil.resolveTumorSeqAllele(
                record.getReferenceAllele(),
                record.getTumorSeqAllele1(),
                record.getTumorSeqAllele2()
            );
            if (!resolvedAllele.equals(record.getTumorSeqAllele())) {
                failCount++;
                errorMessage
                    .append(
                        "\n\tResolved tumor seq allele does not match expected value:  "
                    )
                    .append(resolvedAllele)
                    .append(" != ")
                    .append(record.getTumorSeqAllele());
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
        String referenceAllele;
        String tumorSeqAllele;
        String tumorSeqAllele1;
        String tumorSeqAllele2;

        ExtendedMutation mut = new ExtendedMutation();
        MutationEvent me = new MutationEvent();

        // mock del variant
        referenceAllele = "AACG";
        tumorSeqAllele = "-";
        tumorSeqAllele1 = "AACG";
        tumorSeqAllele2 = "-";
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);

        referenceAllele = "AACG";
        tumorSeqAllele = "-";
        tumorSeqAllele1 = "-";
        tumorSeqAllele2 = "AACG";
        mut = new ExtendedMutation();
        me = new MutationEvent();
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);

        // mock insertion variants
        referenceAllele = "-";
        tumorSeqAllele = "ATGC";
        tumorSeqAllele1 = "ATGC";
        tumorSeqAllele2 = "-";
        mut = new ExtendedMutation();
        me = new MutationEvent();
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);

        referenceAllele = "-";
        tumorSeqAllele = "ATGC";
        tumorSeqAllele1 = "-";
        tumorSeqAllele2 = "ATGC";
        mut = new ExtendedMutation();
        me = new MutationEvent();
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);

        referenceAllele = "-";
        tumorSeqAllele = "C";
        tumorSeqAllele1 = "C";
        tumorSeqAllele2 = "A";
        mut = new ExtendedMutation();
        me = new MutationEvent();
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);

        // mock indel variants
        referenceAllele = "AACG";
        tumorSeqAllele = "ATGC";
        tumorSeqAllele1 = "ATGC";
        tumorSeqAllele2 = "TGCA";
        mut = new ExtendedMutation();
        me = new MutationEvent();
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);

        referenceAllele = "AAGA";
        tumorSeqAllele = "CA";
        tumorSeqAllele1 = "-";
        tumorSeqAllele2 = "CA";
        mut = new ExtendedMutation();
        me = new MutationEvent();
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);

        // mock cases where snps preferred over dels
        referenceAllele = "A";
        tumorSeqAllele = "T";
        tumorSeqAllele1 = "T";
        tumorSeqAllele2 = "C";
        mut = new ExtendedMutation();
        me = new MutationEvent();
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);

        referenceAllele = "A";
        tumorSeqAllele = "C";
        tumorSeqAllele1 = "A";
        tumorSeqAllele2 = "C";
        mut = new ExtendedMutation();
        me = new MutationEvent();
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);

        referenceAllele = "A";
        tumorSeqAllele = "C";
        tumorSeqAllele1 = "-";
        tumorSeqAllele2 = "C";
        mut = new ExtendedMutation();
        me = new MutationEvent();
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);

        referenceAllele = "A";
        tumorSeqAllele = "C";
        tumorSeqAllele1 = "C";
        tumorSeqAllele2 = "-";
        mut = new ExtendedMutation();
        me = new MutationEvent();
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);

        // test null cases
        referenceAllele = "G";
        tumorSeqAllele = "-";
        tumorSeqAllele1 = null;
        tumorSeqAllele2 = "-";
        mut = new ExtendedMutation();
        me = new MutationEvent();
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);

        referenceAllele = "G";
        tumorSeqAllele = "A";
        tumorSeqAllele1 = "NA";
        tumorSeqAllele2 = "A";
        mut = new ExtendedMutation();
        me = new MutationEvent();
        me.setReferenceAllele(referenceAllele);
        me.setTumorSeqAllele(tumorSeqAllele);
        mut.setTumorSeqAllele1(tumorSeqAllele1);
        mut.setTumorSeqAllele2(tumorSeqAllele2);
        mut.setEvent(me);
        records.add(mut);

        return records;
    }

    /**
     * Test that expected namespaces are resolved correctly from MAF header.
     * @throws Exception
     */
    @Test
    public void testResolveAnnotationNamespaces() throws Exception {
        FileReader reader = new FileReader(
            "src/test/resources/data_mutations_extended_json_annotation.txt"
        );
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine().trim();
        while (line.startsWith("#")) {
            line = buf.readLine().trim();
        }
        MafUtil mafUtil = new MafUtil(line, VALID_NAMESPACES);
        Assert.assertTrue(mafUtil.getNamespaceIndexMap().size() == 2);
        for (String ns : VALID_NAMESPACES) {
            if (!mafUtil.getNamespaceIndexMap().containsKey(ns)) {
                Assert.fail(
                    "maUtil.getNamespaceIndexMap() is missing expected namespace: '" +
                    ns +
                    "'"
                );
            }
        }
        Assert.assertFalse(
            "Invalid namespace found in mafUtil.getNamespaceIndexMap(): " +
            INVALID_NAMESPACE,
            mafUtil.getNamespaceIndexMap().containsKey(INVALID_NAMESPACE)
        );
        buf.close();
    }

    /**
     * Test that ASCN namespace is resolved correctly from MAF.
     * @throws Exception
     */
    @Test
    public void testResolveAscnAnnotationNamespace() throws Exception {
        FileReader reader = new FileReader(
            "src/test/resources/data_mutations_extended_json_annotation.txt"
        );
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine().trim();
        while (line.startsWith("#")) {
            line = buf.readLine().trim();
        }
        MafUtil mafUtil = new MafUtil(
            line,
            new LinkedHashSet<>(Arrays.asList(ASCN_NAMESPACE))
        );
        Assert.assertTrue(mafUtil.getNamespaceIndexMap().size() == 1);
        Assert.assertTrue(
            mafUtil.getNamespaceIndexMap().containsKey(ASCN_NAMESPACE)
        );

        List<AlleleSpecificCopyNumber> ascnRecords = new ArrayList<>();
        while ((line = buf.readLine()) != null) {
            if (!line.startsWith("#") && line.trim().length() > 0) {
                MafRecord record = mafUtil.parseRecord(line);
                // every record in test MAF should have ASCN data
                Assert.assertTrue(
                    record.getNamespacesMap().containsKey(ASCN_NAMESPACE)
                );
                Map<String, String> ascnData = record
                    .getNamespacesMap()
                    .get(ASCN_NAMESPACE);
                ascnRecords.add(new AlleleSpecificCopyNumber(ascnData));
            }
        }
        buf.close();
    }
}
