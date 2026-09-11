package org.cbioportal.domain.mutation.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.cbioportal.domain.mutation.GenomicSimilarityResult;
import org.cbioportal.domain.mutation.PatientSimilarityScore;
import org.cbioportal.domain.mutation.repository.MutationRepository;
import org.cbioportal.legacy.model.Gene;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.web.parameter.PatientGenomicSimilarityRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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
    GenomicSimilarityResult result = useCase.execute(request);

    List<PatientSimilarityScore> scores = result.similarPatients();
    assertEquals(3, scores.size());
    assertEquals("P2", scores.get(0).patientId());
    assertEquals("P3", scores.get(1).patientId());
    assertEquals("P4", scores.get(2).patientId());

    assertEquals(2.0 / 3.0, scores.get(0).similarityScore(), 1e-9);
    assertEquals(0.5, scores.get(1).similarityScore(), 1e-9);
    assertEquals(0.0, scores.get(2).similarityScore(), 1e-9);
  }

  @Test
  void execute_referencePatientNotIncludedInResults() {
    when(mutationRepository.getMutationsInMultipleMolecularProfiles(any(), any(), any(), any()))
        .thenReturn(List.of(mutation("P1", "TP53"), mutation("P2", "TP53")));

    PatientGenomicSimilarityRequest request = buildRequest("P1", List.of("TP53"), 10);
    GenomicSimilarityResult result = useCase.execute(request);

    assertTrue(result.similarPatients().stream().noneMatch(s -> "P1".equals(s.patientId())));
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
    GenomicSimilarityResult result = useCase.execute(request);

    assertEquals(2, result.similarPatients().size());
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
    GenomicSimilarityResult result = useCase.execute(request);

    assertEquals(1, result.similarPatients().size());
    // Only TP53 is in both REF and P1; list must be sorted alphabetically
    assertEquals(List.of("TP53"), result.similarPatients().get(0).commonMutatedGenes());
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
    GenomicSimilarityResult result = useCase.execute(request);

    assertEquals(1, result.similarPatients().size());
    // Must be sorted regardless of HashSet iteration order
    assertEquals(
        List.of("BRAF", "KRAS", "TP53"), result.similarPatients().get(0).commonMutatedGenes());
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
    GenomicSimilarityResult result = useCase.execute(request);

    assertEquals(1, result.similarPatients().size());
    // Jaccard = 1/1 = 1.0 (PTEN ignored)
    assertEquals(1.0, result.similarPatients().get(0).similarityScore(), 1e-9);
  }

  @Test
  void execute_returnsEmptyList_whenNoPatientsHaveMutationsInQueriedGenes() {
    // No other patient has any mutation in the queried gene set
    when(mutationRepository.getMutationsInMultipleMolecularProfiles(any(), any(), any(), any()))
        .thenReturn(List.of(mutation("REF", "TP53")));

    PatientGenomicSimilarityRequest request = buildRequest("REF", List.of("TP53"), 10);
    GenomicSimilarityResult result = useCase.execute(request);

    assertTrue(result.similarPatients().isEmpty());
  }

  // ── Upgrade 1: Gene-symbol normalisation ─────────────────────────────────

  @Test
  void execute_normalisesGeneSymbolsToUppercase() {
    // Request uses lowercase "tp53" and "kras"; mutations are stored as "TP53" and "KRAS"
    when(mutationRepository.getMutationsInMultipleMolecularProfiles(any(), any(), any(), any()))
        .thenReturn(
            List.of(
                mutation("REF", "TP53"),
                mutation("REF", "KRAS"),
                mutation("P1", "TP53"),
                mutation("P1", "KRAS")));

    // lowercase symbols in request must match uppercase symbols in the profile
    PatientGenomicSimilarityRequest request = buildRequest("REF", List.of("tp53", "kras"), 10);
    GenomicSimilarityResult result = useCase.execute(request);

    assertEquals(1, result.similarPatients().size());
    // Jaccard = 2/2 = 1.0
    assertEquals(1.0, result.similarPatients().get(0).similarityScore(), 1e-9);
  }

  @Test
  void execute_deduplicatesDuplicateGeneSymbolsInRequest() {
    when(mutationRepository.getMutationsInMultipleMolecularProfiles(any(), any(), any(), any()))
        .thenReturn(List.of(mutation("REF", "TP53"), mutation("P1", "TP53")));

    // "TP53" appears twice; dedup must prevent double-counting
    PatientGenomicSimilarityRequest request =
        buildRequest("REF", List.of("TP53", "TP53", "tp53"), 10);
    GenomicSimilarityResult result = useCase.execute(request);

    assertEquals(1, result.similarPatients().size());
    assertEquals(1.0, result.similarPatients().get(0).similarityScore(), 1e-9);
  }

  // ── Upgrade 2: 404 for unknown reference patient ──────────────────────────

  @Test
  void execute_throws404_whenReferencePatientHasNoMutationsInPanel() {
    // UNKNOWN_PATIENT is not present in the mutation list at all
    when(mutationRepository.getMutationsInMultipleMolecularProfiles(any(), any(), any(), any()))
        .thenReturn(List.of(mutation("P1", "TP53"), mutation("P2", "KRAS")));

    PatientGenomicSimilarityRequest request =
        buildRequest("UNKNOWN_PATIENT", List.of("TP53", "KRAS"), 10);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> useCase.execute(request));

    assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    assertTrue(ex.getMessage().contains("UNKNOWN_PATIENT"));
  }

  // ── Upgrade 3: referencePatientMutatedGenes in response ───────────────────

  @Test
  void execute_includesReferencePatientMutatedGenesInResult() {
    when(mutationRepository.getMutationsInMultipleMolecularProfiles(any(), any(), any(), any()))
        .thenReturn(
            List.of(mutation("REF", "TP53"), mutation("REF", "KRAS"), mutation("P1", "TP53")));

    PatientGenomicSimilarityRequest request =
        buildRequest("REF", List.of("TP53", "KRAS", "BRAF"), 10);
    GenomicSimilarityResult result = useCase.execute(request);

    // Reference has TP53 + KRAS; must appear alphabetically sorted in the result
    assertEquals(List.of("KRAS", "TP53"), result.referencePatientMutatedGenes());
  }

  @Test
  void execute_referencePatientMutatedGenesContainsOnlyQueriedGenes() {
    // REF also has PTEN, but it is not in the queried gene panel
    when(mutationRepository.getMutationsInMultipleMolecularProfiles(any(), any(), any(), any()))
        .thenReturn(
            List.of(
                mutation("REF", "TP53"),
                mutation("REF", "PTEN"), // outside queried panel
                mutation("P1", "TP53")));

    PatientGenomicSimilarityRequest request = buildRequest("REF", List.of("TP53"), 10);
    GenomicSimilarityResult result = useCase.execute(request);

    // PTEN must NOT appear in referencePatientMutatedGenes
    assertEquals(List.of("TP53"), result.referencePatientMutatedGenes());
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
