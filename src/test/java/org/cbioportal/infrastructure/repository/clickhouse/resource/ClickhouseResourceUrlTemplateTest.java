package org.cbioportal.infrastructure.repository.clickhouse.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.cbioportal.domain.resource.ResourceIdentifierFixtures.patients;

import java.util.List;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableRow;
import org.cbioportal.domain.resource.StoredResourceTableRow;
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
 * Each resource row is read with its own definition's url_template, and rows without a URL of their
 * own are served with it expanded. Fixtures: TEMPLATED_SLIDE and TEMPLATED_VIEWER in
 * wsi_test_study, which declare different templates.
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
  @Autowired private GetResourceTableDataUseCase useCase;

  private static ResourceTableQuery query(List<String> studyIds, String resourceId) {
    return new ResourceTableQuery(
        studyIds, resourceId, null, null, null, 0, 100, "displayName", "asc", null);
  }

  private static final String VIEWER = "TEMPLATED_VIEWER";
  private static final String VIEWER_TEMPLATE =
      "https://viewer.example.org/${RESOURCE_ID}/${metadata.slide_key}";

  @Test
  public void eachRowCarriesItsOwnDefinitionsTemplate() {
    assertThat(mapper.getResourceTableRows(query(List.of(STUDY), RESOURCE)))
        .extracting(StoredResourceTableRow::url, StoredResourceTableRow::urlTemplate)
        .containsExactlyInAnyOrder(
            tuple("", TEMPLATE),
            tuple("", TEMPLATE),
            tuple("https://example.com/own-url.svs", TEMPLATE));
  }

  @Test
  public void rowsOfAResourceWithoutATemplateCarryNone() {
    assertThat(mapper.getResourceTableRows(query(List.of(STUDY), "WSI_SAMPLE")))
        .isNotEmpty()
        .extracting(StoredResourceTableRow::urlTemplate)
        .containsOnlyNulls();
  }

  @Test
  public void twoResourcesInOneStudyEachUseTheirOwnTemplate() {
    String base = "https://portal.example.org";

    assertThat(useCase.execute(query(List.of(STUDY), RESOURCE), base))
        .extracting(ResourceTableRow::url)
        .containsExactly(
            "https://example.com/own-url.svs",
            base + "/wsi/patient/WSI-PATIENT?studyId=wsi_test_study&slideKey=c0ffee000002",
            base + "/wsi/patient/WSI-PATIENT?studyId=wsi_test_study&slideKey=c0ffee000001");
    assertThat(useCase.execute(query(List.of(STUDY), VIEWER), base))
        .extracting(ResourceTableRow::url)
        .containsExactly("https://viewer.example.org/TEMPLATED_VIEWER/c0ffee000004");
  }

  @Test
  public void templatedRowsAreNotMultipliedByTheDefinitionJoin() {
    assertThat(mapper.getResourceTableRows(query(List.of(STUDY), RESOURCE))).hasSize(3);
    assertThat(mapper.getResourceTableRows(query(List.of(STUDY), VIEWER)))
        .singleElement()
        .extracting(StoredResourceTableRow::urlTemplate)
        .isEqualTo(VIEWER_TEMPLATE);
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
