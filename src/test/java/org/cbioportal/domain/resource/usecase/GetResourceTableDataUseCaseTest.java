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
    List<ResourceTableRow> rows = List.of();
    when(resourceDataRepository.getResourceTableRows(query)).thenReturn(rows);

    assertThat(useCase.execute(query, "https://portal.example.org")).isEqualTo(rows);

    // The rows endpoint must not pay for the page-invariant work; that is the whole point of
    // splitting them.
    verify(resourceDataRepository, never()).getResourceTableMetadata(query);
    verify(resourceDataRepository, never()).getResourceTableCounts(query);
  }

  private static final String TEMPLATE =
      "${base_url}/wsi/patient/${PATIENT_ID}?studyId=${STUDY_ID}&slideKey=${metadata.slide_key}";

  private static ResourceTableRow slide(String studyId, String patientId, String url, String key) {
    return new ResourceTableRow(
        studyId,
        "WSI_SAMPLE",
        "Slides",
        "SAMPLE",
        patientId,
        patientId + "-T01",
        url,
        null,
        Map.of("slide_key", key));
  }

  private static ResourceTableQuery slideQuery(List<String> studyIds) {
    return new ResourceTableQuery(
        studyIds, "WSI_SAMPLE", null, null, null, 0, 10, null, null, null);
  }

  @Test
  public void execute_expandsTheTemplateForRowsWithoutAUrl() {
    ResourceTableQuery query = slideQuery(List.of("mskimpact", "other_study"));
    when(resourceDataRepository.getResourceTableRows(query))
        .thenReturn(
            List.of(
                slide("mskimpact", "P-1", "", "k1"),
                slide("mskimpact", "P-2", "https://example.com/own.svs", "k2"),
                slide("other_study", "P 3", null, "k3")));
    when(resourceDataRepository.getResourceUrlTemplates(query))
        .thenReturn(Map.of("mskimpact", TEMPLATE));

    List<ResourceTableRow> rows = useCase.execute(query, "https://portal.example.org/cbio");

    assertThat(rows)
        .extracting(ResourceTableRow::url)
        .containsExactly(
            "https://portal.example.org/cbio/wsi/patient/P-1?studyId=mskimpact&slideKey=k1",
            "https://example.com/own.svs",
            // other_study declares no template, so its row is left as it is.
            null);
    assertThat(rows.get(0).metadata()).containsEntry("slide_key", "k1");
    assertThat(rows.get(0).sampleId()).isEqualTo("P-1-T01");
  }

  @Test
  public void execute_skipsTheTemplateLookupWhenEveryRowHasAUrl() {
    ResourceTableQuery query = slideQuery(List.of("mskimpact"));
    List<ResourceTableRow> rows = List.of(slide("mskimpact", "P-1", "https://example.com/a", "k"));
    when(resourceDataRepository.getResourceTableRows(query)).thenReturn(rows);

    assertThat(useCase.execute(query, "https://portal.example.org")).isEqualTo(rows);
    verify(resourceDataRepository, never()).getResourceUrlTemplates(query);
  }
}
