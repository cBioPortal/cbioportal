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

package org.mskcc.cbio.portal.stats;

import junit.framework.TestCase;
import org.mskcc.cbio.portal.model.ProfileData;
import org.mskcc.cbio.portal.model.ProfileDataSummary;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.OncoPrintSpecification;
import org.mskcc.cbio.portal.util.ProfileMerger;
import org.mskcc.cbio.io.WebFileConnect;
import org.mskcc.cbio.portal.util.ZScoreUtil;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.GeneticAlterationType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class TestOddsRatio extends TestCase {

    public void testFisherExact() throws IOException {
        ArrayList<ProfileData> profileList = new ArrayList<ProfileData>();

        GeneticProfile profile0 =
                new GeneticProfile("gbm", 1,
								   GeneticAlterationType.COPY_NUMBER_ALTERATION, "DISCRETE",
								   "CNA", "CNA", true);
		// TBD: change this to use getResourceAsStream()
        String matrix0[][] = WebFileConnect.retrieveMatrix(new File("target/test-classes/cna_sample2.txt"));
        ProfileData data0 = new ProfileData(profile0, matrix0);
        profileList.add(data0);
        
        String[] genes = { "BRCA1", "BRCA2" };
        OncoPrintSpecification anOncoPrintSpecification = new OncoPrintSpecification( genes );
        
        ProfileMerger merger = new ProfileMerger(profileList);
        ProfileData mergedProfile = merger.getMergedProfile();
        ProfileDataSummary pDataSummary = new ProfileDataSummary(mergedProfile, anOncoPrintSpecification,
                ZScoreUtil.Z_SCORE_THRESHOLD_DEFAULT, ZScoreUtil.RPPA_SCORE_THRESHOLD_DEFAULT);

        OddsRatio oddsRatio = new OddsRatio(pDataSummary, "BRCA1", "BRCA2");
        double oddsRatioValue = oddsRatio.getOddsRatio();
        double p = oddsRatio.getCumulativeP();
        assertEquals(0.16666, oddsRatioValue, 0.0001);
        assertEquals(0.0849, p, 0.0001);
        //System.out.println (oddsRatio.getRCommand());
    }
}