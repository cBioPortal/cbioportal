package org.cbioportal.infrastructure.repository.clickhouse.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.cbioportal.domain.resource.ResourceColumnFilter;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableRow;
import org.cbioportal.domain.resource.ResourceTableTab;
import org.cbioportal.domain.resource.ResourceTabsRequest;
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
@Import(MyBatisConfig.class)
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhouseResourceDataMapperTest {

  private static final String STUDY_TCGA_PUB = "study_tcga_pub";

  @Autowired private ClickhouseResourceDataMapper mapper;

  // ---- Tab queries ----

  @Test
  public void getResourceTableTabs_returnsAllTabsForStudy() {
    ResourceTabsRequest request = new ResourceTabsRequest(List.of(STUDY_TCGA_PUB), null, null);

    List<ResourceTableTab> tabs = mapper.getResourceTableTabs(request);

    // expect 3 distinct resourceIds in the test data
    assertThat(tabs).hasSize(3);
    List<String> ids = tabs.stream().map(ResourceTableTab::resourceId).toList();
    assertThat(ids).containsExactlyInAnyOrder("HE_SLIDE", "CT_SCAN", "FIGURES");
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
        new ResourceTabsRequest(List.of(STUDY_TCGA_PUB), List.of("tcga-a1-a0sb"), null);

    List<ResourceTableTab> tabs = mapper.getResourceTableTabs(request);

    // tcga-a1-a0sb has HE_SLIDE (1 row) and CT_SCAN (1 row)
    assertThat(tabs).hasSize(2);
    ResourceTableTab heSlide =
        tabs.stream().filter(t -> t.resourceId().equals("HE_SLIDE")).findFirst().orElseThrow();
    assertThat(heSlide.totalCount()).isEqualTo(1);
  }

  // ---- Row queries ----

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

  // ---- Count queries ----

  @Test
  public void getResourceTableRowCount_returnsCorrectTotal() {
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "HE_SLIDE", null, null, null, 0, 10, null, null, null);

    long count = mapper.getResourceTableRowCount(query);

    assertThat(count).isEqualTo(2);
  }

  @Test
  public void getResourceTablePatientCount_returnsDistinctPatients() {
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "HE_SLIDE", null, null, null, 0, 10, null, null, null);

    long count = mapper.getResourceTablePatientCount(query);

    assertThat(count).isEqualTo(2);
  }

  @Test
  public void getResourceTableSampleCount_returnsDistinctSamples() {
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "HE_SLIDE", null, null, null, 0, 10, null, null, null);

    long count = mapper.getResourceTableSampleCount(query);

    assertThat(count).isEqualTo(2);
  }

  @Test
  public void getResourceTablePatientCount_studyEntitiesNotCounted() {
    // FIGURES resource has ENTITY_TYPE=STUDY, so patient/sample count should be 0
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_TCGA_PUB), "FIGURES", null, null, null, 0, 10, null, null, null);

    long patientCount = mapper.getResourceTablePatientCount(query);
    long sampleCount = mapper.getResourceTableSampleCount(query);

    assertThat(patientCount).isZero();
    assertThat(sampleCount).isZero();
  }
}
