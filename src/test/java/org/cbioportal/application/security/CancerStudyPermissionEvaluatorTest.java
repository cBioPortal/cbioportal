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
    evaluator = new CancerStudyPermissionEvaluator(null, "false", null, null, null);
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

  @Test
  public void testSuperuserCanDownloadEvenWithDownloadGroups() {
    CancerStudy study = new CancerStudy();
    study.setCancerStudyIdentifier("study1");
    study.setGroups("PUBLIC");
    study.setDownloadGroups("RESTRICTED_DOWNLOAD");

    // User is SUPERUSER (ALL_CANCER_STUDIES) but does NOT have RESTRICTED_DOWNLOAD
    when(authentication.getAuthorities())
        .thenReturn(
            (java.util.Collection)
                java.util.Arrays.asList(
                    new SimpleGrantedAuthority("all"))); // "all" is ALL_CANCER_STUDIES_ID

    // Logic: Superuser check happens BEFORE downloadGroups check
    boolean allowed = evaluator.hasPermission(authentication, study, AccessLevel.DOWNLOAD);
    assertTrue("Superuser should have DOWNLOAD access regardless of downloadGroups", allowed);
  }

  @Test
  public void testTBFSuperuserCanDownloadTCGA() {
    CancerStudy study = new CancerStudy();
    study.setCancerStudyIdentifier("coad_tcga"); // Ends with _tcga
    study.setGroups("PUBLIC");
    study.setDownloadGroups("RESTRICTED");

    // User has ALL_TCGA_CANCER_STUDIES
    when(authentication.getAuthorities())
        .thenReturn(
            (java.util.Collection) java.util.Arrays.asList(new SimpleGrantedAuthority("all_tcga")));

    boolean allowed = evaluator.hasPermission(authentication, study, AccessLevel.DOWNLOAD);
    assertTrue("TCGA Superuser should have DOWNLOAD access to TCGA study", allowed);
  }

  @Test
  public void testGlobalDownloadGroupRequiredDeniesWithoutGroup() {
    // Reconfigure evaluator to require a global DOWNLOAD_GROUP
    evaluator = new CancerStudyPermissionEvaluator(null, "false", null, "GLOBAL_DOWNLOAD", null);
    CancerStudy study = new CancerStudy();
    study.setCancerStudyIdentifier("study1");
    study.setGroups("PUBLIC"); // User with PUBLIC should have READ access

    // User has READ (PUBLIC) but lacks the global DOWNLOAD_GROUP
    when(authentication.getAuthorities())
        .thenReturn(
            (java.util.Collection)
                java.util.Collections.singletonList(new SimpleGrantedAuthority("PUBLIC")));

    boolean allowed = evaluator.hasPermission(authentication, study, AccessLevel.DOWNLOAD);
    assertFalse(
        "Should deny DOWNLOAD when a global download group is required and user lacks it", allowed);
  }

  @Test
  public void testGlobalDownloadGroupRequiredAllowsWithGroup() {
    // Reconfigure evaluator to require a global DOWNLOAD_GROUP
    evaluator = new CancerStudyPermissionEvaluator(null, "false", null, "GLOBAL_DOWNLOAD", null);
    CancerStudy study = new CancerStudy();
    study.setCancerStudyIdentifier("study1");
    study.setGroups("PUBLIC"); // User with PUBLIC should have READ access

    // User has both READ (PUBLIC) and the global DOWNLOAD_GROUP
    when(authentication.getAuthorities())
        .thenReturn(
            (java.util.Collection)
                java.util.Arrays.asList(
                    new SimpleGrantedAuthority("PUBLIC"),
                    new SimpleGrantedAuthority("GLOBAL_DOWNLOAD")));

    boolean allowed = evaluator.hasPermission(authentication, study, AccessLevel.DOWNLOAD);
    assertTrue(
        "Should allow DOWNLOAD when a global download group is required and user has it", allowed);
  }

  @Test
  public void testStudySpecificDownloadGroupsCasingAndWhitespace() {
    evaluator = new CancerStudyPermissionEvaluator(null, "false", null, null, null);
    CancerStudy study = new CancerStudy();
    study.setCancerStudyIdentifier("study1");
    study.setGroups("PUBLIC");
    study.setDownloadGroups(" restricted_group ; second_group ");

    // User has the group but in different casing
    when(authentication.getAuthorities())
        .thenReturn(
            (java.util.Collection)
                java.util.Arrays.asList(
                    new SimpleGrantedAuthority("PUBLIC"),
                    new SimpleGrantedAuthority("RESTRICTED_GROUP")));

    boolean allowed = evaluator.hasPermission(authentication, study, AccessLevel.DOWNLOAD);
    assertTrue(
        "Should allow DOWNLOAD when study-specific groups match with different casing or whitespace",
        allowed);
  }
}
