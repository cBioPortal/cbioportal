package org.cbioportal.legacy.persistence.cachemaputil;

import java.util.List;
import java.util.Map;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.persistence.StudyRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CacheMapBuilderTest {

  @InjectMocks private CacheMapBuilder cacheMapBuilder;

  @Mock private StudyRepository studyRepository;

  @Test
  public void buildCancerStudyPermissionMapUsesMinimalPermissionsProjection() {
    CancerStudy study1 = new CancerStudy();
    study1.setCancerStudyId(1);
    study1.setCancerStudyIdentifier("study_tcga_pub");
    study1.setGroups("SU2C-PI3K;PUBLIC;GDAC");

    CancerStudy study2 = new CancerStudy();
    study2.setCancerStudyId(2);
    study2.setCancerStudyIdentifier("acc_tcga");
    study2.setGroups("SU2C-PI3K;PUBLIC;GDAC");

    Mockito.when(studyRepository.getStudyPermissions()).thenReturn(List.of(study1, study2));

    Map<String, CancerStudy> result = cacheMapBuilder.buildCancerStudyPermissionMap();

    Assert.assertEquals(2, result.size());
    Assert.assertSame(study1, result.get("study_tcga_pub"));
    Assert.assertSame(study2, result.get("acc_tcga"));
    // buildCancerStudyPermissionMap must use the cheap, join-free projection, not the expensive
    // getAllStudies() call that joins sample lists for every study.
    Mockito.verify(studyRepository).getStudyPermissions();
    Mockito.verify(studyRepository, Mockito.never())
        .getAllStudies(
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any());
  }
}
