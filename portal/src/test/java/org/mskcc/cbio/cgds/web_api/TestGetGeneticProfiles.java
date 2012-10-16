/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.cgds.web_api;

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
import org.mskcc.cbio.cgds.scripts.ResetDatabase;

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
