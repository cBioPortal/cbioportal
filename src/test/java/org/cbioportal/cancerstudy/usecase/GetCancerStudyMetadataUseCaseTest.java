package org.cbioportal.cancerstudy.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.cbioportal.domain.cancerstudy.repository.CancerStudyRepository;
import org.cbioportal.domain.cancerstudy.usecase.GetCancerStudyMetadataUseCase;
import org.cbioportal.shared.SortAndSearchCriteria;
import org.cbioportal.shared.enums.ProjectionType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetCancerStudyMetadataUseCaseTest {

  @InjectMocks private GetCancerStudyMetadataUseCase getCancerStudyMetadataUseCase;

  @Mock CancerStudyRepository cancerStudyRepository;

  @Test
  public void testExecuteWithProjectionTypeSummary() {
    getCancerStudyMetadataUseCase.execute(
        ProjectionType.SUMMARY, new SortAndSearchCriteria("", "", "", null, null));
    verify(cancerStudyRepository).getCancerStudiesMetadataSummary(any(SortAndSearchCriteria.class));
  }

  @Test
  public void testExecuteWithProjectionTypeDetailed() {
    getCancerStudyMetadataUseCase.execute(
        ProjectionType.DETAILED, new SortAndSearchCriteria("", "", "", null, null));
    verify(cancerStudyRepository).getCancerStudiesMetadata(any(SortAndSearchCriteria.class));
  }

  @Test
  public void testExecuteWithProjectionTypeDefault() {
    Assert.assertTrue(
        getCancerStudyMetadataUseCase
            .execute(ProjectionType.META, new SortAndSearchCriteria("", "", "", null, null))
            .isEmpty());
  }
}
