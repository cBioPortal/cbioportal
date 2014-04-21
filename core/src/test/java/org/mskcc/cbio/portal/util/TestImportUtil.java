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

package org.mskcc.cbio.portal.util;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.scripts.ResetDatabase;

/**
 * JUnit tests utility class.
 */
public class TestImportUtil {

    public static void createSmallDbms(boolean resetDatabase) throws DaoException
    {
      if (resetDatabase){
        ResetDatabase.resetDatabase();
      }
       TypeOfCancer aTypeOfCancer = new TypeOfCancer();
       aTypeOfCancer.setTypeOfCancerId("gbm");
       aTypeOfCancer.setName("Name");
       aTypeOfCancer.setClinicalTrialKeywords("keyword");
       aTypeOfCancer.setDedicatedColor("white");
       aTypeOfCancer.setShortName("shortname");
       DaoTypeOfCancer.addTypeOfCancer(aTypeOfCancer);

       CancerStudy cancerStudy = new CancerStudy("GBM", "description", "gbm", "gbm", true);
       DaoCancerStudy.addCancerStudy(cancerStudy);

       GeneticProfile profile1 = new GeneticProfile();
       profile1.setCancerStudyId(1);
       profile1.setStableId("gbm_rae");
       profile1.setGeneticAlterationType(GeneticAlterationType.COPY_NUMBER_ALTERATION);
       profile1.setDatatype("DISCRETE");
       profile1.setProfileName("Barry CNA Results");
       profile1.setProfileDescription("Blah, Blah, Blah.");
       profile1.setShowProfileInAnalysisTab(true);
       DaoGeneticProfile.addGeneticProfile(profile1);

       GeneticProfile profile2 = new GeneticProfile();
       profile2.setCancerStudyId(1);
       profile2.setStableId("gbm_gistic");
       profile2.setGeneticAlterationType(GeneticAlterationType.COPY_NUMBER_ALTERATION);
       profile2.setDatatype("DISCRETE");
       profile2.setProfileName("Gistic CNA Results");
       profile2.setShowProfileInAnalysisTab(true);
       DaoGeneticProfile.addGeneticProfile(profile2);
    }
}
