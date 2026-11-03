/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.dao;

import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.DaoClinicalAttributeMeta;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.model.ClinicalAttribute;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestDaoClinicalAttribute extends IntegrationTestBase {
	
	@Test
    public void testDaoClinicalAttribute() throws DaoException {

        int added = DaoClinicalAttributeMeta.addDatum(new ClinicalAttribute("attrId", "some attribute", "test attribute", "nonsense", true, "1", 1));
        assertTrue(added == 1);

        ClinicalAttribute clinicalAttribute = DaoClinicalAttributeMeta.getDatum("attrId", 1);
        assertNotNull(clinicalAttribute);
    }
}
