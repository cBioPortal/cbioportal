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

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.lang3.StringEscapeUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.transaction.annotation.Transactional;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;

/**
 * Test class to test functionality of ImportStructralVariantData
*/
@RunWith(JUnitParamsRunner.class)
@Ignore

@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestImportStructuralVariantData{
    int studyId;
    int geneticProfileId;
    Set<String> noNamespaces = null;
    
    // Needed to run with JUnitParamsRunner.
    // See: https://github.com/Pragmatists/junitparams-spring-integration-example#alternative-way-to-start-spring-context
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Before
    public void setUp() throws DaoException {
        studyId = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub").getInternalId();
        geneticProfileId = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_structural_variants").getGeneticProfileId();
    }

    @Test
    public void testImportStructuralVariantData() throws DaoException, IOException {
        ProgressMonitor.setConsoleMode(false);

        // Load test structural variants
        File file = new File("src/test/resources/data_structural_variants.txt");
        ImportStructuralVariantData importer = new ImportStructuralVariantData(file, geneticProfileId, null, noNamespaces);
        importer.importData();
        MySQLbulkLoader.flushAll();

        List<StructuralVariant> structuralVariants = DaoStructuralVariant.getAllStructuralVariants();
        assertEquals("KIAA1549-BRAF.K16B10.COSF509_2", structuralVariants.get(0).getSite2Description());
        assertEquals("ENST00000318522", structuralVariants.get(1).getSite1EnsemblTranscriptId());
        assertEquals(5, structuralVariants.size());
        assertEquals(0L, (long) structuralVariants.get(2).getSite1EntrezGeneId()); // null in db
        assertEquals(673L, (long) structuralVariants.get(2).getSite2EntrezGeneId());
        assertEquals(0L, (long) structuralVariants.get(3).getSite2EntrezGeneId()); // null in db
        assertEquals(673L, (long) structuralVariants.get(3).getSite1EntrezGeneId());
        assertEquals(0L, (long) structuralVariants.get(4).getSite2EntrezGeneId()); // null in db
        assertEquals(207, (long) structuralVariants.get(4).getSite1EntrezGeneId()); // 110384692 in file, 207 in DB by hugo
    }

    private Object[] namespaceJsonParams() {
        return new Object[] {
            // Has namespaces:
            new Object[] { 
                "EML4-ALK.E13A20.AB462411_2",
                "{\"StructVarNamespace2\":{\"foo\":\"bar\"},\"StructVarNamespace\":{\"column1\":\"value1a\",\"column2\":\"value2a\"}}"
            },
            // Does not have namespaces:
            new Object[] { 
                "KIAA1549-BRAF.K16B10.COSF509_2",
                "{\"StructVarNamespace2\":{\"foo\":null},\"StructVarNamespace\":{\"column1\":null,\"column2\":null}}"
            }
        };
    }
    
    @Test
    @Parameters(method = "namespaceJsonParams")
    public void testImportStructuralVariantDataImportsCustomNamespacesFromTwoSamples(
        String site2description, 
        String expectedNamespaceJson
    ) throws DaoException, IOException {
        ProgressMonitor.setConsoleMode(false);

        // Load test structural variants
        File file = new File("src/test/resources/data_structural_variants.txt");
        Set<String> namespacesToImport = newHashSet("StructVarNamespace", "StructVarNamespace2");
        ImportStructuralVariantData importer = new ImportStructuralVariantData(file, geneticProfileId, null, namespacesToImport);
        importer.importData();
        MySQLbulkLoader.flushAll();

        List<StructuralVariant> all = DaoStructuralVariant
            .getAllStructuralVariants();
        
        // Namespace values present:
        String annotationJson = all
            .stream().filter(sv -> site2description.equals(sv.getSite2Description()))
            .findFirst().get()
            .getAnnotationJson();
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(
            mapper.readTree(expectedNamespaceJson), 
            mapper.readTree(annotationJson)
        );
    }
    
    @Test
    public void testImportStructuralVariantDataIgnoresUnspecifiedNamespaces() throws DaoException, IOException {
        ProgressMonitor.setConsoleMode(false);

        // Load test structural variants
        File file = new File("src/test/resources/data_structural_variants_with_unspecified_namespace.txt");
        Set<String> namespacesToImport = newHashSet("StructVarNamespace", "StructVarNamespace2");
        ImportStructuralVariantData importer = new ImportStructuralVariantData(file, geneticProfileId, null, namespacesToImport);
        importer.importData();
        MySQLbulkLoader.flushAll();

        List<StructuralVariant> all = DaoStructuralVariant
            .getAllStructuralVariants();
        
        // FaultyStructVarNamespace.shouldNotBeImported is ignored:
        String annotationJson = all
            .stream().filter(sv -> "EML4-ALK.E13A20.AB462411_2".equals(sv.getSite2Description()))
            .findFirst().get()
            .getAnnotationJson();
        String expectedAnnotationJson = "{\"StructVarNamespace2\":{\"foo\":\"bar\"},\"StructVarNamespace\":{\"column1\":\"value1a\",\"column2\":\"value2a\"}}";
        assertEquals(expectedAnnotationJson, annotationJson);
    }
    
    @Test
    public void testImportStructuralVariantDataWithNoNamespaceData() throws DaoException, IOException {
        ProgressMonitor.setConsoleMode(false);

        // Load test structural variants
        File file = new File("src/test/resources/data_structural_variants_with_no_namespace_data.txt");
        Set<String> namespacesToImport = newHashSet("StructVarNamespace", "StructVarNamespace2");
        ImportStructuralVariantData importer = new ImportStructuralVariantData(file, geneticProfileId, null, namespacesToImport);
        importer.importData();
        MySQLbulkLoader.flushAll();

        List<StructuralVariant> all = DaoStructuralVariant
            .getAllStructuralVariants();
        
        // result should be null:
        String annotationJson = all
            .stream().filter(sv -> "EML4-ALK.E13A20.AB462411_2".equals(sv.getSite2Description()))
            .findFirst().get()
            .getAnnotationJson();
        String expectedAnnotationJson = null;
        assertEquals(expectedAnnotationJson, annotationJson);
    }
}
