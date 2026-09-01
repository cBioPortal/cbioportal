package org.cbioportal.infrastructure.repository.clickhouse.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.cbioportal.domain.resource.ResourceColumnFilter;
import org.cbioportal.domain.resource.ResourceColumnInfo;
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

    Map<String, List<ResourceFacetOption>> facets =
        repository.getResourceTableMetadata(query).facets();

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

    Map<String, ResourceNumericRange> ranges =
        repository.getResourceTableMetadata(query).facetRanges();

    assertThat(ranges).containsEntry("metadata:pages", new ResourceNumericRange(10.0, 25.0));
  }

  @Test
  public void getResourceTableFacets_autoDetectedNumericColumn_excludedFromCategoricalFacets() {
    // A numeric column shouldn't also show up as an enumerated categorical facet.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "FIGURES", null, null, null, 0, 10, null, null, null);

    Map<String, List<ResourceFacetOption>> facets =
        repository.getResourceTableMetadata(query).facets();

    assertThat(facets).doesNotContainKey("metadata:pages");
  }

  @Test
  public void getResourceTableFacetRanges_unitSuffixedColumn_notTreatedAsNumeric() {
    // HE_SLIDE's "magnification" values ("20x"/"40x") don't parse as pure numbers, so it must
    // stay categorical (no facetRange) even though it looks numeric-ish.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "HE_SLIDE", null, null, null, 0, 10, null, null, null);

    Map<String, ResourceNumericRange> ranges =
        repository.getResourceTableMetadata(query).facetRanges();

    assertThat(ranges).doesNotContainKey("metadata:magnification");
  }

  @Test
  public void getResourceTableFacetRanges_schemaOverridesStringForcesCategorical() {
    // RADIOLOGY's custom_metadata schema declares "dose_id" as "string" even though its values
    // ("1001", "1002") look numeric — the explicit override must win over auto-detection.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "RADIOLOGY", null, null, null, 0, 10, null, null, null);

    Map<String, ResourceNumericRange> ranges =
        repository.getResourceTableMetadata(query).facetRanges();
    Map<String, List<ResourceFacetOption>> facets =
        repository.getResourceTableMetadata(query).facets();

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

    Map<String, ResourceNumericRange> ranges =
        repository.getResourceTableMetadata(query).facetRanges();

    assertThat(ranges).containsEntry("metadata:score", new ResourceNumericRange(42.0, 85.0));
  }

  // ---- custom_metadata contract drives metadata column presentation ----

  @Test
  public void getResourceTableMetadataColumns_usesContractLabelAndDescription() {
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "RADIOLOGY", null, null, null, 0, 10, null, null, null);

    ResourceColumnInfo score = columnById(query, "metadata:score");

    assertThat(score.label()).isEqualTo("Dose Score");
    assertThat(score.description()).isEqualTo("Radiation dose score");
    assertThat(score.source()).isEqualTo(ResourceColumnInfo.SOURCE_METADATA);
    assertThat(score.dataType()).isEqualTo("number");
  }

  @Test
  public void getResourceTableMetadataColumns_fallsBackToRawKeyWhenUndeclared() {
    // "aperture" is present in the data but absent from RADIOLOGY's contract.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "RADIOLOGY", null, null, null, 0, 10, null, null, null);

    ResourceColumnInfo aperture = columnById(query, "metadata:aperture");

    assertThat(aperture.label()).isEqualTo("aperture");
    assertThat(aperture.description()).isNull();
    assertThat(aperture.filterable()).isTrue();
  }

  @Test
  public void getResourceTableMetadataColumns_ordersDeclaredFieldsFirstThenDiscovered() {
    // Contract order is score, dose_id, operator; "aperture" is undeclared and sorts after them.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "RADIOLOGY", null, null, null, 0, 10, null, null, null);

    List<String> ids =
        repository.getResourceTableMetadata(query).columns().stream()
            .map(ResourceColumnInfo::id)
            .toList();

    assertThat(ids)
        .containsExactly(
            "metadata:score", "metadata:dose_id", "metadata:operator", "metadata:aperture");
  }

  @Test
  public void getResourceTableMetadataColumns_sortsAlphabeticallyWhenNoContract() {
    // HE_SLIDE has no custom_metadata at all, so the pre-contract behavior must be preserved.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "HE_SLIDE", null, null, null, 0, 10, null, null, null);

    List<ResourceColumnInfo> columns = repository.getResourceTableMetadata(query).columns();

    assertThat(columns.stream().map(ResourceColumnInfo::id).toList())
        .containsExactly("metadata:magnification", "metadata:stain");
    assertThat(columns).allMatch(ResourceColumnInfo::filterable);
    assertThat(columns.get(0).label()).isEqualTo("magnification");
  }

  @Test
  public void metadataColumns_hiddenUnlessTheContractOptsThemIn() {
    // RADIOLOGY's contract marks only "score" visibleByDefault; everything else, declared or
    // discovered, stays hidden so a resource with many keys cannot bury the builtin columns.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "RADIOLOGY", null, null, null, 0, 10, null, null, null);

    List<ResourceColumnInfo> columns = repository.getResourceTableMetadata(query).columns();

    assertThat(columns)
        .filteredOn(ResourceColumnInfo::visibleByDefault)
        .extracting(ResourceColumnInfo::id)
        .containsExactly("metadata:score");
  }

  @Test
  public void metadataColumns_hiddenByDefaultWhenNoContract() {
    // HE_SLIDE has no custom_metadata at all.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "HE_SLIDE", null, null, null, 0, 10, null, null, null);

    assertThat(repository.getResourceTableMetadata(query).columns())
        .noneMatch(ResourceColumnInfo::visibleByDefault);
  }

  @Test
  public void filterableFalse_marksColumnUnfilterableAndSkipsItsFacets() {
    // RADIOLOGY's contract sets "filterable": false on "operator": the column still exists and
    // still renders, it just gets no filter control and costs no facet aggregation.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "RADIOLOGY", null, null, null, 0, 10, null, null, null);

    ResourceColumnInfo operator = columnById(query, "metadata:operator");
    Map<String, List<ResourceFacetOption>> facets =
        repository.getResourceTableMetadata(query).facets();

    assertThat(operator.filterable()).isFalse();
    assertThat(facets).doesNotContainKey("metadata:operator");
    assertThat(facets).containsKey("metadata:dose_id");
  }

  @Test
  public void filterableFalse_alsoSuppressesTheNumericRange() {
    // The gate has to cover facetRanges too, otherwise a non-filterable numeric column would still
    // render a range slider.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "RADIOLOGY", null, null, null, 0, 10, null, null, null);

    Map<String, ResourceNumericRange> ranges =
        repository.getResourceTableMetadata(query).facetRanges();

    assertThat(ranges).containsKey("metadata:score");
    assertThat(ranges).doesNotContainKey("metadata:operator");
  }

  private ResourceColumnInfo columnById(ResourceTableQuery query, String id) {
    return repository.getResourceTableMetadata(query).columns().stream()
        .filter(c -> c.id().equals(id))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no metadata column " + id));
  }
}
