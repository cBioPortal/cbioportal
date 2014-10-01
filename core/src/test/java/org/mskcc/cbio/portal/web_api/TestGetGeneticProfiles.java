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

package org.mskcc.cbio.portal.web_api;

import junit.framework.TestCase;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoTypeOfCancer;
import org.mskcc.cbio.portal.model.GeneticAlterationType;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.TypeOfCancer;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.scripts.ResetDatabase;

/**
 * JUnit test for GeneticProfile class.
 */
public class TestGetGeneticProfiles extends TestCase {

    public void testDaoGeneticProfile() throws DaoException {
        ResetDatabase.resetDatabase();
        TypeOfCancer typeOfCancer = new TypeOfCancer();
        typeOfCancer.setName("GBM");
        typeOfCancer.setTypeOfCancerId("GBM");
        DaoTypeOfCancer.addTypeOfCancer(typeOfCancer);

        CancerStudy cancerStudy = new CancerStudy("GBM", "GBM", "GBM","GBM", true);
        DaoCancerStudy.addCancerStudy(cancerStudy);

        cancerStudy = DaoCancerStudy.getCancerStudyByStableId("GBM");

        DaoGeneticProfile.deleteAllRecords();
        GeneticProfile profile1 = new GeneticProfile();
        profile1.setCancerStudyId(cancerStudy.getInternalId());
        profile1.setStableId("gbm_rae");
        profile1.setGeneticAlterationType(GeneticAlterationType.COPY_NUMBER_ALTERATION);
        profile1.setDatatype("DISCRETE");
        profile1.setProfileName("Barry CNA Results");
        profile1.setProfileDescription("Blah");
        DaoGeneticProfile.addGeneticProfile(profile1);

        GeneticProfile profile2 = new GeneticProfile();
        profile2.setCancerStudyId(cancerStudy.getInternalId());
        profile2.setStableId("gbm_gistic");
        profile2.setGeneticAlterationType(GeneticAlterationType.COPY_NUMBER_ALTERATION);
        profile2.setDatatype("DISCRETE");
        profile2.setProfileName("Gistic CNA Results");
        profile2.setProfileDescription("BlahBlah");
        DaoGeneticProfile.addGeneticProfile(profile2);



        String output = GetGeneticProfiles.getGeneticProfilesAsTable(cancerStudy.getCancerStudyStableId());
        assertTrue(output.contains("gbm_rae\tBarry CNA Results\tBlah\t1\tCOPY_NUMBER_ALTERATION"));
        assertTrue(output.contains("gbm_gistic\tGistic CNA Results\tBlahBlah\t1\tCOPY_NUMBER_ALTERATION"));
    }
}
