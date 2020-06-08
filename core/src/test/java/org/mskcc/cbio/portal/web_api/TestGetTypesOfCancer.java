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

package org.mskcc.cbio.portal.web_api;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoTypeOfCancer;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.TypeOfCancer;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

import java.io.IOException;

/**
 * JUnit Tests for GetTypes of Cancer.
 *
 * @author Ethan Cerami, Arthur Goldberg, Ersin Ciftci.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestGetTypesOfCancer {

    public static final String DESCRIPTION = "<a href=\"http://cancergenome.nih.gov/\">The Cancer Genome Atlas (TCGA)" +
        "</a> Breast Invasive Carcinoma project. 825 cases.<br><i>Nature 2012.</i> " +
        "<a href=\"http://tcga-data.nci.nih.gov/tcga/\">Raw data via the TCGA Data Portal</a>.";

    
    @Before
    public void setUp() throws DaoException {
        //clear cache to ensure this test is not affected by other tests (i.e. some tests add the same
        //samples to the DB and these remain in the cache after tests are done...if tests don't implement
        //teardown properly).
        DaoCancerStudy.reCacheAll();
    }
    
    @After
    public void tearDown() {
        //clear any cached data:
        DaoCancerStudy.reCacheAll();
    }    
    
    /**
     * Tests Get Types of Cancer.
     * @throws DaoException Database Error.
     * @throws IOException IO Error.
     * @throws ProtocolException ProtocolError.
     */
    @Test
    public void testGetTypesOfCancerEmpty() throws DaoException, IOException, ProtocolException {

        // First, verify that protocol exception is thrown when there are no cancer types
        try {
            DaoCancerStudy.deleteCancerStudy("study_tcga_pub");
            DaoTypeOfCancer.deleteAllTypesOfCancer();
            String output = GetTypesOfCancer.getTypesOfCancer();
            fail ("ProtocolException should have been thrown.");
        } catch (ProtocolException e) {
            assertEquals(e.getMsg(), "No Types of Cancer Available.");
        }
	}
	
    @Test
    public void testGetTypesOfCancer() throws DaoException, IOException, ProtocolException {
        //remove study and all cancer types:
        DaoCancerStudy.deleteCancerStudy("study_tcga_pub");
        DaoTypeOfCancer.deleteAllTypesOfCancer();
        
        //  Verify a few of the data lines
        TypeOfCancer typeOfCancer1 = new TypeOfCancer();
        typeOfCancer1.setName("Adenoid Cystic Breast Cancer");
        typeOfCancer1.setTypeOfCancerId("acbc");
        DaoTypeOfCancer.addTypeOfCancer(typeOfCancer1);

        TypeOfCancer typeOfCancer2 = new TypeOfCancer();
        typeOfCancer2.setName("Breast Invasive Carcinoma");
        typeOfCancer2.setTypeOfCancerId("brca");
        DaoTypeOfCancer.addTypeOfCancer(typeOfCancer2);
        
        String output = GetTypesOfCancer.getTypesOfCancer();
        assertTrue(output.contains("acbc\tAdenoid Cystic Breast Cancer"));
        assertTrue(output.contains("brca\tBreast Invasive Carcinoma"));

        //  Verify header
        String lines[] = output.split("\n");
        assertEquals ("type_of_cancer_id\tname", lines[0].trim());
    }

    /**
     * Tests Get Cancer Studies.
     * @throws DaoException Database Error.
     * @throws IOException IO Error.
     * @throws ProtocolException ProtocolError.
     */
    @Test
    public void testGetCancerStudiesEmpty() throws DaoException, IOException, ProtocolException {

        // First, verify that protocol exception is thrown when there are no cancer studies
        try {
            DaoCancerStudy.deleteCancerStudy("study_tcga_pub");
            DaoTypeOfCancer.deleteAllTypesOfCancer();
            String output = GetTypesOfCancer.getCancerStudies();
            fail ("ProtocolException should have been thrown.");
        } catch (ProtocolException e) {
            assertEquals(e.getMsg(), "No Cancer Studies Available.");
        }
    }
    
    @Test
    public void testGetCancerStudies() throws DaoException, IOException, ProtocolException {

        //remove study and all cancer types:
        DaoCancerStudy.deleteCancerStudy("study_tcga_pub");
        DaoTypeOfCancer.deleteAllTypesOfCancer();
        
        //Add dummy cancer type and dummy study on empty DB:
        TypeOfCancer typeOfCancer2 = new TypeOfCancer();
        typeOfCancer2.setName("Breast Invasive Carcinoma");
        typeOfCancer2.setTypeOfCancerId("brca_testapi2");
        DaoTypeOfCancer.addTypeOfCancer(typeOfCancer2);
        
        CancerStudy cancerStudy = new CancerStudy("Breast Invasive Carcinoma (TCGA, Nature 2012)", DESCRIPTION, 
            "study_tcga_pub_testapi2", "brca_testapi2", true);
        cancerStudy.setReferenceGenome("hg19");
        DaoCancerStudy.addCancerStudy(cancerStudy, true);
        
        String output = GetTypesOfCancer.getCancerStudies();
        String lines[] = output.split("\n");

        //  Verify we get exactly two lines
        assertEquals (2, lines.length);

        //  Verify header
        assertEquals ("cancer_study_id\tname\tdescription", lines[0].trim());

        //  Verify data
        assertEquals ("study_tcga_pub_testapi2\tBreast Invasive Carcinoma (TCGA, Nature 2012)\t" + DESCRIPTION, lines[1].trim());
    }
}
