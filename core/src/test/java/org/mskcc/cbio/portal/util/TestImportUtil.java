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

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.scripts.ResetDatabase;

/**
 * JUnit tests utility class.
 * 
 * @deprecated
 */
public class TestImportUtil {

    /**
     * 
     * @param resetDatabase
     * @throws DaoException
     * 
     * @deprecated let's not do this...the new testing framework does this in another way (via seed_mini.sql)
     */
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
