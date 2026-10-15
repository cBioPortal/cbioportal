package org.cbioportal.infrastructure.repository.clickhouse.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.cbioportal.domain.resource.ResourceColumnFilter;
import org.cbioportal.domain.resource.ResourceColumnInfo;
import org.cbioportal.domain.resource.ResourceTableMetadataResult;
import org.cbioportal.domain.resource.ResourceTableMetadataView;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableRow;
import org.cbioportal.domain.resource.ResourceTableTab;
import org.cbioportal.domain.resource.ResourceTabsRequest;
import org.cbioportal.domain.resource.usecase.GetResourceTableMetadataUseCase;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * WSI_SAMPLE / WSI_PATIENT rows never reach the generic resource table, while every other
 * resource's public metadata stays searchable. Uses the wsi_resource_table_study fixture:
 * WSI_SAMPLE rows 900501/900502, EXTERNAL_SLIDES rows 900505/900506 and PATHOLOGY_NOTES rows
 * 900503/900504.
 */
@RunWith(SpringRunner.class)
@Import({MyBatisConfig.class, ClickhouseResourceDataRepository.class})
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhouseResourceDataPrivacyTest {

  private static final String STUDY = "wsi_resource_table_study";
  private static final String WSI = "WSI_SAMPLE";
  private static final String EXTERNAL = "EXTERNAL_SLIDES";
  private static final String NOTES = "PATHOLOGY_NOTES";
  private static final List<String> WSI_ROW_IDS = List.of("900501", "900502");
  private static final String WSI_SLIDE_KEY = "5d41402abc4b2a76b9719d911017c592";

  /**
   * Fixture row id by url. Rows no longer carry their resource_data_id, and each fixture row has a
   * distinct url, so the url names the row.
   */
  private static final Map<String, String> ROW_ID_BY_URL =
      Map.of(
          "https://portal.example.org/wsi/patient/WSI-TABLE-PATIENT?studyId=wsi_resource_table_study&slideKey=5d41402abc4b2a76b9719d911017c592",
          "900501",
          "https://portal.example.org/wsi/patient/WSI-TABLE-PATIENT?studyId=wsi_resource_table_study&slideKey=7d793037a0760186574b0282f2f435e7",
          "900502",
          "https://example.com/notes/1.pdf",
          "900503",
          "https://example.com/notes/2.pdf",
          "900504",
          "https://slides.example.org/viewer/ext-1",
          "900505",
          "https://slides.example.org/viewer/ext-2",
          "900506");

  private static final int FACET_LIMIT = 501;
  private static final int KEY_SAMPLE_ROWS = 100_000;
  private static final long KEY_MAX_MEMORY_BYTES = 2L * 1024 * 1024 * 1024;

  @Autowired private ClickhouseResourceDataRepository repository;
  @Autowired private ClickhouseResourceDataMapper mapper;

  // ---- WSI resources are invisible to the generic resource table ----

  @Test
  public void wsiResourceIsNotATab() {
    List<ResourceTableTab> tabs =
        repository.getResourceTableTabs(new ResourceTabsRequest(List.of(STUDY), null, null));

    assertThat(tabs)
        .extracting(ResourceTableTab::resourceId)
        .containsExactlyInAnyOrder(EXTERNAL, NOTES)
        .doesNotContain("WSI_SAMPLE", "WSI_PATIENT");
    assertThat(mapper.getResourceTableTabs(new ResourceTabsRequest(List.of(STUDY), null, null)))
        .extracting(ResourceTableTab::resourceId)
        .doesNotContain("WSI_SAMPLE", "WSI_PATIENT");
  }

  @Test
  public void wsiRowsAreNeverReturnedOrCounted() {
    for (String resourceId : List.of("WSI_SAMPLE", "WSI_PATIENT")) {
      ResourceTableQuery query = query(resourceId, null, null, null, null);

      assertThat(repository.getResourceTableRows(query)).as(resourceId).isEmpty();
      assertThat(mapper.getResourceTableRows(query)).as(resourceId).isEmpty();
      assertThat(repository.getResourceTableCounts(query).rowCount()).as(resourceId).isZero();
      assertThat(mapper.getResourceDefinitionCustomMetadata(query)).as(resourceId).isEmpty();
    }
  }

  @Test
  public void wsiMetadataIsNeitherDiscoveredNorFaceted() {
    ResourceTableQuery query = query(WSI, null, null, null, null);

    ResourceTableMetadataView metadata = repository.getResourceTableMetadata(query);

    assertThat(metadata.columns()).isEmpty();
    assertThat(metadata.facets()).isEmpty();
    assertThat(metadata.facetRanges()).isEmpty();
    assertThat(
            mapper.getResourceTableMetadataKeyStats(query, KEY_SAMPLE_ROWS, KEY_MAX_MEMORY_BYTES))
        .isEmpty();
    for (String key : List.of("slide_key", "stain_name", "part_description")) {
      assertThat(mapper.getResourceTableMetadataFacets(query, new String[] {key}, FACET_LIMIT))
          .as(key)
          .isEmpty();
    }
  }

  /**
   * metadata/fetch serves the page-invariant half of the table (columns, facets, ranges, counts).
   * For a WSI resource it must describe an empty resource: only the builtin columns, no facets or
   * ranges, zero counts.
   */
  @Test
  public void metadataEndpointDescribesWsiResourcesAsEmpty() {
    GetResourceTableMetadataUseCase useCase = new GetResourceTableMetadataUseCase(repository);
    for (String resourceId : List.of("WSI_SAMPLE", "WSI_PATIENT")) {
      ResourceTableMetadataResult result =
          useCase.execute(query(resourceId, null, null, null, null));

      assertThat(result.columns())
          .as(resourceId)
          .isNotEmpty()
          .allSatisfy(
              column ->
                  assertThat(column.source()).isNotEqualTo(ResourceColumnInfo.SOURCE_METADATA));
      assertThat(result.totalRowCount()).as(resourceId).isZero();
      assertThat(result.filteredPatientCount()).as(resourceId).isZero();
      assertThat(result.filteredSampleCount()).as(resourceId).isZero();
      assertThat(result.facets()).as(resourceId).isEmpty();
      assertThat(result.facetRanges()).as(resourceId).isEmpty();
      assertThat(result.toString()).doesNotContain("Masson").doesNotContain("slide_key");
    }
  }

  @Test
  public void otherResourcesNeverSurfaceWsiRowsOrValues() {
    for (String resourceId : List.of(EXTERNAL, NOTES)) {
      ResourceTableQuery query = query(resourceId, null, null, null, null);
      List<ResourceTableRow> rows = repository.getResourceTableRows(query);

      assertThat(rows)
          .extracting(ClickhouseResourceDataPrivacyTest::rowId)
          .doesNotContainAnyElementsOf(WSI_ROW_IDS);
      assertThat(rows).extracting(ResourceTableRow::resourceId).containsOnly(resourceId);
      assertThat(rows.toString()).doesNotContain(WSI_SLIDE_KEY).doesNotContain("Masson");
      assertThat(repository.getResourceTableMetadata(query).facets().toString())
          .doesNotContain("Masson")
          .doesNotContain("liver");
    }
  }

  // ---- Search ----

  @Test
  public void searchMatchesPublicSlideStainAndSpecimenText() {
    assertThat(ids(query(EXTERNAL, "Periodic acid", null, null, null))).containsExactly("900505");
    assertThat(ids(query(EXTERNAL, "H&E", null, null, null))).containsExactly("900506");
    assertThat(ids(query(EXTERNAL, "kidney", null, null, null)))
        .containsExactlyInAnyOrder("900505", "900506");
    assertThat(ids(query(EXTERNAL, "right kidney margin", null, null, null)))
        .containsExactly("900506");
  }

  private ResourceTableQuery query(
      String resourceId,
      String search,
      String sortBy,
      String direction,
      List<ResourceColumnFilter> filters) {
    return new ResourceTableQuery(
        List.of(STUDY), resourceId, null, null, search, 0, 10, sortBy, direction, filters);
  }

  private List<String> ids(ResourceTableQuery query) {
    return repository.getResourceTableRows(query).stream()
        .map(ClickhouseResourceDataPrivacyTest::rowId)
        .toList();
  }

  private static String rowId(ResourceTableRow row) {
    return ROW_ID_BY_URL.getOrDefault(row.url(), row.url());
  }
}
