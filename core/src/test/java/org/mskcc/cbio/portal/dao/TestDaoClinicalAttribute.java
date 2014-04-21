/**
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.dao;

import junit.framework.TestCase;
import org.mskcc.cbio.portal.model.ClinicalAttribute;
import org.mskcc.cbio.portal.scripts.ResetDatabase;

public class TestDaoClinicalAttribute extends TestCase {
    public void testDaoClinicalAttribute() throws DaoException {
        ResetDatabase.resetDatabase();

        DaoClinicalAttribute daoClinicalAttribute = new DaoClinicalAttribute();
        int added = daoClinicalAttribute.addDatum(new ClinicalAttribute("attrId", "some attribute", "test attribute", "nonsense"));
        assertTrue(added == 1);

        ClinicalAttribute clinicalAttribute = daoClinicalAttribute.getDatum("attrId");
        assertNotNull(clinicalAttribute);
    }
}
