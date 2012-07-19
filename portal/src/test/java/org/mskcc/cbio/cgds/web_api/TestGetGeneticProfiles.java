package org.mskcc.cbio.cgds.test.web_api;

import junit.framework.TestCase;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneticProfile;
import org.mskcc.cbio.cgds.dao.DaoCancerStudy;
import org.mskcc.cbio.cgds.dao.DaoTypeOfCancer;
import org.mskcc.cbio.cgds.model.GeneticAlterationType;
import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.model.TypeOfCancer;
import org.mskcc.cbio.cgds.model.GeneticProfile;
import org.mskcc.cbio.cgds.web_api.GetGeneticProfiles;

/**
 * JUnit test for GeneticProfile class.
 */
public class TestGetGeneticProfiles extends TestCase {

    public void testDaoGeneticProfile() throws DaoException {
        TypeOfCancer typeOfCancer = new TypeOfCancer();
        typeOfCancer.setName("GBM");
        typeOfCancer.setTypeOfCancerId("GBM");
        DaoTypeOfCancer.addTypeOfCancer(typeOfCancer);

        CancerStudy cancerStudy = new CancerStudy("GBM", "GBM", "GBM","GBM", true);
        DaoCancerStudy.addCancerStudy(cancerStudy);

        cancerStudy = DaoCancerStudy.getCancerStudyByStableId("GBM");

        DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();
        daoGeneticProfile.deleteAllRecords();
        GeneticProfile profile1 = new GeneticProfile();
        profile1.setCancerStudyId(cancerStudy.getInternalId());
        profile1.setStableId("gbm_rae");
        profile1.setGeneticAlterationType(GeneticAlterationType.COPY_NUMBER_ALTERATION);
        profile1.setProfileName("Barry CNA Results");
        profile1.setProfileDescription("Blah");
        daoGeneticProfile.addGeneticProfile(profile1);

        GeneticProfile profile2 = new GeneticProfile();
        profile2.setCancerStudyId(cancerStudy.getInternalId());
        profile2.setStableId("gbm_gistic");
        profile2.setGeneticAlterationType(GeneticAlterationType.COPY_NUMBER_ALTERATION);
        profile2.setProfileName("Gistic CNA Results");
        profile2.setProfileDescription("BlahBlah");
        daoGeneticProfile.addGeneticProfile(profile2);



        String output = GetGeneticProfiles.getGeneticProfiles(cancerStudy.getCancerStudyStableId());
        assertTrue(output.contains("gbm_rae\tBarry CNA Results\tBlah\t1\tCOPY_NUMBER_ALTERATION"));
        assertTrue(output.contains("gbm_gistic\tGistic CNA Results\tBlahBlah\t1\tCOPY_NUMBER_ALTERATION"));
    }
}
