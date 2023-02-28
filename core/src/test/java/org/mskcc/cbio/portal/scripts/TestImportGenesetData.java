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
 * @author Sander Tan
*/

package org.mskcc.cbio.portal.scripts;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.mskcc.cbio.portal.dao.DaoGeneset;
import org.mskcc.cbio.portal.model.Geneset;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/*
 * JUnit tests for ImportGenesetData class.
*/

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestImportGenesetData {

	@Test
    public void testImportGenesetData() throws Exception {
        ProgressMonitor.setConsoleMode(false);
        
        // Open genesets test data file
        File file = new File("src/test/resources/genesets/unit-test1_genesets.gmt");
        boolean updateInfo = false;
        boolean newVersion = true;
        int skippedGenes = ImportGenesetData.importData(file, updateInfo, newVersion);

        // Open supplementary file
        file = new File("src/test/resources/genesets/unit-test1_supp-genesets.txt");
        ImportGenesetData.importSuppGenesetData(file);
        
        // Test database entries
        Geneset geneset = DaoGeneset.getGenesetByExternalId("UNITTEST_GENESET5");
        assertEquals("UNITTEST_GENESET5", geneset.getExternalId());
        geneset = DaoGeneset.getGenesetByExternalId("UNITTEST_GENESET10");
        assertEquals("http://www.broadinstitute.org/gsea/msigdb/cards/GCNP_SHH_UP_EARLY.V1_UP", geneset.getRefLink());
        
        // Test warning message
        assertEquals(5, skippedGenes);
        
        // Test database entries supplementary file
        geneset = DaoGeneset.getGenesetByExternalId("UNITTEST_GENESET2");
        assertEquals("Genes up-regulated in RK3E cells (kidney epithelium) over-expressing GLI1 [GeneID=2735].", geneset.getDescription());
        geneset = DaoGeneset.getGenesetByExternalId("UNITTEST_GENESET8");
        assertEquals("UNITTEST_GENESET8", geneset.getName());
        
        // Test update of genes 
        // Open genesets test data file
        file = new File("src/test/resources/genesets/unit-test2_genesets.gmt");
        newVersion = false;
        updateInfo = true;
        skippedGenes = ImportGenesetData.importData(file, updateInfo, newVersion);

        // Open supplementary file
        file = new File("src/test/resources/genesets/unit-test2_supp-genesets.txt");
        ImportGenesetData.importSuppGenesetData(file);
        
        geneset = DaoGeneset.getGenesetByExternalId("UNITTEST_GENESET2");
        assertEquals("A made up description is suited for this a fake gene.", geneset.getDescription());
        geneset = DaoGeneset.getGenesetByExternalId("UNITTEST_GENESET1");
        assertEquals("Thought of new nice name for this geneset", geneset.getName());
        geneset = DaoGeneset.getGenesetByExternalId("UNITTEST_GENESET1");
        assertEquals("http://www.thehyve.nl/", geneset.getRefLink());
        
    }
}
