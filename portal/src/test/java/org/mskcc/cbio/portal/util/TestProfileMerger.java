package org.mskcc.cbio.portal.test.util;

import junit.framework.TestCase;
import org.mskcc.cbio.portal.model.ProfileData;
import org.mskcc.cbio.portal.util.ProfileMerger;
import org.mskcc.cbio.portal.util.WebFileConnect;
import org.mskcc.cbio.cgds.model.GeneticProfile;
import org.mskcc.cbio.cgds.model.GeneticAlterationType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * JUnit test for Profile Merger.
 */
public class TestProfileMerger extends TestCase {

    /**
     * Tests the Profile Merger Utility Class.
     *
     * @throws IOException IO Error.
     */
    public void testProfileMerger() throws IOException {
        ArrayList<ProfileData> profileList = new ArrayList<ProfileData>();

        GeneticProfile profile0 = new GeneticProfile
                ("gbm", 1, GeneticAlterationType.COPY_NUMBER_ALTERATION,
                        "CNA", "NA", true);
        String matrix0[][] = WebFileConnect.retrieveMatrix(new File("/cna_sample.txt"));
        ProfileData data0 = new ProfileData(profile0, matrix0);
        profileList.add(data0);

        GeneticProfile profile1 = new GeneticProfile
                ("gbm", 1, GeneticAlterationType.MUTATION_EXTENDED,
                        "MUTATION", "NA", true);
        String matrix1[][] = WebFileConnect.retrieveMatrix(
                new File("/mutation_sample.txt"));
        ProfileData data1 = new ProfileData(profile1, matrix1);
        profileList.add(data1);

        ProfileMerger merger = new ProfileMerger(profileList);
        ProfileData mergedProfile = merger.getMergedProfile();
        String value = mergedProfile.getValue("BRCA1", "TCGA-02-0004");
        assertEquals(GeneticAlterationType.COPY_NUMBER_ALTERATION + ":1;", value);
        value = mergedProfile.getValue("BRCA2", "TCGA-06-0169");
        assertEquals(GeneticAlterationType.COPY_NUMBER_ALTERATION + ":0;"
                + GeneticAlterationType.MUTATION_EXTENDED + ":P920S;", value);
        //value = mergedProfile.getValue("BRCA1", "TCGA-06-0169");
        //assertEquals("", value);
    }
}
