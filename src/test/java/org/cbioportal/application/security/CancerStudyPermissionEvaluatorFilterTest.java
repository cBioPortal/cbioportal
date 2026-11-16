package org.cbioportal.application.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.cbioportal.legacy.web.parameter.ClinicalDataIdentifier;
import org.cbioportal.legacy.web.parameter.ClinicalDataMultiStudyFilter;
import org.cbioportal.legacy.web.parameter.GenePanelDataMultipleStudyFilter;
import org.cbioportal.legacy.web.parameter.MolecularDataMultipleStudyFilter;
import org.cbioportal.legacy.web.parameter.MutationMultipleStudyFilter;
import org.cbioportal.legacy.web.parameter.SampleMolecularIdentifier;
import org.cbioportal.legacy.web.parameter.StructuralVariantFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CancerStudyPermissionEvaluatorFilterTest {

  private static final String TARGET_TYPE = "MolecularDataMultipleStudyFilter";

  @Mock private CacheMapUtil cacheMapUtil;

  private CancerStudyPermissionEvaluator evaluator;
  private Authentication userOfStudyA;

  @Before
  public void setUp() {
    when(cacheMapUtil.getCancerStudyPermissionMap())
        .thenReturn(Map.of("study_a", study("study_a"), "study_b", study("study_b")));
    when(cacheMapUtil.getMolecularProfileMap())
        .thenReturn(
            Map.of(
                "study_a_mrna", profile("study_a", "study_a_mrna"),
                "study_a_cna", profile("study_a", "study_a_cna"),
                "study_b_mrna", profile("study_b", "study_b_mrna")));
    evaluator = new CancerStudyPermissionEvaluator("cbioportal", "false", "", cacheMapUtil);
    userOfStudyA =
        new UsernamePasswordAuthenticationToken(
            "user", "n/a", List.of(new SimpleGrantedAuthority("ROLE_STUDY_A")));
  }

  @Test
  public void profileIdsOfAnAuthorizedStudyAreAllowed() {
    assertTrue(allowed(byProfileIds("study_a_mrna", "study_a_cna")));
  }

  @Test
  public void anyProfileOfAnUnauthorizedStudyDeniesTheWholeRequest() {
    assertFalse(allowed(byProfileIds("study_b_mrna")));
    assertFalse(allowed(byProfileIds("study_a_mrna", "study_b_mrna")));
  }

  @Test
  public void sampleMolecularIdentifiersAreCheckedByTheirProfilesStudy() {
    assertTrue(allowed(bySamples("study_a_mrna", "study_a_cna")));
    assertFalse(allowed(bySamples("study_a_mrna", "study_b_mrna")));
  }

  @Test
  public void unknownProfileIsDeniedInsteadOfBeingIgnored() {
    // an ignored unknown profile could shrink the checked study set to nothing, which grants access
    assertFalse(allowed(byProfileIds("no_such_profile")));
    assertFalse(allowed(byProfileIds("study_a_mrna", "no_such_profile")));
  }

  @Test
  public void userWithAccessToAllStudiesIsAllowed() {
    Authentication admin =
        new UsernamePasswordAuthenticationToken(
            "admin", "n/a", List.of(new SimpleGrantedAuthority("ROLE_ALL")));
    assertTrue(
        evaluator.hasPermission(
            admin, byProfileIds("study_a_mrna", "study_b_mrna"), TARGET_TYPE, AccessLevel.READ));
  }

  @Test
  public void clinicalDataFilterIsCheckedAgainstEveryStudyOfItsIdentifiers() {
    assertTrue(allowedClinical("study_a", "study_a"));
    assertFalse(allowedClinical("study_b"));
    assertFalse(allowedClinical("study_a", "study_b"));
  }

  private boolean allowedClinical(String... studyIds) {
    ClinicalDataMultiStudyFilter filter = new ClinicalDataMultiStudyFilter();
    filter.setIdentifiers(
        java.util.Arrays.stream(studyIds)
            .map(
                studyId -> {
                  ClinicalDataIdentifier identifier = new ClinicalDataIdentifier();
                  identifier.setStudyId(studyId);
                  identifier.setEntityId("entity");
                  return identifier;
                })
            .toList());
    filter.setAttributeIds(List.of("ATTR"));
    return evaluator.hasPermission(
        userOfStudyA, filter, "ClinicalDataMultiStudyFilter", AccessLevel.READ);
  }

  @Test
  public void mutationFilterIsCheckedByTheStudiesOfItsProfiles() {
    assertTrue(allowedMutation(byProfileIdsMutation("study_a_mrna", "study_a_cna"), userOfStudyA));
    assertFalse(allowedMutation(byProfileIdsMutation("study_b_mrna"), userOfStudyA));
    assertFalse(
        allowedMutation(byProfileIdsMutation("study_a_mrna", "study_b_mrna"), userOfStudyA));
    assertFalse(allowedMutation(byProfileIdsMutation("no_such_profile"), userOfStudyA));

    MutationMultipleStudyFilter pairs = new MutationMultipleStudyFilter();
    pairs.setSampleMolecularIdentifiers(
        List.of(identifier("study_a_mrna"), identifier("study_b_mrna")));
    assertFalse(allowedMutation(pairs, userOfStudyA));
    pairs.setSampleMolecularIdentifiers(List.of(identifier("study_a_mrna")));
    assertTrue(allowedMutation(pairs, userOfStudyA));
  }

  private boolean allowedMutation(MutationMultipleStudyFilter filter, Authentication user) {
    return evaluator.hasPermission(user, filter, "MutationMultipleStudyFilter", AccessLevel.READ);
  }

  private MutationMultipleStudyFilter byProfileIdsMutation(String... profileIds) {
    MutationMultipleStudyFilter filter = new MutationMultipleStudyFilter();
    filter.setMolecularProfileIds(List.of(profileIds));
    filter.setEntrezGeneIds(List.of(1));
    return filter;
  }

  private SampleMolecularIdentifier identifier(String profileId) {
    SampleMolecularIdentifier identifier = new SampleMolecularIdentifier();
    identifier.setMolecularProfileId(profileId);
    identifier.setSampleId("sample");
    return identifier;
  }

  @Test
  public void genePanelDataFilterIsCheckedByTheStudiesOfItsProfiles() {
    assertTrue(allowedGenePanel(profilesGenePanel("study_a_mrna", "study_a_cna")));
    assertFalse(allowedGenePanel(profilesGenePanel("study_b_mrna")));
    assertFalse(allowedGenePanel(profilesGenePanel("study_a_mrna", "study_b_mrna")));
    assertFalse(allowedGenePanel(profilesGenePanel("no_such_profile")));

    GenePanelDataMultipleStudyFilter pairs = new GenePanelDataMultipleStudyFilter();
    pairs.setSampleMolecularIdentifiers(
        List.of(identifier("study_a_mrna"), identifier("study_b_mrna")));
    assertFalse(allowedGenePanel(pairs));
    pairs.setSampleMolecularIdentifiers(List.of(identifier("study_a_mrna")));
    assertTrue(allowedGenePanel(pairs));
  }

  private boolean allowedGenePanel(GenePanelDataMultipleStudyFilter filter) {
    return evaluator.hasPermission(
        userOfStudyA, filter, "GenePanelDataMultipleStudyFilter", AccessLevel.READ);
  }

  private GenePanelDataMultipleStudyFilter profilesGenePanel(String... profileIds) {
    GenePanelDataMultipleStudyFilter filter = new GenePanelDataMultipleStudyFilter();
    filter.setMolecularProfileIds(List.of(profileIds));
    return filter;
  }

  @Test
  public void structuralVariantFilterIsCheckedByTheStudiesOfItsProfiles() {
    assertTrue(allowedSv(profilesSv("study_a_mrna", "study_a_cna")));
    assertFalse(allowedSv(profilesSv("study_b_mrna")));
    assertFalse(allowedSv(profilesSv("study_a_mrna", "study_b_mrna")));
    assertFalse(allowedSv(profilesSv("no_such_profile")));

    StructuralVariantFilter pairs = new StructuralVariantFilter();
    pairs.setSampleMolecularIdentifiers(
        List.of(identifier("study_a_mrna"), identifier("study_b_mrna")));
    assertFalse(allowedSv(pairs));
    pairs.setSampleMolecularIdentifiers(List.of(identifier("study_a_mrna")));
    assertTrue(allowedSv(pairs));
  }

  @Test
  public void structuralVariantPairsTakePrecedenceOverProfileIdsLikeInTheUseCase() {
    // the use case ignores the profile ids when pairs are present, so only the pairs are checked
    StructuralVariantFilter filter = new StructuralVariantFilter();
    filter.setMolecularProfileIds(List.of("study_b_mrna"));
    filter.setSampleMolecularIdentifiers(List.of(identifier("study_a_mrna")));
    assertTrue(allowedSv(filter));
  }

  private boolean allowedSv(StructuralVariantFilter filter) {
    return evaluator.hasPermission(
        userOfStudyA, filter, "StructuralVariantFilter", AccessLevel.READ);
  }

  private StructuralVariantFilter profilesSv(String... profileIds) {
    StructuralVariantFilter filter = new StructuralVariantFilter();
    filter.setMolecularProfileIds(List.of(profileIds));
    return filter;
  }

  private boolean allowed(MolecularDataMultipleStudyFilter filter) {
    return evaluator.hasPermission(userOfStudyA, filter, TARGET_TYPE, AccessLevel.READ);
  }

  private MolecularDataMultipleStudyFilter byProfileIds(String... profileIds) {
    MolecularDataMultipleStudyFilter filter = new MolecularDataMultipleStudyFilter();
    filter.setMolecularProfileIds(List.of(profileIds));
    filter.setEntrezGeneIds(List.of(1));
    return filter;
  }

  private MolecularDataMultipleStudyFilter bySamples(String... profileIds) {
    MolecularDataMultipleStudyFilter filter = new MolecularDataMultipleStudyFilter();
    filter.setSampleMolecularIdentifiers(
        java.util.Arrays.stream(profileIds)
            .map(
                profileId -> {
                  SampleMolecularIdentifier identifier = new SampleMolecularIdentifier();
                  identifier.setMolecularProfileId(profileId);
                  identifier.setSampleId("sample");
                  return identifier;
                })
            .toList());
    filter.setEntrezGeneIds(List.of(1));
    return filter;
  }

  private CancerStudy study(String id) {
    CancerStudy study = new CancerStudy();
    study.setCancerStudyIdentifier(id);
    study.setGroups("");
    return study;
  }

  private MolecularProfile profile(String studyId, String stableId) {
    MolecularProfile profile = new MolecularProfile();
    profile.setStableId(stableId);
    profile.setCancerStudyIdentifier(studyId);
    return profile;
  }
}
