package org.cbioportal.domain.resource.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.cbioportal.domain.resource.ResourceFacetOption;
import org.cbioportal.domain.resource.ResourceNumericRange;
import org.cbioportal.domain.resource.ResourceTableCounts;
import org.cbioportal.domain.resource.ResourceTableMetadataView;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableResult;
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

    when(resourceDataRepository.getResourceTableRows(query)).thenReturn(rows);
    when(resourceDataRepository.getResourceTableCounts(query))
        .thenReturn(new ResourceTableCounts(2L, 1L, 2L, 1L, 1L, 5L));
    when(resourceDataRepository.getResourceTableMetadata(query))
        .thenReturn(new ResourceTableMetadataView(List.of(), facets, facetRanges));

    ResourceTableResult result = useCase.execute(query);

    assertThat(result.facets()).isEqualTo(facets);
    assertThat(result.facetRanges()).isEqualTo(facetRanges);
    assertThat(result.totalRowCount()).isEqualTo(2L);
    assertThat(result.filteredPatientCount()).isEqualTo(1L);
    assertThat(result.filteredSampleCount()).isEqualTo(2L);
  }
}
