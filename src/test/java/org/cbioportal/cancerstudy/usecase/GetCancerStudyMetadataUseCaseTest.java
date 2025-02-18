package org.cbioportal.cancerstudy.usecase;

import org.cbioportal.cancerstudy.repository.CancerStudyRepository;
import org.cbioportal.shared.SortAndSearchCriteria;
import org.cbioportal.shared.enums.ProjectionType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class GetCancerStudyMetadataUseCaseTest {

    @InjectMocks
    private GetCancerStudyMetadataUseCase getCancerStudyMetadataUseCase;

    @Mock
    CancerStudyRepository cancerStudyRepository;

    @Test
    public void testExecuteWithProjectionTypeSummary() {
        getCancerStudyMetadataUseCase.execute(ProjectionType.SUMMARY, new SortAndSearchCriteria("","",""));
        verify(cancerStudyRepository).getCancerStudiesMetadataSummary(any(SortAndSearchCriteria.class));
    }
    @Test
    public void testExecuteWithProjectionTypeDetailed() {
        getCancerStudyMetadataUseCase.execute(ProjectionType.DETAILED, new SortAndSearchCriteria("","", ""));
        verify(cancerStudyRepository).getCancerStudiesMetadata(any(SortAndSearchCriteria.class));
    }
    @Test
    public void testExecuteWithProjectionTypeDefault() {
        Assert.assertTrue(getCancerStudyMetadataUseCase.execute(ProjectionType.META, new SortAndSearchCriteria("","",""
                )).isEmpty());
    }
}