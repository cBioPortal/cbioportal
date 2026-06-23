package org.cbioportal.application.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.cbioportal.legacy.web.parameter.ClinicalDataIdentifier;
import org.cbioportal.legacy.web.parameter.ClinicalDataMultiStudyFilter;
import org.cbioportal.legacy.web.parameter.Group;
import org.cbioportal.legacy.web.parameter.GroupFilter;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class CancerStudyPermissionEvaluatorTest {

  @Mock private CacheMapUtil cacheMapUtil;

  private CancerStudyPermissionEvaluator evaluator;

  @BeforeEach
  void setUp() {
    evaluator = new CancerStudyPermissionEvaluator(null, null, null, cacheMapUtil);
  }

  /** A user with no granted authorities has access to no non-public study. */
  private Authentication userWithoutAccess() {
    return new TestingAuthenticationToken("user", "credentials");
  }

  private void stubPrivateStudy() {
    CancerStudy study = new CancerStudy();
    study.setCancerStudyIdentifier("private_study");
    study.setGroups("");
    when(cacheMapUtil.getCancerStudyMap()).thenReturn(Map.of("private_study", study));
  }

  @Test
  void clinicalDataMultiStudyFilter_deniesAccessToPrivateStudy() {
    stubPrivateStudy();

    ClinicalDataIdentifier identifier = new ClinicalDataIdentifier();
    identifier.setStudyId("private_study");
    identifier.setEntityId("any_sample");
    ClinicalDataMultiStudyFilter filter = new ClinicalDataMultiStudyFilter();
    filter.setIdentifiers(List.of(identifier));

    boolean result =
        evaluator.hasPermission(
            userWithoutAccess(), filter, "ClinicalDataMultiStudyFilter", AccessLevel.READ);

    assertFalse(result);
  }

  @Test
  void groupFilter_deniesAccessToPrivateStudy() {
    stubPrivateStudy();

    SampleIdentifier sampleIdentifier = new SampleIdentifier();
    sampleIdentifier.setStudyId("private_study");
    sampleIdentifier.setSampleId("any_sample");
    Group group = new Group();
    group.setSampleIdentifiers(List.of(sampleIdentifier));
    GroupFilter filter = new GroupFilter();
    filter.setGroups(List.of(group));

    boolean result =
        evaluator.hasPermission(userWithoutAccess(), filter, "GroupFilter", AccessLevel.READ);

    assertFalse(result);
  }

  @Test
  void unsupportedFilterType_failsClosed() {
    Serializable unsupportedFilter = new UnsupportedFilter();

    boolean result =
        evaluator.hasPermission(
            userWithoutAccess(), unsupportedFilter, "SomeUnsupportedFilter", AccessLevel.READ);

    assertFalse(result);
  }

  /** A filter type the evaluator does not explicitly handle. Authorization must fail closed. */
  private static class UnsupportedFilter implements Serializable {}
}
