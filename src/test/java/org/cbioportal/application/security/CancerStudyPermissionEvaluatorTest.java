package org.cbioportal.application.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

class CancerStudyPermissionEvaluatorTest {

  private static final String STUDY_ID = "authorized_study";
  private static final String OTHER_AUTHORIZED_STUDY_ID = "other_authorized_study";
  private static final String UNAUTHORIZED_STUDY_ID = "unauthorized_study";
  private static final String UNKNOWN_STUDY_ID = "unknown_study";
  private static final String PROFILE_ID = "authorized_study_mutations";
  private static final String STUDY_IDS = "Collection<CancerStudyId>";
  private static final String PROFILE_IDS = "Collection<MolecularProfileId>";

  private final Authentication authentication =
      new TestingAuthenticationToken(
          "user",
          null,
          "cbioportal:" + STUDY_ID.toUpperCase(),
          "cbioportal:" + OTHER_AUTHORIZED_STUDY_ID.toUpperCase());

  private static CancerStudy study(String id) {
    CancerStudy study = new CancerStudy();
    study.setCancerStudyIdentifier(id);
    study.setGroups("");
    return study;
  }

  private CancerStudyPermissionEvaluator evaluator() {
    CancerStudy authorized = study(STUDY_ID);
    MolecularProfile profile = new MolecularProfile();
    profile.setStableId(PROFILE_ID);
    profile.setCancerStudyIdentifier(STUDY_ID);
    profile.setCancerStudy(authorized);
    CacheMapUtil cacheMapUtil = mock(CacheMapUtil.class);
    when(cacheMapUtil.getCancerStudyPermissionMap())
        .thenReturn(
            Map.of(
                STUDY_ID,
                authorized,
                OTHER_AUTHORIZED_STUDY_ID,
                study(OTHER_AUTHORIZED_STUDY_ID),
                UNAUTHORIZED_STUDY_ID,
                study(UNAUTHORIZED_STUDY_ID)));
    when(cacheMapUtil.getMolecularProfileMap()).thenReturn(Map.of(PROFILE_ID, profile));
    return new CancerStudyPermissionEvaluator("cbioportal", "true", "", cacheMapUtil);
  }

  private boolean check(Serializable target, String targetType) {
    return evaluator().hasPermission(authentication, target, targetType, AccessLevel.READ);
  }

  private static ArrayList<String> ids(String... ids) {
    return new ArrayList<>(List.of(ids));
  }

  @Test
  void grantsAnAuthorizedStudy() {
    assertTrue(check(STUDY_ID, "CancerStudyId"));
  }

  @Test
  void deniesAnUnauthorizedStudy() {
    assertFalse(check(UNAUTHORIZED_STUDY_ID, "CancerStudyId"));
  }

  @Test
  void deniesASingleStudyIdThatResolvesToNoStudy() {
    assertFalse(check(UNKNOWN_STUDY_ID, "CancerStudyId"));
  }

  @Test
  void grantsAnEmptyCollectionOfStudyIds() {
    assertTrue(check(ids(), STUDY_IDS));
  }

  @Test
  void grantsAnEmptyCollectionOfProfileIds() {
    assertTrue(check(ids(), PROFILE_IDS));
  }

  @Test
  void grantsACollectionWhoseStudiesAllResolveAndAreAuthorized() {
    assertTrue(check(ids(STUDY_ID, OTHER_AUTHORIZED_STUDY_ID), STUDY_IDS));
  }

  @Test
  void deniesACollectionWithAnAuthorizedAndAnUnknownStudy() {
    assertFalse(check(ids(STUDY_ID, UNKNOWN_STUDY_ID), STUDY_IDS));
  }

  @Test
  void deniesACollectionWhoseStudiesAreAllUnknown() {
    assertFalse(check(ids(UNKNOWN_STUDY_ID, "another_unknown_study"), STUDY_IDS));
  }

  @Test
  void deniesACollectionWithAnAuthorizedAndAnUnauthorizedStudy() {
    assertFalse(check(ids(STUDY_ID, UNAUTHORIZED_STUDY_ID), STUDY_IDS));
  }

  @Test
  void grantsAResolvedProfileOfAnAuthorizedStudy() {
    assertTrue(check(ids(PROFILE_ID), PROFILE_IDS));
  }

  @Test
  void deniesAProfileCollectionWithAnUnknownProfile() {
    assertFalse(check(ids(PROFILE_ID, "unknown_profile"), PROFILE_IDS));
  }
}
