package org.cbioportal.application.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@RunWith(MockitoJUnitRunner.class)
public class CancerStudyPermissionEvaluatorTest {

  private CancerStudyPermissionEvaluator evaluator;

  @Mock private Authentication authentication;

  @Before
  public void setUp() {
    // Basic setup with no special properties
    evaluator = new CancerStudyPermissionEvaluator(null, "false", null, null);
    when(authentication.getPrincipal()).thenReturn("user");
  }

  @Test
  public void testDownloadPermissionRequiresReadPermission() {
    CancerStudy study = new CancerStudy();
    study.setCancerStudyIdentifier("study1");
    study.setGroups("PUBLIC"); // User needs PUBLIC to read
    study.setDownloadGroups("DOWNLOAD_GROUP");

    // User has DOWNLOAD_GROUP but NOT PUBLIC (so no READ access)
    when(authentication.getAuthorities())
        .thenReturn(
            (java.util.Collection)
                Collections.singletonList(new SimpleGrantedAuthority("DOWNLOAD_GROUP")));

    // Should be FALSE because READ is denied
    boolean allowed = evaluator.hasPermission(authentication, study, AccessLevel.DOWNLOAD);
    assertFalse("Should deny DOWNLOAD if user lacks READ access", allowed);
  }

  @Test
  public void testDownloadPermissionGrantedWithReadAndDownloadGroup() {
    CancerStudy study = new CancerStudy();
    study.setCancerStudyIdentifier("study1");
    study.setGroups("PUBLIC");
    study.setDownloadGroups("DOWNLOAD_GROUP");

    // User has BOTH
    when(authentication.getAuthorities())
        .thenReturn(
            (java.util.Collection)
                java.util.Arrays.asList(
                    new SimpleGrantedAuthority("PUBLIC"),
                    new SimpleGrantedAuthority("DOWNLOAD_GROUP")));

    boolean allowed = evaluator.hasPermission(authentication, study, AccessLevel.DOWNLOAD);
    assertTrue("Should allow DOWNLOAD if user has READ and matching download group", allowed);
  }
}
