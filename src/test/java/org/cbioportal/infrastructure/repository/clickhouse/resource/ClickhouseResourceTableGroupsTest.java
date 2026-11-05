package org.cbioportal.infrastructure.repository.clickhouse.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.cbioportal.domain.resource.ResourceColumnFilter;
import org.cbioportal.domain.resource.ResourceFacetOption;
import org.cbioportal.domain.resource.ResourceGroupBy;
import org.cbioportal.domain.resource.ResourceGroupLocator;
import org.cbioportal.domain.resource.ResourceQueryContext;
import org.cbioportal.domain.resource.ResourceScope;
import org.cbioportal.domain.resource.ResourceTableCounts;
import org.cbioportal.domain.resource.ResourceTableGroup;
import org.cbioportal.domain.resource.ResourceTableGroupPage;
import org.cbioportal.domain.resource.ResourceTableMetadataView;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableRow;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.domain.studyview.StudyViewFilterFactory;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.legacy.web.parameter.ClinicalDataFilter;
import org.cbioportal.legacy.web.parameter.DataFilterValue;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Grouped resource-table reads, the study-view cohort, combined resources and the compact metadata
 * table, over the GROUPED_* fixtures in study_genie_pub (see clickhouse_data.sql).
 *
 * <p>Fixture summary, patient grouping over GROUPED_SLIDE and GROUPED_SLIDE_PATIENT: 301 has three
 * sample-level slides (H&E, IHC, IHC) and one patient-level IHC slide; 302 one sample-level and one
 * patient-level H&E; 303 two H&E; 304 one IHC; 305 one H&E; 306 only a patient-level IHC.
 */
@RunWith(SpringRunner.class)
@Import({MyBatisConfig.class, ClickhouseResourceDataRepository.class})
@JdbcTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhouseResourceTableGroupsTest {

  private static final String STUDY = "study_genie_pub";
  private static final String SLIDES = "GROUPED_SLIDE";
  private static final String PATIENT_SLIDES = "GROUPED_SLIDE_PATIENT";
  private static final String NO_CONTRACT = "GROUPED_NO_CONTRACT";
  private static final String STAIN = "metadata:stain_group";

  @Autowired private ClickhouseResourceDataRepository repository;
  @Autowired private ClickhouseResourceDataMapper mapper;
  @Autowired private JdbcTemplate jdbcTemplate;

  /** Builds grouped queries; every field defaults to the common case. */
  private static final class Q {
    List<String> studyIds = List.of(STUDY);
    String resourceId = SLIDES;
    List<String> resourceIds = null;
    String search = null;
    int pageNumber = 0;
    int pageSize = 50;
    String sortBy = null;
    String direction = null;
    List<ResourceColumnFilter> filters = null;
    StudyViewFilter studyViewFilter = null;
    String groupBy = "patient";
    List<String> groupCounts = List.of(STAIN);
    ResourceGroupLocator locate = null;

    ResourceTableQuery build() {
      return new ResourceTableQuery(
          studyIds,
          resourceId,
          null,
          null,
          search,
          pageNumber,
          pageSize,
          sortBy,
          direction,
          filters,
          resourceIds,
          studyViewFilter,
          ResourceGroupBy.parse(groupBy),
          groupCounts,
          locate);
    }
  }

  private static Q q() {
    return new Q();
  }

  private static Q combined() {
    Q q = new Q();
    q.resourceId = null;
    q.resourceIds = List.of(SLIDES, PATIENT_SLIDES);
    return q;
  }

  private ResourceQueryContext context(ResourceTableQuery query, StudyViewFilterContext cohort) {
    return new ResourceQueryContext(cohort, repository.getResourceScope(query, true));
  }

  private ResourceTableGroupPage groups(ResourceTableQuery query) {
    return repository.getResourceTableGroups(query, context(query, null));
  }

  private static List<String> keys(ResourceTableGroupPage page) {
    return page.groups().stream()
        .map(g -> g.sampleId() != null ? g.sampleId() : g.patientId())
        .toList();
  }

  private static ResourceTableGroup group(ResourceTableGroupPage page, String patientId) {
    return page.groups().stream()
        .filter(g -> g.patientId().equals(patientId))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no group " + patientId));
  }

  // ---- grouping ----

  @Test
  public void groupByPatient_returnsOneGroupPerPatientWithRowCountsAndTotals() {
    ResourceTableGroupPage page = groups(q().build());

    assertThat(keys(page))
        .containsExactly(
            "GENIE-TEST-301",
            "GENIE-TEST-302",
            "GENIE-TEST-303",
            "GENIE-TEST-304",
            "GENIE-TEST-305");
    assertThat(page.totalGroups()).isEqualTo(5);
    assertThat(page.totalRows()).isEqualTo(8);
    ResourceTableGroup first = page.groups().getFirst();
    assertThat(first.studyId()).isEqualTo(STUDY);
    assertThat(first.sampleId()).isNull();
    assertThat(first.rowCount()).isEqualTo(3);
    assertThat(first.counts()).isEqualTo(Map.of(STAIN, Map.of("H&E", 1L, "IHC", 2L)));
    assertThat(page.locatedIndex()).isNull();
  }

  @Test
  public void groupBySample_returnsSamplesWithTheirPatient_andSkipsPatientLevelRows() {
    Q q = combined();
    q.groupBy = "sample";
    ResourceTableGroupPage page = groups(q.build());

    assertThat(keys(page))
        .containsExactly(
            "GENIE-TEST-301-01",
            "GENIE-TEST-302-01",
            "GENIE-TEST-303-01",
            "GENIE-TEST-304-01",
            "GENIE-TEST-305-01");
    assertThat(page.totalRows()).isEqualTo(8);
    assertThat(page.groups().getFirst().patientId()).isEqualTo("GENIE-TEST-301");
    assertThat(page.groups().getFirst().rowCount()).isEqualTo(3);
  }

  @Test
  public void groupCounts_countsEachRequestedColumn_includingResourceId() {
    Q q = combined();
    q.groupCounts = List.of(STAIN, "metadata:stain_name", "resourceId");
    ResourceTableGroup g301 = group(groups(q.build()), "GENIE-TEST-301");

    assertThat(g301.counts().get(STAIN)).isEqualTo(Map.of("H&E", 1L, "IHC", 3L));
    assertThat(g301.counts().get("metadata:stain_name"))
        .isEqualTo(Map.of("H&E", 1L, "GATA3", 1L, "ER", 1L, "KI67", 1L));
    assertThat(g301.counts().get("resourceId")).isEqualTo(Map.of(SLIDES, 3L, PATIENT_SLIDES, 1L));
  }

  @Test
  public void groupCounts_absent_leavesCountsEmpty() {
    Q q = q();
    q.groupCounts = null;
    ResourceTableGroupPage page = groups(q.build());

    assertThat(page.groups()).hasSize(5);
    assertThat(page.groups().getFirst().counts()).isEmpty();
  }

  @Test
  public void filtersApplyBeforeGrouping_andAGroupWithNoRowsLeftDropsOut() {
    Q q = combined();
    q.filters = List.of(new ResourceColumnFilter(STAIN, "in", List.of("IHC")));
    ResourceTableGroupPage page = groups(q.build());

    // 302, 303 and 305 have no IHC slide, so they drop out; 301 keeps only its IHC rows.
    assertThat(keys(page)).containsExactly("GENIE-TEST-301", "GENIE-TEST-304", "GENIE-TEST-306");
    assertThat(page.totalGroups()).isEqualTo(3);
    assertThat(page.totalRows()).isEqualTo(5);
    ResourceTableGroup g301 = group(page, "GENIE-TEST-301");
    assertThat(g301.rowCount()).isEqualTo(3);
    assertThat(g301.counts().get(STAIN)).isEqualTo(Map.of("IHC", 3L));
  }

  @Test
  public void searchAppliesBeforeGrouping() {
    Q q = combined();
    q.search = "GATA3";
    ResourceTableGroupPage page = groups(q.build());

    assertThat(keys(page)).containsExactly("GENIE-TEST-301");
    assertThat(page.groups().getFirst().rowCount()).isEqualTo(1);
  }

  @Test
  public void sortByRowCount_descending_breaksTiesByStudyAndKey() {
    Q q = combined();
    q.sortBy = "rowCount";
    q.direction = "desc";

    assertThat(keys(groups(q.build())))
        .containsExactly(
            "GENIE-TEST-301",
            "GENIE-TEST-302",
            "GENIE-TEST-303",
            "GENIE-TEST-304",
            "GENIE-TEST-305",
            "GENIE-TEST-306");
  }

  @Test
  public void sortByRowCount_ascending() {
    Q q = combined();
    q.sortBy = "rowCount";
    q.direction = "asc";

    assertThat(keys(groups(q.build())))
        .containsExactly(
            "GENIE-TEST-304",
            "GENIE-TEST-305",
            "GENIE-TEST-306",
            "GENIE-TEST-302",
            "GENIE-TEST-303",
            "GENIE-TEST-301");
  }

  @Test
  public void sortByGroupKey_descending() {
    Q q = q();
    q.sortBy = "patientId";
    q.direction = "desc";

    assertThat(keys(groups(q.build())))
        .containsExactly(
            "GENIE-TEST-305",
            "GENIE-TEST-304",
            "GENIE-TEST-303",
            "GENIE-TEST-302",
            "GENIE-TEST-301");
  }

  @Test
  public void pagingIsOverGroups_andTotalsCoverEveryPage() {
    Q q = combined();
    q.pageSize = 4;
    q.pageNumber = 1;
    ResourceTableGroupPage page = groups(q.build());

    assertThat(keys(page)).containsExactly("GENIE-TEST-305", "GENIE-TEST-306");
    assertThat(page.totalGroups()).isEqualTo(6);
    assertThat(page.totalRows()).isEqualTo(11);
  }

  @Test
  public void aPagePastTheLastGroup_isEmptyButKeepsTheTotals() {
    Q q = combined();
    q.pageSize = 4;
    q.pageNumber = 5;
    ResourceTableGroupPage page = groups(q.build());

    assertThat(page.groups()).isEmpty();
    assertThat(page.totalGroups()).isEqualTo(6);
    assertThat(page.totalRows()).isEqualTo(11);
  }

  @Test
  public void offset_isComputedInLong_soAPageFarPastTheEndReadsNothing() {
    Q q = combined();
    q.groupBy = null;
    q.pageNumber = Integer.MAX_VALUE;
    q.pageSize = 1000;
    ResourceTableQuery query = q.build();

    assertThat(query.offset()).isEqualTo(Integer.MAX_VALUE * 1000L);
    assertThat(repository.getResourceTableRows(query, context(query, null))).isEmpty();

    Q grouped = combined();
    grouped.pageNumber = Integer.MAX_VALUE;
    grouped.pageSize = 1000;
    ResourceTableGroupPage page = groups(grouped.build());
    assertThat(page.groups()).isEmpty();
    assertThat(page.totalGroups()).isEqualTo(6);
  }

  // ---- locate ----

  @Test
  public void locate_returnsTheGroupsIndexUnderTheCurrentSort() {
    Q q = combined();
    q.pageSize = 2;
    q.sortBy = "rowCount";
    q.direction = "desc";
    q.locate = new ResourceGroupLocator(STUDY, "GENIE-TEST-306", null);

    assertThat(groups(q.build()).locatedIndex()).isEqualTo(5L);
  }

  @Test
  public void locate_returnsNullForAGroupTheFiltersRemove() {
    Q q = combined();
    q.filters = List.of(new ResourceColumnFilter(STAIN, "in", List.of("IHC")));
    q.locate = new ResourceGroupLocator(STUDY, "GENIE-TEST-302", null);

    assertThat(groups(q.build()).locatedIndex()).isNull();
  }

  @Test
  public void locate_bySample_underSampleGrouping() {
    Q q = q();
    q.groupBy = "sample";
    q.locate = new ResourceGroupLocator(STUDY, null, "GENIE-TEST-303-01");

    assertThat(groups(q.build()).locatedIndex()).isEqualTo(2L);
  }

  @Test
  public void locate_givesTheIndexOfAGroupOnTheLastPage_andThatPageHoldsIt() {
    Q q = combined();
    q.pageSize = 4;
    q.locate = new ResourceGroupLocator(STUDY, "GENIE-TEST-306", null);
    ResourceTableGroupPage first = groups(q.build());

    assertThat(first.locatedIndex()).isEqualTo(5L);
    assertThat(keys(first)).doesNotContain("GENIE-TEST-306");

    q.pageNumber = (int) (first.locatedIndex() / q.pageSize);
    ResourceTableGroupPage last = groups(q.build());
    assertThat(q.pageNumber).isEqualTo(1);
    assertThat(keys(last)).containsExactly("GENIE-TEST-305", "GENIE-TEST-306");
    assertThat(last.locatedIndex()).isEqualTo(5L);
    assertThat(last.totalGroups()).isEqualTo(6);
  }

  // ---- combined resources ----

  @Test
  public void combinedResources_countPatientLevelRowsWithTheirPatient() {
    ResourceTableGroupPage page = groups(combined().build());

    assertThat(page.totalGroups()).isEqualTo(6);
    assertThat(page.totalRows()).isEqualTo(11);
    assertThat(group(page, "GENIE-TEST-301").rowCount()).isEqualTo(4);
    assertThat(group(page, "GENIE-TEST-302").counts().get(STAIN)).isEqualTo(Map.of("H&E", 2L));
    assertThat(group(page, "GENIE-TEST-306").rowCount()).isEqualTo(1);
  }

  @Test
  public void combinedResources_whereOneResourceIsMissingFromOneStudy() {
    // HE_SLIDE is in both studies; CT_SCAN only in study_tcga_pub, as one patient-level row.
    Q q = q();
    q.studyIds = List.of("study_tcga_pub", "acc_tcga");
    q.resourceId = null;
    q.resourceIds = List.of("HE_SLIDE", "CT_SCAN");
    q.groupCounts = List.of("resourceId");
    ResourceTableQuery query = q.build();
    ResourceQueryContext context = context(query, null);

    assertThat(context.scope().resourceIds()).containsExactlyInAnyOrder("HE_SLIDE", "CT_SCAN");
    assertThat(context.scope().rows())
        .extracting(row -> row.resourceId() + ":" + row.studyId() + ":" + row.mayHoldRows())
        .containsExactly(
            "CT_SCAN:study_tcga_pub:true",
            "HE_SLIDE:acc_tcga:true",
            "HE_SLIDE:study_tcga_pub:true");
    ResourceTableGroupPage page = repository.getResourceTableGroups(query, context);
    assertThat(page.groups())
        .extracting(g -> g.studyId() + ":" + g.patientId() + "=" + g.counts().get("resourceId"))
        .containsExactly(
            "acc_tcga:tcga-a1-a0sb={HE_SLIDE=1}",
            "study_tcga_pub:tcga-a1-a0sb={CT_SCAN=1, HE_SLIDE=1}",
            "study_tcga_pub:tcga-a1-a0sd={HE_SLIDE=1}",
            "acc_tcga:tcga-zz-9999={HE_SLIDE=1}");
    assertThat(page.totalRows()).isEqualTo(5);
  }

  @Test
  public void combinedResources_rowQueryReadsBothResources() {
    Q q = combined();
    q.groupBy = null;
    ResourceTableQuery query = q.build();

    List<ResourceTableRow> rows = repository.getResourceTableRows(query, context(query, null));

    assertThat(rows).hasSize(11);
    assertThat(rows.stream().map(ResourceTableRow::resourceId).distinct())
        .containsExactlyInAnyOrder(SLIDES, PATIENT_SLIDES);
  }

  @Test
  public void resourceScope_listsEachResourceThatTheStudiesDefineOrHold() {
    Q q = combined();
    q.resourceIds = List.of(SLIDES, PATIENT_SLIDES, "NOT_A_RESOURCE");
    ResourceScope scope = repository.getResourceScope(q.build(), true);

    assertThat(scope.resourceIds()).containsExactlyInAnyOrder(SLIDES, PATIENT_SLIDES);
    assertThat(scope.rows()).allMatch(row -> row.defined() && row.mayHoldRows() && row.declared());
    assertThat(scope.compactCurrent()).isTrue();
    // A row query never reads the compact table, so its freshness is not checked.
    q.groupBy = null;
    q.groupCounts = null;
    assertThat(repository.getResourceScope(q.build(), true).compactCurrent()).isFalse();
    // Nor for a request that parses no metadata beyond a page of groups.
    q.groupBy = "patient";
    assertThat(repository.getResourceScope(q.build(), false).compactCurrent()).isFalse();
  }

  @Test
  public void groupsSpanningStudies_keepSameNamedPatientsApart() {
    // tcga-a1-a0sb exists in both studies, with an HE_SLIDE row in each.
    Q q = q();
    q.studyIds = List.of("study_tcga_pub", "acc_tcga");
    q.resourceId = "HE_SLIDE";
    q.groupCounts = List.of("metadata:magnification");
    ResourceTableGroupPage page = groups(q.build());

    assertThat(page.totalGroups()).isEqualTo(4);
    assertThat(
            page.groups().stream()
                .filter(g -> g.patientId().equals("tcga-a1-a0sb"))
                .map(ResourceTableGroup::studyId))
        .containsExactly("acc_tcga", "study_tcga_pub");
    assertThat(page.groups())
        .allSatisfy(g -> assertThat(g.counts().get("metadata:magnification")).hasSize(1));
  }

  // ---- study-view cohort ----

  private static StudyViewFilterContext cohort(StudyViewFilter filter) {
    return StudyViewFilterFactory.make(filter, List.of(), filter.getStudyIds(), null);
  }

  private static StudyViewFilter studyFilter() {
    StudyViewFilter filter = new StudyViewFilter();
    filter.setStudyIds(List.of(STUDY));
    return filter;
  }

  private static StudyViewFilter centerFilter(String center) {
    StudyViewFilter filter = studyFilter();
    ClinicalDataFilter clinical = new ClinicalDataFilter();
    clinical.setAttributeId("center");
    DataFilterValue value = new DataFilterValue();
    value.setValue(center);
    clinical.setValues(List.of(value));
    filter.setClinicalDataFilters(List.of(clinical));
    return filter;
  }

  @Test
  public void studyViewCohort_withoutFilters_isEveryPatientOfTheStudy() {
    Q q = combined();
    q.studyViewFilter = studyFilter();
    ResourceTableQuery query = q.build();

    ResourceTableGroupPage page =
        repository.getResourceTableGroups(query, context(query, cohort(q.studyViewFilter)));

    assertThat(page.totalGroups()).isEqualTo(6);
    assertThat(page.totalRows()).isEqualTo(11);
  }

  @Test
  public void studyViewCohort_clinicalFilter_selectsItsSamplesAndTheirPatientsRows() {
    Q q = combined();
    q.studyViewFilter = centerFilter("msk");
    ResourceTableQuery query = q.build();

    ResourceTableGroupPage page =
        repository.getResourceTableGroups(query, context(query, cohort(q.studyViewFilter)));

    // center=msk is patients 301-303; 306's patient-level row is outside the cohort.
    assertThat(keys(page)).containsExactly("GENIE-TEST-301", "GENIE-TEST-302", "GENIE-TEST-303");
    assertThat(page.totalRows()).isEqualTo(8);
    assertThat(group(page, "GENIE-TEST-301").counts().get(STAIN))
        .isEqualTo(Map.of("H&E", 1L, "IHC", 3L));
  }

  @Test
  public void studyViewCohort_selectingNoSamples_givesNoGroups() {
    Q q = combined();
    q.studyViewFilter = centerFilter("nowhere");
    q.locate = new ResourceGroupLocator(STUDY, "GENIE-TEST-301", null);
    ResourceTableQuery query = q.build();

    ResourceTableGroupPage page =
        repository.getResourceTableGroups(query, context(query, cohort(q.studyViewFilter)));

    assertThat(query.offset()).isZero();
    assertThat(page.groups()).isEmpty();
    assertThat(page.totalGroups()).isZero();
    assertThat(page.totalRows()).isZero();
    assertThat(page.locatedIndex()).isNull();
  }

  @Test
  public void studyViewCohort_appliesToRowsAndCounts_andReplacesIdentifierLists() {
    Q q = combined();
    q.groupBy = null;
    q.studyViewFilter = centerFilter("dfci");
    ResourceTableQuery query = q.build();
    ResourceQueryContext context = context(query, cohort(q.studyViewFilter));

    List<ResourceTableRow> rows = repository.getResourceTableRows(query, context);
    ResourceTableCounts counts = repository.getResourceTableCounts(query, context);

    // center=dfci is patients 304 and 305, one sample-level slide each.
    assertThat(rows)
        .extracting(ResourceTableRow::patientId)
        .containsExactly("GENIE-TEST-304", "GENIE-TEST-305");
    assertThat(counts.rowCount()).isEqualTo(2);
    assertThat(counts.patientCount()).isEqualTo(2);
  }

  // ---- grouped facets ----

  @Test
  public void groupedFacets_countGroupsNotRows() {
    ResourceTableQuery query = combined().build();

    Map<String, List<ResourceFacetOption>> facets =
        repository.getResourceTableMetadata(query, context(query, null)).facets();

    // IHC: rows of 301 (x3), 304, 306 -- three patients. H&E: 301, 302 (x2), 303 (x2), 305.
    assertThat(facets.get(STAIN))
        .containsExactly(new ResourceFacetOption("H&E", 4L), new ResourceFacetOption("IHC", 3L));
    assertThat(facets).doesNotContainKey("metadata:block");
  }

  @Test
  public void groupedMetadata_countsGroupsInItsFacetsOnly() {
    Q q = combined();
    q.groupBy = "sample";
    ResourceTableQuery grouped = q.build();
    ResourceTableQuery rows = grouped.withoutGrouping();
    ResourceQueryContext groupedContext = context(grouped, null);
    ResourceQueryContext rowsContext = context(rows, null);

    // The three patient-level rows belong to no sample, but they are still rows of the table.
    ResourceTableCounts counts = repository.getResourceTableCounts(grouped, groupedContext);
    assertThat(counts).isEqualTo(repository.getResourceTableCounts(rows, rowsContext));
    assertThat(counts.rowCount()).isEqualTo(11);
    assertThat(counts.patientCount()).isEqualTo(6);
    assertThat(counts.sampleCount()).isEqualTo(5);

    ResourceTableMetadataView groupedMetadata =
        repository.getResourceTableMetadata(grouped, groupedContext);
    ResourceTableMetadataView rowMetadata = repository.getResourceTableMetadata(rows, rowsContext);
    assertThat(groupedMetadata.columns()).isEqualTo(rowMetadata.columns());
    assertThat(groupedMetadata.facetRanges()).isEqualTo(rowMetadata.facetRanges());
    // Samples carrying each value: H&E on 301-01, 302-01, 303-01 and 305-01; IHC on 301-01 and
    // 304-01. The rows would count 6 and 5.
    assertThat(groupedMetadata.facets().get(STAIN))
        .containsExactly(new ResourceFacetOption("H&E", 4L), new ResourceFacetOption("IHC", 2L));
    assertThat(rowMetadata.facets().get(STAIN))
        .containsExactly(new ResourceFacetOption("H&E", 6L), new ResourceFacetOption("IHC", 5L));
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void aBlankMetadataKey_namesNoColumnOrFacet() {
    jdbcTemplate.update(
        "INSERT INTO resource (internal_id, resource_id, cancer_study_id, resource_scope,"
            + " patient_id, sample_id, url, display_name, metadata) VALUES (9102, ?, 3, 'SAMPLE',"
            + " 'GENIE-TEST-301', 'GENIE-TEST-301-01', 'https://example.com/g9102.svs',"
            + " 'Slide 9102', '{\"\":\"blank\",\"stain_group\":\"IHC\"}')",
        NO_CONTRACT);
    try {
      Q q = q();
      q.resourceId = NO_CONTRACT;
      ResourceTableQuery query = q.build();

      ResourceTableMetadataView metadata =
          repository.getResourceTableMetadata(query, context(query, null));

      assertThat(metadata.columns())
          .extracting(column -> column.id())
          .containsExactly("metadata:extra", STAIN);
      assertThat(metadata.facets()).containsOnlyKeys("metadata:extra", STAIN);
    } finally {
      jdbcTemplate.execute(
          "ALTER TABLE resource DELETE WHERE internal_id = 9102 SETTINGS mutations_sync = 2");
    }
  }

  @Test
  public void ungroupedFacets_stillCountRows() {
    Q q = combined();
    q.groupBy = null;
    ResourceTableQuery query = q.build();

    Map<String, List<ResourceFacetOption>> facets =
        repository.getResourceTableMetadata(query, context(query, null)).facets();

    assertThat(facets.get(STAIN))
        .containsExactly(new ResourceFacetOption("H&E", 6L), new ResourceFacetOption("IHC", 5L));
  }

  // ---- compact metadata table ----

  @Test
  public void compactTable_holdsOnlyTheContractsDeclaredKeys() {
    String metadata =
        jdbcTemplate.queryForObject(
            "SELECT metadata FROM resource_compact_derived WHERE internal_id = 102", String.class);

    assertThat(metadata)
        .isEqualTo("{\"stain_group\":\"IHC\",\"stain_name\":\"GATA3\",\"block\":\"A2\"}");
  }

  @Test
  public void compactTable_skipsResourcesWithoutAContract() {
    Integer copied =
        jdbcTemplate.queryForObject(
            "SELECT count() FROM resource_compact_derived WHERE resource_id = ?",
            Integer.class,
            NO_CONTRACT);
    Integer source =
        jdbcTemplate.queryForObject(
            "SELECT count() FROM resource WHERE resource_id = ?", Integer.class, SLIDES);
    Integer copiedSlides =
        jdbcTemplate.queryForObject(
            "SELECT count() FROM resource_compact_derived WHERE resource_id = ?",
            Integer.class,
            SLIDES);

    assertThat(copied).isZero();
    assertThat(copiedSlides).isEqualTo(source);
  }

  @Test
  public void groupedQueries_readTheCompactTable_onlyWhenItHoldsEveryKeyTheyRead() {
    ResourceTableQuery declared = q().build();
    Q undeclared = q();
    undeclared.filters = List.of(new ResourceColumnFilter("metadata:serving", "in", List.of("x")));
    Q noContract = q();
    noContract.resourceId = NO_CONTRACT;
    Q rows = q();
    rows.groupBy = null;

    assertThat(context(declared, null).readsCompactTable(declared)).isTrue();
    assertThat(context(undeclared.build(), null).readsCompactTable(undeclared.build())).isFalse();
    assertThat(context(noContract.build(), null).readsCompactTable(noContract.build())).isFalse();
    assertThat(context(rows.build(), null).readsCompactTable(rows.build())).isFalse();
  }

  @Test
  public void groupingAResourceWithoutAContract_fallsBackToTheFullTable() {
    Q q = q();
    q.resourceId = NO_CONTRACT;
    q.groupCounts = List.of(STAIN, "metadata:extra");
    ResourceTableGroupPage page = groups(q.build());

    assertThat(keys(page)).containsExactly("GENIE-TEST-301", "GENIE-TEST-302");
    ResourceTableGroup g302 = group(page, "GENIE-TEST-302");
    assertThat(g302.rowCount()).isEqualTo(2);
    assertThat(g302.counts().get(STAIN)).isEqualTo(Map.of("IHC", 2L));
    // "extra" is in no contract; only the full table holds it.
    assertThat(g302.counts().get("metadata:extra")).isEqualTo(Map.of("y", 1L));
  }

  @Test
  public void groupCountsOnAnUndeclaredKey_fallBackToTheFullTable() {
    Q q = q();
    q.groupCounts = List.of("metadata:serving");
    ResourceTableQuery query = q.build();

    assertThat(context(query, null).readsCompactTable(query)).isFalse();
    // serving is an object, not a string, so it has no countable value either way; the point is
    // that the query runs against resource rather than silently reading the compact table.
    assertThat(groups(query).groups()).hasSize(5);
  }

  @Test
  public void compactAndFullTable_giveTheSameGroups() {
    Q q = combined();
    q.filters = List.of(new ResourceColumnFilter(STAIN, "in", List.of("H&E")));
    q.groupCounts = List.of(STAIN, "metadata:stain_name");
    ResourceTableQuery query = q.build();
    ResourceQueryContext compact = context(query, null);
    assertThat(compact.readsCompactTable(query)).isTrue();

    List<ResourceTableGroupRow> fromCompact = mapper.getResourceTableGroupPage(query, null, true);
    List<ResourceTableGroupRow> fromFull = mapper.getResourceTableGroupPage(query, null, false);

    assertThat(fromCompact).isEqualTo(fromFull).isNotEmpty();
  }

  @Test
  public void search_readsTheFullTable_andMatchesValuesOfUndeclaredKeys() {
    // "opaque-101" is only in slide 101's undeclared serving key, which the compact table drops.
    Q q = combined();
    q.search = "opaque-101";
    q.groupCounts = List.of(STAIN, "metadata:stain_name");
    ResourceTableQuery query = q.build();
    ResourceQueryContext scoped = context(query, null);
    ResourceQueryContext fullTable = ResourceQueryContext.none();
    assertThat(scoped.readsCompactTable(query)).isFalse();

    ResourceTableGroupPage page = repository.getResourceTableGroups(query, scoped);
    assertThat(page).isEqualTo(repository.getResourceTableGroups(query, fullTable));
    assertThat(keys(page)).containsExactly("GENIE-TEST-301");
    assertThat(page.totalRows()).isEqualTo(1);
    assertThat(page.groups().getFirst().counts().get(STAIN)).isEqualTo(Map.of("H&E", 1L));
    // The compact table alone would have found nothing.
    assertThat(mapper.getResourceTableGroupPage(query, null, true)).isEmpty();

    assertThat(repository.getResourceTableMetadata(query, scoped))
        .isEqualTo(repository.getResourceTableMetadata(query, fullTable));
    assertThat(repository.getResourceTableCounts(query, scoped))
        .isEqualTo(repository.getResourceTableCounts(query, fullTable));
    assertThat(repository.getResourceTableMetadata(query, scoped).facets().get(STAIN))
        .containsExactly(new ResourceFacetOption("H&E", 1L));
  }

  // The tests below change the tables between two reads of the same statement. They run outside
  // the test transaction, since inside it MyBatis would answer the second read from the first
  // one's session cache.

  /** The default grouped query's groups, which must come out the same from either table. */
  private static final List<String> ALL_SLIDE_PATIENTS =
      List.of(
          "GENIE-TEST-301", "GENIE-TEST-302", "GENIE-TEST-303", "GENIE-TEST-304", "GENIE-TEST-305");

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void anEmptyCompactTable_isNotTrusted_andGroupsComeFromTheFullTable() {
    jdbcTemplate.execute("DROP TABLE IF EXISTS compact_backup");
    jdbcTemplate.execute("CREATE TABLE compact_backup AS resource_compact_derived");
    jdbcTemplate.execute("INSERT INTO compact_backup SELECT * FROM resource_compact_derived");
    try {
      jdbcTemplate.execute("TRUNCATE TABLE resource_compact_derived");
      ResourceTableQuery query = q().build();
      ResourceQueryContext context = context(query, null);

      assertThat(context.scope().compactCurrent()).isFalse();
      assertThat(context.readsCompactTable(query)).isFalse();
      ResourceTableGroupPage page = repository.getResourceTableGroups(query, context);
      assertThat(keys(page)).isEqualTo(ALL_SLIDE_PATIENTS);
      assertThat(page.totalRows()).isEqualTo(8);
      assertThat(page.groups().getFirst().counts().get(STAIN))
          .isEqualTo(Map.of("H&E", 1L, "IHC", 2L));
    } finally {
      jdbcTemplate.execute("INSERT INTO resource_compact_derived SELECT * FROM compact_backup");
      jdbcTemplate.execute("DROP TABLE compact_backup");
    }
    ResourceTableQuery query = q().build();
    assertThat(context(query, null).readsCompactTable(query)).isTrue();
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void rowsImportedWithoutRepopulating_areNotMissed() {
    // A row added to resource after the compact table was built, as a study import that skipped
    // populate_derived_tables.sql would leave it.
    jdbcTemplate.update(
        "INSERT INTO resource (internal_id, resource_id, cancer_study_id, resource_scope,"
            + " patient_id, sample_id, url, display_name, metadata) VALUES (9101, ?, 3, 'SAMPLE',"
            + " 'GENIE-TEST-307', 'GENIE-TEST-307-01', 'https://example.com/g9101.svs',"
            + " 'Slide 9101', '{\"stain_group\":\"IHC\"}')",
        SLIDES);
    try {
      ResourceTableQuery query = q().build();
      ResourceQueryContext context = context(query, null);

      assertThat(context.scope().compactCurrent()).isFalse();
      assertThat(context.readsCompactTable(query)).isFalse();
      ResourceTableGroupPage page = repository.getResourceTableGroups(query, context);
      assertThat(keys(page)).contains("GENIE-TEST-307");
      assertThat(page.totalRows()).isEqualTo(9);
    } finally {
      jdbcTemplate.execute(
          "ALTER TABLE resource DELETE WHERE internal_id = 9101 SETTINGS mutations_sync = 2");
    }
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void rowsCopiedUnderAnEarlierContract_areNotTrusted() {
    // The contract changed after the compact table was built: a second definition whose contract
    // max() now picks declares "serving", which the compact rows were copied without.
    String contract =
        "{\"version\":2,\"fields\":[{\"key\":\"stain_group\"},{\"key\":\"serving\"}]}";
    jdbcTemplate.update(
        "INSERT INTO resource_definition (resource_id, cancer_study_id, resource_type,"
            + " display_name, description, open_by_default, priority, custom_metadata)"
            + " VALUES (?, 3, 'PATIENT', 'Slides', 'Patient-level slides', 1, 2, ?)",
        PATIENT_SLIDES,
        contract);
    try {
      ResourceTableQuery query = combined().build();
      ResourceQueryContext context = context(query, null);

      // The contracts alone would let the query read the compact table; its copy is stale.
      assertThat(context.scope().contractsCover(query.referencedMetadataKeys())).isTrue();
      assertThat(context.scope().compactCurrent()).isFalse();
      assertThat(context.readsCompactTable(query)).isFalse();
      assertThat(repository.getResourceTableGroups(query, context).totalRows()).isEqualTo(11);
    } finally {
      jdbcTemplate.update(
          "ALTER TABLE resource_definition DELETE WHERE resource_id = ? AND custom_metadata = ?"
              + " SETTINGS mutations_sync = 2",
          PATIENT_SLIDES,
          contract);
    }
    ResourceTableQuery query = combined().build();
    assertThat(context(query, null).readsCompactTable(query)).isTrue();
  }

  @Test
  public void locate_onAPagePastTheLastGroup_isStillReported() {
    Q q = combined();
    q.pageSize = 4;
    q.pageNumber = 5;
    q.locate = new ResourceGroupLocator(STUDY, "GENIE-TEST-303", null);

    ResourceTableGroupPage page = groups(q.build());

    assertThat(page.groups()).isEmpty();
    assertThat(page.locatedIndex()).isEqualTo(2L);
  }

  @Test
  public void groupedFacets_parseKeysThatNeedQuotingInATupleType() {
    assertThat(ClickhouseResourceDataMapper.facetTupleType(new String[] {"a b", "c`d", "e\\f"}))
        .isEqualTo("Tuple(`a b` String, `c\\`d` String, `e\\\\f` String)");
    // And the statement accepts such a type: no fixture row carries these keys, so no values.
    Q q = combined();
    ResourceTableQuery query = q.build();
    String[] keys = {"stain_group", "odd key`"};
    List<org.cbioportal.domain.resource.ResourceMetadataFacetValue> values =
        mapper.getResourceTableGroupedMetadataFacets(
            query,
            null,
            true,
            keys,
            ClickhouseResourceDataMapper.facetTupleType(keys),
            10,
            1L << 30,
            1L << 29);
    assertThat(values).extracting(v -> v.metaKey()).containsOnly("stain_group");
  }

  @Test
  public void studyViewCohort_spanningStudies_matchesEachRowInItsOwnStudy() {
    // tcga-a1-a0sb-01 is a sample of both studies; tcga-zz-9999-01 is no sample at all.
    Q q = q();
    q.studyIds = List.of("study_tcga_pub", "acc_tcga");
    q.resourceId = "HE_SLIDE";
    StudyViewFilter filter = new StudyViewFilter();
    filter.setStudyIds(List.of("study_tcga_pub", "acc_tcga"));
    q.studyViewFilter = filter;
    ResourceTableQuery query = q.build();

    ResourceTableGroupPage page =
        repository.getResourceTableGroups(query, context(query, cohort(filter)));

    assertThat(page.groups())
        .extracting(g -> g.studyId() + ":" + g.patientId())
        .containsExactly(
            "acc_tcga:tcga-a1-a0sb", "study_tcga_pub:tcga-a1-a0sb", "study_tcga_pub:tcga-a1-a0sd");
  }

  @Test
  public void studyViewCohort_fromAnotherStudy_doesNotMatchASameNamedSample() {
    // The cohort is study_tcga_pub's tcga-a1-a0sb-01 only. acc_tcga has a sample of the same
    // name, which a bare-id match would wrongly take for it.
    Q q = q();
    q.studyIds = List.of("acc_tcga");
    q.resourceId = "HE_SLIDE";
    StudyViewFilter filter = new StudyViewFilter();
    filter.setSampleIdentifiers(
        org.cbioportal.domain.resource.ResourceIdentifierFixtures.samples(
            "study_tcga_pub", "tcga-a1-a0sb-01"));
    q.studyViewFilter = filter;
    ResourceTableQuery query = q.build();

    ResourceTableGroupPage page =
        repository.getResourceTableGroups(
            query,
            context(
                query,
                StudyViewFilterFactory.make(filter, List.of(), List.of("study_tcga_pub"), null)));

    assertThat(page.groups()).isEmpty();
    assertThat(page.totalGroups()).isZero();
  }

  @Test
  public void aStudyHoldingRowsWithoutADefinition_keepsTheQueryOnTheFullTable() {
    // acc_tcga declares a CYTOLOGY contract; study_tcga_pub holds a CYTOLOGY row with no
    // definition at all, which the compact table therefore lacks.
    Q q = q();
    q.studyIds = List.of("study_tcga_pub", "acc_tcga");
    q.resourceId = "CYTOLOGY";
    q.groupCounts = null;
    ResourceTableQuery query = q.build();
    ResourceQueryContext context = context(query, null);

    assertThat(context.scope().rows())
        .anyMatch(
            row -> row.studyId().equals("study_tcga_pub") && row.mayHoldRows() && !row.declared());
    assertThat(context.readsCompactTable(query)).isFalse();
    assertThat(repository.getResourceTableGroups(query, context).groups())
        .extracting(g -> g.studyId() + ":" + g.patientId())
        .containsExactly("study_tcga_pub:tcga-a1-a0zz", "acc_tcga:tcga-zz-9999");
  }
}
