package org.cbioportal.domain.discrete_copy_number.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.cbioportal.domain.discrete_copy_number.repository.DiscreteCopyNumberRepository;
import org.cbioportal.domain.genomic_data.usecase.FetchGeneMolecularDataUseCase;
import org.cbioportal.legacy.model.DiscreteCopyNumberData;
import org.cbioportal.legacy.model.Gene;
import org.cbioportal.legacy.model.GeneMolecularData;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfile.MolecularAlterationType;
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
public class FetchDiscreteCopyNumbersUseCaseTest {

  private static final String PROFILE = "study_gistic";
  private static final List<Integer> GENES = List.of(1);
  private static final List<Integer> HOMDEL_AND_AMP = List.of(-2, 2);
  private static final List<Integer> ALL = List.of(-2, -1, 0, 1, 2);

  @InjectMocks private FetchDiscreteCopyNumbersUseCase useCase;

  @Mock private DiscreteCopyNumberRepository repository;
  @Mock private MolecularProfileService molecularProfileService;
  @Mock private FetchGeneMolecularDataUseCase fetchGeneMolecularDataUseCase;

  @Before
  public void setUp() throws Exception {
    mockProfile(MolecularAlterationType.COPY_NUMBER_ALTERATION, "DISCRETE");
  }

  @Test
  public void homdelAndAmpAreReadFromTheCnaEvents() throws Exception {
    List<DiscreteCopyNumberData> expected = List.of(new DiscreteCopyNumberData());
    when(repository.getDiscreteCopyNumbers(any(), any(), any(), any(), any(), anyBoolean()))
        .thenReturn(expected);

    List<DiscreteCopyNumberData> result =
        useCase.execute(PROFILE, "list", null, GENES, HOMDEL_AND_AMP, true);

    Assert.assertSame(expected, result);
    verify(repository)
        .getDiscreteCopyNumbers(
            eq(PROFILE), eq("list"), eq(null), eq(GENES), eq(HOMDEL_AND_AMP), eq(true));
    verifyNoInteractions(fetchGeneMolecularDataUseCase);
  }

  @Test
  public void otherAlterationsAreDerivedFromTheProfilesGeneLevelValues() throws Exception {
    Gene gene = new Gene();
    gene.setEntrezGeneId(1);
    when(fetchGeneMolecularDataUseCase.executeBySampleList(any(), any(), any(), anyBoolean()))
        .thenReturn(
            List.of(
                value("s1", "-2", gene),
                value("s2", "-1", gene),
                value("s3", "0", gene),
                value("s4", "1", gene),
                value("s5", "2", gene),
                value("s6", "1.5", gene),
                value("s7", "NA", gene),
                value("s8", null, gene)));

    List<DiscreteCopyNumberData> gainsAndLosses =
        useCase.execute(PROFILE, "list", null, GENES, List.of(-1, 1), true);

    Assert.assertEquals(
        List.of("s2", "s4"),
        gainsAndLosses.stream().map(DiscreteCopyNumberData::getSampleId).toList());
    DiscreteCopyNumberData first = gainsAndLosses.get(0);
    Assert.assertEquals(Integer.valueOf(-1), first.getAlteration());
    Assert.assertEquals("study", first.getStudyId());
    Assert.assertEquals("patient", first.getPatientId());
    Assert.assertEquals(PROFILE, first.getMolecularProfileId());
    Assert.assertSame(gene, first.getGene());
    Assert.assertEquals(5, useCase.count(PROFILE, "list", null, GENES, ALL));
    verifyNoInteractions(repository);
  }

  @Test
  public void sampleIdsAreUsedWhenNoSampleListIsGiven() throws Exception {
    useCase.execute(PROFILE, null, List.of("s1"), GENES, ALL, false);

    verify(fetchGeneMolecularDataUseCase)
        .executeBySampleIds(eq(PROFILE), eq(List.of("s1")), eq(GENES), eq(false));
  }

  @Test
  public void countOfHomdelAndAmpIsReadFromTheRepository() throws Exception {
    when(repository.countDiscreteCopyNumbers(any(), any(), any(), any(), any())).thenReturn(7);

    Assert.assertEquals(7, useCase.count(PROFILE, "list", null, GENES, HOMDEL_AND_AMP));
  }

  @Test
  public void onlyDiscreteCopyNumberProfilesAreAccepted() throws Exception {
    mockProfile(MolecularAlterationType.COPY_NUMBER_ALTERATION, "CONTINUOUS");
    assertNotFound();
    mockProfile(MolecularAlterationType.MRNA_EXPRESSION, "DISCRETE");
    assertNotFound();
    mockProfile(MolecularAlterationType.MUTATION_EXTENDED, null);
    assertNotFound();
    verifyNoInteractions(repository);
    verifyNoInteractions(fetchGeneMolecularDataUseCase);
  }

  private void assertNotFound() throws Exception {
    try {
      useCase.execute(PROFILE, "list", null, GENES, HOMDEL_AND_AMP, false);
      Assert.fail("Expected MolecularProfileNotFoundException");
    } catch (MolecularProfileNotFoundException expected) {
      Assert.assertEquals(PROFILE, expected.getMolecularProfileId());
    }
  }

  private GeneMolecularData value(String sampleId, String value, Gene gene) {
    GeneMolecularData data = new GeneMolecularData();
    data.setMolecularProfileId(PROFILE);
    data.setSampleId(sampleId);
    data.setPatientId("patient");
    data.setStudyId("study");
    data.setEntrezGeneId(1);
    data.setGene(gene);
    data.setValue(value);
    return data;
  }

  private void mockProfile(MolecularAlterationType type, String datatype)
      throws MolecularProfileNotFoundException {
    MolecularProfile profile = new MolecularProfile();
    profile.setStableId(PROFILE);
    profile.setMolecularAlterationType(type);
    profile.setDatatype(datatype);
    when(molecularProfileService.getMolecularProfile(PROFILE)).thenReturn(profile);
  }
}
