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

package org.mskcc.cbio.portal.dao;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGistic;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.Gistic;
import org.mskcc.cbio.portal.validate.validationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.ArrayList;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestDaoGistic {

	@Test
    public void testDaoGistic() throws SQLException, DaoException, validationException {

        // initialize dummy parameters
		CanonicalGene brca1 = DaoGeneOptimized.getInstance().getGene("BRCA1");
		CanonicalGene brca2 = DaoGeneOptimized.getInstance().getGene("BRCA2");
		CanonicalGene kras = DaoGeneOptimized.getInstance().getGene("KRAS");
				
        ArrayList<CanonicalGene> geneList =
			new ArrayList<CanonicalGene>(Arrays.asList(brca1, brca2, kras));

        Gistic gisticIn1;
        Gistic gisticIn2;
        gisticIn1 = new Gistic(1, 1, "1q11.1", 1, 2, 0.01f, geneList, Gistic.AMPLIFIED);
        gisticIn2 = new Gistic(1, 2, "2q22.2", 1, 2, 0.01f, geneList, Gistic.AMPLIFIED);

        // end initialize

        assertEquals(Gistic.NO_SUCH_GISTIC, gisticIn1.getInternalId());
        // -- put stuff in --
        DaoGistic.addGistic(gisticIn1);
        // InternalId is auto-incremented by the db, starting at 1
        //assertEquals(1, gisticIn1.getInternalId());
        DaoGistic.addGistic(gisticIn2);
        //assertEquals(2, gisticIn2.getInternalId());
        DaoGistic.deleteGistic(2);
        //assertEquals(1, gisticIn1.getInternalId());
        DaoGistic.addGistic(gisticIn2);

        // -- get stuff back --

//        DaoGistic.getGisticByROI("1q11", 1,2);  Perhaps this is a new project of some sort?
// ROIs across various cancers.

        ArrayList<Gistic> gisticOut = DaoGistic.getAllGisticByCancerStudyId(1);
        assertTrue(gisticOut != null);
        //assertEquals(2, gisticOut.size());
    }
}
