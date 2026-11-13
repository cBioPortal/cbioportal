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
 * Grouped resource-table reads, the study-view cohort, combined resources and the metadata_json
 * column they read metadata values from, over the GROUPED_* fixtures in study_genie_pub (see
 * clickhouse_data.sql).
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
    return new ResourceQueryContext(cohort, repository.getResourceScope(query));
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
    ResourceScope scope = repository.getResourceScope(q.build());

    assertThat(scope.resourceIds()).containsExactlyInAnyOrder(SLIDES, PATIENT_SLIDES);
    assertThat(scope.rows()).allMatch(row -> row.defined() && row.mayHoldRows() && row.declared());
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
  public void groupedFacets_countSameNamedGroupsOfTwoStudiesApart() {
    // Both studies hold a patient tcga-a1-a0sb with sample tcga-a1-a0sb-01 carrying 20x; 40x is on
    // study_tcga_pub's tcga-a1-a0sd and acc_tcga's tcga-zz-9999.
    for (String groupBy : new String[] {"patient", "sample"}) {
      Q q = q();
      q.studyIds = List.of("study_tcga_pub", "acc_tcga");
      q.resourceId = "HE_SLIDE";
      q.groupBy = groupBy;
      q.groupCounts = null;
      ResourceTableQuery query = q.build();

      assertThat(
              repository
                  .getResourceTableMetadata(query, context(query, null))
                  .facets()
                  .get("metadata:magnification"))
          .as("grouped by %s", groupBy)
          .containsExactly(new ResourceFacetOption("20x", 2L), new ResourceFacetOption("40x", 2L));
    }
  }

  @Test
  public void groupedFacets_countTheGroupsWhoseRowsCarryEachValue() {
    // Each facet count against the groups themselves, read by the groups statement: the number of
    // groups whose value counts include the value. Across groupings, combined resources, a search
    // and the study-view cohort.
    for (String groupBy : new String[] {"patient", "sample"}) {
      for (String search : new String[] {null, "g10"}) {
        for (StudyViewFilter filter : new StudyViewFilter[] {null, centerFilter("msk")}) {
          Q q = combined();
          q.groupBy = groupBy;
          q.search = search;
          q.studyViewFilter = filter;
          q.pageSize = 1000;
          ResourceTableQuery query = q.build();
          ResourceQueryContext context = context(query, filter == null ? null : cohort(filter));

          Map<String, Long> fromGroups = new java.util.TreeMap<>();
          for (ResourceTableGroup g : repository.getResourceTableGroups(query, context).groups()) {
            g.counts()
                .getOrDefault(STAIN, Map.of())
                .keySet()
                .forEach(value -> fromGroups.merge(value, 1L, Long::sum));
          }
          List<ResourceFacetOption> expected =
              fromGroups.entrySet().stream()
                  .map(e -> new ResourceFacetOption(e.getKey(), e.getValue()))
                  .toList();

          assertThat(expected)
              .as("groups by %s, search %s, cohort %s", groupBy, search, filter)
              .isNotEmpty();
          assertThat(repository.getResourceTableMetadata(query, context).facets().get(STAIN))
              .as("grouped by %s, search %s, cohort %s", groupBy, search, filter)
              .isEqualTo(expected);
        }
      }
    }
  }

  @Test
  public void contracts_followTheStudyViewCohort() {
    // GROUPED_NO_CONTRACT has rows of patients 301 and 302 only, which center=msk includes and
    // center=dfci does not.
    Q q = combined();
    q.groupBy = null;
    q.resourceIds = List.of(SLIDES, NO_CONTRACT);
    for (Map.Entry<String, List<Boolean>> c :
        Map.of("msk", List.of(true, false), "dfci", List.of(true), "nowhere", List.<Boolean>of())
            .entrySet()) {
      q.studyViewFilter = centerFilter(c.getKey());
      ResourceTableQuery query = q.build();

      assertThat(mapper.getResourceDefinitionCustomMetadata(query, cohort(q.studyViewFilter)))
          .as("center %s", c.getKey())
          .extracting(org.cbioportal.domain.resource.ResourceContractRow::declared)
          .containsExactlyInAnyOrderElementsOf(c.getValue());
    }
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

  // ---- metadata_json ----

  @Test
  public void metadataJson_holdsEveryKey_asPrefixedText() {
    // Slide 102 carries the declared keys and an undeclared nested serving object.
    Map<String, Object> stored =
        jdbcTemplate.queryForMap(
            "SELECT CAST(getSubcolumn(metadata_json, 'stain_name'), 'Nullable(String)') AS stain,"
                + " CAST(getSubcolumn(metadata_json, 'serving'), 'Nullable(String)') AS serving,"
                + " JSONExtractString(metadata, 'serving') AS servingText"
                + " FROM resource WHERE internal_id = 102");

    assertThat(stored.get("stain")).isEqualTo(":GATA3");
    assertThat(stored.get("serving")).isEqualTo(":" + stored.get("servingText"));
  }

  @Test
  public void groupCounts_readKeysNoContractDeclares() {
    Q q = q();
    q.resourceId = NO_CONTRACT;
    q.groupCounts = List.of(STAIN, "metadata:extra");
    ResourceTableGroupPage page = groups(q.build());

    assertThat(keys(page)).containsExactly("GENIE-TEST-301", "GENIE-TEST-302");
    ResourceTableGroup g302 = group(page, "GENIE-TEST-302");
    assertThat(g302.rowCount()).isEqualTo(2);
    assertThat(g302.counts().get(STAIN)).isEqualTo(Map.of("IHC", 2L));
    assertThat(g302.counts().get("metadata:extra")).isEqualTo(Map.of("y", 1L));
  }

  @Test
  public void groupCounts_onANestedObject_countItsJsonText() {
    // serving is an object in no contract; it counts by the text JSONExtractString gives it, the
    // same text a filter on it compares.
    Q q = q();
    q.groupCounts = List.of("metadata:serving");
    ResourceTableQuery query = q.build();
    String serving =
        jdbcTemplate.queryForObject(
            "SELECT JSONExtractString(metadata, 'serving') FROM resource WHERE internal_id = 101",
            String.class);

    ResourceTableGroupPage page = groups(query);
    assertThat(page.groups()).hasSize(5);
    assertThat(group(page, "GENIE-TEST-301").counts().get("metadata:serving"))
        .containsEntry(serving, 1L)
        .hasSize(2);
    assertThat(group(page, "GENIE-TEST-302").counts().get("metadata:serving")).isEmpty();

    q.filters = List.of(new ResourceColumnFilter("metadata:serving", "in", List.of(serving)));
    assertThat(keys(groups(q.build()))).containsExactly("GENIE-TEST-301");
  }

  @Test
  public void search_matchesValuesInTheRawMetadata() {
    // "opaque-101" is only inside slide 101's nested serving object.
    Q q = combined();
    q.search = "opaque-101";
    q.groupCounts = List.of(STAIN, "metadata:stain_name");
    ResourceTableQuery query = q.build();
    ResourceQueryContext scoped = context(query, null);

    ResourceTableGroupPage page = repository.getResourceTableGroups(query, scoped);
    assertThat(keys(page)).containsExactly("GENIE-TEST-301");
    assertThat(page.totalRows()).isEqualTo(1);
    assertThat(page.groups().getFirst().counts().get(STAIN)).isEqualTo(Map.of("H&E", 1L));
    assertThat(repository.getResourceTableMetadata(query, scoped).facets().get(STAIN))
        .containsExactly(new ResourceFacetOption("H&E", 1L));
  }

  @Test
  public void groupedFacets_readKeysThatNeedQuoting() {
    // Keys are spliced into the SQL escaped (ResourceMetadataValueSql), so any text names its own
    // subcolumn, with or without a search. No fixture row carries these keys, so only stain_group
    // has values.
    String[] keys = {
      "stain_group", "odd key`", "a\\b", "x') OR 1 = 1 --", "x`) OR 1 = 1 --", "dotted.key"
    };
    for (String search : new String[] {null, "example.com"}) {
      Q q = combined();
      q.search = search;

      List<org.cbioportal.domain.resource.ResourceMetadataFacetValue> values =
          mapper.getResourceTableGroupedMetadataFacets(
              q.build(), null, keys, 10, 1L << 30, 1L << 29);

      assertThat(values)
          .as("search %s", search)
          .extracting(v -> v.metaKey())
          .containsOnly("stain_group");
    }
  }

  // The tests below change resource between two reads. They run outside the test transaction,
  // since inside it MyBatis would answer the second read from the first one's session cache.

  /** The default grouped query's groups. */
  private static final List<String> ALL_SLIDE_PATIENTS =
      List.of(
          "GENIE-TEST-301", "GENIE-TEST-302", "GENIE-TEST-303", "GENIE-TEST-304", "GENIE-TEST-305");

  private static final String EDGE = "GROUPED_EDGE";

  /**
   * Metadata documents whose values are easy to read back differently from JSONExtractString: by
   * type inference (numbers, booleans, date-like strings), by structure (nested values, duplicate
   * keys, dotted keys) or by not being a JSON object at all.
   */
  private static final List<String> EDGE_DOCUMENTS =
      List.of(
          "{\"v\":1.50}",
          "{\"v\":1e3}",
          "{\"v\":-0.0}",
          "{\"v\":0.1}",
          "{\"v\":123456789012345678}",
          "{\"v\":18446744073709551616}",
          "{\"v\":true}",
          "{\"v\":false}",
          "{\"v\":null}",
          "{\"v\":{\"z\":1, \"y\":[1,2]}}",
          "{\"v\":[1,\"a\"]}",
          "{\"v\":\"caf\\u00e9\"}",
          "{\"v\":\"007\"}",
          "{\"v\":\"1.50\"}",
          "{\"v\":\"true\"}",
          "{\"v\":\"2024-01-01\"}",
          "{\"v\":\"2024-1-1\"}",
          "{\"v\":\"2024-13-45\"}",
          "{\"v\":\"2024-01-01 10:00:00\"}",
          "{\"v\":\"2024-01-01T10:00:00Z\"}",
          "{\"v\":\"q\\\"uote\"}",
          "{\"v\":\"  spaced  \"}",
          "{\"v\":\"a\",\"v\":\"b\"}",
          "{\"v\":\"\",\"v\":\"b\"}",
          "{\"v\":null,\"v\":\"b\"}",
          "{\"v\":\"\"}",
          "{\"v\":\"x\",\"v.x\":\"dot\"}",
          "{\"v\":1,\"w\":{\"v\":2}}",
          "{}",
          "not json",
          "[1,2]");

  /** One GROUPED_EDGE row per edge document, plus one with no metadata at all. */
  private void insertEdgeDocuments() {
    for (int i = 0; i < EDGE_DOCUMENTS.size(); i++) {
      jdbcTemplate.update(
          "INSERT INTO resource (internal_id, resource_id, cancer_study_id, resource_scope,"
              + " patient_id, sample_id, url, display_name, metadata) VALUES (?, ?, 3, 'PATIENT',"
              + " ?, NULL, 'https://example.com/edge.svs', NULL, ?)",
          9400 + i,
          EDGE,
          "EDGE-" + (100 + i),
          EDGE_DOCUMENTS.get(i));
    }
    jdbcTemplate.update(
        "INSERT INTO resource (internal_id, resource_id, cancer_study_id, resource_scope,"
            + " patient_id, sample_id, url, display_name, metadata) VALUES (9499, ?, 3, 'PATIENT',"
            + " 'EDGE-999', NULL, 'https://example.com/edge.svs', NULL, NULL)",
        EDGE);
  }

  private void deleteEdgeDocuments() {
    jdbcTemplate.update(
        "ALTER TABLE resource DELETE WHERE resource_id = ? SETTINGS mutations_sync = 2", EDGE);
  }

  /**
   * The ungrouped facets, the numeric ranges and the key stats of named keys count the values
   * JSONExtractString gives each row's key, with or without a search. A document that repeats a key
   * counts its first value, as the column filters match it.
   */
  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void facetsRangesAndKeyStats_countTheValuesJsonExtractStringGives() {
    insertEdgeDocuments();
    try {
      String[] keys = {"v", "w", "v.x"};
      for (String search : new String[] {null, "edge.svs"}) {
        Q q = q();
        q.resourceId = EDGE;
        q.groupBy = null;
        q.search = search;
        ResourceTableQuery query = q.build();
        for (String key : keys) {
          String value = "JSONExtractString(ifNull(metadata, '{}'), ?)";
          Map<String, Long> expectedFacet = new java.util.TreeMap<>();
          jdbcTemplate.query(
              "SELECT "
                  + value
                  + " AS v, count() FROM resource WHERE resource_id = ? AND v != ''"
                  + " GROUP BY v",
              rs -> {
                expectedFacet.put(rs.getString(1), rs.getLong(2));
              },
              key,
              EDGE);
          assertThat(expectedFacet).as("%s has values", key).isNotEmpty();
          Map<String, Long> facet = new java.util.TreeMap<>();
          mapper
              .getResourceTableMetadataFacets(query, null, new String[] {key}, 1000)
              .forEach(v -> facet.put(v.value(), v.count()));
          assertThat(facet).as("facet on %s, search %s", key, search).isEqualTo(expectedFacet);

          Map<String, Object> expectedStats =
              jdbcTemplate.queryForMap(
                  "SELECT countIf(v != '') AS nonBlank,"
                      + " countIf(v != '' AND toFloat64OrNull(v) IS NOT NULL) AS numeric,"
                      + " minIf(toFloat64OrNull(v), toFloat64OrNull(v) IS NOT NULL) AS lo,"
                      + " maxIf(toFloat64OrNull(v), toFloat64OrNull(v) IS NOT NULL) AS hi"
                      + " FROM (SELECT "
                      + value
                      + " AS v FROM resource WHERE resource_id = ?)",
                  key,
                  EDGE);
          assertThat(
                  mapper.getResourceTableMetadataKeyStats(
                      query, null, new String[] {key}, 100_000, 1L << 30))
              .as("key stats of %s, search %s", key, search)
              .singleElement()
              .satisfies(
                  stats -> {
                    assertThat(stats.key()).isEqualTo(key);
                    assertThat(stats.nonBlankCount())
                        .isEqualTo(((Number) expectedStats.get("nonBlank")).longValue());
                    assertThat(stats.numericCount())
                        .isEqualTo(((Number) expectedStats.get("numeric")).longValue());
                  });
          List<org.cbioportal.domain.resource.ResourceMetadataRange> ranges =
              mapper.getResourceTableMetadataRanges(query, null, new String[] {key});
          if (((Number) expectedStats.get("numeric")).longValue() == 0) {
            assertThat(ranges).as("range of %s, search %s", key, search).isEmpty();
          } else {
            assertThat(ranges)
                .as("range of %s, search %s", key, search)
                .containsExactly(
                    new org.cbioportal.domain.resource.ResourceMetadataRange(
                        key,
                        ((Number) expectedStats.get("lo")).doubleValue(),
                        ((Number) expectedStats.get("hi")).doubleValue()));
          }
        }
      }
    } finally {
      deleteEdgeDocuments();
    }
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void metadataValues_readBackExactlyAsJsonExtractStringGivesThem() {
    insertEdgeDocuments();
    try {
      Map<String, String> expected = new java.util.TreeMap<>();
      jdbcTemplate.query(
          "SELECT patient_id, JSONExtractString(ifNull(metadata, '{}'), 'v') FROM resource"
              + " WHERE resource_id = ?",
          rs -> {
            expected.put(rs.getString(1), rs.getString(2));
          },
          EDGE);
      assertThat(expected).hasSize(EDGE_DOCUMENTS.size() + 1);

      Q q = q();
      q.resourceId = EDGE;
      q.pageSize = 100;
      q.groupCounts = List.of("metadata:v");
      ResourceTableGroupPage page = groups(q.build());

      assertThat(page.groups()).hasSize(expected.size());
      for (ResourceTableGroup group : page.groups()) {
        String value = expected.get(group.patientId());
        Map<String, Long> counts = group.counts().get("metadata:v");
        assertThat(counts)
            .as("%s", group.patientId())
            .isEqualTo(value.isEmpty() ? Map.of() : Map.of(value, 1L));
        if (!value.isEmpty()) {
          Q filtered = q();
          filtered.resourceId = EDGE;
          filtered.filters = List.of(new ResourceColumnFilter("metadata:v", "in", List.of(value)));
          assertThat(keys(groups(filtered.build())))
              .as("%s filtered on %s", group.patientId(), value)
              .contains(group.patientId())
              .allMatch(key -> expected.get(key).equals(value));
        }
      }

      // The facet values are the same text, and the dotted key reads its own value.
      q.groupCounts = List.of("metadata:v.x");
      assertThat(group(groups(q.build()), "EDGE-126").counts().get("metadata:v.x"))
          .isEqualTo(Map.of("dot", 1L));
      ResourceTableQuery query = q.build();
      assertThat(
              repository
                  .getResourceTableMetadata(query, context(query, null))
                  .facets()
                  .get("metadata:v"))
          .extracting(ResourceFacetOption::value)
          .containsExactlyInAnyOrderElementsOf(
              expected.values().stream().filter(v -> !v.isEmpty()).distinct().toList());

      // Under a search the facets read the raw documents instead, and give the same values. The
      // search matches every edge row's url.
      Q searched = q();
      searched.resourceId = EDGE;
      searched.search = "edge.svs";
      ResourceTableQuery searchQuery = searched.build();
      assertThat(
              repository
                  .getResourceTableMetadata(searchQuery, context(searchQuery, null))
                  .facets()
                  .get("metadata:v"))
          .isEqualTo(
              repository
                  .getResourceTableMetadata(query, context(query, null))
                  .facets()
                  .get("metadata:v"));
    } finally {
      deleteEdgeDocuments();
    }
  }

  /**
   * Keys that MyBatis or the subcolumn syntax could read as something else: MyBatis placeholders
   * and their escape, and the .: and .^ of the subcolumn syntax, alone and before a backquoted
   * part.
   */
  private static final List<String> SYNTAX_KEYS =
      List.of("#{search}", "\\#{x}", "${x}", "a.:`b`", "a.:b", "a.^b", "a.^`b`", "#");

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void keysShapedLikeMyBatisOrSubcolumnSyntax_readBackTheirOwnValues() throws Exception {
    Map<String, String> document = new java.util.LinkedHashMap<>();
    for (int i = 0; i < SYNTAX_KEYS.size(); i++) {
      document.put(SYNTAX_KEYS.get(i), "value-" + i);
    }
    jdbcTemplate.update(
        "INSERT INTO resource (internal_id, resource_id, cancer_study_id, resource_scope,"
            + " patient_id, sample_id, url, display_name, metadata) VALUES (9501, ?, 3, 'PATIENT',"
            + " 'KEYS-1', NULL, 'https://example.com/keys.svs', NULL, ?)",
        EDGE,
        new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(document));
    try {
      for (int i = 0; i < SYNTAX_KEYS.size(); i++) {
        String key = SYNTAX_KEYS.get(i);
        String column = "metadata:" + key;
        String value = "value-" + i;
        assertThat(
                jdbcTemplate.queryForObject(
                    "SELECT JSONExtractString(metadata, ?) FROM resource WHERE internal_id = 9501",
                    String.class,
                    key))
            .isEqualTo(value);

        Q q = q();
        q.resourceId = EDGE;
        q.groupCounts = List.of(column);
        assertThat(groups(q.build()).groups())
            .as("counts on %s", key)
            .singleElement()
            .satisfies(g -> assertThat(g.counts().get(column)).isEqualTo(Map.of(value, 1L)));

        q.filters = List.of(new ResourceColumnFilter(column, "in", List.of(value)));
        assertThat(keys(groups(q.build()))).as("filter on %s", key).containsExactly("KEYS-1");
        q.filters = List.of(new ResourceColumnFilter(column, "in", List.of("other")));
        assertThat(keys(groups(q.build()))).as("other value of %s", key).isEmpty();

        q.sortBy = column;
        q.groupBy = null;
        q.filters = null;
        assertThat(repository.getResourceTableRows(q.build(), context(q.build(), null)))
            .as("sorted on %s", key)
            .hasSize(1);

        for (String search : new String[] {null, "keys.svs"}) {
          Q f = q();
          f.resourceId = EDGE;
          f.search = search;
          assertThat(
                  mapper.getResourceTableGroupedMetadataFacets(
                      f.build(), null, new String[] {key}, 10, 1L << 30, 1L << 29))
              .as("facet on %s, search %s", key, search)
              .singleElement()
              .satisfies(
                  v -> {
                    assertThat(v.metaKey()).isEqualTo(key);
                    assertThat(v.value()).isEqualTo(value);
                    assertThat(v.count()).isEqualTo(1L);
                  });
        }
      }
    } finally {
      jdbcTemplate.execute(
          "ALTER TABLE resource DELETE WHERE internal_id = 9501 SETTINGS mutations_sync = 2");
    }
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void insertedRows_areReadAtOnce_withNoPopulateStep() {
    jdbcTemplate.update(
        "INSERT INTO resource (internal_id, resource_id, cancer_study_id, resource_scope,"
            + " patient_id, sample_id, url, display_name, metadata) VALUES (9101, ?, 3, 'SAMPLE',"
            + " 'GENIE-TEST-307', 'GENIE-TEST-307-01', 'https://example.com/g9101.svs',"
            + " 'Slide 9101', '{\"stain_group\":\"Special\"}')",
        SLIDES);
    try {
      Q q = q();
      q.filters = List.of(new ResourceColumnFilter(STAIN, "in", List.of("Special")));
      ResourceTableQuery query = q.build();
      ResourceTableGroupPage page = groups(query);

      assertThat(keys(page)).containsExactly("GENIE-TEST-307");
      assertThat(page.groups().getFirst().counts().get(STAIN)).isEqualTo(Map.of("Special", 1L));
      assertThat(
              repository.getResourceTableMetadata(query, context(query, null)).facets().get(STAIN))
          .contains(new ResourceFacetOption("Special", 1L));
    } finally {
      jdbcTemplate.execute(
          "ALTER TABLE resource DELETE WHERE internal_id = 9101 SETTINGS mutations_sync = 2");
    }
    assertThat(keys(groups(q().build()))).isEqualTo(ALL_SLIDE_PATIENTS);
  }

  private static final String SLIDE_108 =
      "INSERT INTO resource (internal_id, resource_id, cancer_study_id, resource_scope, patient_id,"
          + " sample_id, url, display_name, metadata) VALUES (108, 'GROUPED_SLIDE', 3, 'SAMPLE',"
          + " 'GENIE-TEST-305', 'GENIE-TEST-305-01', 'https://example.com/g108.svs', 'Slide 108',"
          + " '{\"stain_group\":\"H&E\",\"stain_name\":\"H&E\",\"block\":\"E1\"}')";

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void reimportedRows_readTheirNewMetadata() {
    // Slide 108 reimported with corrected metadata: the importer deletes the row and inserts it
    // again under a new internal_id, here with a lightweight DELETE.
    jdbcTemplate.execute("DELETE FROM resource WHERE internal_id = 108");
    jdbcTemplate.update(
        "INSERT INTO resource (internal_id, resource_id, cancer_study_id, resource_scope,"
            + " patient_id, sample_id, url, display_name, metadata) VALUES (9301, ?, 3, 'SAMPLE',"
            + " 'GENIE-TEST-305', 'GENIE-TEST-305-01', 'https://example.com/g108.svs',"
            + " 'Slide 108', '{\"stain_group\":\"IHC\",\"stain_name\":\"ER\",\"block\":\"E1\"}')",
        SLIDES);
    try {
      ResourceTableGroupPage page = groups(q().build());
      assertThat(page.totalRows()).isEqualTo(8);
      assertThat(group(page, "GENIE-TEST-305").counts().get(STAIN)).isEqualTo(Map.of("IHC", 1L));

      Q heOnly = q();
      heOnly.filters = List.of(new ResourceColumnFilter(STAIN, "in", List.of("H&E")));
      assertThat(keys(groups(heOnly.build()))).doesNotContain("GENIE-TEST-305");
    } finally {
      jdbcTemplate.execute(
          "ALTER TABLE resource DELETE WHERE internal_id = 9301 SETTINGS mutations_sync = 2");
      jdbcTemplate.execute(SLIDE_108);
    }
    assertThat(group(groups(q().build()), "GENIE-TEST-305").counts().get(STAIN))
        .isEqualTo(Map.of("H&E", 1L));
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void deletedRows_leaveTheGroupsAndCounts() {
    // Patient 304's only slide, 107, removed with a mutation.
    jdbcTemplate.execute(
        "ALTER TABLE resource DELETE WHERE internal_id = 107 SETTINGS mutations_sync = 2");
    try {
      Q ihc = q();
      ihc.filters = List.of(new ResourceColumnFilter(STAIN, "in", List.of("IHC")));
      assertThat(keys(groups(ihc.build()))).containsExactly("GENIE-TEST-301");
      ResourceTableQuery query = q().build();
      assertThat(
              repository.getResourceTableMetadata(query, context(query, null)).facets().get(STAIN))
          .containsExactly(new ResourceFacetOption("H&E", 4L), new ResourceFacetOption("IHC", 1L));
    } finally {
      jdbcTemplate.execute(
          "INSERT INTO resource (internal_id, resource_id, cancer_study_id, resource_scope,"
              + " patient_id, sample_id, url, display_name, metadata) VALUES (107,"
              + " 'GROUPED_SLIDE', 3, 'SAMPLE', 'GENIE-TEST-304', 'GENIE-TEST-304-01',"
              + " 'https://example.com/g107.svs', 'Slide 107',"
              + " '{\"stain_group\":\"IHC\",\"stain_name\":\"PD-L1\",\"block\":\"D1\"}')");
    }
    assertThat(keys(groups(q().build()))).isEqualTo(ALL_SLIDE_PATIENTS);
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
  public void aStudyHoldingRowsWithoutADefinition_isGroupedWithTheRest() {
    // acc_tcga declares a CYTOLOGY contract; study_tcga_pub holds a CYTOLOGY row with no
    // definition at all.
    Q q = q();
    q.studyIds = List.of("study_tcga_pub", "acc_tcga");
    q.resourceId = "CYTOLOGY";
    q.groupCounts = null;
    ResourceTableQuery query = q.build();
    ResourceQueryContext context = context(query, null);

    assertThat(context.scope().rows())
        .anyMatch(
            row -> row.studyId().equals("study_tcga_pub") && row.mayHoldRows() && !row.declared());
    assertThat(repository.getResourceTableGroups(query, context).groups())
        .extracting(g -> g.studyId() + ":" + g.patientId())
        .containsExactly("study_tcga_pub:tcga-a1-a0zz", "acc_tcga:tcga-zz-9999");
  }
}
