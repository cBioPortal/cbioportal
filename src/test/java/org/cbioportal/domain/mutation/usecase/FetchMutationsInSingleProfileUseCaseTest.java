package org.cbioportal.domain.mutation.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.cbioportal.domain.mutation.repository.MutationRepository;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.model.SampleList;
import org.cbioportal.legacy.model.meta.MutationMeta;
import org.cbioportal.legacy.persistence.SampleListRepository;
import org.cbioportal.legacy.service.MolecularProfileService;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.shared.MutationQueryOptions;
import org.cbioportal.shared.enums.ProjectionType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FetchMutationsInSingleProfileUseCaseTest {

  private static final String STUDY = "study_a";
  private static final String PROFILE = "study_a_mutations";
  private static final String SAMPLE_LIST = "study_a_all";
  private static final List<Integer> GENES = List.of(1, 2);
  private static final MutationQueryOptions OPTIONS =
      new MutationQueryOptions(ProjectionType.DETAILED, 100, 0, null, Direction.ASC);

  @InjectMocks private FetchMutationsInSingleProfileUseCase useCase;

  @Mock private MutationRepository mutationRepository;
  @Mock private MolecularProfileService molecularProfileService;
  @Mock private SampleListRepository sampleListRepository;

  @Before
  public void setUp() throws Exception {
    mockProfile(MolecularAlterationType.MUTATION_EXTENDED);
  }

  @Test
  public void sampleIdsAreQueriedAsPairsWithTheProfile() throws Exception {
    List<Mutation> expected = List.of(new Mutation());
    when(mutationRepository.getMutationsInMultipleMolecularProfiles(any(), any(), any(), any()))
        .thenReturn(expected);

    List<Mutation> result = useCase.execute(PROFILE, null, List.of("s1", "s2"), GENES, OPTIONS);

    Assert.assertSame(expected, result);
    verify(mutationRepository)
        .getMutationsInMultipleMolecularProfiles(
            eq(List.of(PROFILE, PROFILE)), eq(List.of("s1", "s2")), eq(GENES), eq(OPTIONS));
  }

  @Test
  public void sampleListOfTheProfilesStudyIsResolvedToSampleIds() throws Exception {
    mockSampleList(SAMPLE_LIST, STUDY, List.of("s1", "s2", "s3"));

    useCase.execute(PROFILE, SAMPLE_LIST, null, null, OPTIONS);

    verify(mutationRepository)
        .getMutationsInMultipleMolecularProfiles(
            eq(List.of(PROFILE, PROFILE, PROFILE)),
            eq(List.of("s1", "s2", "s3")),
            eq(null),
            eq(OPTIONS));
  }

  @Test
  public void sampleListOfAnotherStudyOrAnUnknownOneSelectsNothing() throws Exception {
    mockSampleList("other_all", "study_b", List.of("s1"));
    when(sampleListRepository.getSampleList("unknown")).thenReturn(null);

    Assert.assertTrue(useCase.execute(PROFILE, "other_all", null, GENES, OPTIONS).isEmpty());
    Assert.assertTrue(useCase.execute(PROFILE, "unknown", null, GENES, OPTIONS).isEmpty());
    verifyNoInteractions(mutationRepository);
  }

  @Test
  public void emptySampleSelectionNeverReachesTheRepository() throws Exception {
    // the repository treats an empty sample list as "all samples of the profile"
    mockSampleList(SAMPLE_LIST, STUDY, Collections.emptyList());

    Assert.assertTrue(useCase.execute(PROFILE, SAMPLE_LIST, null, GENES, OPTIONS).isEmpty());
    Assert.assertTrue(
        useCase.execute(PROFILE, null, Collections.emptyList(), GENES, OPTIONS).isEmpty());
    verifyNoInteractions(mutationRepository);
  }

  @Test
  public void metaIsEmptyForEmptySampleSelection() throws Exception {
    mockSampleList(SAMPLE_LIST, STUDY, Collections.emptyList());

    MutationMeta meta = useCase.executeMeta(PROFILE, SAMPLE_LIST, null, GENES);

    Assert.assertEquals(Integer.valueOf(0), meta.getTotalCount());
    Assert.assertEquals(Integer.valueOf(0), meta.getSampleCount());
    verifyNoInteractions(mutationRepository);
  }

  @Test
  public void metaIsReadFromTheRepository() throws Exception {
    MutationMeta expected = new MutationMeta();
    when(mutationRepository.getMetaMutationsInMultipleMolecularProfiles(any(), any(), any()))
        .thenReturn(expected);

    Assert.assertSame(expected, useCase.executeMeta(PROFILE, null, List.of("s1"), GENES));
    verify(mutationRepository)
        .getMetaMutationsInMultipleMolecularProfiles(
            eq(List.of(PROFILE)), eq(List.of("s1")), eq(GENES));
  }

  @Test
  public void nonMutationProfilesAreNotFound() throws Exception {
    for (MolecularAlterationType type :
        List.of(
            MolecularAlterationType.MRNA_EXPRESSION,
            MolecularAlterationType.COPY_NUMBER_ALTERATION,
            MolecularAlterationType.STRUCTURAL_VARIANT)) {
      mockProfile(type);
      try {
        useCase.execute(PROFILE, null, List.of("s1"), GENES, OPTIONS);
        Assert.fail("Expected MolecularProfileNotFoundException for " + type);
      } catch (MolecularProfileNotFoundException expected) {
        Assert.assertEquals(PROFILE, expected.getMolecularProfileId());
      }
    }
    verifyNoInteractions(mutationRepository);
  }

  @Test
  public void uncalledMutationProfilesAreAccepted() throws Exception {
    mockProfile(MolecularAlterationType.MUTATION_UNCALLED);

    useCase.execute(PROFILE, null, List.of("s1"), GENES, OPTIONS);

    verify(mutationRepository).getMutationsInMultipleMolecularProfiles(any(), any(), any(), any());
  }

  @Test(expected = MolecularProfileNotFoundException.class)
  public void unknownProfileIsNotFound() throws Exception {
    when(molecularProfileService.getMolecularProfile(PROFILE))
        .thenThrow(new MolecularProfileNotFoundException(PROFILE));

    useCase.execute(PROFILE, null, List.of("s1"), GENES, OPTIONS);
  }

  private void mockProfile(MolecularAlterationType type) throws MolecularProfileNotFoundException {
    MolecularProfile profile = new MolecularProfile();
    profile.setStableId(PROFILE);
    profile.setCancerStudyIdentifier(STUDY);
    profile.setMolecularAlterationType(type);
    when(molecularProfileService.getMolecularProfile(PROFILE)).thenReturn(profile);
  }

  private void mockSampleList(String id, String studyId, List<String> sampleIds) {
    SampleList sampleList = new SampleList();
    sampleList.setStableId(id);
    sampleList.setCancerStudyIdentifier(studyId);
    when(sampleListRepository.getSampleList(id)).thenReturn(sampleList);
    when(sampleListRepository.getAllSampleIdsInSampleList(id)).thenReturn(sampleIds);
  }
}
