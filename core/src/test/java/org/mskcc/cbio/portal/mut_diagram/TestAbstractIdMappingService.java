/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.mut_diagram;

import junit.framework.TestCase;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.mut_diagram.IdMappingService;
import org.mskcc.cbio.portal.mut_diagram.impl.CgdsIdMappingService;

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
            idMappingService.mapFromHugoToUniprotAccessions(null);
            fail("Null Pointer Exception should have been thrown.");
        } catch (NullPointerException e) {
        }
    }
}