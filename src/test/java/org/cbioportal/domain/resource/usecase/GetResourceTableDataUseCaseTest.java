package org.cbioportal.domain.resource.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.cbioportal.domain.resource.ResourceFacetOption;
import org.cbioportal.domain.resource.ResourceNumericRange;
import org.cbioportal.domain.resource.ResourceTableCounts;
import org.cbioportal.domain.resource.ResourceTableMetadataResult;
import org.cbioportal.domain.resource.ResourceTableMetadataView;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableRow;
import org.cbioportal.domain.resource.StoredResourceTableRow;
import org.cbioportal.domain.resource.repository.ResourceDataRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetResourceTableDataUseCaseTest {

  @Mock private ResourceDataRepository resourceDataRepository;

  @InjectMocks private GetResourceTableDataUseCase useCase;
  @InjectMocks private GetResourceTableMetadataUseCase metadataUseCase;

  @Test
  public void execute_returnsComputedFacets() {
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of("study_tcga_pub"), "HE_SLIDE", null, null, null, 0, 10, null, null, null);
    List<ResourceTableRow> rows = List.of();
    Map<String, List<ResourceFacetOption>> facets =
        Map.of(
            "patientId", List.of(new ResourceFacetOption("tcga-a1-a0sb", 1L)),
            "type", List.of(new ResourceFacetOption("IMAGE", 2L)));
    Map<String, ResourceNumericRange> facetRanges =
        Map.of("metadata:pages", new ResourceNumericRange(1.0, 10.0));

    when(resourceDataRepository.getResourceTableCounts(query))
        .thenReturn(new ResourceTableCounts(2L, 1L, 2L, 1L, 1L, 5L));
    when(resourceDataRepository.getResourceTableMetadata(query))
        .thenReturn(new ResourceTableMetadataView(List.of(), facets, facetRanges));

    ResourceTableMetadataResult result = metadataUseCase.execute(query);

    assertThat(result.facets()).isEqualTo(facets);
    assertThat(result.facetRanges()).isEqualTo(facetRanges);
    assertThat(result.totalRowCount()).isEqualTo(2L);
    assertThat(result.filteredPatientCount()).isEqualTo(1L);
    assertThat(result.filteredSampleCount()).isEqualTo(2L);
  }

  @Test
  public void execute_returnsOnlyTheRequestedPage() {
    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of("study_tcga_pub"), "HE_SLIDE", null, null, null, 0, 10, null, null, null);
    when(resourceDataRepository.getResourceTableRows(query)).thenReturn(List.of());

    assertThat(useCase.execute(query, "https://portal.example.org")).isEmpty();

    // The rows endpoint must not pay for the page-invariant work; that is the whole point of
    // splitting them.
    verify(resourceDataRepository, never()).getResourceTableMetadata(query);
    verify(resourceDataRepository, never()).getResourceTableCounts(query);
  }

  private static final String TEMPLATE =
      "${base_url}/wsi/patient/${PATIENT_ID}?studyId=${STUDY_ID}&slideKey=${metadata.slide_key}";

  private static StoredResourceTableRow slide(
      String studyId, String patientId, String url, String key, String template) {
    return new StoredResourceTableRow(
        studyId,
        "WSI_SAMPLE",
        "Slides",
        "SAMPLE",
        patientId,
        patientId + "-T01",
        url,
        null,
        Map.of("slide_key", key),
        template);
  }

  private static ResourceTableQuery slideQuery(List<String> studyIds) {
    return new ResourceTableQuery(
        studyIds, "WSI_SAMPLE", null, null, null, 0, 10, null, null, null);
  }

  @Test
  public void execute_expandsEachRowsOwnTemplateForRowsWithoutAUrl() {
    ResourceTableQuery query = slideQuery(List.of("mskimpact", "other_study"));
    when(resourceDataRepository.getResourceTableRows(query))
        .thenReturn(
            List.of(
                slide("mskimpact", "P-1", "", "k1", TEMPLATE),
                slide("mskimpact", "P-2", "https://example.com/own.svs", "k2", TEMPLATE),
                slide("other_study", "P 3", null, "k3", null)));

    List<ResourceTableRow> rows = useCase.execute(query, "https://portal.example.org/cbio");

    assertThat(rows)
        .extracting(ResourceTableRow::url)
        .containsExactly(
            "https://portal.example.org/cbio/wsi/patient/P-1?studyId=mskimpact&slideKey=k1",
            "https://example.com/own.svs",
            // other_study's definition has no template, so its row is left as it is.
            null);
    assertThat(rows.get(0).metadata()).containsEntry("slide_key", "k1");
    assertThat(rows.get(0).sampleId()).isEqualTo("P-1-T01");
  }

  @Test
  public void execute_usesTheTemplateEachRowCarries() {
    // Rows of two resources (or of one resource in two studies) carry different templates; each
    // row is expanded with its own, not with one template chosen for the whole page.
    ResourceTableQuery query = slideQuery(List.of("mskimpact"));
    when(resourceDataRepository.getResourceTableRows(query))
        .thenReturn(
            List.of(
                slide("mskimpact", "P-1", "", "k1", TEMPLATE),
                slide(
                    "mskimpact",
                    "P-2",
                    "",
                    "k2",
                    "https://viewer.example.org/${metadata.slide_key}")));

    assertThat(useCase.execute(query, "https://portal.example.org"))
        .extracting(ResourceTableRow::url)
        .containsExactly(
            "https://portal.example.org/wsi/patient/P-1?studyId=mskimpact&slideKey=k1",
            "https://viewer.example.org/k2");
  }

  @Test
  public void execute_ignoresATemplateWithoutAWebScheme() {
    ResourceTableQuery query = slideQuery(List.of("mskimpact"));
    when(resourceDataRepository.getResourceTableRows(query))
        .thenReturn(List.of(slide("mskimpact", "P-1", "", "k1", "javascript:alert(1)")));

    assertThat(useCase.execute(query, "https://portal.example.org"))
        .extracting(ResourceTableRow::url)
        .containsExactly("");
  }

  @Test
  public void execute_servesARowsOwnUrlUnchanged() {
    ResourceTableQuery query = slideQuery(List.of("mskimpact"));
    StoredResourceTableRow stored =
        slide("mskimpact", "P-1", "https://example.com/a", "k", TEMPLATE);
    when(resourceDataRepository.getResourceTableRows(query)).thenReturn(List.of(stored));

    assertThat(useCase.execute(query, "https://portal.example.org"))
        .containsExactly(stored.toRow("https://example.com/a"));
  }
}
