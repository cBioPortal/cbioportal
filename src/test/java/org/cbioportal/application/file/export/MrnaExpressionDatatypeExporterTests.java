package org.cbioportal.application.file.export;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.cbioportal.application.file.export.exporters.ExportDetails;
import org.cbioportal.application.file.export.exporters.MrnaExpressionContinuousDatatypeExporter;
import org.cbioportal.application.file.export.services.GeneticProfileDataService;
import org.cbioportal.application.file.export.services.GeneticProfileService;
import org.cbioportal.application.file.model.Gene;
import org.cbioportal.application.file.model.GeneticEntity;
import org.cbioportal.application.file.model.GeneticProfileData;
import org.cbioportal.application.file.model.GeneticProfileDatatypeMetadata;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.junit.Test;

public class MrnaExpressionDatatypeExporterTests {

  GeneticProfileService geneticProfileService =
      new GeneticProfileService(null) {
        @Override
        public List<GeneticProfileDatatypeMetadata> getGeneticProfiles(
            String studyId, Set<String> sampleIds, String geneticAlterationType, String datatype) {
          GeneticProfileDatatypeMetadata metadata = new GeneticProfileDatatypeMetadata();
          metadata.setCancerStudyIdentifier(studyId);
          metadata.setStableId("MAF_STABLE_ID");
          metadata.setGeneticAlterationType("MRNA_EXPRESSION");
          metadata.setDatatype("CONTINUOUS");
          return List.of(metadata);
        }
      };

  @Test
  public void testNotExported() {
    var factory = new InMemoryFileWriterFactory();

    GeneticProfileDataService geneticProfileDataService =
        new GeneticProfileDataService(null) {
          @Override
          public CloseableIterator<GeneticProfileData> getData(String molecularProfileStableId) {
            return new SimpleCloseableIterator<>(emptyList());
          }
        };

    MrnaExpressionContinuousDatatypeExporter exporter =
        new MrnaExpressionContinuousDatatypeExporter(
            new GeneticProfileService(null) {
              @Override
              public List<GeneticProfileDatatypeMetadata> getGeneticProfiles(
                  String studyId,
                  Set<String> sampleIds,
                  String geneticAlterationType,
                  String datatype) {
                return emptyList();
              }
            },
            geneticProfileDataService);

    boolean exported = exporter.exportData(factory, new ExportDetails("TEST_STUDY_ID"));

    assertFalse(exported);
  }

  @Test
  public void testMrnaExpressionExport() {
    var factory = new InMemoryFileWriterFactory();

    GeneticProfileDataService geneticProfileDataService =
        new GeneticProfileDataService(null) {
          @Override
          public CloseableIterator<GeneticProfileData> getData(String molecularProfileStableId) {
            GeneticProfileData data = new GeneticProfileData();
            var gene = new Gene();
            gene.setHugoGeneSymbol("GENE_SYMBOL");
            gene.setEntrezGeneId(12345);
            data.setGene(gene);
            GeneticEntity geneticEntity = new GeneticEntity();
            geneticEntity.setGeneticEntityId(43210);
            geneticEntity.setStableId("GENE1");
            data.setGeneticEntity(geneticEntity);
            data.setCommaSeparatedValues("1.23,4.56,");
            return new SimpleCloseableIterator<>(List.of(data));
          }

          @Override
          public List<String> getSampleStableIds(String molecularProfileStableId) {
            return List.of("SAMPLE_1", "SAMPLE_2");
          }
        };

    MrnaExpressionContinuousDatatypeExporter exporter =
        new MrnaExpressionContinuousDatatypeExporter(
            geneticProfileService, geneticProfileDataService);

    boolean exported = exporter.exportData(factory, new ExportDetails("TEST_STUDY_ID"));

    assertTrue(exported);
    var fileContents = factory.getFileContents();
    assertEquals(
        Set.of(
            "meta_mrna_expression_continuous_maf_stable_id.txt",
            "data_mrna_expression_continuous_maf_stable_id.txt"),
        fileContents.keySet());

    assertEquals(
        """
            cancer_study_identifier: TEST_STUDY_ID
            genetic_alteration_type: MRNA_EXPRESSION
            datatype: CONTINUOUS
            stable_id: MAF_STABLE_ID
            data_filename: data_mrna_expression_continuous_maf_stable_id.txt
            """,
        fileContents.get("meta_mrna_expression_continuous_maf_stable_id.txt").toString());

    assertEquals(
        """
            Hugo_Symbol\tEntrez_Gene_Id\tSAMPLE_1\tSAMPLE_2
            GENE_SYMBOL\t12345\t1.23\t4.56
            """,
        fileContents.get("data_mrna_expression_continuous_maf_stable_id.txt").toString());
  }

  @Test
  public void testMrnaExpressionExport_SampleIdsOrder() {
    var factory = new InMemoryFileWriterFactory();

    GeneticProfileDataService geneticProfileDataService =
        new GeneticProfileDataService(null) {
          @Override
          public CloseableIterator<GeneticProfileData> getData(String molecularProfileStableId) {
            GeneticProfileData data = new GeneticProfileData();
            var gene = new Gene();
            gene.setHugoGeneSymbol("GENE_SYMBOL");
            gene.setEntrezGeneId(12345);
            data.setGene(gene);
            GeneticEntity geneticEntity = new GeneticEntity();
            geneticEntity.setGeneticEntityId(43210);
            geneticEntity.setStableId("GENE1");
            data.setGeneticEntity(geneticEntity);
            data.setCommaSeparatedValues("1.23,4.56,");
            return new SimpleCloseableIterator<>(List.of(data));
          }

          @Override
          public List<String> getSampleStableIds(String molecularProfileStableId) {
            return List.of("SAMPLE_1", "SAMPLE_2");
          }
        };

    MrnaExpressionContinuousDatatypeExporter exporter =
        new MrnaExpressionContinuousDatatypeExporter(
            geneticProfileService, geneticProfileDataService);

    ExportDetails exportDetails =
        new ExportDetails(
            "TEST_STUDY_ID",
            null,
            new LinkedHashSet<>(List.of("SAMPLE_2", "SAMPLE_1"))); // Different order
    boolean exported = exporter.exportData(factory, exportDetails);

    assertTrue(exported);
    var fileContents = factory.getFileContents();
    assertEquals(
        Set.of(
            "meta_mrna_expression_continuous_maf_stable_id.txt",
            "data_mrna_expression_continuous_maf_stable_id.txt"),
        fileContents.keySet());

    assertEquals(
        """
            cancer_study_identifier: TEST_STUDY_ID
            genetic_alteration_type: MRNA_EXPRESSION
            datatype: CONTINUOUS
            stable_id: MAF_STABLE_ID
            data_filename: data_mrna_expression_continuous_maf_stable_id.txt
            """,
        fileContents.get("meta_mrna_expression_continuous_maf_stable_id.txt").toString());

    assertEquals(
        """
                Hugo_Symbol\tEntrez_Gene_Id\tSAMPLE_1\tSAMPLE_2
                GENE_SYMBOL\t12345\t1.23\t4.56
                """,
        fileContents.get("data_mrna_expression_continuous_maf_stable_id.txt").toString());
  }

  @Test
  public void testMrnaExpressionExportNoRows() {
    var factory = new InMemoryFileWriterFactory();

    GeneticProfileDataService geneticProfileDataService =
        new GeneticProfileDataService(null) {
          @Override
          public CloseableIterator<GeneticProfileData> getData(String molecularProfileStableId) {
            return new SimpleCloseableIterator<>(emptyList());
          }

          @Override
          public List<String> getSampleStableIds(String molecularProfileStableId) {
            return List.of("SAMPLE_1", "SAMPLE_2");
          }
        };

    MrnaExpressionContinuousDatatypeExporter exporter =
        new MrnaExpressionContinuousDatatypeExporter(
            geneticProfileService, geneticProfileDataService);

    boolean exported = exporter.exportData(factory, new ExportDetails("TEST_STUDY_ID"));

    assertTrue(exported);
    var fileContents = factory.getFileContents();
    assertEquals(
        Set.of(
            "meta_mrna_expression_continuous_maf_stable_id.txt",
            "data_mrna_expression_continuous_maf_stable_id.txt"),
        fileContents.keySet());

    assertEquals(
        """
            Hugo_Symbol\tEntrez_Gene_Id\tSAMPLE_1\tSAMPLE_2
            """,
        fileContents.get("data_mrna_expression_continuous_maf_stable_id.txt").toString());
  }

  @Test
  public void testMismatchedSampleSizes() {
    var factory = new InMemoryFileWriterFactory();

    GeneticProfileDataService geneticProfileDataService =
        new GeneticProfileDataService(null) {
          @Override
          public CloseableIterator<GeneticProfileData> getData(String molecularProfileStableId) {
            GeneticProfileData data = new GeneticProfileData();
            var gene = new Gene();
            gene.setHugoGeneSymbol("GENE_SYMBOL");
            gene.setEntrezGeneId(12345);
            data.setGene(gene);
            GeneticEntity geneticEntity = new GeneticEntity();
            geneticEntity.setGeneticEntityId(43210);
            geneticEntity.setStableId("GENE1");
            data.setGeneticEntity(geneticEntity);
            data.setCommaSeparatedValues("1.23"); // Only one value
            return new SimpleCloseableIterator<>(List.of(data));
          }

          @Override
          public List<String> getSampleStableIds(String molecularProfileStableId) {
            return List.of("SAMPLE_1", "SAMPLE_2"); // Two sample IDs
          }
        };

    MrnaExpressionContinuousDatatypeExporter exporter =
        new MrnaExpressionContinuousDatatypeExporter(
            geneticProfileService, geneticProfileDataService);
    ExportDetails exportDetails = new ExportDetails("TEST_STUDY_ID");
    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> exporter.exportData(factory, exportDetails));
    assertThat(
        exception.getMessage(),
        containsString("Number of values does not match number of sample stable IDs"));
  }

  @Test
  public void testNullSampleStableId() {
    var factory = new InMemoryFileWriterFactory();

    GeneticProfileDataService geneticProfileDataService =
        new GeneticProfileDataService(null) {
          @Override
          public List<String> getSampleStableIds(String molecularProfileStableId) {
            var list = new ArrayList<String>();
            list.add("SAMPLE_1");
            list.add(null); // Adding a null sample ID
            return list;
          }
        };

    MrnaExpressionContinuousDatatypeExporter exporter =
        new MrnaExpressionContinuousDatatypeExporter(
            geneticProfileService, geneticProfileDataService);
    ExportDetails exportDetails = new ExportDetails("TEST_STUDY_ID");
    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> exporter.exportData(factory, exportDetails));
    assertThat(exception.getMessage(), containsString("Sample stable ID is null"));
  }
}
