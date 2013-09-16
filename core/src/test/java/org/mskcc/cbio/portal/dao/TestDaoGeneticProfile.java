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

package org.mskcc.cbio.portal.dao;

import java.util.ArrayList;
import junit.framework.TestCase;
import org.mskcc.cbio.portal.model.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.scripts.ResetDatabase;

/**
 * JUnit tests for DaoGeneticProfile class.
 */
public class TestDaoGeneticProfile extends TestCase {

    public void testDaoGeneticProfile() throws DaoException {
       
       createSmallDbms();

       ArrayList<GeneticProfile> list = DaoGeneticProfile.getAllGeneticProfiles(1);
        assertEquals(2, list.size());
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

        geneticProfile = DaoGeneticProfile.getGeneticProfileByStableId("gbm_gistic");
        assertEquals(1, geneticProfile.getCancerStudyId());
        assertEquals("Gistic CNA Results", geneticProfile.getProfileName());
        assertEquals(GeneticAlterationType.COPY_NUMBER_ALTERATION,
                geneticProfile.getGeneticAlterationType());

        geneticProfile = DaoGeneticProfile.getGeneticProfileById(2);
        assertEquals(1, geneticProfile.getCancerStudyId());
        assertEquals("Gistic CNA Results", geneticProfile.getProfileName());
        assertEquals(GeneticAlterationType.COPY_NUMBER_ALTERATION,
                geneticProfile.getGeneticAlterationType());
        
        assertEquals(2, DaoGeneticProfile.getCount() );
        DaoGeneticProfile.deleteGeneticProfile(geneticProfile);
        assertEquals(1, DaoGeneticProfile.getCount() );
        list = DaoGeneticProfile.getAllGeneticProfiles(1);
        assertEquals(1, list.size());
        geneticProfile = list.get(0);
        assertEquals(1, geneticProfile.getCancerStudyId());
        assertEquals("Barry CNA Results", geneticProfile.getProfileName());
        assertEquals(GeneticAlterationType.COPY_NUMBER_ALTERATION,
                geneticProfile.getGeneticAlterationType());
        assertEquals ("Blah, Blah, Blah.", geneticProfile.getProfileDescription());

        assertTrue ( DaoGeneticProfile.updateNameAndDescription
                (geneticProfile.getGeneticProfileId(), "Updated Name", "Updated Description") );
        list = DaoGeneticProfile.getAllGeneticProfiles(1);
        assertEquals(1, list.size());
        geneticProfile = list.get(0);
        assertEquals(1, geneticProfile.getCancerStudyId());
        assertEquals("Updated Name", geneticProfile.getProfileName());
        assertEquals(GeneticAlterationType.COPY_NUMBER_ALTERATION,
                geneticProfile.getGeneticAlterationType());
        assertEquals ("Updated Description", geneticProfile.getProfileDescription());
        DaoGeneticProfile.deleteAllRecords();
        assertEquals(0, DaoGeneticProfile.getCount() );
    }
    
    public static void createSmallDbms() throws DaoException{
       ResetDatabase.resetDatabase();

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
