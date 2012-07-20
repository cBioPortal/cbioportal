package org.mskcc.cbio.cgds.dao;

import junit.framework.TestCase;
import org.mskcc.cbio.cgds.dao.DaoDrug;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.model.Drug;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;

public class TestDaoDrug extends TestCase {
    public void testDaoDrug() throws DaoException {
        ResetDatabase.resetDatabase();

        DaoDrug daoDrug = DaoDrug.getInstance();
        Drug drug = new Drug("Dummy:1", "MyDrug", "description",
                "synonym,synonym2", "this is an xref", "DUMMY", true, "B01AE02");
        Drug drug2 = new Drug("Dummy:2", "MyDrug2", "description2",
                "synonym", "this is an xref2", "BLA", false, "L01XX29");

        assertEquals(daoDrug.addDrug(drug), 1);
        assertEquals(daoDrug.addDrug(drug2), 1);

        Drug tmpDrug = daoDrug.getDrug("Dummy:1");
        assertNotNull(tmpDrug);
        assertEquals(tmpDrug.getName(), "MyDrug");
        assertEquals(tmpDrug.getDescription(), "description");
        assertEquals(tmpDrug.getSynonyms(), "synonym,synonym2");
        assertEquals(tmpDrug.getResource(), "DUMMY");
        assertTrue(tmpDrug.isApprovedFDA());
        assertEquals("B01AE02", drug.getATCCode());

        Drug tmpDrug2 = daoDrug.getDrug("Dummy:2");
        assertNotNull(tmpDrug2);
        assertEquals(tmpDrug2.getName(), "MyDrug2");
        assertFalse(tmpDrug2.isApprovedFDA());

        assertNull(daoDrug.getDrug("Dummy:BLABLA"));

        assertEquals(2, daoDrug.getAllDrugs().size());
    }
}
