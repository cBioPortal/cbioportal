package org.cbioportal.application.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.Serializable;
import org.cbioportal.legacy.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.junit.Test;

/**
 * Guards the authorization-extraction behavior that replaced {@code
 * InvolvedCancerStudyExtractorInterceptor}. Many request filter types are now authorized directly
 * by {@link CancerStudyPermissionEvaluator}; the most important property is that an unrecognized
 * target must fail CLOSED rather than fall through to an empty study set (which historically
 * returned {@code true} == access granted).
 */
public class CancerStudyPermissionEvaluatorTest {

  private final CacheMapUtil cacheMapUtil = mock(CacheMapUtil.class);
  private final CancerStudyPermissionEvaluator evaluator =
      new CancerStudyPermissionEvaluator("public_portal", "true", "", cacheMapUtil);

  @Test
  public void unrecognizedAuthorizationTargetFailsClosed() {
    boolean granted =
        evaluator.hasPermission(
            null, (Serializable) Integer.valueOf(42), "MysteryFilter", AccessLevel.READ);
    assertFalse(granted);
  }

  @Test
  public void recognizedFilterWithNoStudiesIsHandledWithoutError() {
    // A recognized filter type routes through extraction without throwing; with no referenced
    // studies there is nothing to deny.
    boolean granted =
        evaluator.hasPermission(null, new StudyViewFilter(), "StudyViewFilter", AccessLevel.READ);
    assertTrue(granted);
  }
}
