package org.mskcc.cgds.test.web_api;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneticProfile;
import org.mskcc.cgds.model.GeneticAlterationType;
import org.mskcc.cgds.model.GeneticProfile;
import org.mskcc.cgds.web_api.GetGeneticProfiles;

public class TestGetGeneticProfiles extends TestCase {

    public void testDaoGeneticProfile() throws DaoException {
        DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();
        daoGeneticProfile.deleteAllRecords();
        GeneticProfile profile1 = new GeneticProfile();
        profile1.setCancerStudyId( 1 );
        profile1.setStableId("gbm_rae");
        profile1.setGeneticAlterationType(GeneticAlterationType.COPY_NUMBER_ALTERATION);
        profile1.setProfileName("Barry CNA Results");
        profile1.setProfileDescription("Blah");
        daoGeneticProfile.addGeneticProfile(profile1);

        GeneticProfile profile2 = new GeneticProfile();
        profile2.setCancerStudyId( 1 );
        profile2.setStableId("gbm_gistic");
        profile2.setGeneticAlterationType(GeneticAlterationType.COPY_NUMBER_ALTERATION);
        profile2.setProfileName("Gistic CNA Results");
        profile2.setProfileDescription("BlahBlah");
        daoGeneticProfile.addGeneticProfile(profile2);

        String output = GetGeneticProfiles.getGeneticProfiles( 1 );
        assertTrue(output.contains("gbm_rae\tBarry CNA Results\tBlah\t1\tCOPY_NUMBER_ALTERATION"));
        assertTrue(output.contains("gbm_gistic\tGistic CNA Results\tBlahBlah\t1\tCOPY_NUMBER_ALTERATION"));
    }
}
