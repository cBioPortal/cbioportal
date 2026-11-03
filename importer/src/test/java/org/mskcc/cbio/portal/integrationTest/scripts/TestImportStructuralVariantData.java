/*
 * Copyright (c) 2018 The Hyve B.V.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

/*
 * @author Sander Tan
*/

package org.mskcc.cbio.portal.integrationTest.scripts;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.*;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.ClickHouseBulkLoader;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoStructuralVariant;
import org.mskcc.cbio.portal.model.StructuralVariant;
import org.mskcc.cbio.portal.scripts.ImportStructuralVariantData;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;

/**
 * Test class to test functionality of ImportStructralVariantData
*/
@RunWith(JUnitParamsRunner.class)

@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestImportStructuralVariantData extends IntegrationTestBase {
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
        ImportStructuralVariantData importer = new ImportStructuralVariantData(file, geneticProfileId, null, noNamespaces, false);
        importer.importData();
        ClickHouseBulkLoader.flushAll();

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
        ImportStructuralVariantData importer = new ImportStructuralVariantData(file, geneticProfileId, null, namespacesToImport, false);
        importer.importData();
        ClickHouseBulkLoader.flushAll();

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
        ImportStructuralVariantData importer = new ImportStructuralVariantData(file, geneticProfileId, null, namespacesToImport, false);
        importer.importData();
        ClickHouseBulkLoader.flushAll();

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
        ImportStructuralVariantData importer = new ImportStructuralVariantData(file, geneticProfileId, null, namespacesToImport, false);
        importer.importData();
        ClickHouseBulkLoader.flushAll();

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
