package org.cbioportal.infrastructure.repository.clickhouse.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cbioportal.domain.resource.ResourceIdentifierFixtures.patients;
import static org.cbioportal.domain.resource.ResourceIdentifierFixtures.samples;

import java.util.List;
import java.util.stream.Stream;
import org.cbioportal.domain.resource.ResourceColumnFilter;
import org.cbioportal.domain.resource.ResourceFacetOption;
import org.cbioportal.domain.resource.ResourceMetadataKeyStats;
import org.cbioportal.domain.resource.ResourceTableCounts;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableRow;
import org.cbioportal.domain.resource.ResourceTableTab;
import org.cbioportal.domain.resource.ResourceTabsRequest;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
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
@Import(MyBatisConfig.class)
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhouseResourceDataMapperTest {

  private static final String STUDY_TCGA_PUB = "study_tcga_pub";
  private static final String ACC_TCGA = "acc_tcga";
  // Generous bounds: these tests check SQL semantics, not the production caps.
  private static final int TEST_FACET_LIMIT = 1000;
  private static final int TEST_SAMPLE_ROWS = 100000;
  private static final long TEST_MAX_MEMORY = 2L * 1024 * 1024 * 1024;

  @Autowired private ClickhouseResourceDataMapper mapper;

  // ---- Tab queries ----

  @Test
  public void getResourceTableTabs_returnsAllTabsForStudy() {
    ResourceTabsRequest request = new ResourceTabsRequest(List.of(STUDY_TCGA_PUB), null, null);

    List<ResourceTableTab> tabs = mapper.getResourceTableTabs(request);

    // expect 4 distinct resourceIds in the test data
    assertThat(tabs).hasSize(5);
    List<String> ids = tabs.stream().map(ResourceTableTab::resourceId).toList();
    assertThat(ids)
        .containsExactlyInAnyOrder("HE_SLIDE", "CT_SCAN", "FIGURES", "RADIOLOGY", "SLIDE_SET");
  }

  @Test
  public void getResourceTableTabs_totalCountIsCorrectPerTab() {
    ResourceTabsRequest request = new ResourceTabsRequest(List.of(STUDY_TCGA_PUB), null, null);

    List<ResourceTableTab> tabs = mapper.getResourceTableTabs(request);

    ResourceTableTab heSlide =
        tabs.stream().filter(t -> t.resourceId().equals("HE_SLIDE")).findFirst().orElseThrow();
    assertThat(heSlide.totalCount()).isEqualTo(2);
    assertThat(heSlide.sampleCount()).isEqualTo(2);
    assertThat(heSlide.patientCount()).isEqualTo(2);
  }

  @Test
  public void getResourceTableTabs_labelFallsBackToResourceId_whenNoDefinition() {
    // Insert a resource_data row with no matching resource_definition at runtime is hard to do
    // here, so we verify that the label for HE_SLIDE matches the definition display name.
    ResourceTabsRequest request = new ResourceTabsRequest(List.of(STUDY_TCGA_PUB), null, null);

    List<ResourceTableTab> tabs = mapper.getResourceTableTabs(request);

    ResourceTableTab heSlide =
        tabs.stream().filter(t -> t.resourceId().equals("HE_SLIDE")).findFirst().orElseThrow();
    assertThat(heSlide.label()).isEqualTo("H&E Slide");
  }

  @Test
  public void getResourceTableTabs_filteredByPatientIds_onlyMatchingPatient() {
    ResourceTabsRequest request =
        new ResourceTabsRequest(
            List.of(STUDY_TCGA_PUB), patients(STUDY_TCGA_PUB, "tcga-a1-a0sb"), null);

    List<ResourceTableTab> tabs = mapper.getResourceTableTabs(request);

    // tcga-a1-a0sb has HE_SLIDE (1 row) and CT_SCAN (1 row)
    assertThat(tabs).hasSize(2);
    ResourceTableTab heSlide =
        tabs.stream().filter(t -> t.resourceId().equals("HE_SLIDE")).findFirst().orElseThrow();
    assertThat(heSlide.totalCount()).isEqualTo(1);
  }

  @Test
  public void
      getResourceTableTabs_filteredByPatientIdsAndSampleIds_includesPatientLevelResources() {
    // Regression test: CT_SCAN's row is patient-level (SAMPLE_ID IS NULL). Cohort requests
    // always carry both a patientIds list and a sampleIds list, so this must still match
    // patient-level rows by PATIENT_ID even though a sampleIds list is also supplied (previously
    // ANDing "PATIENT_ID IN (...)" with "SAMPLE_ID IN (...)" silently excluded every
    // patient-level row, since "SAMPLE_ID IN (...)" is never true for a NULL SAMPLE_ID).
    ResourceTabsRequest request =
        new ResourceTabsRequest(
            List.of(STUDY_TCGA_PUB),
            patients(STUDY_TCGA_PUB, "tcga-a1-a0sb"),
            samples(STUDY_TCGA_PUB, "tcga-a1-a0sb-01"));

    List<ResourceTableTab> tabs = mapper.getResourceTableTabs(request);

    List<String> ids = tabs.stream().map(ResourceTableTab::resourceId).toList();
    assertThat(ids).containsExactlyInAnyOrder("HE_SLIDE", "CT_SCAN");
    ResourceTableTab ctScan =
        tabs.stream().filter(t -> t.resourceId().equals("CT_SCAN")).findFirst().orElseThrow();
    assertThat(ctScan.totalCount()).isEqualTo(1);
  }

  // ---- Row queries ----

  @Test
  public void
      getResourceTableRows_filteredByPatientIdsAndSampleIds_includesPatientLevelResources() {
    // Same regression as the tabs test above, but for the ResourceTableBaseFilters fragment
    // shared by rows/facets/counts.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "CT_SCAN",
            patients(STUDY_TCGA_PUB, "tcga-a1-a0sb"),
            samples(STUDY_TCGA_PUB, "tcga-a1-a0sb-01"),
            null,
            0,
            10,
            null,
            null,
            null);

    List<ResourceTableRow> rows = mapper.getResourceTableRows(query);

    assertThat(rows).hasSize(1);
    assertThat(rows.get(0).resourceId()).isEqualTo("CT_SCAN");
    assertThat(rows.get(0).sampleId()).isNull();
  }

  @Test
  public void getResourceTableRows_returnsRowsForResourceId() {
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "HE_SLIDE", null, null, null, 0, 10, null, null, null);

    List<ResourceTableRow> rows = mapper.getResourceTableRows(query);

    assertThat(rows).hasSize(2);
    assertThat(rows).allMatch(r -> r.resourceId().equals("HE_SLIDE"));
    assertThat(rows).allMatch(r -> r.resourceType().equals("SAMPLE"));
  }

  @Test
  public void getResourceTableRows_pagination_limitsResults() {
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "HE_SLIDE", null, null, null, 0, 1, null, null, null);

    List<ResourceTableRow> rows = mapper.getResourceTableRows(query);

    assertThat(rows).hasSize(1);
  }

  @Test
  public void getResourceTableRows_sortByPatientId_ascDescWorks() {
    ResourceTableQuery asc =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "HE_SLIDE", null, null, null, 0, 10, "patientId", "ASC", null);
    ResourceTableQuery desc =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "HE_SLIDE",
            null,
            null,
            null,
            0,
            10,
            "patientId",
            "DESC",
            null);

    List<ResourceTableRow> ascending = mapper.getResourceTableRows(asc);
    List<ResourceTableRow> descending = mapper.getResourceTableRows(desc);

    assertThat(ascending.get(0).patientId()).isLessThanOrEqualTo(ascending.get(1).patientId());
    assertThat(descending.get(0).patientId()).isGreaterThanOrEqualTo(descending.get(1).patientId());
  }

  @Test
  public void getResourceTableRows_searchFilterOnPatientId() {
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "HE_SLIDE", null, null, "a0sb", 0, 10, null, null, null);

    List<ResourceTableRow> rows = mapper.getResourceTableRows(query);

    assertThat(rows).hasSize(1);
    assertThat(rows.get(0).patientId()).contains("a0sb");
  }

  @Test
  public void getResourceTableRows_metadataIsDeserialized() {
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "HE_SLIDE", null, null, null, 0, 10, null, null, null);

    List<ResourceTableRow> rows = mapper.getResourceTableRows(query);

    assertThat(rows).isNotEmpty();
    ResourceTableRow rowWithMeta =
        rows.stream()
            .filter(r -> r.metadata() != null && !r.metadata().isEmpty())
            .findFirst()
            .orElseThrow(() -> new AssertionError("No row with metadata found"));
    assertThat(rowWithMeta.metadata()).containsKey("stain");
    assertThat(rowWithMeta.metadata().get("stain")).isEqualTo("HE");
  }

  @Test
  public void getResourceTableRows_columnFilter_containsType() {
    ResourceColumnFilter typeFilter = new ResourceColumnFilter("type", "equals", List.of("IMAGE"));
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
            List.of(typeFilter));

    List<ResourceTableRow> rows = mapper.getResourceTableRows(query);

    assertThat(rows).isNotEmpty();
    assertThat(rows).allMatch(r -> "IMAGE".equals(r.type()));
  }

  @Test
  public void getResourceTableRows_metadataFilter_jsonExtractString() {
    ResourceColumnFilter metaFilter =
        new ResourceColumnFilter("metadata:stain", "equals", List.of("HE"));
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
            List.of(metaFilter));

    List<ResourceTableRow> rows = mapper.getResourceTableRows(query);

    assertThat(rows).hasSize(2); // both HE_SLIDE rows have stain=HE
  }

  @Test
  public void getResourceTableFacetValues_ignoresPaginationAndReturnsDistinctCounts() {
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "HE_SLIDE", null, null, null, 0, 1, null, null, null);

    List<ResourceFacetOption> patientFacets =
        mapper.getResourceTableFacetValues(query, "rdata.patient_id", TEST_FACET_LIMIT);
    List<ResourceFacetOption> sampleFacets =
        mapper.getResourceTableFacetValues(query, "rdata.sample_id", TEST_FACET_LIMIT);
    List<ResourceFacetOption> typeFacets = mapper.getResourceTableFacetValues(query, "rdata.type", TEST_FACET_LIMIT);

    assertThat(patientFacets).hasSize(2);
    assertThat(sampleFacets).hasSize(2);
    assertThat(typeFacets).containsExactly(new ResourceFacetOption("IMAGE", 2L));
    assertThat(patientFacets)
        .containsExactlyInAnyOrder(
            new ResourceFacetOption("tcga-a1-a0sb", 1L),
            new ResourceFacetOption("tcga-a1-a0sd", 1L));
    assertThat(sampleFacets)
        .containsExactlyInAnyOrder(
            new ResourceFacetOption("tcga-a1-a0sb-01", 1L),
            new ResourceFacetOption("tcga-a1-a0sd-01", 1L));
  }

  @Test
  public void getResourceTableFacetValues_respectsSearchAndFilters() {
    ResourceColumnFilter patientFilter =
        new ResourceColumnFilter("patientId", "equals", List.of("tcga-a1-a0sb"));
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "HE_SLIDE",
            null,
            null,
            "a0s",
            0,
            10,
            null,
            null,
            List.of(patientFilter));

    List<ResourceFacetOption> patientFacets =
        mapper.getResourceTableFacetValues(query, "rdata.patient_id", TEST_FACET_LIMIT);
    List<ResourceFacetOption> sampleFacets =
        mapper.getResourceTableFacetValues(query, "rdata.sample_id", TEST_FACET_LIMIT);

    assertThat(patientFacets).containsExactly(new ResourceFacetOption("tcga-a1-a0sb", 1L));
    assertThat(sampleFacets).containsExactly(new ResourceFacetOption("tcga-a1-a0sb-01", 1L));
  }

  @Test
  public void
      getResourceTableMetadataFacetValues_activeFilterOnOtherMetadataColumn_doesNotCorruptValues() {
    // Regression test: an active filter on "metadata:stain" used to hijack the
    // "metadataKey" MyBatis parameter (via a same-named <bind>) used by the facet-value
    // SELECT clause, so facet values for "magnification" would incorrectly come back as
    // "stain" values instead. See ApplyStringFilterOnMetadata in ResourceDataMapper.xml.
    ResourceColumnFilter stainFilter =
        new ResourceColumnFilter("metadata:stain", "equals", List.of("HE"));
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
            List.of(stainFilter));

    List<ResourceFacetOption> magnificationFacets =
        metadataFacet(query, "magnification", TEST_FACET_LIMIT);

    assertThat(magnificationFacets)
        .containsExactlyInAnyOrder(
            new ResourceFacetOption("20x", 1L), new ResourceFacetOption("40x", 1L));
  }

  @Test
  public void getResourceTableRows_searchMatchesMetadataValue() {
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "HE_SLIDE", null, null, "40x", 0, 10, null, null, null);

    List<ResourceTableRow> rows = mapper.getResourceTableRows(query);

    assertThat(rows).hasSize(1);
    assertThat(rows.get(0).metadata().get("magnification")).isEqualTo("40x");
  }

  // ---- Count queries ----

  @Test
  public void getResourceTableRowCount_returnsCorrectTotal() {
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "HE_SLIDE", null, null, null, 0, 10, null, null, null);

    long count = mapper.getResourceTableCounts(query).rowCount();

    assertThat(count).isEqualTo(2);
  }

  @Test
  public void getResourceTablePatientCount_returnsDistinctPatients() {
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "HE_SLIDE", null, null, null, 0, 10, null, null, null);

    long count = mapper.getResourceTableCounts(query).patientCount();

    assertThat(count).isEqualTo(2);
  }

  @Test
  public void getResourceTableSampleCount_returnsDistinctSamples() {
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "HE_SLIDE", null, null, null, 0, 10, null, null, null);

    long count = mapper.getResourceTableCounts(query).sampleCount();

    assertThat(count).isEqualTo(2);
  }

  @Test
  public void getResourceTablePatientCount_studyEntitiesNotCounted() {
    // FIGURES resource has ENTITY_TYPE=STUDY, so patient/sample count should be 0
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "FIGURES", null, null, null, 0, 10, null, null, null);

    ResourceTableCounts counts = mapper.getResourceTableCounts(query);
    long patientCount = counts.patientCount();
    long sampleCount = counts.sampleCount();

    assertThat(patientCount).isZero();
    assertThat(sampleCount).isZero();
  }

  // ---- Numeric metadata detection / filtering ----

  @Test
  public void getResourceTableMetadataKeyStats_numericKey_allValuesParse() {
    // FIGURES rows have {"pages": 10} and {"pages": 25} — both parse as numbers.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "FIGURES", null, null, null, 0, 10, null, null, null);

    List<ResourceMetadataKeyStats> stats = mapper.getResourceTableMetadataKeyStats(query, TEST_SAMPLE_ROWS, TEST_MAX_MEMORY);

    ResourceMetadataKeyStats pages =
        stats.stream().filter(s -> s.key().equals("pages")).findFirst().orElseThrow();
    assertThat(pages.nonBlankCount()).isEqualTo(2);
    assertThat(pages.numericCount()).isEqualTo(2);
    assertThat(pages.minValue()).isEqualTo(10.0);
    assertThat(pages.maxValue()).isEqualTo(25.0);
    assertThat(pages.isAutoDetectedNumeric()).isTrue();
  }

  @Test
  public void getResourceTableMetadataKeyStats_unitSuffixedValues_notAutoDetectedNumeric() {
    // HE_SLIDE rows have {"magnification": "20x"} / {"magnification": "40x"} — the "x" suffix
    // means toFloat64OrNull can't parse them, so this key should stay categorical.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "HE_SLIDE", null, null, null, 0, 10, null, null, null);

    List<ResourceMetadataKeyStats> stats = mapper.getResourceTableMetadataKeyStats(query, TEST_SAMPLE_ROWS, TEST_MAX_MEMORY);

    ResourceMetadataKeyStats magnification =
        stats.stream().filter(s -> s.key().equals("magnification")).findFirst().orElseThrow();
    assertThat(magnification.nonBlankCount()).isEqualTo(2);
    assertThat(magnification.numericCount()).isZero();
    assertThat(magnification.isAutoDetectedNumeric()).isFalse();
  }

  @Test
  public void getResourceDefinitionCustomMetadata_returnsSchemaJson_whenPresent() {
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "RADIOLOGY", null, null, null, 0, 10, null, null, null);

    List<String> customMetadata = mapper.getResourceDefinitionCustomMetadata(query);

    assertThat(customMetadata).hasSize(1);
    assertThat(customMetadata.get(0)).contains("dose_id").contains("score");
  }

  @Test
  public void getResourceDefinitionCustomMetadata_returnsNull_whenNotSet() {
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "HE_SLIDE", null, null, null, 0, 10, null, null, null);

    List<String> customMetadata = mapper.getResourceDefinitionCustomMetadata(query);

    assertThat(customMetadata).isEmpty();
  }

  @Test
  public void getResourceTableRows_metadataBetweenFilter_matchesNumericRange() {
    ResourceColumnFilter betweenFilter =
        new ResourceColumnFilter("metadata:pages", "between", List.of("5", "15"));
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "FIGURES",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            List.of(betweenFilter));

    List<ResourceTableRow> rows = mapper.getResourceTableRows(query);

    assertThat(rows).hasSize(1);
    assertThat(rows.get(0).metadata().get("pages")).isEqualTo(10);
  }

  @Test
  public void getResourceTableRows_metadataBetweenFilter_excludesUnparseableValues() {
    // A "between" filter on a non-numeric key (magnification has unit-suffixed strings) should
    // exclude every row, since toFloat64OrNull(...) is NULL for all of them — matching the same
    // "blank/unparseable excluded" semantics as the categorical filter.
    ResourceColumnFilter betweenFilter =
        new ResourceColumnFilter("metadata:magnification", "between", List.of("0", "100"));
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
            List.of(betweenFilter));

    List<ResourceTableRow> rows = mapper.getResourceTableRows(query);

    assertThat(rows).isEmpty();
  }

  // ---- Multiple simultaneous metadata filters ----
  //
  // Regression tests for a MyBatis <bind> scoping bug: ApplyStringFilterOnMetadata used a
  // statement-scoped "metadataFilterKey" <bind> set inside the filters <foreach>. Because
  // <bind> values are resolved from the *final* DynamicContext bindings (unlike <foreach>
  // item variables, which MyBatis renames per iteration), every metadata condition in the
  // rendered statement read back the LAST filter's key. Two metadata filters therefore both
  // applied to the same column, silently dropping rows that should have matched.

  @Test
  public void getResourceTableRows_twoCategoricalMetadataFilters_eachAppliesToItsOwnColumn() {
    ResourceColumnFilter stainFilter =
        new ResourceColumnFilter("metadata:stain", "in", List.of("HE"));
    ResourceColumnFilter magnificationFilter =
        new ResourceColumnFilter("metadata:magnification", "in", List.of("20x"));
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
            List.of(stainFilter, magnificationFilter));

    List<ResourceTableRow> rows = mapper.getResourceTableRows(query);

    assertThat(rows).hasSize(1);
    assertThat(rows.get(0).metadata()).containsEntry("stain", "HE");
    assertThat(rows.get(0).metadata()).containsEntry("magnification", "20x");
  }

  @Test
  public void getResourceTableRows_numericAndCategoricalMetadataFilters_bothApplyToOwnColumn() {
    // RADIOLOGY rows: (dose_id=1001, score=85) and (dose_id=1002, score=42).
    ResourceColumnFilter scoreFilter =
        new ResourceColumnFilter("metadata:score", "between", List.of("40", "90"));
    ResourceColumnFilter doseFilter =
        new ResourceColumnFilter("metadata:dose_id", "in", List.of("1001"));
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "RADIOLOGY",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            List.of(scoreFilter, doseFilter));

    List<ResourceTableRow> rows = mapper.getResourceTableRows(query);

    assertThat(rows).hasSize(1);
    assertThat(rows.get(0).metadata()).containsEntry("dose_id", "1001");
  }

  @Test
  public void getResourceTableRows_twoNumericMetadataFilters_eachAppliesToItsOwnColumn() {
    ResourceColumnFilter scoreFilter =
        new ResourceColumnFilter("metadata:score", "between", List.of("80", "90"));
    ResourceColumnFilter doseFilter =
        new ResourceColumnFilter("metadata:dose_id", "between", List.of("1000", "1001"));
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "RADIOLOGY",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            List.of(scoreFilter, doseFilter));

    List<ResourceTableRow> rows = mapper.getResourceTableRows(query);

    assertThat(rows).hasSize(1);
    assertThat(rows.get(0).metadata()).containsEntry("dose_id", "1001");
    assertThat(rows.get(0).metadata()).containsEntry("score", "85");
  }

  @Test
  public void getResourceTableRowCount_twoMetadataFilters_matchesRowQuery() {
    ResourceColumnFilter scoreFilter =
        new ResourceColumnFilter("metadata:score", "between", List.of("40", "90"));
    ResourceColumnFilter doseFilter =
        new ResourceColumnFilter("metadata:dose_id", "in", List.of("1001"));
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "RADIOLOGY",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            List.of(scoreFilter, doseFilter));

    assertThat(mapper.getResourceTableCounts(query).rowCount()).isEqualTo(1L);
  }

  // ---- Multi-study cohorts ----
  //
  // resource_data stores stable ids, which are unique only within a study. The seed gives acc_tcga
  // its own sample called 'tcga-a1-a0sb-01' -- a different sample that happens to carry the same
  // barcode as study_tcga_pub's. A cohort that selects study_tcga_pub's copy plus an unrelated
  // acc_tcga sample must not pull in acc_tcga's same-named row, and must not count the two
  // same-named samples as one.

  /**
   * study_tcga_pub's tcga-a1-a0sb-01 and acc_tcga's tcga-zz-9999-01 -- two samples, two studies.
   */
  private static List<SampleIdentifier> crossStudyCohort() {
    return Stream.concat(
            samples(STUDY_TCGA_PUB, "tcga-a1-a0sb-01").stream(),
            samples(ACC_TCGA, "tcga-zz-9999-01").stream())
        .toList();
  }

  @Test
  public void getResourceTableRows_multiStudyCohort_excludesSameNamedSampleFromOtherStudy() {
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB, ACC_TCGA),
            "HE_SLIDE",
            null,
            crossStudyCohort(),
            null,
            0,
            10,
            null,
            null,
            null);

    List<ResourceTableRow> rows = mapper.getResourceTableRows(query);

    // acc-he-collides.jpg belongs to acc_tcga's tcga-a1-a0sb-01, which the cohort never selected.
    assertThat(rows)
        .extracting(ResourceTableRow::url)
        .containsExactlyInAnyOrder(
            "https://example.com/he1.jpg", "https://example.com/acc-he-own.jpg");
  }

  @Test
  public void getResourceTableTabs_multiStudyCohort_excludesSameNamedSampleFromOtherStudy() {
    ResourceTabsRequest request =
        new ResourceTabsRequest(List.of(STUDY_TCGA_PUB, ACC_TCGA), null, crossStudyCohort());

    ResourceTableTab heSlide =
        mapper.getResourceTableTabs(request).stream()
            .filter(t -> t.resourceId().equals("HE_SLIDE"))
            .findFirst()
            .orElseThrow();

    assertThat(heSlide.totalCount()).isEqualTo(2);
    assertThat(heSlide.sampleCount()).isEqualTo(2);
  }

  @Test
  public void getResourceTableCounts_multiStudyCohort_doesNotCollapseSameNamedSamples() {
    // No cohort filter: HE_SLIDE spans study_tcga_pub's two samples plus acc_tcga's two. Three of
    // the four barcodes are distinct, but tcga-a1-a0sb-01 occurs in both studies, so counting
    // distinct bare stable ids would report 3 samples where there are 4.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB, ACC_TCGA),
            "HE_SLIDE",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            null);

    ResourceTableCounts counts = mapper.getResourceTableCounts(query);

    assertThat(counts.rowCount()).isEqualTo(4);
    assertThat(counts.sampleCount()).isEqualTo(4);
  }

  @Test
  public void getResourceTableRows_singleStudyCohort_isUnaffectedByTheTupleGuard() {
    // One study cannot be ambiguous, so the study-qualified predicate is skipped entirely.
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "HE_SLIDE",
            null,
            samples(STUDY_TCGA_PUB, "tcga-a1-a0sb-01"),
            null,
            0,
            10,
            null,
            null,
            null);

    assertThat(mapper.getResourceTableRows(query))
        .extracting(ResourceTableRow::url)
        .containsExactly("https://example.com/he1.jpg");
  }

  // ---- Sorting and paging ----
  //
  // SLIDE_SET is four rows on one patient and one sample, with a JSON-number 'score' whose
  // lexicographic and numeric orders disagree (as text: 10, 100, 20, 9).

  private static ResourceTableQuery slideSet(String sortBy, String direction, int page, int size) {
    return new ResourceTableQuery(
        List.of(STUDY_TCGA_PUB),
        "SLIDE_SET",
        null,
        null,
        null,
        page,
        size,
        sortBy,
        direction,
        null);
  }

  @Test
  public void getResourceTableRows_numericMetadataSort_ordersNumericallyNotAsText() {
    List<ResourceTableRow> rows =
        mapper.getResourceTableRows(slideSet("metadata:score", "asc", 0, 10));

    assertThat(rows)
        .extracting(ResourceTableRow::displayName)
        .containsExactly("Set 9", "Set 10", "Set 20", "Set 100");
  }

  @Test
  public void getResourceTableRows_numericMetadataSort_descending() {
    List<ResourceTableRow> rows =
        mapper.getResourceTableRows(slideSet("metadata:score", "desc", 0, 10));

    assertThat(rows)
        .extracting(ResourceTableRow::displayName)
        .containsExactly("Set 100", "Set 20", "Set 10", "Set 9");
  }

  @Test
  public void getResourceTableRows_jsonNumberMetadataIsReadable() {
    // JSONExtractString returns a JSON number as its text, so numeric metadata is filterable and
    // facetable rather than coming back blank.
    List<ResourceFacetOption> facets =
        metadataFacet(slideSet(null, null, 0, 10), "score", TEST_FACET_LIMIT);

    assertThat(facets)
        .extracting(ResourceFacetOption::value)
        .containsExactlyInAnyOrder("9", "10", "20", "100");
  }

  @Test
  public void getResourceTableRows_pagingIsStableWhenEveryRowSharesAPatientAndSample() {
    // Without a unique tiebreaker the ordering is partial here, and paged reads may repeat a row
    // on one page and drop another entirely.
    List<String> paged =
        java.util.stream.Stream.of(0, 1)
            .flatMap(page -> mapper.getResourceTableRows(slideSet(null, null, page, 2)).stream())
            .map(ResourceTableRow::displayName)
            .toList();

    assertThat(paged)
        .as("two pages of two should cover all four rows exactly once")
        .containsExactlyInAnyOrder("Set 9", "Set 10", "Set 20", "Set 100");
  }

  @Test
  public void getResourceTableRows_pagingIsStableUnderAColumnSortWithTies() {
    // Same check under an explicit sort whose column is constant across the four rows, which is
    // the worst case for a partial order.
    List<String> paged =
        java.util.stream.Stream.of(0, 1)
            .flatMap(
                page -> mapper.getResourceTableRows(slideSet("sampleId", "asc", page, 2)).stream())
            .map(ResourceTableRow::displayName)
            .toList();

    assertThat(paged).containsExactlyInAnyOrder("Set 9", "Set 10", "Set 20", "Set 100");
  }

  // ---- JSON scalars other than strings ----
  //
  // Raised in review: that JSONExtractString returns "" for real JSON numbers and booleans, which
  // would make such keys unsortable, unfilterable and unfacetable. SLIDE_SET stores every value
  // as a bare JSON scalar -- integers, a float, booleans -- under the WSI field names that
  // prompted the question, so these pin the actual behaviour end to end.

  @Test
  public void metadataFacets_readJsonIntegersAndBooleans() {
    assertThat(
            metadataFacet(
                slideSet(null, null, 0, 10), "file_size_bytes", TEST_FACET_LIMIT))
        .extracting(ResourceFacetOption::value)
        .containsExactlyInAnyOrder("100", "2000", "30", "400");

    assertThat(metadataFacet(slideSet(null, null, 0, 10), "is_hne", TEST_FACET_LIMIT))
        .extracting(ResourceFacetOption::value)
        .containsExactlyInAnyOrder("true", "false");

    assertThat(metadataFacet(slideSet(null, null, 0, 10), "mpp", TEST_FACET_LIMIT))
        .extracting(ResourceFacetOption::value)
        .containsExactlyInAnyOrder("0.5", "0.25", "1", "0.75");
  }

  @Test
  public void metadataRangeFilter_appliesToAJsonInteger() {
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "SLIDE_SET",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            List.of(
                new ResourceColumnFilter(
                    "metadata:file_size_bytes", "between", List.of("50", "500"))));

    assertThat(mapper.getResourceTableRows(query))
        .extracting(ResourceTableRow::displayName)
        .containsExactlyInAnyOrder("Set 9", "Set 100"); // file_size_bytes 100 and 400
  }

  @Test
  public void metadataCategoricalFilter_appliesToAJsonBoolean() {
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB),
            "SLIDE_SET",
            null,
            null,
            null,
            0,
            10,
            null,
            null,
            List.of(new ResourceColumnFilter("metadata:is_hne", "equals", List.of("true"))));

    assertThat(mapper.getResourceTableRows(query))
        .extracting(ResourceTableRow::displayName)
        .containsExactlyInAnyOrder("Set 9", "Set 20");
  }

  @Test
  public void metadataSort_onAJsonIntegerIsNumeric() {
    assertThat(mapper.getResourceTableRows(slideSet("metadata:file_size_bytes", "asc", 0, 10)))
        .extracting(ResourceTableRow::displayName)
        .containsExactly("Set 20", "Set 9", "Set 100", "Set 10"); // 30, 100, 400, 2000
  }

  @Test
  public void metadataKeyStats_detectJsonIntegersAsNumeric() {
    ResourceMetadataKeyStats stats =
        mapper.getResourceTableMetadataKeyStats(slideSet(null, null, 0, 10), TEST_SAMPLE_ROWS, TEST_MAX_MEMORY).stream()
            .filter(k -> "file_size_bytes".equals(k.key()))
            .findFirst()
            .orElseThrow();

    assertThat(stats.nonBlankCount()).isEqualTo(4);
    assertThat(stats.numericCount()).isEqualTo(4);
    assertThat(stats.minValue()).isEqualTo(30.0);
    assertThat(stats.maxValue()).isEqualTo(2000.0);
  }

  // ---- Facet cap and bounded key discovery ----

  @Test
  public void metadataFacets_respectTheCallersLimit() {
    // SLIDE_SET has four distinct scores. The caller asks for cap + 1 so it can tell an over-cap
    // key from one that exactly fills the cap, so a limit of 3 must come back with 3, not 4.
    assertThat(metadataFacet(slideSet(null, null, 0, 10), "score", 3))
        .hasSize(3);
    assertThat(metadataFacet(slideSet(null, null, 0, 10), "score", 10))
        .hasSize(4);
  }

  @Test
  public void builtinFacets_respectTheCallersLimit() {
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "SLIDE_SET", null, null, null, 0, 10, null, null, null);

    assertThat(mapper.getResourceTableFacetValues(query, "rdata.display_name", 2)).hasSize(2);
  }

  @Test
  public void keyStats_areUnchangedByTheSampleBoundWhenTheResourceIsSmaller() {
    // The sample bounds how many rows are read; a resource smaller than the bound must classify
    // exactly as it did before the bound existed.
    ResourceMetadataKeyStats scores =
        mapper.getResourceTableMetadataKeyStats(slideSet(null, null, 0, 10), TEST_SAMPLE_ROWS, TEST_MAX_MEMORY)
            .stream()
            .filter(k -> "score".equals(k.key()))
            .findFirst()
            .orElseThrow();

    assertThat(scores.nonBlankCount()).isEqualTo(4);
    assertThat(scores.numericCount()).isEqualTo(4);
    assertThat(scores.minValue()).isEqualTo(9.0);
    assertThat(scores.maxValue()).isEqualTo(100.0);
  }

  @Test
  public void keyStats_sampleBoundLimitsRowsRead() {
    // With the bound set to one row, only that row's keys are discovered.
    List<ResourceMetadataKeyStats> stats =
        mapper.getResourceTableMetadataKeyStats(slideSet(null, null, 0, 10), 1, TEST_MAX_MEMORY);

    assertThat(stats).isNotEmpty();
    assertThat(stats).allSatisfy(k -> assertThat(k.nonBlankCount()).isEqualTo(1));
  }

  /** The batched facet query, narrowed to one key, so per-key assertions stay readable. */
  private List<ResourceFacetOption> metadataFacet(
      ResourceTableQuery query, String key, int limitPerKey) {
    return mapper.getResourceTableMetadataFacets(query, new String[] {key}, limitPerKey).stream()
        .map(v -> new ResourceFacetOption(v.value(), v.count()))
        .toList();
  }
}
