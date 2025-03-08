package org.cbioportal.cancerstudy.usecase;

import org.cbioportal.domain.cancerstudy.repository.CancerStudyRepository;
import org.cbioportal.domain.cancerstudy.usecase.SearchCancerStudiesMetadataUseCase;
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
public class SearchCancerStudiesMetadataUseCaseTest {

    @InjectMocks
    private SearchCancerStudiesMetadataUseCase searchCancerStudiesMetadataUseCase;

    @Mock
    CancerStudyRepository cancerStudyRepository;

    @Test
    public void testExecuteWithProjectionTypeSummary() {
        searchCancerStudiesMetadataUseCase.execute(ProjectionType.SUMMARY, new SortAndSearchCriteria("","",""));
        verify(cancerStudyRepository).getCancerStudiesMetadataSummary(any(SortAndSearchCriteria.class));
    }
    @Test
    public void testExecuteWithProjectionTypeDetailed() {
        searchCancerStudiesMetadataUseCase.execute(ProjectionType.DETAILED, new SortAndSearchCriteria("","", ""));
        verify(cancerStudyRepository).getCancerStudyMetadata(any(SortAndSearchCriteria.class));
    }
    @Test
    public void testExecuteWithProjectionTypeDefault() {
        Assert.assertTrue(searchCancerStudiesMetadataUseCase.execute(ProjectionType.META, new SortAndSearchCriteria("","",""
                )).isEmpty());
    }
}