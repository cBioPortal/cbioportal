package org.cbioportal.domain.mutation.usecase;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.cbioportal.domain.mutation.repository.MutationRepository;
import org.cbioportal.legacy.model.meta.MutationMeta;
import org.cbioportal.legacy.web.parameter.MutationMultipleStudyFilter;
import org.cbioportal.legacy.web.parameter.SampleMolecularIdentifier;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FetchMetaMutationsUseCaseTest {

  @InjectMocks private FetchMetaMutationsUseCase fetchMetaMutationsUseCase;
  @Mock private MutationRepository mutationRepository;

  private static @NotNull List<SampleMolecularIdentifier> getSampleMolecularIdentifiers() {
    var sampleMolecularIdentifier1 = new SampleMolecularIdentifier();
    var sampleMolecularIdentifier2 = new SampleMolecularIdentifier();
    sampleMolecularIdentifier1.setSampleId("TCGA-A1-A0SH-01");
    sampleMolecularIdentifier1.setMolecularProfileId("study_tcga_pub_mutations");
    sampleMolecularIdentifier2.setSampleId("TCGA-A1-A0SO-01");
    sampleMolecularIdentifier2.setMolecularProfileId("study_tcga_pub_mutations");
    return List.of(sampleMolecularIdentifier1, sampleMolecularIdentifier2);
  }

  @Test
  public void testExecuteWithGetMolecularProfileIdsNull() {
    MutationMultipleStudyFilter mutationMultipleStudyFilter;
    mutationMultipleStudyFilter = new MutationMultipleStudyFilter();
    mutationMultipleStudyFilter.setSampleMolecularIdentifiers(getSampleMolecularIdentifiers());
    mutationMultipleStudyFilter.setEntrezGeneIds(List.of(672));

    when(mutationRepository.getMetaMutationsInMultipleMolecularProfiles(
            anyList(), any(), anyList()))
        .thenReturn(new MutationMeta());

    MutationMeta result = fetchMetaMutationsUseCase.execute(mutationMultipleStudyFilter);
    verify(mutationRepository)
        .getMetaMutationsInMultipleMolecularProfiles(
            eq(List.of("study_tcga_pub_mutations", "study_tcga_pub_mutations")),
            eq(List.of("TCGA-A1-A0SH-01", "TCGA-A1-A0SO-01")),
            eq(mutationMultipleStudyFilter.getEntrezGeneIds()));
    assertNotNull(result);
  }

  @Test
  public void testExecuteWithGetMolecularProfileIdsNotNull() {
    MutationMultipleStudyFilter mutationMultipleStudyFilter;
    mutationMultipleStudyFilter = new MutationMultipleStudyFilter();
    mutationMultipleStudyFilter.setMolecularProfileIds(
        List.of("study_tcga_pub_mutations", "TCGA-A1-A0SO-01"));
    mutationMultipleStudyFilter.setEntrezGeneIds(List.of(672));

    when(mutationRepository.getMetaMutationsInMultipleMolecularProfiles(
            anyList(), any(), anyList()))
        .thenReturn(new MutationMeta());

    MutationMeta result = fetchMetaMutationsUseCase.execute(mutationMultipleStudyFilter);
    verify(mutationRepository)
        .getMetaMutationsInMultipleMolecularProfiles(
            eq(mutationMultipleStudyFilter.getMolecularProfileIds()),
            isNull(),
            eq(mutationMultipleStudyFilter.getEntrezGeneIds()));
    assertNotNull(result);
  }
}
