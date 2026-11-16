package org.cbioportal.application.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.cbioportal.legacy.web.parameter.ClinicalDataIdentifier;
import org.cbioportal.legacy.web.parameter.ClinicalDataMultiStudyFilter;
import org.cbioportal.legacy.web.parameter.Group;
import org.cbioportal.legacy.web.parameter.GroupFilter;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

public class CancerStudyPermissionEvaluatorTest {

  private static final String PUBLIC_STUDY = "public_study";
  private static final String PRIVATE_STUDY = "private_study";

  private CancerStudyPermissionEvaluator evaluator;

  // May read public_study (through the group of that study), but not private_study
  private final Authentication user =
      new TestingAuthenticationToken("user", "password", "ROLE_PUBLIC_GROUP");
  private final Authentication privateStudyUser =
      new TestingAuthenticationToken("user", "password", "ROLE_PUBLIC_GROUP", "ROLE_PRIVATE_GROUP");

  @Before
  public void setUp() {
    CacheMapUtil cacheMapUtil = Mockito.mock(CacheMapUtil.class);
    Mockito.when(cacheMapUtil.getCancerStudyPermissionMap())
        .thenReturn(
            Map.of(
                PUBLIC_STUDY, study(PUBLIC_STUDY, "PUBLIC_GROUP"),
                PRIVATE_STUDY, study(PRIVATE_STUDY, "PRIVATE_GROUP")));
    evaluator = new CancerStudyPermissionEvaluator("cbioportal", "false", null, cacheMapUtil);
  }

  // ClinicalDataMultiStudyFilter (clinical-data/fetch)

  @Test
  public void clinicalDataMultiStudyFilterDeniesStudyTheUserCannotRead() {
    assertFalse(allowed(user, clinicalData(PRIVATE_STUDY), "ClinicalDataMultiStudyFilter"));
  }

  @Test
  public void clinicalDataMultiStudyFilterDeniesWhenOneOfTheStudiesIsNotReadable() {
    assertFalse(
        allowed(user, clinicalData(PUBLIC_STUDY, PRIVATE_STUDY), "ClinicalDataMultiStudyFilter"));
  }

  @Test
  public void clinicalDataMultiStudyFilterAllowsReadableStudies() {
    assertTrue(allowed(user, clinicalData(PUBLIC_STUDY), "ClinicalDataMultiStudyFilter"));
    assertTrue(
        allowed(
            privateStudyUser,
            clinicalData(PUBLIC_STUDY, PRIVATE_STUDY),
            "ClinicalDataMultiStudyFilter"));
  }

  @Test
  public void clinicalDataMultiStudyFilterDeniesWhenStudiesCannotBeDetermined() {
    ClinicalDataMultiStudyFilter noIdentifiers = new ClinicalDataMultiStudyFilter();
    assertFalse(allowed(user, noIdentifiers, "ClinicalDataMultiStudyFilter"));

    ClinicalDataMultiStudyFilter noStudyId = new ClinicalDataMultiStudyFilter();
    noStudyId.setIdentifiers(
        Arrays.asList(clinicalDataIdentifier(PUBLIC_STUDY), new ClinicalDataIdentifier()));
    assertFalse(allowed(user, noStudyId, "ClinicalDataMultiStudyFilter"));

    ClinicalDataMultiStudyFilter nullIdentifier = new ClinicalDataMultiStudyFilter();
    nullIdentifier.setIdentifiers(Arrays.asList(clinicalDataIdentifier(PUBLIC_STUDY), null));
    assertFalse(allowed(user, nullIdentifier, "ClinicalDataMultiStudyFilter"));
  }

  // GroupFilter (clinical-data-enrichments/fetch)

  @Test
  public void groupFilterDeniesStudyTheUserCannotRead() {
    assertFalse(allowed(user, groups(group(PUBLIC_STUDY), group(PRIVATE_STUDY)), "GroupFilter"));
    assertFalse(
        allowed(
            user, groups(group(PUBLIC_STUDY, PRIVATE_STUDY), group(PUBLIC_STUDY)), "GroupFilter"));
  }

  @Test
  public void groupFilterAllowsReadableStudies() {
    assertTrue(allowed(user, groups(group(PUBLIC_STUDY), group(PUBLIC_STUDY)), "GroupFilter"));
    assertTrue(
        allowed(
            privateStudyUser, groups(group(PUBLIC_STUDY), group(PRIVATE_STUDY)), "GroupFilter"));
  }

  @Test
  public void groupFilterDeniesWhenStudiesCannotBeDetermined() {
    assertFalse(allowed(user, new GroupFilter(), "GroupFilter"));

    Group noSamples = new Group();
    assertFalse(allowed(user, groups(group(PUBLIC_STUDY), noSamples), "GroupFilter"));

    Group noStudyId = new Group();
    noStudyId.setSampleIdentifiers(Arrays.asList(sample(PUBLIC_STUDY), new SampleIdentifier()));
    assertFalse(allowed(user, groups(group(PUBLIC_STUDY), noStudyId), "GroupFilter"));

    assertFalse(allowed(user, groups(group(PUBLIC_STUDY), null), "GroupFilter"));
  }

  // Filter types the evaluator does not know

  @Test
  public void unsupportedFilterTypeIsDeniedInsteadOfPassingVacuously() {
    // A target that refers to no study the evaluator can find must not be waved through
    assertFalse(allowed(user, new UnknownFilter(), "UnknownFilter"));
  }

  @Test
  public void supportedFilterTypesAreStillEvaluated() {
    StudyViewFilter privateOnly = new StudyViewFilter();
    privateOnly.setStudyIds(List.of(PRIVATE_STUDY));
    assertFalse(allowed(user, privateOnly, "StudyViewFilter"));
    assertTrue(allowed(privateStudyUser, privateOnly, "StudyViewFilter"));
  }

  // Study and profile ids, and collections of them

  private static final String STUDY_ID = "authorized_study";
  private static final String OTHER_AUTHORIZED_STUDY_ID = "other_authorized_study";
  private static final String UNAUTHORIZED_STUDY_ID = "unauthorized_study";
  private static final String UNKNOWN_STUDY_ID = "unknown_study";
  private static final String PROFILE_ID = "authorized_study_mutations";
  private static final String STUDY_IDS = "Collection<CancerStudyId>";
  private static final String PROFILE_IDS = "Collection<MolecularProfileId>";

  private final Authentication idUser =
      new TestingAuthenticationToken(
          "user",
          null,
          "cbioportal:" + STUDY_ID.toUpperCase(),
          "cbioportal:" + OTHER_AUTHORIZED_STUDY_ID.toUpperCase());

  private CancerStudyPermissionEvaluator idEvaluator() {
    CancerStudy authorized = study(STUDY_ID, "");
    MolecularProfile profile = new MolecularProfile();
    profile.setStableId(PROFILE_ID);
    profile.setCancerStudyIdentifier(STUDY_ID);
    profile.setCancerStudy(authorized);
    CacheMapUtil cacheMapUtil = Mockito.mock(CacheMapUtil.class);
    Mockito.when(cacheMapUtil.getCancerStudyPermissionMap())
        .thenReturn(
            Map.of(
                STUDY_ID,
                authorized,
                OTHER_AUTHORIZED_STUDY_ID,
                study(OTHER_AUTHORIZED_STUDY_ID, ""),
                UNAUTHORIZED_STUDY_ID,
                study(UNAUTHORIZED_STUDY_ID, "")));
    Mockito.when(cacheMapUtil.getMolecularProfileMap()).thenReturn(Map.of(PROFILE_ID, profile));
    return new CancerStudyPermissionEvaluator("cbioportal", "true", "", cacheMapUtil);
  }

  private boolean check(Serializable target, String targetType) {
    return idEvaluator().hasPermission(idUser, target, targetType, AccessLevel.READ);
  }

  private static ArrayList<String> ids(String... ids) {
    return new ArrayList<>(List.of(ids));
  }

  @Test
  public void grantsAnAuthorizedStudy() {
    assertTrue(check(STUDY_ID, "CancerStudyId"));
  }

  @Test
  public void deniesAnUnauthorizedStudy() {
    assertFalse(check(UNAUTHORIZED_STUDY_ID, "CancerStudyId"));
  }

  @Test
  public void deniesASingleStudyIdThatResolvesToNoStudy() {
    assertFalse(check(UNKNOWN_STUDY_ID, "CancerStudyId"));
  }

  @Test
  public void grantsAnEmptyCollectionOfStudyIds() {
    assertTrue(check(ids(), STUDY_IDS));
  }

  @Test
  public void grantsAnEmptyCollectionOfProfileIds() {
    assertTrue(check(ids(), PROFILE_IDS));
  }

  @Test
  public void grantsACollectionWhoseStudiesAllResolveAndAreAuthorized() {
    assertTrue(check(ids(STUDY_ID, OTHER_AUTHORIZED_STUDY_ID), STUDY_IDS));
  }

  @Test
  public void deniesACollectionWithAnAuthorizedAndAnUnknownStudy() {
    assertFalse(check(ids(STUDY_ID, UNKNOWN_STUDY_ID), STUDY_IDS));
  }

  @Test
  public void deniesACollectionWhoseStudiesAreAllUnknown() {
    assertFalse(check(ids(UNKNOWN_STUDY_ID, "another_unknown_study"), STUDY_IDS));
  }

  @Test
  public void deniesACollectionWithAnAuthorizedAndAnUnauthorizedStudy() {
    assertFalse(check(ids(STUDY_ID, UNAUTHORIZED_STUDY_ID), STUDY_IDS));
  }

  @Test
  public void grantsAResolvedProfileOfAnAuthorizedStudy() {
    assertTrue(check(ids(PROFILE_ID), PROFILE_IDS));
  }

  @Test
  public void deniesAProfileCollectionWithAnUnknownProfile() {
    assertFalse(check(ids(PROFILE_ID, "unknown_profile"), PROFILE_IDS));
  }

  private boolean allowed(Authentication authentication, java.io.Serializable filter, String type) {
    return evaluator.hasPermission(authentication, filter, type, AccessLevel.READ);
  }

  private static class UnknownFilter implements java.io.Serializable {}

  private static CancerStudy study(String id, String groups) {
    CancerStudy study = new CancerStudy();
    study.setCancerStudyIdentifier(id);
    study.setGroups(groups);
    return study;
  }

  private static ClinicalDataIdentifier clinicalDataIdentifier(String studyId) {
    ClinicalDataIdentifier identifier = new ClinicalDataIdentifier();
    identifier.setStudyId(studyId);
    identifier.setEntityId("any_sample");
    return identifier;
  }

  private static ClinicalDataMultiStudyFilter clinicalData(String... studyIds) {
    ClinicalDataMultiStudyFilter filter = new ClinicalDataMultiStudyFilter();
    filter.setIdentifiers(
        Arrays.stream(studyIds)
            .map(CancerStudyPermissionEvaluatorTest::clinicalDataIdentifier)
            .toList());
    filter.setAttributeIds(List.of("AGE"));
    return filter;
  }

  private static SampleIdentifier sample(String studyId) {
    SampleIdentifier identifier = new SampleIdentifier();
    identifier.setStudyId(studyId);
    identifier.setSampleId("any_sample");
    return identifier;
  }

  private static Group group(String... studyIds) {
    Group group = new Group();
    group.setSampleIdentifiers(
        Arrays.stream(studyIds).map(CancerStudyPermissionEvaluatorTest::sample).toList());
    return group;
  }

  private static GroupFilter groups(Group... groups) {
    GroupFilter filter = new GroupFilter();
    filter.setGroups(Arrays.asList(groups));
    return filter;
  }
}
