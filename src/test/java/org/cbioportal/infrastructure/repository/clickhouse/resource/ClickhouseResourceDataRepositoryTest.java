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
import org.mockito.Mockito;
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
  private static final String STUDY_ACC = "acc_tcga";

  @Autowired private ClickhouseResourceDataRepository repository;
  @Autowired private ClickhouseResourceDataMapper mapper;

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
  public void getResourceTableMetadataColumns_dropsKeysTheContractDoesNotDeclare() {
    // "aperture" is present in the data but absent from RADIOLOGY's contract. A resource that
    // declares a contract gets exactly the columns it declares, so the key discovery sample
    // cannot decide whether a column appears.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "RADIOLOGY", null, null, null, 0, 10, null, null, null);

    assertThat(repository.getResourceTableMetadata(query).columns())
        .extracting(ResourceColumnInfo::id)
        .doesNotContain("metadata:aperture");
  }

  @Test
  public void getResourceTableMetadataColumns_followsContractOrder() {
    // The columns and their order both come from the contract: score, dose_id, operator, series.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "RADIOLOGY", null, null, null, 0, 10, null, null, null);

    List<String> ids =
        repository.getResourceTableMetadata(query).columns().stream()
            .map(ResourceColumnInfo::id)
            .toList();

    assertThat(ids)
        .containsExactly(
            "metadata:score", "metadata:dose_id", "metadata:operator", "metadata:series");
  }

  @Test
  public void getResourceTableMetadata_skipsKeyDiscoveryWhenTheContractTypesEveryField() {
    // RADIOLOGY's contract names every column and gives each a type, so nothing is left for the
    // discovery scan to answer and it must not run.
    ClickhouseResourceDataMapper spy = Mockito.spy(mapper);
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "RADIOLOGY", null, null, null, 0, 10, null, null, null);

    new ClickhouseResourceDataRepository(spy).getResourceTableMetadata(query);

    Mockito.verify(spy, Mockito.never())
        .getResourceTableMetadataKeyStats(Mockito.any(), Mockito.anyInt(), Mockito.anyLong());
  }

  @Test
  public void getResourceTableMetadata_runsKeyDiscoveryWhenThereIsNoContract() {
    // HE_SLIDE has no contract, so the data is the only source of columns and the scan has to run.
    ClickhouseResourceDataMapper spy = Mockito.spy(mapper);
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "HE_SLIDE", null, null, null, 0, 10, null, null, null);

    new ClickhouseResourceDataRepository(spy).getResourceTableMetadata(query);

    Mockito.verify(spy)
        .getResourceTableMetadataKeyStats(Mockito.any(), Mockito.anyInt(), Mockito.anyLong());
  }

  @Test
  public void divergingContracts_showTheUnionOfTheirFieldsInStudyOrder() {
    // PATHOLOGY is declared by both studies: acc_tcga declares grade/reviewer, study_tcga_pub
    // declares stain/grade. Taking either alone would hide the other's declared keys. Contracts
    // are ordered by study identifier, and acc_tcga sorts first.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB, STUDY_ACC),
            "PATHOLOGY",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null);

    assertThat(repository.getResourceTableMetadata(query).columns())
        .extracting(ResourceColumnInfo::id)
        .containsExactly("metadata:grade", "metadata:reviewer", "metadata:stain");
  }

  @Test
  public void divergingContracts_takeEachKeyFromItsFirstDeclaration() {
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB, STUDY_ACC),
            "PATHOLOGY",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null);

    assertThat(columnById(query, "metadata:grade").label()).isEqualTo("Grade (acc)");
    assertThat(columnById(query, "metadata:stain").label()).isEqualTo("Stain (tcga)");
  }

  @Test
  public void divergingContracts_leaveAConflictingTypeToTheData() {
    // study_tcga_pub types grade as number, acc_tcga as string, and the data holds "3" and
    // "high". A declared number would put a range filter on text.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB, STUDY_ACC),
            "PATHOLOGY",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null);

    assertThat(columnById(query, "metadata:grade").dataType()).isEqualTo("string");
  }

  @Test
  public void aContractIsAuthoritativeOnlyWhereEveryStudyInScopeDeclaresOne() {
    // CYTOLOGY has rows in both studies but only acc_tcga declares a contract. study_tcga_pub's
    // rows were never checked against it, so restricting the columns to it would hide "fixative".
    ResourceTableQuery bothStudies =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB, STUDY_ACC),
            "CYTOLOGY",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null);

    assertThat(repository.getResourceTableMetadata(bothStudies).columns())
        .extracting(ResourceColumnInfo::id)
        .containsExactly("metadata:preparation", "metadata:fixative");
  }

  @Test
  public void thatSameContractIsAuthoritativeForItsOwnStudyAlone() {
    ResourceTableQuery declaringStudyOnly =
        new ResourceTableQuery(
            List.of(STUDY_ACC), "CYTOLOGY", null, null, null, 0, 10, null, null, null);

    assertThat(repository.getResourceTableMetadata(declaringStudyOnly).columns())
        .extracting(ResourceColumnInfo::id)
        .containsExactly("metadata:preparation");
  }

  @Test
  public void keyDiscoveryStillRunsWhenOnlySomeStudiesDeclareAContract() {
    // The undeclared study's keys are only knowable from the data, so the scan cannot be skipped
    // however completely the one contract types itself.
    ClickhouseResourceDataMapper spy = Mockito.spy(mapper);
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB, STUDY_ACC),
            "CYTOLOGY",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null);

    new ClickhouseResourceDataRepository(spy).getResourceTableMetadata(query);

    Mockito.verify(spy)
        .getResourceTableMetadataKeyStats(Mockito.any(), Mockito.anyInt(), Mockito.anyLong());
  }

  @Test
  public void getResourceTableMetadataColumns_keepsDeclaredKeysNoRowCarries() {
    // "series" is declared but absent from every row. The column still renders, so a resource's
    // column set stays stable no matter which rows happen to be populated.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "RADIOLOGY", null, null, null, 0, 10, null, null, null);

    ResourceColumnInfo series = columnById(query, "metadata:series");

    assertThat(series.label()).isEqualTo("Series");
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
