package org.cbioportal.application.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

class CancerStudyPermissionEvaluatorTest {

  private static final String STUDY_ID = "authorized_study";

  private final Authentication authentication =
      new TestingAuthenticationToken("user", null, "cbioportal:" + STUDY_ID.toUpperCase());

  private CancerStudyPermissionEvaluator evaluator() {
    CancerStudy study = new CancerStudy();
    study.setCancerStudyIdentifier(STUDY_ID);
    study.setGroups("");
    CacheMapUtil cacheMapUtil = mock(CacheMapUtil.class);
    when(cacheMapUtil.getCancerStudyPermissionMap()).thenReturn(Map.of(STUDY_ID, study));
    return new CancerStudyPermissionEvaluator("cbioportal", "true", "", cacheMapUtil);
  }

  @Test
  void grantsAnAuthorizedStudy() {
    assertTrue(
        evaluator().hasPermission(authentication, STUDY_ID, "CancerStudyId", AccessLevel.READ));
  }

  @Test
  void deniesAStudyIdThatResolvesToNoStudy() {
    assertFalse(
        evaluator()
            .hasPermission(authentication, "unknown_study", "CancerStudyId", AccessLevel.READ));
  }

  @Test
  void deniesAnEmptyCollectionOfStudyIds() {
    assertFalse(
        evaluator()
            .hasPermission(
                authentication,
                new ArrayList<>(List.of()),
                "Collection<CancerStudyId>",
                AccessLevel.READ));
  }
}
