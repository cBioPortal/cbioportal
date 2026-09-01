package org.cbioportal.infrastructure.repository.clickhouse.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.cbioportal.domain.resource.ResourceColumnFilter;
import org.cbioportal.domain.resource.ResourceFacetOption;
import org.cbioportal.domain.resource.ResourceNumericRange;
import org.cbioportal.domain.resource.ResourceTableQuery;
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

@RunWith(SpringRunner.class)
@Import({MyBatisConfig.class, ClickhouseResourceDataRepository.class})
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhouseResourceDataRepositoryTest {

  private static final String STUDY_TCGA_PUB = "study_tcga_pub";

  @Autowired private ClickhouseResourceDataRepository repository;

  @Test
  public void
      getResourceTableFacets_metadataColumnsRemainDiscoverable_whenAnotherFilterZeroesRows() {
    // Regression test: metadata key discovery previously ran against the FULLY filtered
    // query, so deselecting every option in one column's filter (a "1 = 0" filter that
    // zeroes out all matching rows) made ALL dynamic metadata columns disappear from the
    // facets map, not just the affected one.
    ResourceColumnFilter zeroingFilter =
        new ResourceColumnFilter("metadata:stain", "in", List.of());
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "HE_SLIDE",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            List.of(zeroingFilter));

    Map<String, List<ResourceFacetOption>> facets = repository.getResourceTableFacets(query);

    assertThat(facets).containsKey("metadata:magnification");
    assertThat(facets.get("metadata:magnification"))
        .containsExactlyInAnyOrder(
            new ResourceFacetOption("20x", 1L), new ResourceFacetOption("40x", 1L));
  }

  @Test
  public void getResourceTableFacetRanges_autoDetectedNumericColumn_returnsMinMax() {
    // FIGURES rows have {"pages": 10} / {"pages": 25} — a genuinely numeric key with no schema
    // override, so this must be auto-detected as numeric.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "FIGURES", null, null, null, 0, 10, null, null, null);

    Map<String, ResourceNumericRange> ranges = repository.getResourceTableFacetRanges(query);

    assertThat(ranges).containsEntry("metadata:pages", new ResourceNumericRange(10.0, 25.0));
  }

  @Test
  public void getResourceTableFacets_autoDetectedNumericColumn_excludedFromCategoricalFacets() {
    // A numeric column shouldn't also show up as an enumerated categorical facet.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "FIGURES", null, null, null, 0, 10, null, null, null);

    Map<String, List<ResourceFacetOption>> facets = repository.getResourceTableFacets(query);

    assertThat(facets).doesNotContainKey("metadata:pages");
  }

  @Test
  public void getResourceTableFacetRanges_unitSuffixedColumn_notTreatedAsNumeric() {
    // HE_SLIDE's "magnification" values ("20x"/"40x") don't parse as pure numbers, so it must
    // stay categorical (no facetRange) even though it looks numeric-ish.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "HE_SLIDE", null, null, null, 0, 10, null, null, null);

    Map<String, ResourceNumericRange> ranges = repository.getResourceTableFacetRanges(query);

    assertThat(ranges).doesNotContainKey("metadata:magnification");
  }

  @Test
  public void getResourceTableFacetRanges_schemaOverridesStringForcesCategorical() {
    // RADIOLOGY's custom_metadata schema declares "dose_id" as "string" even though its values
    // ("1001", "1002") look numeric — the explicit override must win over auto-detection.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "RADIOLOGY", null, null, null, 0, 10, null, null, null);

    Map<String, ResourceNumericRange> ranges = repository.getResourceTableFacetRanges(query);
    Map<String, List<ResourceFacetOption>> facets = repository.getResourceTableFacets(query);

    assertThat(ranges).doesNotContainKey("metadata:dose_id");
    assertThat(facets).containsKey("metadata:dose_id");
  }

  @Test
  public void getResourceTableFacetRanges_schemaDeclaresNumber_usesDeclaredType() {
    // RADIOLOGY's schema declares "score" as "number"; auto-detection would agree here too, but
    // this confirms the schema path resolves correctly end-to-end.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "RADIOLOGY", null, null, null, 0, 10, null, null, null);

    Map<String, ResourceNumericRange> ranges = repository.getResourceTableFacetRanges(query);

    assertThat(ranges).containsEntry("metadata:score", new ResourceNumericRange(42.0, 85.0));
  }
}
