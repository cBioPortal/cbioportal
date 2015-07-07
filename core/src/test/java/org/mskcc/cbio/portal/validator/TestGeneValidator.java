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

package org.mskcc.cbio.portal.validator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.validate.gene.InvalidGeneSymbolException;
import org.mskcc.cbio.portal.validate.gene.TooManyGenesException;
import org.mskcc.cbio.portal.validate.gene.GeneValidationException;
import org.mskcc.cbio.portal.validate.gene.GeneValidator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

import java.util.ArrayList;

/**
 * Tests the Gene Validator Class.
 *
 * @author Ethan Cerami.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TestGeneValidator {

    @Test
    public void testGeneValidator1() throws DaoException, GeneValidationException {
        GeneValidator validator = new GeneValidator ("BRCA1:  AMP");
        ArrayList<CanonicalGene> validGeneList = validator.getValidGeneList();
        assertEquals (1, validGeneList.size());
    }

    @Test
    public void testGeneValidator2() throws DaoException, GeneValidationException {
        try {
            GeneValidator validator = new GeneValidator ("CERAMI:  AMP");
            fail ("GeneValidationException should have been thrown");
        } catch (InvalidGeneSymbolException e) {
            ArrayList<String> invalidList = e.getInvalidGeneList();
            assertEquals (1, invalidList.size());
        }
    }

    @Test
    public void testGeneValidator3() throws DaoException, GeneValidationException {
        GeneValidator validator = new GeneValidator ("BRCA2:  CNA >= GAIN");
        ArrayList<CanonicalGene> validGeneList = validator.getValidGeneList();
        assertEquals (1, validGeneList.size());
    }

    @Test
    public void testGeneValidator4() throws DaoException, GeneValidationException {
        try {
            GeneValidator validator = new GeneValidator("BRCA2:  CNA >= GAIN\nCERAMI:  CNA>=GAIN");
            fail ("GeneValidationException should have been thrown");
        } catch (InvalidGeneSymbolException e) {
            ArrayList<String> invalidList = e.getInvalidGeneList();
            assertEquals (1, invalidList.size());
        }
    }

    @Test
    public void testGeneValidator5() throws DaoException, GeneValidationException {
        try {
            GeneValidator validator = new GeneValidator("DATATYPES: AMP GAIN HOMDEL " +
                "EXP > 1.5 EXP<=-1.5; BRCA1 CERAMI");
            fail ("GeneValidationException should have been thrown");
        } catch (InvalidGeneSymbolException e) {
            ArrayList<String> invalidList = e.getInvalidGeneList();
            assertEquals (1, invalidList.size());
            assertEquals ("CERAMI", invalidList.get(0));
        }
    }

    @Test
    public void testGeneValidator6() throws DaoException, GeneValidationException {
        try {
            GeneValidator validator = new GeneValidator ("BRCA2:  AMP\nCERAMI:  AMP\nSANDER: AMP");
            fail ("GeneValidationException should have been thrown");
        } catch (InvalidGeneSymbolException e) {
            ArrayList<String> invalidList = e.getInvalidGeneList();
            assertEquals (2, invalidList.size());
            assertEquals ("CERAMI, SANDER.", e.getInvalidGeneSymbolsAsString());
        }
    }

    @Test
    public void testGeneValidator7() throws DaoException, GeneValidationException {
        try {
            GeneValidator validator = new GeneValidator ("");
            fail ("GeneValidationException should have been thrown");
        } catch (GeneValidationException e) {
            assertEquals ("No genes specified.  Please specify at least one gene symbol.",
                    e.getMessage());
        }
    }

    @Test
    public void testGeneValidator8() throws DaoException, GeneValidationException {
        try {
            GeneValidator validator = new GeneValidator ("BRCA1 BRCA2 KRAS", 2);
            fail ("TooManyGenesException should have been thrown");
        } catch (TooManyGenesException e) {
            assertEquals ("Too many genes specified:  3.  Please restrict your " +
                    "query to a maximum of 2 genes.",
                    e.getMessage());
        }
    }
}
