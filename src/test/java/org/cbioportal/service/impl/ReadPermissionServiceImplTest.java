package org.cbioportal.service.impl;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.security.CancerStudyPermissionEvaluator;
import org.cbioportal.utils.security.AccessLevel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReadPermissionServiceImplTest {

    @InjectMocks
    private ReadPermissionServiceImpl readPermissionService;

    @Mock
    private CancerStudyPermissionEvaluator cancerStudyPermissionEvaluator;

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
        when(cancerStudyPermissionEvaluator.hasPermission(any(), any(), eq(AccessLevel.READ))).thenReturn(false, true);
    }

    @Test
    public void setReadPermissionSuccess() {
        readPermissionService.setReadPermission(cancerStudies, authentication);
        Assert.assertFalse(cancerStudies.get(0).getReadPermission());
        Assert.assertTrue(cancerStudies.get(1).getReadPermission());
    }

    @Test
    public void setReadPermissionUnAuthenticatedPortal() {
        ReflectionTestUtils.setField(readPermissionService, "cancerStudyPermissionEvaluator", null);
        readPermissionService.setReadPermission(cancerStudies, authentication);
        Assert.assertTrue(cancerStudies.get(0).getReadPermission());
        Assert.assertTrue(cancerStudies.get(1).getReadPermission());
        ReflectionTestUtils.setField(readPermissionService, "cancerStudyPermissionEvaluator", cancerStudyPermissionEvaluator);
    }

    @Test
    public void setReadPermissionNoAuthObject() {
        readPermissionService.setReadPermission(cancerStudies, null);
        Assert.assertTrue(cancerStudies.get(0).getReadPermission());
        Assert.assertTrue(cancerStudies.get(1).getReadPermission());
    }
}