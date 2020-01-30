/*
 * Copyright (c) 2015 - 2016 Memorial Sloan-Kettering Cancer Center.
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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.mskcc.cbio.io.WebFileConnect;
import org.mskcc.cbio.portal.model.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.ProfileData;

/**
 * JUnit test for Profile Merger.
 */
public class TestProfileMerger {

    /**
     * Tests the Profile Merger Utility Class.
     *
     * @throws IOException IO Error.
     */
    public void testProfileMerger() throws IOException {
        ArrayList<ProfileData> profileList = new ArrayList<ProfileData>();

        GeneticProfile profile0 = new GeneticProfile(
            "gbm",
            1,
            GeneticAlterationType.COPY_NUMBER_ALTERATION,
            "DISCRETE",
            "CNA",
            "NA",
            true
        );
        // TBD: change this to use getResourceAsStream()
        String matrix0[][] = WebFileConnect.retrieveMatrix(
            new File("target/test-classes/cna_sample.txt")
        );
        ProfileData data0 = new ProfileData(profile0, matrix0);
        profileList.add(data0);

        GeneticProfile profile1 = new GeneticProfile(
            "gbm",
            1,
            GeneticAlterationType.MUTATION_EXTENDED,
            "MAF",
            "MUTATION",
            "NA",
            true
        );
        // TBD: change this to use getResourceAsStream()
        String matrix1[][] = WebFileConnect.retrieveMatrix(
            new File("target/test-classes/mutation_sample.txt")
        );
        ProfileData data1 = new ProfileData(profile1, matrix1);
        profileList.add(data1);

        ProfileMerger merger = new ProfileMerger(profileList);
        ProfileData mergedProfile = merger.getMergedProfile();
        String value = mergedProfile.getValue("BRCA1", "TCGA-02-0004");
        assertEquals(
            GeneticAlterationType.COPY_NUMBER_ALTERATION.name() + ":1;",
            value
        );
        value = mergedProfile.getValue("BRCA2", "TCGA-06-0169");
        assertEquals(
            GeneticAlterationType.COPY_NUMBER_ALTERATION.name() +
            ":0;" +
            GeneticAlterationType.MUTATION_EXTENDED.name() +
            ":P920S;",
            value
        );
        //value = mergedProfile.getValue("BRCA1", "TCGA-06-0169");
        //assertEquals("", value);
    }
}
