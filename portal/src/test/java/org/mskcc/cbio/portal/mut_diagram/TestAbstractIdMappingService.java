package org.mskcc.cbio.portal.test.mut_diagram;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.portal.mut_diagram.IdMappingService;
import org.mskcc.portal.mut_diagram.impl.CgdsIdMappingService;

/**
 * Abstract unit test for implementations of IdMappingService.
 */
public class TestAbstractIdMappingService extends TestCase {
    protected IdMappingService idMappingService;

    public void setUp() throws DaoException {
        idMappingService = new CgdsIdMappingService(DaoGeneOptimized.getInstance());
    }

    public final void testCreateIdMappingService() {
        assertNotNull(idMappingService);
    }

    public final void testGetUniProtIdNullHugoGeneSymbol() {
        try {
            idMappingService.getUniProtIds(null);
            fail("Null Pointer Exception should have been thrown.");
        } catch (NullPointerException e) {
        }
    }
}