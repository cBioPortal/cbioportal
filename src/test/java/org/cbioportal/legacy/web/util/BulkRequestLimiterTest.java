package org.cbioportal.legacy.web.util;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.MolecularProfileSamples;
import org.cbioportal.legacy.model.SampleList;
import org.cbioportal.legacy.persistence.MolecularDataRepository;
import org.cbioportal.legacy.service.MolecularProfileService;
import org.cbioportal.legacy.service.SampleListService;
import org.cbioportal.legacy.service.exception.BulkRequestTooLargeException;
import org.cbioportal.legacy.service.exception.SampleListNotFoundException;
import org.cbioportal.legacy.web.parameter.SampleMolecularIdentifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BulkRequestLimiterTest {

  @Mock private SampleListService sampleListService;
  @Mock private MolecularDataRepository molecularDataRepository;
  @Mock private MolecularProfileService molecularProfileService;

  private BulkRequestLimiter limiter;

  @Before
  public void setUp() {
    limiter = limiter(5_000_000L, 1_000_000L);
  }

  private BulkRequestLimiter limiter(long maxMatrixSize, long maxRows) {
    return new BulkRequestLimiter(
        sampleListService,
        molecularDataRepository,
        molecularProfileService,
        maxMatrixSize,
        maxRows,
        "See /llms.txt.");
  }

  private static List<Integer> genes(int n) {
    return IntStream.range(0, n).boxed().toList();
  }

  private static SampleList sampleListOf(int n) {
    SampleList list = new SampleList();
    list.setSampleIds(IntStream.range(0, n).mapToObj(i -> "S" + i).toList());
    return list;
  }

  private static SampleMolecularIdentifier id(String sampleId, String profileId) {
    SampleMolecularIdentifier identifier = new SampleMolecularIdentifier();
    identifier.setSampleId(sampleId);
    identifier.setMolecularProfileId(profileId);
    return identifier;
  }

  private static MolecularProfile profile(String profileId, String studyId) {
    MolecularProfile profile = new MolecularProfile();
    profile.setStableId(profileId);
    profile.setCancerStudyIdentifier(studyId);
    return profile;
  }

  @Test
  public void allGenesForAFewSamplesIsAllowed() {
    // The patient view's copy-number table: all genes for one patient's samples.
    limiter.checkMatrix(5, null);
  }

  @Test
  public void allGenesForAWholeStudyIsRejected() {
    BulkRequestTooLargeException e =
        assertThrows(BulkRequestTooLargeException.class, () -> limiter.checkMatrix(1000, null));
    assertTrue(e.getMessage().contains("without entrezGeneIds"));
    assertTrue(e.getMessage().endsWith("See /llms.txt."));
  }

  @Test
  public void geneListIsCountedExactly() {
    List<Integer> withinLimit = genes(500);
    List<Integer> overLimit = genes(501);
    limiter.checkMatrix(10_000, withinLimit);
    assertThrows(BulkRequestTooLargeException.class, () -> limiter.checkMatrix(10_000, overLimit));
  }

  @Test
  public void sampleListSizeIsResolved() throws Exception {
    when(sampleListService.getSampleList("big_all")).thenReturn(sampleListOf(1000));
    List<Integer> tenGenes = genes(10);

    assertThrows(
        BulkRequestTooLargeException.class,
        () -> limiter.checkMatrixForSampleList("big_all", null));
    limiter.checkMatrixForSampleList("big_all", tenGenes);
  }

  @Test
  public void missingSampleListIsLeftToTheDataQuery() throws Exception {
    when(sampleListService.getSampleList("nope"))
        .thenThrow(new SampleListNotFoundException("nope"));
    limiter.checkMatrixForSampleList("nope", null);
  }

  private static MolecularProfileSamples samplesOf(int n) {
    MolecularProfileSamples samples = new MolecularProfileSamples();
    samples.setCommaSeparatedSampleIds(
        String.join(",", IntStream.range(0, n).mapToObj(String::valueOf).toList()) + ",");
    return samples;
  }

  @Test
  public void wholeProfilesSumTheirSampleCountsInOneQuery() {
    when(molecularDataRepository.commaSeparatedSampleIdsOfMolecularProfilesMap(anySet()))
        .thenReturn(Map.of("a_mrna", samplesOf(3000), "b_mrna", samplesOf(2001)));
    List<String> profiles = List.of("a_mrna", "b_mrna");
    List<Integer> thousandGenes = genes(1000);

    // (3000 + 2001) samples x 1000 genes = 5,001,000 > 5,000,000
    assertThrows(
        BulkRequestTooLargeException.class,
        () -> limiter.checkMatrixForProfiles(profiles, thousandGenes));
    verify(molecularDataRepository, times(1))
        .commaSeparatedSampleIdsOfMolecularProfilesMap(anySet());
  }

  @Test
  public void absurdProfileListIsRejectedBeforeQuerying() {
    List<String> profiles = IntStream.range(0, 300).mapToObj(i -> "p" + i).toList();

    // 300 profiles x 20,000 genes (no gene list) > 5,000,000 even at one sample each
    assertThrows(
        BulkRequestTooLargeException.class, () -> limiter.checkMatrixForProfiles(profiles, null));
    verifyNoInteractions(molecularDataRepository);
  }

  @Test
  public void identifiersCountEverySampleInEveryProfileOfTheStudy() {
    // 2 samples x 2 profiles in one study = 4 sample/profile pairs, though only 2 identifiers.
    when(molecularProfileService.getMolecularProfiles(anySet(), eq("SUMMARY")))
        .thenReturn(List.of(profile("s_mrna", "s"), profile("s_cna", "s")));
    List<SampleMolecularIdentifier> identifiers = List.of(id("S1", "s_mrna"), id("S2", "s_cna"));
    BulkRequestLimiter strict = limiter(3L, -1L);
    List<Integer> oneGene = genes(1);

    assertThrows(
        BulkRequestTooLargeException.class,
        () -> strict.checkMatrixForSampleMolecularIdentifiers(identifiers, oneGene));
  }

  @Test
  public void rowLimit() {
    limiter.checkRows(1_000_000);
    assertThrows(BulkRequestTooLargeException.class, () -> limiter.checkRows(1_000_001));
  }

  @Test
  public void disabledByDefault() {
    BulkRequestLimiter disabled = limiter(-1L, -1L);
    List<SampleMolecularIdentifier> identifiers = List.of(id("S1", "p"));

    disabled.checkMatrix(1_000_000, null);
    disabled.checkMatrixForSampleList("big_all", null);
    disabled.checkMatrixForProfiles(List.of("p"), null);
    disabled.checkMatrixForSampleMolecularIdentifiers(identifiers, null);
    disabled.checkRows(Long.MAX_VALUE);
    verifyNoInteractions(sampleListService, molecularDataRepository, molecularProfileService);
  }
}
