/*
 * Copyright (c) 2017 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
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

/*
 * @author Pim van Nierop, pim@thehyve.nl
*/

package org.mskcc.cbio.portal.scripts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoTreatment;
import org.mskcc.cbio.portal.dao.JdbcUtil;
import org.mskcc.cbio.portal.model.Treatment;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/*
 * JUnit tests for ImportTreatmentData class.
*/

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TestImportGenericAssayData {

	@Test
    public void testImportTreatmentData() throws Exception {

        ProgressMonitor.setConsoleMode(false);
        
        // Open genesets test data file
        File file = new File("src/test/resources/treatments/data_treatment_ic50.txt");
        
        // import data and test all treatments were added
        ImportGenericAssayEntity.importData(file);
        assertEquals(10, getNumRecords());
 
        // test wether a record can be retrieved via stable id 
        Treatment treatment1 = DaoTreatment.getTreatmentByStableId("Irinotecan");
        assertNotNull(treatment1);

        // Test whether fields were populated correctly
        assertEquals("Name of Irinotecan", treatment1.getName());
        assertEquals("Desc of Irinotecan", treatment1.getDescription());
        assertEquals("Url of Irinotecan", treatment1.getRefLink());

        // test fields are updated after loading new treatment file
        File fileNewDesc = new File("src/test/resources/treatments/data_treatment_ic50_newdesc.txt");
        ImportGenericAssayEntity.importData(fileNewDesc);
        Treatment treatment3 = DaoTreatment.getTreatmentByStableId("Irinotecan");
        assertEquals("New desc of Irinotecan", treatment3.getDescription());
        
    }

    private int getNumRecords() {

		Connection con = null;
		PreparedStatement stat = null;
		ResultSet rs = null;
		try {
            con = JdbcUtil.getDbConnection(DaoTreatment.class);
            stat = con.prepareStatement("SELECT COUNT(*) FROM treatment");
            rs = stat.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            System.out.println(e.getStackTrace());
        } finally {
            JdbcUtil.closeAll(DaoTreatment.class, con, stat, rs);
        }

        return 0;
    }

}