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

import org.cbioportal.model.EntityType;
import org.cbioportal.model.meta.GenericAssayMeta;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoGenericAssay;
import org.mskcc.cbio.portal.dao.DaoGeneticEntity;
import org.mskcc.cbio.portal.dao.JdbcUtil;
import org.mskcc.cbio.portal.model.GeneticAlterationType;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/*
 * JUnit tests for ImportTreatmentData class.
*/

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestImportGenericAssayData {

	@Test
    public void testImportTreatmentData() throws Exception {

        ProgressMonitor.setConsoleMode(false);
        
        // Open genesets test data file
        File file = new File("src/test/resources/treatments/data_treatment_ic50.txt");
        
        // import data and test all treatments were added
        ImportGenericAssayEntity.importData(file, GeneticAlterationType.GENERIC_ASSAY, "NAME,DESCRIPTION,URL", true);
        assertEquals(10, getNumRecordsForGenericAssay());
 
        // test wether a record can be retrieved via stable id 
        GenericAssayMeta treatment1 = DaoGenericAssay.getGenericAssayMetaByStableId("Irinotecan");
        assertNotNull(treatment1);

        // Test whether fields were populated correctly
        assertEquals("Name of Irinotecan", treatment1.getGenericEntityMetaProperties().get("NAME"));
        assertEquals("Desc of Irinotecan", treatment1.getGenericEntityMetaProperties().get("DESCRIPTION"));
        assertEquals("Url of Irinotecan", treatment1.getGenericEntityMetaProperties().get("URL"));

        // test fields are updated after loading new treatment file
        File fileNewDesc = new File("src/test/resources/treatments/data_treatment_ic50_newdesc.txt");
        ImportGenericAssayEntity.importData(fileNewDesc, GeneticAlterationType.GENERIC_ASSAY, "NAME,DESCRIPTION,URL", true);
        GenericAssayMeta treatment2 = DaoGenericAssay.getGenericAssayMetaByStableId("Irinotecan");
        assertEquals("New desc of Irinotecan", treatment2.getGenericEntityMetaProperties().get("DESCRIPTION"));
    }

    @Test
    public void testImportGenericAssayData() throws Exception {

        ProgressMonitor.setConsoleMode(false);
        
        // Open mutational signature test data file
        File file = new File("src/test/resources/data_mutational_signature.txt");
        
        // import data and test all mutational signatures were added
        ImportGenericAssayEntity.importData(file, GeneticAlterationType.GENERIC_ASSAY, "name,description", false);
        assertEquals(60, getNumRecordsForGenericAssay());
 
        // test wether a record can be retrieved via stable id 
        GenericAssayMeta genericAssayMeta1 = DaoGenericAssay.getGenericAssayMetaByStableId("mean_1");
        assertNotNull(genericAssayMeta1);

        // Test whether fields were populated correctly
        assertEquals("mean_1", genericAssayMeta1.getGenericEntityMetaProperties().get("name"));
        assertEquals("mean_1", genericAssayMeta1.getGenericEntityMetaProperties().get("description"));

        // // test fields should not be updated after loading new generic assay meta file
        File fileNewDesc = new File("src/test/resources/data_mutational_signature_new.txt");
        ImportGenericAssayEntity.importData(fileNewDesc, GeneticAlterationType.GENERIC_ASSAY, "name,description", false);
        GenericAssayMeta genericAssayMeta2 = DaoGenericAssay.getGenericAssayMetaByStableId("mean_1");
        assertEquals("mean_1", genericAssayMeta2.getGenericEntityMetaProperties().get("description"));

        // // test fields should be updated after loading new generic assay meta file
        ImportGenericAssayEntity.importData(fileNewDesc, GeneticAlterationType.GENERIC_ASSAY, "name,description", true);
        GenericAssayMeta genericAssayMeta3 = DaoGenericAssay.getGenericAssayMetaByStableId("mean_1");
        assertEquals("new mean_1", genericAssayMeta3.getGenericEntityMetaProperties().get("description"));

    }

    private int getNumRecordsForGenericAssay() {

		Connection con = null;
		PreparedStatement stat = null;
		ResultSet rs = null;
		try {
            con = JdbcUtil.getDbConnection(DaoGeneticEntity.class);
            stat = con.prepareStatement("SELECT COUNT(*) FROM genetic_entity WHERE ENTITY_TYPE = 'GENERIC_ASSAY'");
            rs = stat.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            System.out.println(e.getStackTrace());
        } finally {
            JdbcUtil.closeAll(DaoGeneticEntity.class, con, stat, rs);
        }

        return 0;
    }

}