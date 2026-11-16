package org.cbioportal.domain.genomic_data.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.cbioportal.domain.genomic_data.repository.GeneMolecularDataRepository;
import org.cbioportal.legacy.model.GeneMolecularData;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.legacy.service.MolecularProfileService;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FetchGeneMolecularDataUseCaseTest {

  private static final String STUDY_ID = "test_study";
  private static final String PROFILE_TYPE = "rna_seq_v2_mrna";
  private static final String PROFILE_ID = STUDY_ID + "_" + PROFILE_TYPE;
  private static final String SAMPLE_LIST_ID = "test_study_all";
  private static final List<Integer> GENES = List.of(1, 2);
  private static final List<String> SAMPLES = List.of("s1", "s2");

  @InjectMocks private FetchGeneMolecularDataUseCase useCase;

  @Mock private GeneMolecularDataRepository repository;
  @Mock private MolecularProfileService molecularProfileService;

  @Test
  public void executeBySampleListDerivesStudyAndProfileTypeFromProfile() throws Exception {
    mockProfile(MolecularAlterationType.MRNA_EXPRESSION);
    List<GeneMolecularData> expected = List.of(new GeneMolecularData());
    when(repository.getGeneMolecularData(any(), any(), any(), any(), any(), any(), anyBoolean()))
        .thenReturn(expected);

    List<GeneMolecularData> result =
        useCase.executeBySampleList(PROFILE_ID, SAMPLE_LIST_ID, GENES, true);

    Assert.assertSame(expected, result);
    verify(repository)
        .getGeneMolecularData(
            eq(STUDY_ID),
            eq(PROFILE_ID),
            eq(PROFILE_TYPE),
            eq(GENES),
            eq(SAMPLE_LIST_ID),
            eq(null),
            eq(true));
  }

  @Test
  public void executeBySampleIdsPassesSampleIdsAndNoSampleList() throws Exception {
    mockProfile(MolecularAlterationType.COPY_NUMBER_ALTERATION);

    useCase.executeBySampleIds(PROFILE_ID, SAMPLES, null, false);

    verify(repository)
        .getGeneMolecularData(
            eq(STUDY_ID),
            eq(PROFILE_ID),
            eq(PROFILE_TYPE),
            eq(null),
            eq(null),
            eq(SAMPLES),
            eq(false));
  }

  @Test
  public void executeBySampleIdsWithEmptySampleIdsReturnsEmptyWithoutQuerying() throws Exception {
    mockProfile(MolecularAlterationType.MRNA_EXPRESSION);

    List<GeneMolecularData> result =
        useCase.executeBySampleIds(PROFILE_ID, Collections.emptyList(), GENES, false);

    Assert.assertTrue(result.isEmpty());
    verifyNoInteractions(repository);
  }

  @Test
  public void mutationAndStructuralVariantProfilesAreNotFound() throws Exception {
    for (MolecularAlterationType type :
        List.of(
            MolecularAlterationType.MUTATION_EXTENDED,
            MolecularAlterationType.MUTATION_UNCALLED,
            MolecularAlterationType.STRUCTURAL_VARIANT)) {
      mockProfile(type);
      try {
        useCase.executeBySampleList(PROFILE_ID, SAMPLE_LIST_ID, GENES, false);
        Assert.fail("Expected MolecularProfileNotFoundException for " + type);
      } catch (MolecularProfileNotFoundException expected) {
        Assert.assertEquals(PROFILE_ID, expected.getMolecularProfileId());
      }
    }
    verifyNoInteractions(repository);
  }

  @Test(expected = MolecularProfileNotFoundException.class)
  public void unknownProfileIsNotFound() throws Exception {
    when(molecularProfileService.getMolecularProfile(PROFILE_ID))
        .thenThrow(new MolecularProfileNotFoundException(PROFILE_ID));

    useCase.executeBySampleList(PROFILE_ID, SAMPLE_LIST_ID, GENES, false);
  }

  @Test
  public void inMultipleProfilesWithoutSamplesReadsAllSamplesOfTheResolvedProfiles() {
    MolecularProfile profile = profile("study_a", "study_a_mrna");
    when(molecularProfileService.getMolecularProfiles(any(), eq("SUMMARY")))
        .thenReturn(List.of(profile));

    useCase.executeInMultipleProfiles(List.of("study_a_mrna", "study_a_mrna"), null, GENES, true);

    verify(molecularProfileService).getMolecularProfiles(eq(Set.of("study_a_mrna")), eq("SUMMARY"));
    verify(repository)
        .getGeneMolecularDataInMultipleProfiles(
            eq(List.of(profile)), eq(GENES), isNull(), isNull(), eq(true));
  }

  @Test
  public void inMultipleProfilesMapsEachSampleToTheStudyOfItsProfile() {
    when(molecularProfileService.getMolecularProfiles(any(), eq("SUMMARY")))
        .thenReturn(
            List.of(profile("study_a", "study_a_mrna"), profile("study_b", "study_b_mrna")));

    useCase.executeInMultipleProfiles(
        List.of("study_a_mrna", "study_b_mrna", "unknown_profile"),
        List.of("s1", "s2", "s3"),
        GENES,
        false);

    verify(repository)
        .getGeneMolecularDataInMultipleProfiles(
            any(),
            eq(GENES),
            eq(List.of("s1", "s2")),
            eq(List.of("study_a", "study_b")),
            eq(false));
  }

  @Test
  public void inMultipleProfilesWithoutGenesOrKnownProfilesReturnsEmptyWithoutQuerying() {
    Assert.assertTrue(
        useCase.executeInMultipleProfiles(List.of("study_a_mrna"), null, null, false).isEmpty());
    Assert.assertTrue(
        useCase
            .executeInMultipleProfiles(
                List.of("study_a_mrna"), null, Collections.emptyList(), false)
            .isEmpty());

    when(molecularProfileService.getMolecularProfiles(any(), any()))
        .thenReturn(Collections.emptyList());
    Assert.assertTrue(
        useCase.executeInMultipleProfiles(List.of("unknown"), null, GENES, false).isEmpty());
    Assert.assertTrue(
        useCase
            .executeInMultipleProfiles(List.of("unknown"), List.of("s1"), GENES, false)
            .isEmpty());
    verifyNoInteractions(repository);
  }

  private MolecularProfile profile(String studyId, String stableId) {
    MolecularProfile profile = new MolecularProfile();
    profile.setStableId(stableId);
    profile.setCancerStudyIdentifier(studyId);
    return profile;
  }

  private void mockProfile(MolecularAlterationType type) throws MolecularProfileNotFoundException {
    MolecularProfile profile = new MolecularProfile();
    profile.setStableId(PROFILE_ID);
    profile.setCancerStudyIdentifier(STUDY_ID);
    profile.setMolecularAlterationType(type);
    when(molecularProfileService.getMolecularProfile(PROFILE_ID)).thenReturn(profile);
  }
}
