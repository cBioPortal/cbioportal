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
import org.cbioportal.legacy.model.MutationSpectrum;
import org.cbioportal.legacy.service.MolecularProfileService;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FetchMutationSpectrumsUseCaseTest {

  private static final String PROFILE = "study_mutations";

  @InjectMocks private FetchMutationSpectrumsUseCase useCase;

  @Mock private MutationRepository mutationRepository;
  @Mock private MolecularProfileService molecularProfileService;

  @Test
  public void spectrumsOfMutationProfilesAreReadFromTheRepository() throws Exception {
    List<MutationSpectrum> expected = List.of(new MutationSpectrum());
    when(mutationRepository.getMutationSpectrums(any(), any(), any())).thenReturn(expected);

    for (MolecularAlterationType type :
        List.of(
            MolecularAlterationType.MUTATION_EXTENDED, MolecularAlterationType.MUTATION_UNCALLED)) {
      mockProfile(type);
      Assert.assertSame(expected, useCase.execute(PROFILE, "list", null));
    }
    verify(mutationRepository, org.mockito.Mockito.times(2))
        .getMutationSpectrums(eq(PROFILE), eq("list"), eq(null));
  }

  @Test
  public void sampleIdsArePassedOn() throws Exception {
    mockProfile(MolecularAlterationType.MUTATION_EXTENDED);

    useCase.execute(PROFILE, null, List.of("s1"));

    verify(mutationRepository).getMutationSpectrums(eq(PROFILE), eq(null), eq(List.of("s1")));
  }

  @Test
  public void emptySampleIdsNeverReachTheRepository() throws Exception {
    mockProfile(MolecularAlterationType.MUTATION_EXTENDED);

    Assert.assertTrue(useCase.execute(PROFILE, null, Collections.emptyList()).isEmpty());
    verifyNoInteractions(mutationRepository);
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
        useCase.execute(PROFILE, "list", null);
        Assert.fail("Expected MolecularProfileNotFoundException for " + type);
      } catch (MolecularProfileNotFoundException expected) {
        Assert.assertEquals(PROFILE, expected.getMolecularProfileId());
      }
    }
    verifyNoInteractions(mutationRepository);
  }

  private void mockProfile(MolecularAlterationType type) throws MolecularProfileNotFoundException {
    MolecularProfile profile = new MolecularProfile();
    profile.setStableId(PROFILE);
    profile.setMolecularAlterationType(type);
    when(molecularProfileService.getMolecularProfile(PROFILE)).thenReturn(profile);
  }
}
