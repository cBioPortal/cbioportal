package org.cbioportal.cancerstudy.usecase;

import org.cbioportal.cancerstudy.repository.CancerStudyRepository;
import org.cbioportal.shared.enums.ProjectionType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class GetCancerStudyMetadataUseCaseTest {

    @InjectMocks
    private GetCancerStudyMetadataUseCase getCancerStudyMetadataUseCase;

    @Mock
    CancerStudyRepository cancerStudyRepository;

    @Test
    public void testExecuteWithProjectionTypeSummary() {
        getCancerStudyMetadataUseCase.execute(ProjectionType.SUMMARY);
        verify(cancerStudyRepository).getCancerStudiesMetadataSummary();
    }
    @Test
    public void testExecuteWithProjectionTypeDetailed() {
        getCancerStudyMetadataUseCase.execute(ProjectionType.DETAILED);
        verify(cancerStudyRepository).getCancerStudiesMetadata();
    }
    @Test
    public void testExecuteWithProjectionTypeDefault() {
        Assert.assertTrue(getCancerStudyMetadataUseCase.execute(ProjectionType.META).isEmpty());
    }
}