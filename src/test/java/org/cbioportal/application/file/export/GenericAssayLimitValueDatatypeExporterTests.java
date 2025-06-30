package org.cbioportal.application.file.export;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.cbioportal.application.file.export.exporters.ExportDetails;
import org.cbioportal.application.file.export.exporters.GenericAssayLimitValueDatatypeExporter;
import org.cbioportal.application.file.export.services.GeneticProfileDataService;
import org.cbioportal.application.file.export.services.GeneticProfileService;
import org.cbioportal.application.file.model.GenericEntityProperty;
import org.cbioportal.application.file.model.GeneticEntity;
import org.cbioportal.application.file.model.GeneticProfileData;
import org.cbioportal.application.file.model.GeneticProfileDatatypeMetadata;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.junit.Test;

public class GenericAssayLimitValueDatatypeExporterTests {

  GeneticProfileService geneticProfileService =
      new GeneticProfileService(null) {
        @Override
        public List<GeneticProfileDatatypeMetadata> getGeneticProfiles(
            String studyId, Set<String> sampleIds, String geneticAlterationType, String datatype) {
          GeneticProfileDatatypeMetadata metadata = new GeneticProfileDatatypeMetadata();
          metadata.setCancerStudyIdentifier(studyId);
          metadata.setStableId("GENERIC_ASSAY_STABLE_ID");
          metadata.setGeneticAlterationType("GENERIC_ASSAY");
          metadata.setDatatype("LIMIT-VALUE");
          return List.of(metadata);
        }
      };

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

          @Override
          public List<String> getDistinctGenericEntityMetaPropertyNames(
              String molecularProfileStableId) {
            return emptyList();
          }
        };

    GenericAssayLimitValueDatatypeExporter exporter =
        new GenericAssayLimitValueDatatypeExporter(
            geneticProfileService, geneticProfileDataService);
    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> exporter.exportData(factory, new ExportDetails("TEST_STUDY_ID")));
    assertThat(exception.getMessage(), containsString("Sample stable ID is null"));
  }

  @Test
  public void testMismatchedSampleSizes() {
    var factory = new InMemoryFileWriterFactory();

    GeneticProfileDataService geneticProfileDataService =
        new GeneticProfileDataService(null) {
          @Override
          public CloseableIterator<GeneticProfileData> getData(String molecularProfileStableId) {
            GeneticProfileData data = new GeneticProfileData();
            var geneticEntity = new GeneticEntity();
            geneticEntity.setGeneticEntityId(1);
            geneticEntity.setStableId("GENETIC_ENTITY_1");
            data.setGeneticEntity(geneticEntity);
            data.setCommaSeparatedValues("1.23"); // Only one value
            return new SimpleCloseableIterator<>(List.of(data));
          }

          @Override
          public List<String> getSampleStableIds(String molecularProfileStableId) {
            return List.of("SAMPLE_1", "SAMPLE_2"); // Two sample IDs
          }

          @Override
          public List<String> getDistinctGenericEntityMetaPropertyNames(
              String molecularProfileStableId) {
            return emptyList();
          }
        };

    GenericAssayLimitValueDatatypeExporter exporter =
        new GenericAssayLimitValueDatatypeExporter(
            geneticProfileService, geneticProfileDataService);

    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> exporter.exportData(factory, new ExportDetails("TEST_STUDY_ID")));
    assertThat(
        exception.getMessage(),
        containsString("Number of values does not match number of sample stable IDs"));
  }

  @Test
  public void testGeneticEntityIdNotAscending() {
    var factory = new InMemoryFileWriterFactory();

    GeneticProfileDataService geneticProfileDataService =
        new GeneticProfileDataService(null) {
          @Override
          public CloseableIterator<GeneticProfileData> getData(String molecularProfileStableId) {
            GeneticProfileData data1 = new GeneticProfileData();
            var geneticEntity2 = new GeneticEntity();
            geneticEntity2.setGeneticEntityId(2);
            data1.setGeneticEntity(geneticEntity2);
            var geneticEntity1 = new GeneticEntity();
            geneticEntity1.setGeneticEntityId(1); // First entity
            GeneticProfileData data2 = new GeneticProfileData();
            data2.setGeneticEntity(geneticEntity1);
            return new SimpleCloseableIterator<>(List.of(data1, data2));
          }

          @Override
          public List<String> getSampleStableIds(String molecularProfileStableId) {
            return List.of("SAMPLE_1", "SAMPLE_2"); // Two sample IDs
          }

          @Override
          public List<String> getDistinctGenericEntityMetaPropertyNames(
              String molecularProfileStableId) {
            return emptyList();
          }
        };

    GenericAssayLimitValueDatatypeExporter exporter =
        new GenericAssayLimitValueDatatypeExporter(
            geneticProfileService, geneticProfileDataService);

    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> exporter.exportData(factory, new ExportDetails("TEST_STUDY_ID")));
    assertThat(
        exception.getMessage(), containsString("Genetic entity ID is not in ascending order"));
  }

  @Test
  public void testNullPropertyName() {
    var factory = new InMemoryFileWriterFactory();

    GeneticProfileDataService geneticProfileDataService =
        new GeneticProfileDataService(null) {
          @Override
          public CloseableIterator<GenericEntityProperty> getGenericEntityMetaProperties(
              String molecularProfileStableId) {
            GenericEntityProperty property = new GenericEntityProperty();
            property.setGeneticEntityId(1);
            property.setName(null); // Null property name
            return new SimpleCloseableIterator<>(List.of(property));
          }

          @Override
          public List<String> getDistinctGenericEntityMetaPropertyNames(
              String molecularProfileStableId) {
            return List.of("property1");
          }

          @Override
          public List<String> getSampleStableIds(String molecularProfileStableId) {
            return List.of("SAMPLE_1", "SAMPLE_2"); // Two sample IDs
          }

          @Override
          public CloseableIterator<GeneticProfileData> getData(String molecularProfileStableId) {
            GeneticProfileData data = new GeneticProfileData();
            var geneticEntity = new GeneticEntity();
            geneticEntity.setGeneticEntityId(1);
            geneticEntity.setStableId("GENETIC_ENTITY_1");
            data.setGeneticEntity(geneticEntity);
            data.setCommaSeparatedValues("1.23,2.34"); // Two values
            return new SimpleCloseableIterator<>(List.of(data));
          }
        };

    GenericAssayLimitValueDatatypeExporter exporter =
        new GenericAssayLimitValueDatatypeExporter(
            geneticProfileService, geneticProfileDataService);

    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> exporter.exportData(factory, new ExportDetails("TEST_STUDY_ID")));
    assertThat(exception.getMessage(), containsString("Property name or value is null"));
  }

  @Test
  public void throwsExceptionWhenGeneticEntityIsNull() {
    var factory = new InMemoryFileWriterFactory();

    GeneticProfileDataService geneticProfileDataService =
        new GeneticProfileDataService(null) {
          @Override
          public CloseableIterator<GeneticProfileData> getData(String molecularProfileStableId) {
            GeneticProfileData data = new GeneticProfileData();
            data.setGeneticEntity(null); // Genetic entity is null
            return new SimpleCloseableIterator<>(List.of(data));
          }

          @Override
          public List<String> getSampleStableIds(String molecularProfileStableId) {
            return List.of("SAMPLE_1");
          }

          @Override
          public List<String> getDistinctGenericEntityMetaPropertyNames(
              String molecularProfileStableId) {
            return emptyList();
          }
        };

    GenericAssayLimitValueDatatypeExporter exporter =
        new GenericAssayLimitValueDatatypeExporter(
            geneticProfileService, geneticProfileDataService);

    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> exporter.exportData(factory, new ExportDetails("TEST_STUDY_ID")));
    assertThat(exception.getMessage(), containsString("Genetic entity or its ID is null"));
  }

  @Test
  public void throwsExceptionWhenGeneticEntityIdIsNull() {
    var factory = new InMemoryFileWriterFactory();

    GeneticProfileDataService geneticProfileDataService =
        new GeneticProfileDataService(null) {
          @Override
          public CloseableIterator<GeneticProfileData> getData(String molecularProfileStableId) {
            GeneticProfileData data = new GeneticProfileData();
            var geneticEntity = new GeneticEntity();
            geneticEntity.setGeneticEntityId(null); // Genetic entity ID is null
            data.setGeneticEntity(geneticEntity);
            return new SimpleCloseableIterator<>(List.of(data));
          }

          @Override
          public List<String> getSampleStableIds(String molecularProfileStableId) {
            return List.of("SAMPLE_1");
          }

          @Override
          public List<String> getDistinctGenericEntityMetaPropertyNames(
              String molecularProfileStableId) {
            return emptyList();
          }
        };
    GenericAssayLimitValueDatatypeExporter exporter =
        new GenericAssayLimitValueDatatypeExporter(
            geneticProfileService, geneticProfileDataService);

    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () -> exporter.exportData(factory, new ExportDetails("TEST_STUDY_ID")));
    assertThat(exception.getMessage(), containsString("Genetic entity or its ID is null"));
  }

  @Test
  public void testExport() {
    var factory = new InMemoryFileWriterFactory();

    GeneticProfileDataService geneticProfileDataService =
        new GeneticProfileDataService(null) {
          @Override
          public CloseableIterator<GeneticProfileData> getData(String molecularProfileStableId) {
            GeneticProfileData data1 = new GeneticProfileData();
            var geneticEntity1 = new GeneticEntity();
            geneticEntity1.setGeneticEntityId(1);
            geneticEntity1.setStableId("GENETIC_ENTITY_1");
            data1.setGeneticEntity(geneticEntity1);
            data1.setCommaSeparatedValues("1.23,2.34,3.45");

            GeneticProfileData data2 = new GeneticProfileData();
            var geneticEntity2 = new GeneticEntity();
            geneticEntity2.setGeneticEntityId(2);
            geneticEntity2.setStableId("GENETIC_ENTITY_2");
            data2.setGeneticEntity(geneticEntity2);
            data2.setCommaSeparatedValues("4.56,5.67,6.78");

            return new SimpleCloseableIterator<>(List.of(data1, data2));
          }

          @Override
          public CloseableIterator<GenericEntityProperty> getGenericEntityMetaProperties(
              String molecularProfileStableId) {
            GenericEntityProperty property1 = new GenericEntityProperty();
            property1.setGeneticEntityId(1);
            property1.setName("property1");
            property1.setValue("value1");

            GenericEntityProperty property2 = new GenericEntityProperty();
            property2.setGeneticEntityId(1);
            property2.setName("property2");
            property2.setValue("value2");

            GenericEntityProperty property3 = new GenericEntityProperty();
            property3.setGeneticEntityId(2);
            property3.setName("property1");
            property3.setValue("value3");

            return new SimpleCloseableIterator<>(List.of(property1, property2, property3));
          }

          @Override
          public List<String> getSampleStableIds(String molecularProfileStableId) {
            return List.of("SAMPLE_1", "SAMPLE_2", "SAMPLE_3");
          }

          @Override
          public List<String> getDistinctGenericEntityMetaPropertyNames(
              String molecularProfileStableId) {
            return List.of("property1", "property2");
          }
        };

    GenericAssayLimitValueDatatypeExporter exporter =
        new GenericAssayLimitValueDatatypeExporter(
            geneticProfileService, geneticProfileDataService);

    boolean exported = exporter.exportData(factory, new ExportDetails("TEST_STUDY_ID"));

    assertTrue(exported);
    var fileContents = factory.getFileContents();
    assertEquals(
        Set.of(
            "meta_generic_assay_limit-value_generic_assay_stable_id.txt",
            "data_generic_assay_limit-value_generic_assay_stable_id.txt"),
        fileContents.keySet());

    assertEquals(
        "cancer_study_identifier: TEST_STUDY_ID\n"
            + "genetic_alteration_type: GENERIC_ASSAY\n"
            + "datatype: LIMIT-VALUE\n"
            + "stable_id: GENERIC_ASSAY_STABLE_ID\n"
            + "generic_entity_meta_properties: property1,property2\n"
            + "data_filename: data_generic_assay_limit-value_generic_assay_stable_id.txt\n",
        fileContents.get("meta_generic_assay_limit-value_generic_assay_stable_id.txt").toString());

    assertEquals(
        """
            ENTITY_STABLE_ID\tproperty1\tproperty2\tSAMPLE_1\tSAMPLE_2\tSAMPLE_3
            GENETIC_ENTITY_1\tvalue1\tvalue2\t1.23\t2.34\t3.45
            GENETIC_ENTITY_2\tvalue3\t\t4.56\t5.67\t6.78
            """,
        fileContents.get("data_generic_assay_limit-value_generic_assay_stable_id.txt").toString());
  }

  @Test
  public void exportsDataWithOnlyHeaderWhenNoRows() {
    var factory = new InMemoryFileWriterFactory();

    GeneticProfileDataService geneticProfileDataService =
        new GeneticProfileDataService(null) {
          @Override
          public CloseableIterator<GeneticProfileData> getData(String molecularProfileStableId) {
            return new SimpleCloseableIterator<>(List.of()); // No rows
          }

          @Override
          public CloseableIterator<GenericEntityProperty> getGenericEntityMetaProperties(
              String molecularProfileStableId) {
            return new SimpleCloseableIterator<>(List.of()); // No properties
          }

          @Override
          public List<String> getSampleStableIds(String molecularProfileStableId) {
            return List.of("SAMPLE_1", "SAMPLE_2", "SAMPLE_3"); // Sample IDs for the header
          }

          @Override
          public List<String> getDistinctGenericEntityMetaPropertyNames(
              String molecularProfileStableId) {
            return List.of("property1", "property2"); // Properties for the header
          }
        };

    GenericAssayLimitValueDatatypeExporter exporter =
        new GenericAssayLimitValueDatatypeExporter(
            geneticProfileService, geneticProfileDataService);

    boolean exported = exporter.exportData(factory, new ExportDetails("TEST_STUDY_ID"));

    assertTrue(exported);
    var fileContents = factory.getFileContents();
    assertEquals(
        Set.of(
            "meta_generic_assay_limit-value_generic_assay_stable_id.txt",
            "data_generic_assay_limit-value_generic_assay_stable_id.txt"),
        fileContents.keySet());

    assertEquals(
        "cancer_study_identifier: TEST_STUDY_ID\n"
            + "genetic_alteration_type: GENERIC_ASSAY\n"
            + "datatype: LIMIT-VALUE\n"
            + "stable_id: GENERIC_ASSAY_STABLE_ID\n"
            + "generic_entity_meta_properties: property1,property2\n"
            + "data_filename: data_generic_assay_limit-value_generic_assay_stable_id.txt\n",
        fileContents.get("meta_generic_assay_limit-value_generic_assay_stable_id.txt").toString());

    assertEquals(
        """
            ENTITY_STABLE_ID\tproperty1\tproperty2\tSAMPLE_1\tSAMPLE_2\tSAMPLE_3
            """,
        fileContents.get("data_generic_assay_limit-value_generic_assay_stable_id.txt").toString());
  }

  @Test
  public void testDoNotExportPatientLevelData() {
    var factory = new InMemoryFileWriterFactory();

    GenericAssayLimitValueDatatypeExporter exporter =
        new GenericAssayLimitValueDatatypeExporter(
            new GeneticProfileService(null) {
              @Override
              public List<GeneticProfileDatatypeMetadata> getGeneticProfiles(
                  String studyId,
                  Set<String> sampleIds,
                  String geneticAlterationType,
                  String datatype) {
                GeneticProfileDatatypeMetadata metadata = new GeneticProfileDatatypeMetadata();
                metadata.setCancerStudyIdentifier(studyId);
                metadata.setStableId("GENERIC_ASSAY_STABLE_ID");
                metadata.setGeneticAlterationType("GENERIC_ASSAY");
                metadata.setDatatype("LIMIT-VALUE");
                metadata.setPatientLevel(true); // Set patient level to false
                return List.of(metadata);
              }
            },
            null);

    boolean exported = exporter.exportData(factory, new ExportDetails("TEST_STUDY_ID"));
    assertFalse(exported);
    assertTrue(factory.getFileContents().isEmpty());
  }
}
