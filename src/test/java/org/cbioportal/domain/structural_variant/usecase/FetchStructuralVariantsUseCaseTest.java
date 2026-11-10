package org.cbioportal.domain.structural_variant.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.cbioportal.domain.structural_variant.repository.StructuralVariantRepository;
import org.cbioportal.legacy.model.StructuralVariant;
import org.cbioportal.legacy.web.parameter.SampleMolecularIdentifier;
import org.cbioportal.legacy.web.parameter.StructuralVariantFilter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FetchStructuralVariantsUseCaseTest {

  @InjectMocks private FetchStructuralVariantsUseCase useCase;

  @Mock private StructuralVariantRepository repository;

  @Test
  public void profileIdsAreQueriedWithoutSamples() {
    List<StructuralVariant> expected = List.of(new StructuralVariant());
    when(repository.fetchStructuralVariants(any(), any(), any(), any())).thenReturn(expected);
    StructuralVariantFilter filter = new StructuralVariantFilter();
    filter.setMolecularProfileIds(List.of("a", "b"));
    filter.setEntrezGeneIds(List.of(1));

    Assert.assertSame(expected, useCase.execute(filter));

    verify(repository)
        .fetchStructuralVariants(eq(List.of("a", "b")), isNull(), eq(List.of(1)), isNull());
  }

  @Test
  public void identifiersAreSplitIntoParallelProfileAndSampleLists() {
    StructuralVariantFilter filter = new StructuralVariantFilter();
    filter.setSampleMolecularIdentifiers(List.of(identifier("a", "s1"), identifier("b", "s2")));

    useCase.execute(filter);

    verify(repository)
        .fetchStructuralVariants(
            eq(List.of("a", "b")), eq(List.of("s1", "s2")), isNull(), isNull());
  }

  @Test
  public void identifiersTakePrecedenceOverProfileIds() {
    StructuralVariantFilter filter = new StructuralVariantFilter();
    filter.setMolecularProfileIds(List.of("ignored"));
    filter.setSampleMolecularIdentifiers(List.of(identifier("a", "s1")));

    useCase.execute(filter);

    verify(repository)
        .fetchStructuralVariants(eq(List.of("a")), eq(List.of("s1")), isNull(), isNull());
  }

  @Test
  public void noProfilesYieldAnEmptyResultWithoutQuerying() {
    Assert.assertTrue(useCase.execute(new StructuralVariantFilter()).isEmpty());

    StructuralVariantFilter emptyIdentifiers = new StructuralVariantFilter();
    emptyIdentifiers.setSampleMolecularIdentifiers(List.of());
    emptyIdentifiers.setMolecularProfileIds(List.of("a"));
    Assert.assertTrue(useCase.execute(emptyIdentifiers).isEmpty());

    verifyNoInteractions(repository);
  }

  private SampleMolecularIdentifier identifier(String profileId, String sampleId) {
    SampleMolecularIdentifier identifier = new SampleMolecularIdentifier();
    identifier.setMolecularProfileId(profileId);
    identifier.setSampleId(sampleId);
    return identifier;
  }
}
