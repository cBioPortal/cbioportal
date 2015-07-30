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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

/**
 * Tests Get Clinical Data.
 *
 * @author Ethan Cerami.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TestGetClinicalData {

    /**
     * Tests Get Clinical Data.
     *
     * @throws DaoException Database Access Error.
     * @throws IOException IO Error.
     */
	@Ignore
	@Test
    public void testGetClinicalData() throws DaoException, IOException {

        // ResetDatabase.resetDatabase();
        // ProgressMonitor pMonitor = new ProgressMonitor();
        // pMonitor.setConsoleMode(false);
		// // TBD: change this to use getResourceAsStream()
        // File file = new File("target/test-classes/clinical_test.txt");
        // CancerStudy cancerStudy = new CancerStudy("test","test","test","test",true);
        // ImportClinicalData importClinicalData = new ImportClinicalData(cancerStudy, file, pMonitor);
		// importClinicalData.importData();
		// 
        // HashSet<String> caseSet = new HashSet<String>();
        // caseSet.add("TCGA-04-1331");
        // caseSet.add("TCGA-24-2030");
        // caseSet.add("TCGA-24-2261");
        // String clinicalDataOut = GetClinicalData.getClinicalData(1,caseSet, false);
//        String lines[] = clinicalDataOut.split("\n");
//
//        assertTrue(lines[2].startsWith("TCGA-24-2030\tNA\tNA\t21.18\tRecurred/Progressed\tNA"));
//        assertTrue(lines[3].startsWith("TCGA-24-2261\t0.79\tDECEASED\tNA" +
//                "\tRecurred/Progressed\t76.43"));
    }
}
