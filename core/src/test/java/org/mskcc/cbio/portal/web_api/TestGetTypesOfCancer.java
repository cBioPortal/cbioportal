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

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

/**
 * JUnit Tests for GetTypes of Cancer.
 *
 * @author Ethan Cerami, Arthur Goldberg.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TestGetTypesOfCancer {

    /**
     * Tests Get Types of Cancer.
     * @throws DaoException Database Error.
     * @throws IOException IO Error.
     * @throws ProtocolException ProtocolError.
     */
	@Ignore
    @Test
    public void testGetTypesOfCancerEmpty() throws DaoException, IOException, ProtocolException {

        // First, verify that protocol exception is thrown when there are no cancer types
        try {
            String output = GetTypesOfCancer.getTypesOfCancer();
            fail ("ProtocolException should have been thrown.");
        } catch (ProtocolException e) {
            assertEquals(e.getMsg(), "No Types of Cancer Available.");
        }
	}
	
    @Test
    public void testGetTypesOfCancer() throws DaoException, IOException, ProtocolException {

        //  Verify a few of the data lines
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
    @Ignore
    @Test
    public void testGetCancerStudiesEmpty() throws DaoException, IOException, ProtocolException {

        // First, verify that protocol exception is thrown when there are no cancer studies
        try {
            String output = GetTypesOfCancer.getCancerStudies();
            fail ("ProtocolException should have been thrown.");
        } catch (ProtocolException e) {
            assertEquals(e.getMsg(), "No Cancer Studies Available.");
        }
    }
    
    @Test
    public void testGetCancerStudies() throws DaoException, IOException, ProtocolException {

        String output = GetTypesOfCancer.getCancerStudies();
        String lines[] = output.split("\n");

        //  Verify we get exactly two lines
        assertEquals (2, lines.length);

        //  Verify header
        assertEquals ("cancer_study_id\tname\tdescription", lines[0].trim());

        //  Verify data
        assertEquals ("study_tcga_pub\tBreast Invasive Carcinoma (TCGA, Nature 2012)\t<a href=\"http://cancergenome.nih.gov/\">The Cancer Genome Atlas (TCGA)</a> Breast Invasive Carcinoma project. 825 cases.<br><i>Nature 2012.</i> <a href=\"http://tcga-data.nci.nih.gov/tcga/\">Raw data via the TCGA Data Portal</a>.", lines[1].trim());
    }
}
