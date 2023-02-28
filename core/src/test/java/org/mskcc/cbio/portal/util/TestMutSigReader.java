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

package org.mskcc.cbio.portal.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

import java.io.File;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestMutSigReader {

	// TBD: change these to use getResourceAsStream()
    File properties = new File("target/test-classes/testCancerStudy.txt");
    File mutSigFile = new File("target/test-classes/test_mut_sig_data.txt");

	int studyId;
	
	@Before 
	public void setUp() throws DaoException
	{
		studyId = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub").getInternalId();
		DaoGeneticProfile.reCache();
	}

    @Test
    public void testloadMutSig() throws Exception {

        ProgressMonitor.setConsoleMode(false);

        MutSigReader.loadMutSig(studyId, mutSigFile);
        
        // Is the data in the database?
        MutSig mutSig = DaoMutSig.getMutSig("AKT1", studyId);
        assertTrue(mutSig != null);
        CanonicalGene testGene = mutSig.getCanonicalGene();
        assertTrue(testGene != null);

        assertTrue("AKT1".equals(testGene.getHugoGeneSymbolAllCaps()));
        assertEquals(mutSig.getNumMutations(), 5);
        assertEquals(mutSig.getNumBasesCovered(), 150306);
        assertTrue(Math.abs(mutSig.getpValue() - 1.82E-7) < 1E-12);
        assertTrue(Math.abs(mutSig.getqValue() - 2.7E-5) < 1E-12);
    }
}
