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
 * WSI_SAMPLE / WSI_PATIENT rows never reach the generic resource table, and for every other row
 * public metadata stays searchable while wsi_serving stays private, whatever its TYPE. Uses the
 * wsi_resource_table_study fixture: WSI_SAMPLE rows 900501/900502, and EXTERNAL_SLIDES rows
 * 900505/900506 whose sealed serving values contain "secretpath" and sort in the opposite order
 * ("zzz" for 900505, "aaa" for 900506) to the rows' ids.
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
    assertThat(mapper.getResourceTableFacetValues(query, "rdata.type", FACET_LIMIT)).isEmpty();
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
    ResourceTableMetadataResult external = useCase.execute(query(EXTERNAL, null, null, null, null));
    assertThat(external.totalRowCount()).isEqualTo(2);
    assertThat(external.columns())
        .extracting(ResourceColumnInfo::id)
        .doesNotContain("metadata:wsi_serving");
    assertThat(external.toString()).doesNotContain("secretpath");
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

  @Test
  public void searchDoesNotMatchServingValues() {
    for (String term :
        List.of("secretpath", "sealed_source", "zzz-secretpath-1-", "AAAAAAAAAAAA")) {
      ResourceTableQuery query = query(EXTERNAL, term, null, null, null);
      assertThat(repository.getResourceTableRows(query)).as(term).isEmpty();
      assertThat(repository.getResourceTableCounts(query).rowCount()).as(term).isZero();
    }
  }

  @Test
  public void searchMatchesMetadataOfRowsWithNullType() {
    ResourceTableQuery query = query(NOTES, "tumor board", null, null, null);

    List<ResourceTableRow> rows = repository.getResourceTableRows(query);

    assertThat(ids(query)).containsExactly("900503");
    assertThat(rows.get(0).type()).isNull();
    assertThat(repository.getResourceTableCounts(query).rowCount()).isEqualTo(1);
    assertThat(repository.getResourceTableRows(query(NOTES, "secretpath", null, null, null)))
        .isEmpty();
  }

  // ---- Filters, sorting, facets and discovery ----

  @Test
  public void filterOnServingMetadataDoesNotChangeRowsOrCounts() {
    ResourceTableQuery unfiltered = query(EXTERNAL, null, null, null, null);
    List<String> expectedIds = ids(unfiltered);
    long expectedCount = repository.getResourceTableCounts(unfiltered).rowCount();
    assertThat(expectedIds).hasSize(2);

    for (ResourceColumnFilter filter :
        List.of(
            new ResourceColumnFilter("metadata:wsi_serving", "contains", List.of("secretpath")),
            new ResourceColumnFilter("metadata:wsi_serving", "contains", List.of("no-such-path")),
            new ResourceColumnFilter("metadata:wsi_serving", "in", List.of()),
            new ResourceColumnFilter("metadata:wsi_serving", "notEquals", List.of("x")))) {
      ResourceTableQuery filtered = query(EXTERNAL, null, null, null, List.of(filter));
      assertThat(ids(filtered))
          .as(filter.toString())
          .containsExactlyInAnyOrderElementsOf(expectedIds);
      assertThat(repository.getResourceTableCounts(filtered).rowCount())
          .as(filter.toString())
          .isEqualTo(expectedCount);
    }
  }

  @Test
  public void sortOnServingMetadataDoesNotFollowServingPaths() {
    // Sorting by the serving path would put 900506 ("aaa...") first ascending and 900505
    // ("zzz...") first descending. The guard orders by row id instead.
    assertThat(ids(query(EXTERNAL, null, "metadata:wsi_serving", "ASC", null)))
        .containsExactly("900505", "900506");
    assertThat(ids(query(EXTERNAL, null, "metadata:wsi_serving", "DESC", null)))
        .containsExactly("900506", "900505");
  }

  @Test
  public void servingMetadataIsNeitherDiscoveredNorFaceted() {
    for (String resourceId : List.of(EXTERNAL, NOTES)) {
      ResourceTableQuery query = query(resourceId, null, null, null, null);

      ResourceTableMetadataView metadata = repository.getResourceTableMetadata(query);

      assertThat(metadata.columns())
          .extracting(ResourceColumnInfo::id)
          .doesNotContain("metadata:wsi_serving");
      assertThat(metadata.facets()).doesNotContainKey("metadata:wsi_serving");
      assertThat(metadata.facetRanges()).doesNotContainKey("metadata:wsi_serving");
      assertThat(
              mapper.getResourceTableMetadataFacets(
                  query, new String[] {"wsi_serving"}, FACET_LIMIT))
          .isEmpty();
    }
    assertThat(
            repository
                .getResourceTableMetadata(query(EXTERNAL, null, null, null, null))
                .facets()
                .get("metadata:stain_name"))
        .hasSize(2);
  }

  // ---- Row responses ----

  @Test
  public void rowsNeverContainServingMetadata() {
    for (String resourceId : List.of(EXTERNAL, NOTES)) {
      List<ResourceTableRow> rows =
          repository.getResourceTableRows(query(resourceId, null, null, null, null));

      assertThat(rows).hasSize(2);
      assertThat(rows)
          .allSatisfy(row -> assertThat(row.metadata()).doesNotContainKey("wsi_serving"));
    }
    ResourceTableRow note =
        repository.getResourceTableRows(query(NOTES, "tumor board", null, null, null)).get(0);
    assertThat(note.metadata()).containsEntry("note", "Reviewed by tumor board");
    ResourceTableRow slide =
        repository.getResourceTableRows(query(EXTERNAL, "Periodic acid", null, null, null)).get(0);
    assertThat(slide.metadata())
        .containsEntry("stain_name", "Periodic acid-Schiff")
        .containsEntry("part_description", "left kidney core biopsy");
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
