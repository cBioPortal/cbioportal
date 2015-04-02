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

package org.mskcc.cbio.portal.util;

import junit.framework.TestCase;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.scripts.ImportTypesOfCancers;
import org.mskcc.cbio.portal.scripts.ResetDatabase;

import java.io.File;
import java.util.ArrayList;

/**
 * JUnit test for GeneticProfileReader class.
 */
public class TestGeneticProfileReader extends TestCase {

    public void testGeneticProfileReader() throws Exception {
        ResetDatabase.resetDatabase();
        // load cancers
		// TBD: change this to use getResourceAsStream()
        ImportTypesOfCancers.load(new ProgressMonitor(), new File("target/test-classes/cancers.txt"));

        DaoGeneticProfile.deleteAllRecords();

        DaoCancerStudy.deleteAllRecords();

        CancerStudy cancerStudy = new CancerStudy("GBM", "GBM Description",
                "gbm", "gbm", true);
        DaoCancerStudy.addCancerStudy(cancerStudy);
		// TBD: change this to use getResourceAsStream()
        File file = new File("target/test-classes/genetic_profile_test.txt");
        GeneticProfile geneticProfile = GeneticProfileReader.loadGeneticProfile(file);
        assertEquals("Barry", geneticProfile.getTargetLine());
        assertEquals("Blah Blah.", geneticProfile.getProfileDescription());

        cancerStudy = DaoCancerStudy.getCancerStudyByStableId("gbm");
        ArrayList<GeneticProfile> list = DaoGeneticProfile.getAllGeneticProfiles
                (cancerStudy.getInternalId());
        geneticProfile = list.get(0);

        assertEquals(cancerStudy.getInternalId(), geneticProfile.getCancerStudyId());
        assertEquals("Barry's CNA Data", geneticProfile.getProfileName());
        assertEquals(GeneticAlterationType.COPY_NUMBER_ALTERATION,
                geneticProfile.getGeneticAlterationType());

        geneticProfile = GeneticProfileReader.loadGeneticProfile(file);
        assertEquals("Barry", geneticProfile.getTargetLine());
        assertEquals("Blah Blah.", geneticProfile.getProfileDescription());
    }
}
