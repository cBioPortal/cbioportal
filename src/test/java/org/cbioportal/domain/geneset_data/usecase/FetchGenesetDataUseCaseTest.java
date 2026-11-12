package org.cbioportal.domain.geneset_data.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.cbioportal.domain.geneset_data.repository.GenesetDataRepository;
import org.cbioportal.legacy.model.GenesetMolecularData;
import org.cbioportal.legacy.model.SampleList;
import org.cbioportal.legacy.persistence.SampleListRepository;
import org.cbioportal.legacy.service.MolecularProfileService;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.service.exception.SampleListNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FetchGenesetDataUseCaseTest {

  private static final String PROFILE = "study_gsva_scores";
  private static final List<String> GENESETS = List.of("set1");

  @InjectMocks private FetchGenesetDataUseCase useCase;

  @Mock private GenesetDataRepository repository;
  @Mock private MolecularProfileService molecularProfileService;
  @Mock private SampleListRepository sampleListRepository;

  @Test
  public void sampleIdsArePassedOnAndNullMeansAllSamples() throws Exception {
    List<GenesetMolecularData> expected = List.of(new GenesetMolecularData());
    when(repository.getGenesetData(any(), any(), any())).thenReturn(expected);

    Assert.assertSame(expected, useCase.execute(PROFILE, null, List.of("s1"), GENESETS));
    useCase.execute(PROFILE, null, null, null);

    verify(repository).getGenesetData(eq(PROFILE), eq(List.of("s1")), eq(GENESETS));
    verify(repository).getGenesetData(eq(PROFILE), eq(null), eq(null));
  }

  @Test
  public void aSampleListIsResolvedToSampleIdsOfTheProfilesStudy() throws Exception {
    when(sampleListRepository.getSampleList("list")).thenReturn(new SampleList());
    when(sampleListRepository.getAllSampleIdsInSampleList("list")).thenReturn(List.of("s1", "s2"));

    useCase.execute(PROFILE, "list", List.of("ignored"), GENESETS);

    verify(repository).getGenesetData(eq(PROFILE), eq(List.of("s1", "s2")), eq(GENESETS));
  }

  @Test(expected = SampleListNotFoundException.class)
  public void unknownSampleListIsNotFound() throws Exception {
    when(sampleListRepository.getSampleList("nope")).thenReturn(null);

    useCase.execute(PROFILE, "nope", null, GENESETS);
  }

  @Test
  public void emptySampleSelectionNeverReachesTheRepository() throws Exception {
    when(sampleListRepository.getSampleList("list")).thenReturn(new SampleList());
    when(sampleListRepository.getAllSampleIdsInSampleList("list"))
        .thenReturn(Collections.emptyList());

    Assert.assertTrue(useCase.execute(PROFILE, "list", null, GENESETS).isEmpty());
    Assert.assertTrue(useCase.execute(PROFILE, null, Collections.emptyList(), GENESETS).isEmpty());
    verifyNoInteractions(repository);
  }

  @Test(expected = MolecularProfileNotFoundException.class)
  public void unknownProfileIsNotFound() throws Exception {
    when(molecularProfileService.getMolecularProfile(PROFILE))
        .thenThrow(new MolecularProfileNotFoundException(PROFILE));

    useCase.execute(PROFILE, null, List.of("s1"), GENESETS);
  }
}
