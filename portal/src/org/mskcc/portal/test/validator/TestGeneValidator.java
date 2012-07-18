package org.mskcc.portal.test.validator;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.cgds.validate.gene.InvalidGeneSymbolException;
import org.mskcc.cgds.validate.gene.TooManyGenesException;
import org.mskcc.cgds.validate.gene.GeneValidationException;
import org.mskcc.cgds.validate.gene.GeneValidator;

import java.util.ArrayList;

/**
 * Tests the Gene Validator Class.
 *
 * @author Ethan Cerami.
 */
public class TestGeneValidator extends TestCase {

    public void setUp() throws DaoException {
        ResetDatabase.resetDatabase();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        daoGene.addGene(new CanonicalGene(672, "BRCA1"));
        daoGene.addGene(new CanonicalGene(675, "BRCA2"));
        daoGene.addGene(new CanonicalGene(7157, "TP53"));
    }

    public void testGeneValidator1() throws DaoException, GeneValidationException {
        GeneValidator validator = new GeneValidator ("BRCA1:  AMP");
        ArrayList<CanonicalGene> validGeneList = validator.getValidGeneList();
        assertEquals (1, validGeneList.size());
    }

    public void testGeneValidator2() throws DaoException, GeneValidationException {
        try {
            GeneValidator validator = new GeneValidator ("CERAMI:  AMP");
            fail ("GeneValidationException should have been thrown");
        } catch (InvalidGeneSymbolException e) {
            ArrayList<String> invalidList = e.getInvalidGeneList();
            assertEquals (1, invalidList.size());
        }
    }

    public void testGeneValidator3() throws DaoException, GeneValidationException {
        GeneValidator validator = new GeneValidator ("BRCA2:  CNA >= GAIN");
        ArrayList<CanonicalGene> validGeneList = validator.getValidGeneList();
        assertEquals (1, validGeneList.size());
    }

    public void testGeneValidator4() throws DaoException, GeneValidationException {
        try {
            GeneValidator validator = new GeneValidator("BRCA2:  CNA >= GAIN\nCERAMI:  CNA>=GAIN");
            fail ("GeneValidationException should have been thrown");
        } catch (InvalidGeneSymbolException e) {
            ArrayList<String> invalidList = e.getInvalidGeneList();
            assertEquals (1, invalidList.size());
        }
    }

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

    public void testGeneValidator7() throws DaoException, GeneValidationException {
        try {
            GeneValidator validator = new GeneValidator ("");
            fail ("GeneValidationException should have been thrown");
        } catch (GeneValidationException e) {
            assertEquals ("No genes specified.  Please specify at least one gene symbol.",
                    e.getMessage());
        }
    }

    public void testGeneValidator8() throws DaoException, GeneValidationException {
        try {
            GeneValidator validator = new GeneValidator ("BRCA1 BRCA2 TP53", 2);
            fail ("TooManyGenesException should have been thrown");
        } catch (TooManyGenesException e) {
            assertEquals ("Too many genes specified:  3.  Please restrict your " +
                    "query to a maximum of 2 genes.",
                    e.getMessage());
        }
    }
}
