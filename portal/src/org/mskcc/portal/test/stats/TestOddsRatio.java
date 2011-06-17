package org.mskcc.portal.test.stats;

import junit.framework.TestCase;
import org.mskcc.portal.model.GeneticAlterationType;
import org.mskcc.portal.model.GeneticProfile;
import org.mskcc.portal.model.ProfileData;
import org.mskcc.portal.model.ProfileDataSummary;
import org.mskcc.portal.oncoPrintSpecLanguage.OncoPrintSpecification;
import org.mskcc.portal.stats.OddsRatio;
import org.mskcc.portal.util.ProfileMerger;
import org.mskcc.portal.util.WebFileConnect;
import org.mskcc.portal.util.ZScoreUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class TestOddsRatio extends TestCase {

    public void testFisherExact() throws IOException {
        ArrayList<ProfileData> profileList = new ArrayList<ProfileData>();

        GeneticProfile profile0 = new GeneticProfile("gbm", "CNA", "NA",
                GeneticAlterationType.COPY_NUMBER_ALTERATION, true);
        String matrix0[][] = WebFileConnect.retrieveMatrix(new File("test_data/cna_sample2.txt"));
        ProfileData data0 = new ProfileData(profile0, matrix0);
        profileList.add(data0);
        
        String[] genes = { "BRCA1", "BRCA2" };
        OncoPrintSpecification anOncoPrintSpecification = new OncoPrintSpecification( genes );
        
        ProfileMerger merger = new ProfileMerger(profileList);
        ProfileData mergedProfile = merger.getMergedProfile();
        ProfileDataSummary pDataSummary = new ProfileDataSummary(mergedProfile, anOncoPrintSpecification, ZScoreUtil.Z_SCORE_THRESHOLD_DEFAULT);

        OddsRatio oddsRatio = new OddsRatio(pDataSummary, "BRCA1", "BRCA2");
        double oddsRatioValue = oddsRatio.getOddsRatio();
        double p = oddsRatio.getCumulativeP();
        assertEquals(0.16666, oddsRatioValue, 0.0001);
        assertEquals(0.0849, p, 0.0001);
        //System.out.println (oddsRatio.getRCommand());
    }
}