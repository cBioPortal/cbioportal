package org.cbioportal.domain.mutation.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.cbioportal.domain.mutation.PatientSimilarityScore;
import org.cbioportal.domain.mutation.repository.MutationRepository;
import org.cbioportal.legacy.model.Gene;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.web.parameter.PatientGenomicSimilarityRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GetPatientGenomicSimilarityUseCaseTest {

  private MutationRepository mutationRepository;
  private GetPatientGenomicSimilarityUseCase useCase;

  @BeforeEach
  void setUp() {
    mutationRepository = mock(MutationRepository.class);
    useCase = new GetPatientGenomicSimilarityUseCase(mutationRepository);
  }

  // ── Happy path ────────────────────────────────────────────────────────────

  @Test
  void execute_ranksPatientsByDescendingJaccardSimilarity() {
    // Reference patient P1 has TP53 + KRAS
    // Candidate P2 has TP53 + KRAS + BRAF  → Jaccard = 2/3 ≈ 0.667
    // Candidate P3 has TP53 only            → Jaccard = 1/2 = 0.5
    // Candidate P4 has EGFR only            → Jaccard = 0/3 = 0.0
    when(mutationRepository.getMutationsInMultipleMolecularProfiles(
            eq(List.of("brca_mutations")), any(), any(), any()))
        .thenReturn(
            List.of(
                mutation("P1", "TP53"),
                mutation("P1", "KRAS"),
                mutation("P2", "TP53"),
                mutation("P2", "KRAS"),
                mutation("P2", "BRAF"),
                mutation("P3", "TP53"),
                mutation("P4", "EGFR")));

    PatientGenomicSimilarityRequest request =
        buildRequest("P1", List.of("TP53", "KRAS", "BRAF", "EGFR"), 10);
    List<PatientSimilarityScore> result = useCase.execute(request);

    assertEquals(3, result.size());
    assertEquals("P2", result.get(0).patientId());
    assertEquals("P3", result.get(1).patientId());
    assertEquals("P4", result.get(2).patientId());

    assertEquals(2.0 / 3.0, result.get(0).similarityScore(), 1e-9);
    assertEquals(0.5, result.get(1).similarityScore(), 1e-9);
    assertEquals(0.0, result.get(2).similarityScore(), 1e-9);
  }

  @Test
  void execute_referencePatientNotIncludedInResults() {
    when(mutationRepository.getMutationsInMultipleMolecularProfiles(any(), any(), any(), any()))
        .thenReturn(List.of(mutation("P1", "TP53"), mutation("P2", "TP53")));

    PatientGenomicSimilarityRequest request = buildRequest("P1", List.of("TP53"), 10);
    List<PatientSimilarityScore> result = useCase.execute(request);

    assertTrue(result.stream().noneMatch(s -> "P1".equals(s.patientId())));
  }

  @Test
  void execute_honorsTopNLimit() {
    when(mutationRepository.getMutationsInMultipleMolecularProfiles(any(), any(), any(), any()))
        .thenReturn(
            List.of(
                mutation("REF", "TP53"),
                mutation("P1", "TP53"),
                mutation("P2", "TP53"),
                mutation("P3", "TP53")));

    PatientGenomicSimilarityRequest request = buildRequest("REF", List.of("TP53"), 2);
    List<PatientSimilarityScore> result = useCase.execute(request);

    assertEquals(2, result.size());
  }

  @Test
  void execute_commonMutatedGenesIsCorrectIntersection() {
    when(mutationRepository.getMutationsInMultipleMolecularProfiles(any(), any(), any(), any()))
        .thenReturn(
            List.of(
                mutation("REF", "TP53"),
                mutation("REF", "KRAS"),
                mutation("P1", "TP53"),
                mutation("P1", "BRAF")));

    PatientGenomicSimilarityRequest request =
        buildRequest("REF", List.of("TP53", "KRAS", "BRAF"), 10);
    List<PatientSimilarityScore> result = useCase.execute(request);

    assertEquals(1, result.size());
    // Only TP53 is in both REF and P1; list must be sorted alphabetically
    assertEquals(List.of("TP53"), result.get(0).commonMutatedGenes());
  }

  @Test
  void execute_commonMutatedGenesAreSortedAlphabetically() {
    when(mutationRepository.getMutationsInMultipleMolecularProfiles(any(), any(), any(), any()))
        .thenReturn(
            List.of(
                mutation("REF", "TP53"),
                mutation("REF", "KRAS"),
                mutation("REF", "BRAF"),
                mutation("P1", "TP53"),
                mutation("P1", "KRAS"),
                mutation("P1", "BRAF")));

    PatientGenomicSimilarityRequest request =
        buildRequest("REF", List.of("TP53", "KRAS", "BRAF"), 10);
    List<PatientSimilarityScore> result = useCase.execute(request);

    assertEquals(1, result.size());
    // Must be sorted regardless of HashSet iteration order
    assertEquals(List.of("BRAF", "KRAS", "TP53"), result.get(0).commonMutatedGenes());
  }

  @Test
  void execute_genesOutsideRequestedSetIgnored() {
    // PTEN is NOT in the requested gene set; it must not affect similarity
    when(mutationRepository.getMutationsInMultipleMolecularProfiles(any(), any(), any(), any()))
        .thenReturn(
            List.of(
                mutation("REF", "TP53"),
                mutation("P1", "TP53"),
                mutation("P1", "PTEN"))); // PTEN outside filter

    PatientGenomicSimilarityRequest request = buildRequest("REF", List.of("TP53"), 10);
    List<PatientSimilarityScore> result = useCase.execute(request);

    assertEquals(1, result.size());
    // Jaccard = 1/1 = 1.0 (PTEN ignored)
    assertEquals(1.0, result.get(0).similarityScore(), 1e-9);
  }

  @Test
  void execute_referenceWithNoMutationsGivesZeroScoresToAll() {
    when(mutationRepository.getMutationsInMultipleMolecularProfiles(any(), any(), any(), any()))
        .thenReturn(List.of(mutation("P1", "TP53"), mutation("P2", "KRAS")));
    // REF has no mutations in the queried genes

    PatientGenomicSimilarityRequest request = buildRequest("REF", List.of("TP53", "KRAS"), 10);
    List<PatientSimilarityScore> result = useCase.execute(request);

    // P1 and P2 each have score 0.0; REF itself is absent from results
    assertEquals(2, result.size());
    result.forEach(s -> assertEquals(0.0, s.similarityScore(), 1e-9));
  }

  @Test
  void execute_returnsEmptyList_whenNoPatientsHaveMutationsInQueriedGenes() {
    // No other patient has any mutation in the queried gene set
    when(mutationRepository.getMutationsInMultipleMolecularProfiles(any(), any(), any(), any()))
        .thenReturn(List.of(mutation("REF", "TP53")));

    PatientGenomicSimilarityRequest request = buildRequest("REF", List.of("TP53"), 10);
    List<PatientSimilarityScore> result = useCase.execute(request);

    assertTrue(result.isEmpty());
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private static Mutation mutation(String patientId, String hugoGeneSymbol) {
    Gene gene = new Gene();
    gene.setHugoGeneSymbol(hugoGeneSymbol);

    Mutation m = new Mutation();
    m.setPatientId(patientId);
    m.setSampleId(patientId + "_sample");
    m.setGene(gene);
    return m;
  }

  private static PatientGenomicSimilarityRequest buildRequest(
      String referencePatientId, List<String> genes, int topN) {
    PatientGenomicSimilarityRequest req = new PatientGenomicSimilarityRequest();
    req.setMolecularProfileId("brca_mutations");
    req.setReferencePatientId(referencePatientId);
    req.setHugoGeneSymbols(genes);
    req.setTopN(topN);
    return req;
  }
}
