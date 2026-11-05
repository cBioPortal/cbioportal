package org.cbioportal.legacy.persistence.mybatis;

import java.util.Comparator;
import java.util.List;
import org.cbioportal.legacy.AbstractLegacyTestcontainers;
import org.cbioportal.legacy.model.ResourceData;
import org.cbioportal.legacy.model.ResourceDefinition;
import org.cbioportal.legacy.model.ResourceType;
import org.cbioportal.legacy.persistence.config.MyBatisLegacyConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@Import({MyBatisLegacyConfig.class, ResourceDataMyBatisRepository.class})
@JdbcTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractLegacyTestcontainers.Initializer.class)
public class ResourceDataMyBatisRepositoryTest {

  @Autowired private ResourceDataMyBatisRepository resourceDataMyBatisRepository;

  @Test
  public void getAllResourceDataOfSampleInStudyIdProjection() throws Exception {

    List<ResourceData> result =
        resourceDataMyBatisRepository.getAllResourceDataOfSampleInStudy(
            "study_tcga_pub", null, null, "ID", null, null, null, null);
    result = sortedResult(result);

    Assert.assertEquals(7, result.size());
    ResourceData resourceData = result.get(0);
    Assert.assertEquals("TCGA-A1-A0SB-01", resourceData.getSampleId());
    Assert.assertEquals("TCGA-A1-A0SB", resourceData.getPatientId());
    Assert.assertEquals("HE", resourceData.getResourceId());
    Assert.assertNull(resourceData.getUrl());
    Assert.assertNull(resourceData.getResourceDefinition());
  }

  @Test
  public void getAllResourceDataOfSampleInStudySummaryProjection() throws Exception {

    List<ResourceData> result =
        resourceDataMyBatisRepository.getAllResourceDataOfSampleInStudy(
            "study_tcga_pub", null, null, "SUMMARY", null, null, null, null);
    result = sortedResult(result);

    Assert.assertEquals(7, result.size());
    ResourceData resourceData = result.get(0);
    Assert.assertEquals("TCGA-A1-A0SB-01", resourceData.getSampleId());
    Assert.assertEquals("TCGA-A1-A0SB", resourceData.getPatientId());
    Assert.assertEquals("HE", resourceData.getResourceId());
    Assert.assertEquals(
        "https://upload.wikimedia.org/wikipedia/commons/8/80/Breast_DCIS_histopathology_%281%29.jpg",
        resourceData.getUrl());
    Assert.assertNull(resourceData.getResourceDefinition());
  }

  @Test
  public void getAllResourceDataOfSampleInStudyDetailedProjection() throws Exception {

    List<ResourceData> result =
        resourceDataMyBatisRepository.getAllResourceDataOfSampleInStudy(
            "study_tcga_pub", null, null, "DETAILED", null, null, null, null);
    result = sortedResult(result);

    Assert.assertEquals(7, result.size());
    ResourceData resourceData = result.get(0);
    Assert.assertEquals("TCGA-A1-A0SB-01", resourceData.getSampleId());
    Assert.assertEquals("TCGA-A1-A0SB", resourceData.getPatientId());
    Assert.assertEquals("HE", resourceData.getResourceId());
    Assert.assertEquals(
        "https://upload.wikimedia.org/wikipedia/commons/8/80/Breast_DCIS_histopathology_%281%29.jpg",
        resourceData.getUrl());
    ResourceDefinition resourceDefinition = resourceData.getResourceDefinition();
    Assert.assertEquals("H&E Slide", resourceDefinition.getDisplayName());
    Assert.assertEquals("H&E Slide", resourceDefinition.getDescription());
    ResourceType resourceType = resourceDefinition.getResourceType();
    Assert.assertEquals(ResourceType.SAMPLE, resourceType);
    Assert.assertEquals(true, resourceDefinition.getOpenByDefault());
    Assert.assertEquals("1", resourceDefinition.getPriority());
    Assert.assertEquals("study_tcga_pub", resourceDefinition.getCancerStudyIdentifier());
  }

  @Test
  public void getAllResourceDataOfSampleInStudySummaryProjection1PageSize() throws Exception {

    List<ResourceData> result =
        resourceDataMyBatisRepository.getAllResourceDataOfSampleInStudy(
            "study_tcga_pub", null, null, "SUMMARY", 1, 0, null, null);

    Assert.assertEquals(1, result.size());
  }

  @Test
  public void getAllResourceDataOfSampleInStudySummaryProjectionSampleIdSort() throws Exception {

    List<ResourceData> result =
        resourceDataMyBatisRepository.getAllResourceDataOfSampleInStudy(
            "study_tcga_pub", null, null, "SUMMARY", null, null, "sampleId", "DESC");

    Assert.assertEquals(7, result.size());
    Assert.assertEquals("TCGA-A1-A0SH-01", result.get(0).getSampleId());
    Assert.assertEquals("TCGA-A1-A0SG-01", result.get(1).getSampleId());
    Assert.assertEquals("TCGA-A1-A0SF-01", result.get(2).getSampleId());
    Assert.assertEquals("TCGA-A1-A0SE-01", result.get(3).getSampleId());
    Assert.assertEquals("TCGA-A1-A0SD-01", result.get(4).getSampleId());
    Assert.assertEquals("TCGA-A1-A0SB-02", result.get(5).getSampleId());
    Assert.assertEquals("TCGA-A1-A0SB-01", result.get(6).getSampleId());
  }

  @Test
  public void getAllResouceDataOfPatientInStudy() throws Exception {

    List<ResourceData> result =
        resourceDataMyBatisRepository.getAllResourceDataOfPatientInStudy(
            "study_tcga_pub", "TCGA-A1-A0SB", null, "DETAILED", null, null, null, null);
    result = sortedResult(result);

    Assert.assertEquals(1, result.size());
    ResourceData resourceData = result.get(0);
    Assert.assertEquals("TCGA-A1-A0SB", resourceData.getPatientId());
    Assert.assertEquals("IDC_OHIF_V2", resourceData.getResourceId());
    Assert.assertEquals(
        "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ea/SADDLE_PE.JPG/721px-SADDLE_PE.JPG",
        resourceData.getUrl());
    ResourceDefinition resourceDefinition = resourceData.getResourceDefinition();
    Assert.assertEquals("CT Scan", resourceDefinition.getDisplayName());
    Assert.assertEquals("CT Scan", resourceDefinition.getDescription());
    ResourceType resourceType = resourceDefinition.getResourceType();
    Assert.assertEquals(ResourceType.PATIENT, resourceType);
    Assert.assertEquals(true, resourceDefinition.getOpenByDefault());
    Assert.assertEquals("1", resourceDefinition.getPriority());
    Assert.assertEquals("study_tcga_pub", resourceDefinition.getCancerStudyIdentifier());
  }

  @Test
  public void getAllResourceDataForStudy() throws Exception {

    List<ResourceData> result =
        resourceDataMyBatisRepository.getAllResourceDataForStudy(
            "acc_tcga", null, "DETAILED", null, null, null, null);
    result = sortedResult(result);

    Assert.assertEquals(1, result.size());
    ResourceData resourceData = result.get(0);
    Assert.assertEquals("FIGURES", resourceData.getResourceId());
    Assert.assertEquals(
        "https://upload.wikimedia.org/wikipedia/commons/thumb/5/56/Tumor_Mesothelioma2_legend.jpg/220px-Tumor_Mesothelioma2_legend.jpg",
        resourceData.getUrl());
    ResourceDefinition resourceDefinition = resourceData.getResourceDefinition();
    Assert.assertEquals("Figures", resourceDefinition.getDisplayName());
    Assert.assertEquals("Figures", resourceDefinition.getDescription());
    ResourceType resourceType = resourceDefinition.getResourceType();
    Assert.assertEquals(ResourceType.STUDY, resourceType);
    Assert.assertEquals(true, resourceDefinition.getOpenByDefault());
    Assert.assertEquals("1", resourceDefinition.getPriority());
    Assert.assertEquals("acc_tcga", resourceDefinition.getCancerStudyIdentifier());
  }

  private static final String SLIDE_TEMPLATE =
      "${base_url}/wsi/patient/${PATIENT_ID}?studyId=${STUDY_ID}&slideKey=${metadata.slide_key}";

  @Test
  public void templatedSampleRowsCarryTheTemplateAndMetadata() throws Exception {

    List<ResourceData> result =
        sortedResult(
            resourceDataMyBatisRepository.getAllResourceDataOfSampleInStudy(
                "acc_tcga", null, "SLIDE", "SUMMARY", null, null, null, null));

    Assert.assertEquals(2, result.size());
    ResourceData ownUrl = result.get(0);
    Assert.assertEquals("TCGA-A1-A0SB-01", ownUrl.getSampleId());
    Assert.assertEquals("https://example.com/own-url.svs", ownUrl.getUrl());
    Assert.assertEquals(SLIDE_TEMPLATE, ownUrl.getUrlTemplate());
    // Metadata is only read for rows that need it to expand their URL.
    Assert.assertNull(ownUrl.getMetadata());

    ResourceData templated = result.get(1);
    Assert.assertEquals("TCGA-A1-B0SQ-01", templated.getSampleId());
    Assert.assertEquals("TCGA-A1-B0SQ", templated.getPatientId());
    Assert.assertEquals("", templated.getUrl());
    Assert.assertEquals(SLIDE_TEMPLATE, templated.getUrlTemplate());
    Assert.assertEquals("{\"slide_key\":\"c0ffee000001\"}", templated.getMetadata());
  }

  @Test
  public void templatedPatientRowsCarryTheTemplateAndMetadata() throws Exception {

    List<ResourceData> result =
        resourceDataMyBatisRepository.getAllResourceDataOfPatientInStudy(
            "acc_tcga", "TCGA-A1-B0SP", "SLIDE", "DETAILED", null, null, null, null);

    Assert.assertEquals(1, result.size());
    ResourceData templated = result.get(0);
    Assert.assertEquals("", templated.getUrl());
    Assert.assertEquals(SLIDE_TEMPLATE, templated.getUrlTemplate());
    Assert.assertEquals("{\"slide_key\":\"c0ffee000002\"}", templated.getMetadata());
    Assert.assertEquals(SLIDE_TEMPLATE, templated.getResourceDefinition().getUrlTemplate());
  }

  @Test
  public void rowsOfAResourceWithoutATemplateCarryNone() throws Exception {

    List<ResourceData> result =
        resourceDataMyBatisRepository.getAllResourceDataOfSampleInStudy(
            "study_tcga_pub", null, null, "SUMMARY", null, null, null, null);

    Assert.assertFalse(result.isEmpty());
    for (ResourceData row : result) {
      Assert.assertNull(row.getUrlTemplate());
      Assert.assertNull(row.getMetadata());
    }
  }

  private List<ResourceData> sortedResult(List<ResourceData> result) {
    return result.stream()
        .sorted(
            Comparator.comparing(
                rd ->
                    rd.getResourceId()
                        + "_"
                        + rd.getStudyId()
                        + "_"
                        + rd.getPatientId()
                        + "_"
                        + rd.getSampleId()))
        .toList();
  }
}
