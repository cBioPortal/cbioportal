package org.cbioportal.domain.gene_panel_data.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.cbioportal.domain.gene_panel_data.repository.GenePanelDataRepository;
import org.cbioportal.legacy.model.GenePanelData;
import org.cbioportal.legacy.web.parameter.GenePanelDataMultipleStudyFilter;
import org.cbioportal.legacy.web.parameter.SampleMolecularIdentifier;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FetchGenePanelDataUseCaseTest {

  @InjectMocks private FetchGenePanelDataUseCase useCase;

  @Mock private GenePanelDataRepository repository;

  @Test
  public void profileIdsAreDeduplicatedAndQueriedByProfile() {
    List<GenePanelData> expected = List.of(new GenePanelData());
    when(repository.getGenePanelDataByProfileIds(any())).thenReturn(expected);
    GenePanelDataMultipleStudyFilter filter = new GenePanelDataMultipleStudyFilter();
    filter.setMolecularProfileIds(List.of("a", "b", "a"));

    Assert.assertSame(expected, useCase.execute(filter));

    verify(repository).getGenePanelDataByProfileIds(eq(Set.of("a", "b")));
  }

  @Test
  public void identifiersAreQueriedAsPairs() {
    SampleMolecularIdentifier identifier = new SampleMolecularIdentifier();
    identifier.setMolecularProfileId("a");
    identifier.setSampleId("s1");
    GenePanelDataMultipleStudyFilter filter = new GenePanelDataMultipleStudyFilter();
    filter.setSampleMolecularIdentifiers(List.of(identifier));

    useCase.execute(filter);

    verify(repository).getGenePanelDataByProfileSamplePairs(eq(List.of(identifier)));
  }

  @Test
  public void emptyProfileIdsFallBackToIdentifiersAndNothingYieldsAnEmptyResult() {
    GenePanelDataMultipleStudyFilter emptyProfiles = new GenePanelDataMultipleStudyFilter();
    emptyProfiles.setMolecularProfileIds(List.of());
    Assert.assertTrue(useCase.execute(emptyProfiles).isEmpty());

    GenePanelDataMultipleStudyFilter emptyIdentifiers = new GenePanelDataMultipleStudyFilter();
    emptyIdentifiers.setSampleMolecularIdentifiers(List.of());
    Assert.assertTrue(useCase.execute(emptyIdentifiers).isEmpty());

    verifyNoInteractions(repository);
  }
}
