package org.cbioportal.application.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.Patient;
import org.cbioportal.legacy.model.SampleList;
import org.cbioportal.legacy.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

/** Access rules of {@link CancerStudyPermissionEvaluator} for study, profile and list targets. */
class CancerStudyPermissionEvaluatorRulesTest {

  private static final String PUBLIC_STUDY = "public_study";
  private static final String GROUP_STUDY = "group_study";
  private static final String PRIVATE_STUDY = "private_study";
  private static final String TCGA_STUDY = "brca_tcga";
  private static final String TARGET_STUDY = "aml_target";
  private static final String TARGET_PHASE_STUDY = "all_target_phase2";
  private static final String PUBLIC_PROFILE = "public_study_mutations";
  private static final String PRIVATE_PROFILE = "private_study_mutations";
  private static final String PUBLIC_SAMPLE_LIST = "public_study_all";
  private static final String PRIVATE_SAMPLE_LIST = "private_study_all";

  private CacheMapUtil cacheMapUtil;

  @BeforeEach
  void setUp() {
    cacheMapUtil = Mockito.mock(CacheMapUtil.class);
    Mockito.when(cacheMapUtil.getCancerStudyPermissionMap())
        .thenReturn(
            Map.of(
                PUBLIC_STUDY, study(PUBLIC_STUDY, "PUBLIC"),
                GROUP_STUDY, study(GROUP_STUDY, "other;Team_A"),
                PRIVATE_STUDY, study(PRIVATE_STUDY, ""),
                TCGA_STUDY, study(TCGA_STUDY, ""),
                TARGET_STUDY, study(TARGET_STUDY, ""),
                TARGET_PHASE_STUDY, study(TARGET_PHASE_STUDY, "")));
    Mockito.when(cacheMapUtil.getMolecularProfileMap())
        .thenReturn(
            Map.of(
                PUBLIC_PROFILE, profile(PUBLIC_STUDY),
                PRIVATE_PROFILE, profile(PRIVATE_STUDY)));
    Mockito.when(cacheMapUtil.getSampleListMap())
        .thenReturn(
            Map.of(
                PUBLIC_SAMPLE_LIST, sampleList(PUBLIC_STUDY),
                PRIVATE_SAMPLE_LIST, sampleList(PRIVATE_STUDY)));
  }

  // Authorities, with filter_groups_by_appname=false and no always-shown group

  @Test
  void userWithoutMatchingAuthorityIsDenied() {
    assertFalse(readStudy(plain(), user("OTHER_GROUP"), PRIVATE_STUDY));
    assertFalse(readStudy(plain(), user("OTHER_GROUP"), GROUP_STUDY));
  }

  @Test
  void anyOfTheStudyGroupsGrantsAccessIgnoringCase() {
    assertTrue(readStudy(plain(), user("team_a"), GROUP_STUDY));
    assertTrue(readStudy(plain(), user("OTHER"), GROUP_STUDY));
  }

  @Test
  void studyIdentifierAsAuthorityGrantsAccessToThatStudyOnly() {
    assertTrue(readStudy(plain(), user(PRIVATE_STUDY.toUpperCase()), PRIVATE_STUDY));
    assertFalse(readStudy(plain(), user(PRIVATE_STUDY), GROUP_STUDY));
  }

  @Test
  void rolePrefixIsIgnored() {
    assertTrue(readStudy(plain(), user("ROLE_TEAM_A"), GROUP_STUDY));
  }

  @Test
  void allAuthorityGrantsEveryStudy() {
    assertTrue(readStudy(plain(), user("ALL"), PRIVATE_STUDY));
    assertTrue(readStudy(plain(), user("all"), GROUP_STUDY));
  }

  @Test
  void allTcgaAuthorityGrantsTcgaStudiesOnly() {
    assertTrue(readStudy(plain(), user("ALL_TCGA"), TCGA_STUDY));
    assertFalse(readStudy(plain(), user("ALL_TCGA"), PRIVATE_STUDY));
  }

  @Test
  void allNciTargetAuthorityGrantsTargetStudiesOnly() {
    assertTrue(readStudy(plain(), user("ALL_NCI_TARGET"), TARGET_STUDY));
    assertTrue(readStudy(plain(), user("ALL_NCI_TARGET"), TARGET_PHASE_STUDY));
    assertFalse(readStudy(plain(), user("ALL_NCI_TARGET"), TCGA_STUDY));
  }

  @Test
  void listPermissionIsAlwaysGranted() {
    assertTrue(plain().hasPermission(user(), PRIVATE_STUDY, "CancerStudyId", AccessLevel.LIST));
  }

  // always_show_study_group

  @Test
  void alwaysShownGroupIsReadableByEveryone() {
    CancerStudyPermissionEvaluator evaluator = evaluator("", "false", "PUBLIC");
    assertTrue(readStudy(evaluator, user(), PUBLIC_STUDY));
    assertTrue(readStudy(evaluator, anonymous(), PUBLIC_STUDY));
    assertFalse(readStudy(evaluator, anonymous(), PRIVATE_STUDY));
  }

  @Test
  void withoutAlwaysShownGroupPublicStudyNeedsTheGroup() {
    assertFalse(readStudy(plain(), user(), PUBLIC_STUDY));
    assertTrue(readStudy(plain(), user("PUBLIC"), PUBLIC_STUDY));
  }

  // filter_groups_by_appname

  @Test
  void appNameFilteringUsesOnlyAuthoritiesForThisApp() {
    CancerStudyPermissionEvaluator evaluator = evaluator("my-portal", "true", "");
    assertTrue(readStudy(evaluator, user("my-portal:TEAM_A"), GROUP_STUDY));
    assertTrue(readStudy(evaluator, user("MY-PORTAL:team_a"), GROUP_STUDY));
    assertFalse(readStudy(evaluator, user("other-portal:TEAM_A"), GROUP_STUDY));
    assertFalse(readStudy(evaluator, user("TEAM_A"), GROUP_STUDY));
  }

  @Test
  void appNameFilteringDefaultsToPublicPortalAppName() {
    CancerStudyPermissionEvaluator evaluator = evaluator("", "true", "");
    assertTrue(readStudy(evaluator, user("public_portal:TEAM_A"), GROUP_STUDY));
    assertFalse(readStudy(evaluator, user("TEAM_A"), GROUP_STUDY));
  }

  @Test
  void appNameFilteringIsOnWhenUnset() {
    CancerStudyPermissionEvaluator evaluator = evaluator("my-portal", null, "");
    assertFalse(readStudy(evaluator, user("TEAM_A"), GROUP_STUDY));
    assertTrue(readStudy(evaluator, user("my-portal:TEAM_A"), GROUP_STUDY));
  }

  @Test
  void appNameFilteringAppliesToAllAuthority() {
    CancerStudyPermissionEvaluator evaluator = evaluator("my-portal", "true", "");
    assertFalse(readStudy(evaluator, user("ALL"), PRIVATE_STUDY));
    assertTrue(readStudy(evaluator, user("my-portal:ALL"), PRIVATE_STUDY));
  }

  // Identifier targets

  @Test
  void molecularProfileAndSampleListResolveToTheirStudy() {
    Authentication publicUser = user("PUBLIC");
    assertTrue(allowed(publicUser, PUBLIC_PROFILE, "MolecularProfileId"));
    assertTrue(allowed(publicUser, PUBLIC_PROFILE, "GeneticProfileId"));
    assertFalse(allowed(publicUser, PRIVATE_PROFILE, "MolecularProfileId"));
    assertTrue(allowed(publicUser, PUBLIC_SAMPLE_LIST, "SampleListId"));
    assertFalse(allowed(publicUser, PRIVATE_SAMPLE_LIST, "SampleListId"));
  }

  @Test
  void collectionIsAllowedOnlyIfEveryStudyIsReadable() {
    Authentication publicUser = user("PUBLIC");
    assertTrue(allowed(publicUser, ids(PUBLIC_STUDY), "Collection<CancerStudyId>"));
    assertFalse(allowed(publicUser, ids(PUBLIC_STUDY, PRIVATE_STUDY), "Collection<CancerStudyId>"));
    assertTrue(
        allowed(
            user("PUBLIC", PRIVATE_STUDY),
            ids(PUBLIC_STUDY, PRIVATE_STUDY),
            "Collection<CancerStudyId>"));
    assertFalse(
        allowed(
            publicUser, ids(PUBLIC_PROFILE, PRIVATE_PROFILE), "Collection<MolecularProfileId>"));
    assertFalse(
        allowed(
            publicUser, ids(PUBLIC_SAMPLE_LIST, PRIVATE_SAMPLE_LIST), "Collection<SampleListId>"));
  }

  @Test
  void nullTargetIsDenied() {
    assertFalse(plain().hasPermission(user("ALL"), null, "CancerStudyId", AccessLevel.READ));
    assertFalse(plain().hasPermission(user("ALL"), null, AccessLevel.READ));
  }

  // Domain-object targets

  @Test
  void domainObjectsResolveToTheirStudy() {
    Authentication publicUser = user("PUBLIC");
    CancerStudyPermissionEvaluator evaluator = plain();
    assertTrue(
        evaluator.hasPermission(publicUser, study(PUBLIC_STUDY, "PUBLIC"), AccessLevel.READ));
    assertFalse(evaluator.hasPermission(publicUser, study(PRIVATE_STUDY, ""), AccessLevel.READ));
    assertTrue(evaluator.hasPermission(publicUser, profile(PUBLIC_STUDY), AccessLevel.READ));
    assertFalse(evaluator.hasPermission(publicUser, profile(PRIVATE_STUDY), AccessLevel.READ));
    assertTrue(evaluator.hasPermission(publicUser, sampleList(PUBLIC_STUDY), AccessLevel.READ));
    assertFalse(evaluator.hasPermission(publicUser, sampleList(PRIVATE_STUDY), AccessLevel.READ));
    assertTrue(evaluator.hasPermission(publicUser, patient(PUBLIC_STUDY), AccessLevel.READ));
    assertFalse(evaluator.hasPermission(publicUser, patient(PRIVATE_STUDY), AccessLevel.READ));
  }

  @Test
  void domainObjectOfUnknownStudyOrTypeIsDenied() {
    assertFalse(plain().hasPermission(user("ALL"), profile("no_such_study"), AccessLevel.READ));
    assertFalse(plain().hasPermission(user("ALL"), "not a domain object", AccessLevel.READ));
  }

  private CancerStudyPermissionEvaluator plain() {
    return evaluator("", "false", "");
  }

  private CancerStudyPermissionEvaluator evaluator(
      String appName, String filterGroupsByAppName, String alwaysShowStudyGroup) {
    return new CancerStudyPermissionEvaluator(
        appName, filterGroupsByAppName, alwaysShowStudyGroup, cacheMapUtil);
  }

  private boolean readStudy(
      CancerStudyPermissionEvaluator evaluator, Authentication authentication, String studyId) {
    return evaluator.hasPermission(authentication, studyId, "CancerStudyId", AccessLevel.READ);
  }

  private boolean allowed(Authentication authentication, Serializable target, String type) {
    return plain().hasPermission(authentication, target, type, AccessLevel.READ);
  }

  private static Authentication user(String... authorities) {
    return new TestingAuthenticationToken("user", "password", authorities);
  }

  private static Authentication anonymous() {
    return new TestingAuthenticationToken("anonymousUser", "", "ROLE_ANONYMOUS");
  }

  private static ArrayList<String> ids(String... ids) {
    return new ArrayList<>(Arrays.asList(ids));
  }

  private static CancerStudy study(String id, String groups) {
    CancerStudy study = new CancerStudy();
    study.setCancerStudyIdentifier(id);
    study.setGroups(groups);
    return study;
  }

  private static MolecularProfile profile(String studyId) {
    MolecularProfile profile = new MolecularProfile();
    profile.setCancerStudyIdentifier(studyId);
    return profile;
  }

  private static SampleList sampleList(String studyId) {
    SampleList sampleList = new SampleList();
    sampleList.setCancerStudyIdentifier(studyId);
    return sampleList;
  }

  private static Patient patient(String studyId) {
    Patient patient = new Patient();
    patient.setCancerStudyIdentifier(studyId);
    return patient;
  }
}
