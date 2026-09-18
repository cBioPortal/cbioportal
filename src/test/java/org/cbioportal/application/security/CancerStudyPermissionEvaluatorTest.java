package org.cbioportal.application.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class CancerStudyPermissionEvaluatorTest {

  private static final String STUDY_ID = "study1";
  private static final String GROUP = "SOME_GROUP";

  private CancerStudyPermissionEvaluator newEvaluator(boolean unavailable) {
    CacheMapUtil cacheMapUtil = mock(CacheMapUtil.class);
    when(cacheMapUtil.getCancerStudyMap()).thenReturn(Map.of(STUDY_ID, study()));
    UnavailableCancerStudyIds unavailableCancerStudyIds = mock(UnavailableCancerStudyIds.class);
    when(unavailableCancerStudyIds.isUnavailable(STUDY_ID)).thenReturn(unavailable);
    // doFilterGroupsByAppName=false: compare authorities to study groups without app prefixing.
    return new CancerStudyPermissionEvaluator(
        "public_portal", "false", null, cacheMapUtil, unavailableCancerStudyIds);
  }

  private CancerStudy study() {
    CancerStudy study = new CancerStudy();
    study.setCancerStudyIdentifier(STUDY_ID);
    study.setGroups(GROUP);
    return study;
  }

  private Authentication authenticatedUserInGroup() {
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn("test-user");
    List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(GROUP));
    doReturn(authorities).when(authentication).getAuthorities();
    return authentication;
  }

  private Authentication authenticatedUserWithNoGroups() {
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn("test-user");
    doReturn(List.<GrantedAuthority>of()).when(authentication).getAuthorities();
    return authentication;
  }

  @Test
  void readDeniedAndFlaggedWhenAuthorizedButStudyUnavailable() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    try {
      boolean result =
          newEvaluator(true)
              .hasPermission(
                  authenticatedUserInGroup(), STUDY_ID, "CancerStudyId", AccessLevel.READ);
      assertFalse(result);
      assertEquals(
          STUDY_ID,
          request.getAttribute(CancerStudyPermissionEvaluator.UNAVAILABLE_STUDY_ATTRIBUTE));
    } finally {
      RequestContextHolder.resetRequestAttributes();
    }
  }

  @Test
  void readAllowedWhenStudyAvailable() {
    boolean result =
        newEvaluator(false)
            .hasPermission(authenticatedUserInGroup(), STUDY_ID, "CancerStudyId", AccessLevel.READ);
    assertTrue(result, "an available study the user has group access to must be readable");
  }

  @Test
  void unavailabilityNotRevealedWhenUserLacksPermission() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    try {
      boolean result =
          newEvaluator(true)
              .hasPermission(
                  authenticatedUserWithNoGroups(), STUDY_ID, "CancerStudyId", AccessLevel.READ);
      assertFalse(result);
      assertEquals(
          null, request.getAttribute(CancerStudyPermissionEvaluator.UNAVAILABLE_STUDY_ATTRIBUTE));
    } finally {
      RequestContextHolder.resetRequestAttributes();
    }
  }

  @Test
  void forbiddenStudyTakesPrecedenceOverUnavailableStudyInCollection() {
    CancerStudy forbidden = study();
    forbidden.setCancerStudyIdentifier("forbidden");
    forbidden.setGroups("OTHER_GROUP");
    CacheMapUtil cacheMapUtil = mock(CacheMapUtil.class);
    when(cacheMapUtil.getCancerStudyMap())
        .thenReturn(Map.of(STUDY_ID, study(), "forbidden", forbidden));
    UnavailableCancerStudyIds unavailable = mock(UnavailableCancerStudyIds.class);
    CancerStudyPermissionEvaluator evaluator =
        new CancerStudyPermissionEvaluator(
            "public_portal", "false", null, cacheMapUtil, unavailable);
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    try {
      for (ArrayList<String> ids :
          List.of(
              new ArrayList<>(List.of(STUDY_ID, "forbidden")),
              new ArrayList<>(List.of("forbidden", STUDY_ID)))) {
        assertFalse(
            evaluator.hasPermission(
                authenticatedUserInGroup(), ids, "Collection<CancerStudyId>", AccessLevel.READ));
        assertEquals(
            null, request.getAttribute(CancerStudyPermissionEvaluator.UNAVAILABLE_STUDY_ATTRIBUTE));
      }
      verifyNoInteractions(unavailable);
    } finally {
      RequestContextHolder.resetRequestAttributes();
    }
  }

  @Test
  void unavailableStudyInAuthorizedCollectionIsDeniedAndFlagged() {
    CancerStudy other = study();
    other.setCancerStudyIdentifier("other");
    CacheMapUtil cacheMapUtil = mock(CacheMapUtil.class);
    when(cacheMapUtil.getCancerStudyMap()).thenReturn(Map.of(STUDY_ID, study(), "other", other));
    UnavailableCancerStudyIds unavailable = mock(UnavailableCancerStudyIds.class);
    when(unavailable.isUnavailable(STUDY_ID)).thenReturn(true);
    CancerStudyPermissionEvaluator evaluator =
        new CancerStudyPermissionEvaluator(
            "public_portal", "false", null, cacheMapUtil, unavailable);
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    try {
      assertFalse(
          evaluator.hasPermission(
              authenticatedUserInGroup(),
              new ArrayList<>(List.of("other", STUDY_ID)),
              "Collection<CancerStudyId>",
              AccessLevel.READ));
      assertEquals(
          STUDY_ID,
          request.getAttribute(CancerStudyPermissionEvaluator.UNAVAILABLE_STUDY_ATTRIBUTE));
    } finally {
      RequestContextHolder.resetRequestAttributes();
    }
  }

  @Test
  void listIsUnaffectedByUnavailability() {
    boolean result =
        newEvaluator(true)
            .hasPermission(authenticatedUserInGroup(), STUDY_ID, "CancerStudyId", AccessLevel.LIST);
    assertTrue(result, "LIST must keep showing all studies regardless of availability");
  }

  @Test
  void postFilterKeepsUnavailableStudyWithoutFlagging() {
    // Backs @PostFilter (e.g. the study list): the frontend needs unavailable studies listed.
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    try {
      assertTrue(
          newEvaluator(true).hasPermission(authenticatedUserInGroup(), study(), AccessLevel.READ));
      assertEquals(
          null, request.getAttribute(CancerStudyPermissionEvaluator.UNAVAILABLE_STUDY_ATTRIBUTE));
    } finally {
      RequestContextHolder.resetRequestAttributes();
    }
  }
}
