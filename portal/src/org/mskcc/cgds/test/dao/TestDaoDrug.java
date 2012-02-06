package org.mskcc.cgds.test.dao;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoDrug;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.Drug;
import org.mskcc.cgds.scripts.ResetDatabase;

public class TestDaoDrug extends TestCase {
    public void testDaoDrug() throws DaoException {
        ResetDatabase.resetDatabase();

        DaoDrug daoDrug = DaoDrug.getInstance();
        Drug drug = new Drug("Dummy:1", "MyDrug", "description", "synonym1, synonym2", "this is an xref", "DUMMY");
        Drug drug2 = new Drug("Dummy:2", "MyDrug2", "description2", "synonym1, synonym2", "this is an xref2", "BLA");

        assertEquals(daoDrug.addDrug(drug), 1);
        assertEquals(daoDrug.addDrug(drug2), 1);

        Drug tmpDrug = daoDrug.getDrug("Dummy:1");
        assertNotNull(tmpDrug);
        assertEquals(tmpDrug.getName(), "MyDrug");
        assertEquals(tmpDrug.getDescription(), "description");
        assertEquals(tmpDrug.getSynonyms(), "synonym1, synonym2");
        assertEquals(tmpDrug.getResource(), "DUMMY");

        Drug tmpDrug2 = daoDrug.getDrug("Dummy:2");
        assertNotNull(tmpDrug2);
        assertEquals(tmpDrug2.getName(), "MyDrug2");

        assertNull(daoDrug.getDrug("Dummy:BLABLA"));

        assertEquals(2, daoDrug.getAllDrugs().size());
    }
}
