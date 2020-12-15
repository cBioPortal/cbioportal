/*
 * Copyright (c) 2018 The Hyve B.V.
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

/*
 * @author Sander Tan
*/

package org.mskcc.cbio.portal.scripts;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.Assert.*;
import java.io.*;
import java.sql.*;
import java.util.List;

/**
 * Test class to test functionality of ImportStructralVariantData
*/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestImportStructuralVariantData{
    int studyId;
    int geneticProfileId;

    @Before
    public void setUp() throws DaoException
    {
        studyId = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub").getInternalId();
        geneticProfileId = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_structural_variants").getGeneticProfileId();
    }

    @Test
    public void testImportStructuralVariantData() throws DaoException, IOException {
        ProgressMonitor.setConsoleMode(false);

        // Load test structural variants
        File file = new File("src/test/resources/data_structural_variants.txt");
        ImportStructuralVariantData importer = new ImportStructuralVariantData(file, geneticProfileId, null);
        importer.importData();
        MySQLbulkLoader.flushAll();

        List<StructuralVariant> structuralVariants = DaoStructuralVariant.getAllStructuralVariants();
        assertEquals("KIAA1549-BRAF.K16B10.COSF509_2", structuralVariants.get(0).getSite2Description());
        assertEquals("ENST00000318522", structuralVariants.get(1).getSite1EnsemblTranscriptId());
    }
}
