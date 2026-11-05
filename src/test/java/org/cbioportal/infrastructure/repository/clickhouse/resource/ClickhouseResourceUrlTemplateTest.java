package org.cbioportal.infrastructure.repository.clickhouse.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.cbioportal.domain.resource.ResourceIdentifierFixtures.patients;

import java.util.List;
import java.util.Map;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableRow;
import org.cbioportal.domain.resource.ResourceUrlTemplateRow;
import org.cbioportal.domain.resource.usecase.GetResourceTableDataUseCase;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * resource_definition.url_template is read for the studies a request names, and rows without a URL
 * of their own are served with it expanded. Fixtures: TEMPLATED_SLIDE in wsi_test_study.
 */
@RunWith(SpringRunner.class)
@Import({
  MyBatisConfig.class,
  ClickhouseResourceDataRepository.class,
  GetResourceTableDataUseCase.class
})
@JdbcTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhouseResourceUrlTemplateTest {

  private static final String STUDY = "wsi_test_study";
  private static final String RESOURCE = "TEMPLATED_SLIDE";
  private static final String TEMPLATE =
      "${base_url}/wsi/patient/${PATIENT_ID}?studyId=${STUDY_ID}&slideKey=${metadata.slide_key}";

  @Autowired private ClickhouseResourceDataMapper mapper;
  @Autowired private ClickhouseResourceDataRepository repository;
  @Autowired private GetResourceTableDataUseCase useCase;

  private static ResourceTableQuery query(List<String> studyIds, String resourceId) {
    return new ResourceTableQuery(
        studyIds, resourceId, null, null, null, 0, 100, "displayName", "asc", null);
  }

  @Test
  public void mapperReadsTheTemplateColumn() {
    assertThat(mapper.getResourceUrlTemplates(query(List.of(STUDY), RESOURCE)))
        .containsExactly(new ResourceUrlTemplateRow(STUDY, TEMPLATE));
  }

  @Test
  public void mapperReturnsNothingForAResourceWithoutATemplate() {
    assertThat(mapper.getResourceUrlTemplates(query(List.of(STUDY), "WSI_SAMPLE"))).isEmpty();
  }

  @Test
  public void mapperOnlyReadsTheRequestedStudies() {
    assertThat(mapper.getResourceUrlTemplates(query(List.of("study_tcga_pub"), RESOURCE)))
        .isEmpty();
  }

  @Test
  public void repositoryKeysTemplatesByStudy() {
    assertThat(
            repository.getResourceUrlTemplates(query(List.of(STUDY, "study_tcga_pub"), RESOURCE)))
        .containsExactly(Map.entry(STUDY, TEMPLATE));
  }

  @Test
  public void templatedRowsAreStoredWithoutAUrl() {
    assertThat(repository.getResourceTableRows(query(List.of(STUDY), RESOURCE)))
        .extracting(ResourceTableRow::url)
        .containsExactlyInAnyOrder("", "", "https://example.com/own-url.svs");
  }

  @Test
  public void rowsAreServedWithTheTemplateExpanded() {
    List<ResourceTableRow> rows =
        useCase.execute(
            new ResourceTableQuery(
                List.of(STUDY),
                RESOURCE,
                patients(STUDY, "WSI-PATIENT"),
                List.of(),
                null,
                0,
                100,
                "displayName",
                "asc",
                null),
            "https://portal.example.org/cbioportal");

    assertThat(rows)
        .extracting(ResourceTableRow::displayName, ResourceTableRow::url)
        .containsExactly(
            tuple("Slide with its own URL", "https://example.com/own-url.svs"),
            tuple(
                "Templated patient slide",
                "https://portal.example.org/cbioportal/wsi/patient/WSI-PATIENT"
                    + "?studyId=wsi_test_study&slideKey=c0ffee000002"),
            tuple(
                "Templated sample slide",
                "https://portal.example.org/cbioportal/wsi/patient/WSI-PATIENT"
                    + "?studyId=wsi_test_study&slideKey=c0ffee000001"));
  }
}
