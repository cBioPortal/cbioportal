package org.mskcc.cbio.cgds.dao;

import java.util.ArrayList;
import junit.framework.TestCase;
import org.mskcc.cbio.cgds.model.GeneticAlterationType;
import org.mskcc.cbio.cgds.model.GeneticProfile;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;

/**
 * JUnit tests for DaoGeneticProfile class.
 */
public class TestDaoGeneticProfile extends TestCase {

    public void testDaoGeneticProfile() throws DaoException {
       
       DaoGeneticProfile daoGeneticProfile = createSmallDbms();

       ArrayList<GeneticProfile> list = daoGeneticProfile.getAllGeneticProfiles(1);
        //assertEquals(2, list.size());
        GeneticProfile geneticProfile = list.get(0);

        assertEquals(1, geneticProfile.getCancerStudyId());
        assertEquals("Barry CNA Results", geneticProfile.getProfileName());
        assertEquals(GeneticAlterationType.COPY_NUMBER_ALTERATION,
                geneticProfile.getGeneticAlterationType());
        assertEquals ("Blah, Blah, Blah.", geneticProfile.getProfileDescription());

        geneticProfile = list.get(1);
        assertEquals(1, geneticProfile.getCancerStudyId());
        assertEquals("Gistic CNA Results", geneticProfile.getProfileName());
        assertEquals(GeneticAlterationType.COPY_NUMBER_ALTERATION,
                geneticProfile.getGeneticAlterationType());
        assertEquals(true, geneticProfile.showProfileInAnalysisTab());

        geneticProfile = daoGeneticProfile.getGeneticProfileByStableId("gbm_gistic");
        assertEquals(1, geneticProfile.getCancerStudyId());
        assertEquals("Gistic CNA Results", geneticProfile.getProfileName());
        assertEquals(GeneticAlterationType.COPY_NUMBER_ALTERATION,
                geneticProfile.getGeneticAlterationType());

        geneticProfile = daoGeneticProfile.getGeneticProfileById(2);
        assertEquals(1, geneticProfile.getCancerStudyId());
        assertEquals("Gistic CNA Results", geneticProfile.getProfileName());
        assertEquals(GeneticAlterationType.COPY_NUMBER_ALTERATION,
                geneticProfile.getGeneticAlterationType());
        
        assertEquals(2, daoGeneticProfile.getCount() );
        daoGeneticProfile.deleteGeneticProfile(geneticProfile);
        assertEquals(1, daoGeneticProfile.getCount() );
        list = daoGeneticProfile.getAllGeneticProfiles(1);
        assertEquals(1, list.size());
        geneticProfile = list.get(0);
        assertEquals(1, geneticProfile.getCancerStudyId());
        assertEquals("Barry CNA Results", geneticProfile.getProfileName());
        assertEquals(GeneticAlterationType.COPY_NUMBER_ALTERATION,
                geneticProfile.getGeneticAlterationType());
        assertEquals ("Blah, Blah, Blah.", geneticProfile.getProfileDescription());

        assertTrue ( daoGeneticProfile.updateNameAndDescription
                (geneticProfile.getGeneticProfileId(), "Updated Name", "Updated Description") );
        list = daoGeneticProfile.getAllGeneticProfiles(1);
        assertEquals(1, list.size());
        geneticProfile = list.get(0);
        assertEquals(1, geneticProfile.getCancerStudyId());
        assertEquals("Updated Name", geneticProfile.getProfileName());
        assertEquals(GeneticAlterationType.COPY_NUMBER_ALTERATION,
                geneticProfile.getGeneticAlterationType());
        assertEquals ("Updated Description", geneticProfile.getProfileDescription());
        daoGeneticProfile.deleteAllRecords();
        assertEquals(0, daoGeneticProfile.getCount() );
    }
    
    public static DaoGeneticProfile createSmallDbms() throws DaoException{
       ResetDatabase.resetDatabase();
       DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();

       GeneticProfile profile1 = new GeneticProfile();
       profile1.setCancerStudyId(1);
       profile1.setStableId("gbm_rae");
       profile1.setGeneticAlterationType(GeneticAlterationType.COPY_NUMBER_ALTERATION);
       profile1.setProfileName("Barry CNA Results");
       profile1.setProfileDescription("Blah, Blah, Blah.");
       profile1.setShowProfileInAnalysisTab(true);
       daoGeneticProfile.addGeneticProfile(profile1);

       GeneticProfile profile2 = new GeneticProfile();
       profile2.setCancerStudyId(1);
       profile2.setStableId("gbm_gistic");
       profile2.setGeneticAlterationType(GeneticAlterationType.COPY_NUMBER_ALTERATION);
       profile2.setProfileName("Gistic CNA Results");
       profile2.setShowProfileInAnalysisTab(true);
       daoGeneticProfile.addGeneticProfile(profile2);
       
       return daoGeneticProfile;
       
    }
}
