package org.mskcc.portal.test.util;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.portal.util.GeneValidator;

import java.util.ArrayList;

/**
 * Tests the Gene Validator Class.
 *
 * @author Ethan Cerami.
 */
public class TestGeneValidator extends TestCase {

    /**
     * Tests the Gene Validator.
     * @throws DaoException Database Error.
     */
    public void testGeneValidator() throws DaoException {
        ResetDatabase.resetDatabase();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        daoGene.addGene(new CanonicalGene(672, "BRCA1"));
        daoGene.addGene(new CanonicalGene(677, "BRCA2"));

        GeneValidator validator = new GeneValidator ("BRCA1:  AMP");
        ArrayList<CanonicalGene> validGeneList = validator.getValidGeneList();
        ArrayList<String> invalidList = validator.getInvalidGeneList();
        assertEquals (1, validGeneList.size());
        assertEquals (0, invalidList.size());

        validator = new GeneValidator ("CERAMI:  AMP");
        validGeneList = validator.getValidGeneList();
        invalidList = validator.getInvalidGeneList();
        assertEquals (0, validGeneList.size());
        assertEquals (1, invalidList.size());

        validator = new GeneValidator ("BRCA2:  CNA >= GAIN");
        validGeneList = validator.getValidGeneList();
        invalidList = validator.getInvalidGeneList();
        assertEquals (1, validGeneList.size());
        assertEquals (0, invalidList.size());        

        validator = new GeneValidator ("BRCA2:  CNA >= GAIN\nCERAMI:  CNA>=GAIN");
        validGeneList = validator.getValidGeneList();
        invalidList = validator.getInvalidGeneList();
        assertEquals (1, validGeneList.size());
        assertEquals (1, invalidList.size());        

        validator = new GeneValidator ("DATATYPES: AMP GAIN HOMDEL EXP > 1.5 EXP<=-1.5; BRCA1 CERAMI");
        validGeneList = validator.getValidGeneList();
        invalidList = validator.getInvalidGeneList();
        assertEquals (1, validGeneList.size());
        assertEquals ("BRCA1", validGeneList.get(0).getHugoGeneSymbol());
        assertEquals (1, invalidList.size());
        assertEquals ("CERAMI", invalidList.get(0));
    }
}
