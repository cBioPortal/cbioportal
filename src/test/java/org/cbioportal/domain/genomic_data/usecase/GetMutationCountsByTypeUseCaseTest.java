package org.cbioportal.domain.genomic_data.usecase;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.cbioportal.domain.genomic_data.repository.GenomicDataRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.GenomicDataCountItem;
import org.cbioportal.legacy.web.parameter.GenomicDataFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetMutationCountsByTypeUseCaseTest {

  @InjectMocks private GetMutationCountsByTypeUseCase useCase;

  @Mock private GenomicDataRepository repository;

  @Mock private StudyViewFilterContext studyViewFilterContext;

  @Test
  public void execute_includeSampleIdsFalse_callsRepository() {

    List<GenomicDataFilter> filters = List.of(new GenomicDataFilter("AKT1", "mutation"));

    when(repository.getMutationCountsByType(any(), any(), anyBoolean(), any()))
        .thenReturn(Collections.emptyList());

    List<GenomicDataCountItem> result = useCase.execute(studyViewFilterContext, filters, false);

    verify(repository).getMutationCountsByType(studyViewFilterContext, filters, false, null);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void execute_includeSampleIdsTrue_singleGene_callsRepositoryWithHugo() {
    GenomicDataFilter filter = new GenomicDataFilter("AKT1", "mutation");
    List<GenomicDataFilter> filters = List.of(filter);

    when(repository.getMutationCountsByType(any(), any(), anyBoolean(), any()))
        .thenReturn(Collections.emptyList());

    List<GenomicDataCountItem> result = useCase.execute(studyViewFilterContext, filters, true);

    verify(repository).getMutationCountsByType(studyViewFilterContext, filters, true, "AKT1");

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test(expected = IllegalArgumentException.class)
  public void execute_includeSampleIdsTrue_nullFilters_throwsException() {
    useCase.execute(studyViewFilterContext, null, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void execute_includeSampleIdsTrue_multipleGenes_throwsException() {
    List<GenomicDataFilter> filters =
        List.of(
            new GenomicDataFilter("AKT1", "mutation"), new GenomicDataFilter("TP53", "mutation"));

    useCase.execute(studyViewFilterContext, filters, true);
  }
}
