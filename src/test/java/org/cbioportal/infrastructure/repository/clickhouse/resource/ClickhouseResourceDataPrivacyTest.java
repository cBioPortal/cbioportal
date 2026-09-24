package org.cbioportal.infrastructure.repository.clickhouse.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.cbioportal.domain.resource.ResourceColumnFilter;
import org.cbioportal.domain.resource.ResourceColumnInfo;
import org.cbioportal.domain.resource.ResourceTableMetadataView;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableRow;
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
 * Public metadata stays searchable while wsi_serving stays private for every row, whatever its
 * TYPE. Uses the wsi_resource_table_study fixture, whose serving paths contain "secretpath" and
 * sort in the opposite order ("zzz" for 900501, "aaa" for 900502) to the rows' ids.
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
  private static final String NOTES = "PATHOLOGY_NOTES";

  @Autowired private ClickhouseResourceDataRepository repository;
  @Autowired private ClickhouseResourceDataMapper mapper;

  // ---- Search ----

  @Test
  public void searchMatchesPublicWsiStainAndSpecimenText() {
    assertThat(ids(query(WSI, "Periodic acid", null, null, null))).containsExactly("900501");
    assertThat(ids(query(WSI, "H&E", null, null, null))).containsExactly("900502");
    assertThat(ids(query(WSI, "kidney", null, null, null)))
        .containsExactlyInAnyOrder("900501", "900502");
    assertThat(ids(query(WSI, "right kidney margin", null, null, null))).containsExactly("900502");
  }

  @Test
  public void searchDoesNotMatchServingPaths() {
    for (String term : List.of("secretpath", "private-bucket", "zzz-secretpath-1.svs", "s3://")) {
      ResourceTableQuery query = query(WSI, term, null, null, null);
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
    ResourceTableQuery unfiltered = query(WSI, null, null, null, null);
    List<String> expectedIds = ids(unfiltered);
    long expectedCount = repository.getResourceTableCounts(unfiltered).rowCount();
    assertThat(expectedIds).hasSize(2);

    for (ResourceColumnFilter filter :
        List.of(
            new ResourceColumnFilter("metadata:wsi_serving", "contains", List.of("secretpath")),
            new ResourceColumnFilter("metadata:wsi_serving", "contains", List.of("no-such-path")),
            new ResourceColumnFilter("metadata:wsi_serving", "in", List.of()),
            new ResourceColumnFilter("metadata:wsi_serving", "notEquals", List.of("x")))) {
      ResourceTableQuery filtered = query(WSI, null, null, null, List.of(filter));
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
    // Sorting by the serving path would put 900502 ("aaa...") first ascending and 900501
    // ("zzz...") first descending. The guard orders by row id instead.
    assertThat(ids(query(WSI, null, "metadata:wsi_serving", "ASC", null)))
        .containsExactly("900501", "900502");
    assertThat(ids(query(WSI, null, "metadata:wsi_serving", "DESC", null)))
        .containsExactly("900502", "900501");
  }

  @Test
  public void servingMetadataIsNeitherDiscoveredNorFaceted() {
    for (String resourceId : List.of(WSI, NOTES)) {
      ResourceTableQuery query = query(resourceId, null, null, null, null);

      ResourceTableMetadataView metadata = repository.getResourceTableMetadata(query);

      assertThat(metadata.columns())
          .extracting(ResourceColumnInfo::id)
          .doesNotContain("metadata:wsi_serving");
      assertThat(metadata.facets()).doesNotContainKey("metadata:wsi_serving");
      assertThat(metadata.facetRanges()).doesNotContainKey("metadata:wsi_serving");
      assertThat(mapper.getResourceTableMetadataFacetValues(query, "wsi_serving")).isEmpty();
    }
    assertThat(
            repository
                .getResourceTableMetadata(query(WSI, null, null, null, null))
                .facets()
                .get("metadata:stain_name"))
        .hasSize(2);
  }

  // ---- Row responses ----

  @Test
  public void rowsNeverContainServingMetadata() {
    for (String resourceId : List.of(WSI, NOTES)) {
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
        repository.getResourceTableRows(query(WSI, "Periodic acid", null, null, null)).get(0);
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
        .map(ResourceTableRow::resourceDataId)
        .toList();
  }
}
