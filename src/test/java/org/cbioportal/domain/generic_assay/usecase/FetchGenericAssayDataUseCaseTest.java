package org.cbioportal.domain.generic_assay.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.cbioportal.domain.generic_assay.repository.GenericAssayDataRepository;
import org.cbioportal.legacy.model.GenericAssayData;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.legacy.persistence.SampleListRepository;
import org.cbioportal.legacy.service.MolecularProfileService;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FetchGenericAssayDataUseCaseTest {

  private static final String STUDY = "study_a";
  private static final String TYPE = "treatment_ic50";
  private static final String PROFILE = STUDY + "_" + TYPE;

  @InjectMocks private FetchGenericAssayDataUseCase useCase;

  @Mock private GenericAssayDataRepository repository;
  @Mock private MolecularProfileService molecularProfileService;
  @Mock private SampleListRepository sampleListRepository;

  @Before
  public void setUp() throws Exception {
    mockProfile(MolecularAlterationType.GENERIC_ASSAY);
  }

  @Test
  public void derivesStudyAndProfileTypeFromTheProfile() throws Exception {
    List<GenericAssayData> expected = List.of(new GenericAssayData());
    when(repository.getGenericAssayData(any(), any(), any(), any(), any())).thenReturn(expected);

    List<GenericAssayData> result =
        useCase.execute(PROFILE, null, List.of("s1"), List.of("entity"));

    Assert.assertSame(expected, result);
    verify(repository)
        .getGenericAssayData(
            eq(STUDY), eq(PROFILE), eq(TYPE), eq(List.of("s1")), eq(List.of("entity")));
  }

  @Test
  public void aSampleListIsResolvedToSampleIdsOfTheProfilesStudy() throws Exception {
    when(sampleListRepository.getAllSampleIdsInSampleList("list")).thenReturn(List.of("s1", "s2"));

    useCase.execute(PROFILE, "list", null, null);

    verify(repository)
        .getGenericAssayData(eq(STUDY), eq(PROFILE), eq(TYPE), eq(List.of("s1", "s2")), eq(null));
  }

  @Test
  public void emptySampleSelectionNeverReachesTheRepository() throws Exception {
    when(sampleListRepository.getAllSampleIdsInSampleList("list"))
        .thenReturn(Collections.emptyList());

    Assert.assertTrue(useCase.execute(PROFILE, "list", null, null).isEmpty());
    Assert.assertTrue(useCase.execute(PROFILE, null, Collections.emptyList(), null).isEmpty());
    verifyNoInteractions(repository);
  }

  @Test
  public void onlyGenericAssayProfilesAreAccepted() throws Exception {
    for (MolecularAlterationType type :
        List.of(
            MolecularAlterationType.MRNA_EXPRESSION,
            MolecularAlterationType.MUTATION_EXTENDED,
            MolecularAlterationType.GENESET_SCORE)) {
      mockProfile(type);
      try {
        useCase.execute(PROFILE, "list", null, null);
        Assert.fail("Expected MolecularProfileNotFoundException for " + type);
      } catch (MolecularProfileNotFoundException expected) {
        Assert.assertEquals(PROFILE, expected.getMolecularProfileId());
      }
    }
    verifyNoInteractions(repository);
  }

  private void mockProfile(MolecularAlterationType type) throws MolecularProfileNotFoundException {
    MolecularProfile profile = new MolecularProfile();
    profile.setStableId(PROFILE);
    profile.setCancerStudyIdentifier(STUDY);
    profile.setMolecularAlterationType(type);
    when(molecularProfileService.getMolecularProfile(PROFILE)).thenReturn(profile);
  }
}
