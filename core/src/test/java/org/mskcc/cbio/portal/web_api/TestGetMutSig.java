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

package org.mskcc.cbio.portal.web_api;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoMutSig;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.MutSig;
import org.mskcc.cbio.portal.dao.DaoException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

import java.io.*;

/**
 * @author Lennart Bastian
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestGetMutSig {

	int studyId;
	
	@Before 
	public void setUp() throws DaoException
	{
		studyId = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub").getInternalId();
		DaoGeneticProfile.reCache();
	}

    @Test
    public void testGetMutSig() throws DaoException, IOException {

        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        CanonicalGene gene = daoGeneOptimized.getGene("AKT1");
        CanonicalGene gene2 = daoGeneOptimized.getGene("AKT2");
        
        MutSig mutSig = new MutSig(1, gene, 1, 502500, 20, 1E-11f, 1E-8f);
        MutSig mutSig2 = new MutSig(1, gene2, 14, 273743, 3, 1E-11f, 1E-8f);

        assertTrue(1E-11f == mutSig.getpValue());
        assertTrue(1E-8f == mutSig2.getqValue());

        DaoMutSig.addMutSig(mutSig);
        DaoMutSig.addMutSig(mutSig2);

        StringBuffer stringBuffer = GetMutSig.getMutSig(studyId);

        String lines[] = stringBuffer.toString().split("\n");
        assertEquals("Cancer\tEntrez\tHugo\tRank\tN\tn\tnVal\tnVer\tCpG\tC+G\tA+T\tINDEL\tp\tq", lines[0]);
        assertEquals("1\t207\tAKT1\t1\t502500\t20\t1.0E-11\t1.0E-8", lines[1]);
    }
}

