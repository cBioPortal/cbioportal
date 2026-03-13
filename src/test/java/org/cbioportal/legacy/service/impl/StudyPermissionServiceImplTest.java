package org.cbioportal.legacy.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.application.security.CancerStudyPermissionEvaluator;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class StudyPermissionServiceImplTest {

  @InjectMocks private StudyPermissionServiceImpl studyPermissionService;

  @Mock private CancerStudyPermissionEvaluator cancerStudyPermissionEvaluator;

  Authentication authentication;
  List<CancerStudy> cancerStudies;

  @Before
  public void init() {
    CancerStudy cancerStudy1 = new CancerStudy();
    CancerStudy cancerStudy2 = new CancerStudy();
    cancerStudies = new ArrayList<>();
    cancerStudies.add(cancerStudy1);
    cancerStudies.add(cancerStudy2);
    authentication = mock(Authentication.class);
    when(cancerStudyPermissionEvaluator.hasPermission(any(), any(), eq(AccessLevel.READ)))
        .thenReturn(false, true); // First study false, second true for READ
    when(cancerStudyPermissionEvaluator.hasPermission(any(), any(), eq(AccessLevel.DOWNLOAD)))
        .thenReturn(true, false); // First study true, second false for DOWNLOAD
  }

  @Test
  public void setPermissionsSuccess() {
    studyPermissionService.setPermissions(cancerStudies, authentication);
    Assert.assertFalse(cancerStudies.get(0).getReadPermission());
    Assert.assertTrue(cancerStudies.get(1).getReadPermission());
    Assert.assertTrue(cancerStudies.get(0).getDownloadPermission());
    Assert.assertFalse(cancerStudies.get(1).getDownloadPermission());
  }

  @Test
  public void setPermissionsUnAuthenticatedPortal() {
    ReflectionTestUtils.setField(studyPermissionService, "cancerStudyPermissionEvaluator", null);
    studyPermissionService.setPermissions(cancerStudies, authentication);
    Assert.assertTrue(cancerStudies.get(0).getReadPermission());
    Assert.assertTrue(cancerStudies.get(1).getReadPermission());
    Assert.assertTrue(cancerStudies.get(0).getDownloadPermission());
    Assert.assertTrue(cancerStudies.get(1).getDownloadPermission());
    ReflectionTestUtils.setField(
        studyPermissionService, "cancerStudyPermissionEvaluator", cancerStudyPermissionEvaluator);
  }

  @Test
  public void setPermissionsNoAuthObject() {
    studyPermissionService.setPermissions(cancerStudies, null);
    Assert.assertTrue(cancerStudies.get(0).getReadPermission());
    Assert.assertTrue(cancerStudies.get(1).getReadPermission());
    Assert.assertTrue(cancerStudies.get(0).getDownloadPermission());
    Assert.assertTrue(cancerStudies.get(1).getDownloadPermission());
  }
}
