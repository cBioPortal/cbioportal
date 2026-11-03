/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.util;

import java.io.*;
import java.util.*;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.shared.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.util.GeneticProfileReader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * JUnit test for GeneticProfileReader class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestGeneticProfileReader extends IntegrationTestBase {

    @Test
    public void testGeneticProfileReader() throws Exception {
        // load cancers
        // TBD: change this to use getResourceAsStream()
        // TBD: change this to use getResourceAsStream()

        File file = new File("target/test-classes/genetic_profile_test.txt");
        GeneticProfile geneticProfile = GeneticProfileReader.loadGeneticProfile(file);
        assertEquals("Barry", geneticProfile.getTargetLine());
        assertEquals("Blah Blah.", geneticProfile.getProfileDescription());

        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub");
        assertEquals(cancerStudy.getInternalId(), geneticProfile.getCancerStudyId());

        List<String> profileNames = DaoGeneticProfile.getAllGeneticProfiles
                (cancerStudy.getInternalId()).stream().map(GeneticProfile::getProfileName).toList();
        assertTrue(profileNames.contains("Putative copy-number alterations from GISTIC"));
        assertEquals(GeneticAlterationType.COPY_NUMBER_ALTERATION,
                geneticProfile.getGeneticAlterationType());
    }

    @Test(expected = RuntimeException.class)
    public void testTreatmentResponseMissingPivotField() throws IOException, DaoException {
        File file = new File("target/test-classes/test_meta_treatment_missing_pivot.txt");
        GeneticProfileReader.loadGeneticProfile(file);
    }

    @Test(expected = RuntimeException.class)
    public void testTreatmentResponseMissingSortOrderField() throws IOException, DaoException {
        File file = new File("target/test-classes/test_meta_treatment_missing_sortorder.txt");
        GeneticProfileReader.loadGeneticProfile(file);
    }

}
