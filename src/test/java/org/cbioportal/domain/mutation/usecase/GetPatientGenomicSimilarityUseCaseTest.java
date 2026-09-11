package org.cbioportal.domain.mutation.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.domain.mutation.GenomicSimilarityResult;
import org.cbioportal.domain.mutation.PatientSimilarityScore;
import org.cbioportal.domain.mutation.repository.MutationRepository;
import org.cbioportal.legacy.model.Gene;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.service.MolecularProfileService;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.web.parameter.PatientGenomicSimilarityRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class GetPatientGenomicSimilarityUseCaseTest {

  private static final String STUDY_ID = "brca_tcga";
  private static final String PROFILE_ID = "brca_mutations";

  private MutationRepository mutationRepository;
  private MolecularProfileService molecularProfileService;
  private GetPatientGenomicSimilarityUseCase useCase;

  @BeforeEach
  void setUp() throws MolecularProfileNotFoundException {
    mutationRepository = mock(MutationRepository.class);
    molecularProfileService = mock(MolecularProfileService.class);
    useCase = new GetPatientGenomicSimilarityUseCase(mutationRepository, molecularProfileService);

    // Default: profile exists and belongs to the study
    MolecularProfile profile = new MolecularProfile();
    profile.setStableId(PROFILE_ID);
    profile.setCancerStudyIdentifier(STUDY_ID);
    when(molecularProfileService.getMolecularProfile(PROFILE_ID)).thenReturn(profile);
  }

  // ── Happy path ────────────────────────────────────────────────────────────

  @Test
  void execute_ranksPatientsByDescendingJaccardSimilarity()
      throws MolecularProfileNotFoundException {
    // Reference patient P1 has TP53 + KRAS
    // Candidate P2 has TP53 + KRAS + BRAF  → Jaccard = 2/3 ≈ 0.667
    // Candidate P3 has TP53 only            → Jaccard = 1/2 = 0.5
    // Candidate P4 has EGFR only            → Jaccard = 0/3 = 0.0
    when(mutationRepository.getMutationsInMultipleMolecularProfiles(
            eq(List.of(PROFILE_ID)), any(), any(), any()))
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
    GenomicSimilarityResult result = useCase.execute(STUDY_ID, request);

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

    GenomicSimilarityResult result =
        useCase.execute(STUDY_ID, buildRequest("P1", List.of("TP53"), 10));

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

    GenomicSimilarityResult result =
        useCase.execute(STUDY_ID, buildRequest("REF", List.of("TP53"), 2));

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

    GenomicSimilarityResult result =
        useCase.execute(STUDY_ID, buildRequest("REF", List.of("TP53", "KRAS", "BRAF"), 10));

    assertEquals(1, result.similarPatients().size());
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

    GenomicSimilarityResult result =
        useCase.execute(STUDY_ID, buildRequest("REF", List.of("TP53", "KRAS", "BRAF"), 10));

    assertEquals(1, result.similarPatients().size());
    assertEquals(
        List.of("BRAF", "KRAS", "TP53"), result.similarPatients().get(0).commonMutatedGenes());
  }

  @Test
  void execute_genesOutsideRequestedSetIgnored() {
    when(mutationRepository.getMutationsInMultipleMolecularProfiles(any(), any(), any(), any()))
        .thenReturn(
            List.of(mutation("REF", "TP53"), mutation("P1", "TP53"), mutation("P1", "PTEN")));

    GenomicSimilarityResult result =
        useCase.execute(STUDY_ID, buildRequest("REF", List.of("TP53"), 10));

    assertEquals(1, result.similarPatients().size());
    assertEquals(1.0, result.similarPatients().get(0).similarityScore(), 1e-9);
  }

  @Test
  void execute_returnsEmptyList_whenNoPatientsHaveMutationsInQueriedGenes() {
    when(mutationRepository.getMutationsInMultipleMolecularProfiles(any(), any(), any(), any()))
        .thenReturn(List.of(mutation("REF", "TP53")));

    GenomicSimilarityResult result =
        useCase.execute(STUDY_ID, buildRequest("REF", List.of("TP53"), 10));

    assertTrue(result.similarPatients().isEmpty());
  }

  // ── Fix #1: Gene-symbol normalisation ────────────────────────────────────

  @Test
  void execute_normalisesGeneSymbolsToUppercase() {
    when(mutationRepository.getMutationsInMultipleMolecularProfiles(any(), any(), any(), any()))
        .thenReturn(
            List.of(
                mutation("REF", "TP53"),
                mutation("REF", "KRAS"),
                mutation("P1", "TP53"),
                mutation("P1", "KRAS")));

    GenomicSimilarityResult result =
        useCase.execute(STUDY_ID, buildRequest("REF", List.of("tp53", "kras"), 10));

    assertEquals(1, result.similarPatients().size());
    assertEquals(1.0, result.similarPatients().get(0).similarityScore(), 1e-9);
  }

  @Test
  void execute_deduplicatesDuplicateGeneSymbolsInRequest() {
    when(mutationRepository.getMutationsInMultipleMolecularProfiles(any(), any(), any(), any()))
        .thenReturn(List.of(mutation("REF", "TP53"), mutation("P1", "TP53")));

    GenomicSimilarityResult result =
        useCase.execute(STUDY_ID, buildRequest("REF", List.of("TP53", "TP53", "tp53"), 10));

    assertEquals(1, result.similarPatients().size());
    assertEquals(1.0, result.similarPatients().get(0).similarityScore(), 1e-9);
  }

  // ── Fix #2: Profile ≠ Study validation ───────────────────────────────────

  @Test
  void execute_throws400_whenMolecularProfileDoesNotBelongToStudy()
      throws MolecularProfileNotFoundException {
    // The profile exists but belongs to a different study
    MolecularProfile wrongProfile = new MolecularProfile();
    wrongProfile.setStableId(PROFILE_ID);
    wrongProfile.setCancerStudyIdentifier("luad_tcga"); // different study
    when(molecularProfileService.getMolecularProfile(PROFILE_ID)).thenReturn(wrongProfile);

    when(mutationRepository.getMutationsInMultipleMolecularProfiles(any(), any(), any(), any()))
        .thenReturn(List.of());

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> useCase.execute(STUDY_ID, buildRequest("P1", List.of("TP53"), 10)));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    assertTrue(ex.getMessage().contains(STUDY_ID));
  }

  @Test
  void execute_throws400_whenMolecularProfileDoesNotExist()
      throws MolecularProfileNotFoundException {
    when(molecularProfileService.getMolecularProfile(PROFILE_ID))
        .thenThrow(new MolecularProfileNotFoundException(PROFILE_ID));

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> useCase.execute(STUDY_ID, buildRequest("P1", List.of("TP53"), 10)));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
  }

  // ── Fix #4: 404 for unknown reference patient ─────────────────────────────

  @Test
  void execute_throws404_whenReferencePatientHasNoMutationsInPanel() {
    when(mutationRepository.getMutationsInMultipleMolecularProfiles(any(), any(), any(), any()))
        .thenReturn(List.of(mutation("P1", "TP53"), mutation("P2", "KRAS")));

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () ->
                useCase.execute(
                    STUDY_ID, buildRequest("UNKNOWN_PATIENT", List.of("TP53", "KRAS"), 10)));

    assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    assertTrue(ex.getMessage().contains("UNKNOWN_PATIENT"));
  }

  // ── Fix #5: Patient count cap ─────────────────────────────────────────────

  @Test
  void execute_throws400_whenPatientCountExceedsCap() {
    // Generate MAX + 1 mutations from distinct patients to trip the cap
    List<Mutation> hugeMutationList = new ArrayList<>();
    for (int i = 0; i <= GetPatientGenomicSimilarityUseCase.MAX_PATIENTS_FOR_SIMILARITY; i++) {
      hugeMutationList.add(mutation("PATIENT_" + i, "TP53"));
    }
    when(mutationRepository.getMutationsInMultipleMolecularProfiles(any(), any(), any(), any()))
        .thenReturn(hugeMutationList);

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () -> useCase.execute(STUDY_ID, buildRequest("PATIENT_0", List.of("TP53"), 10)));

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    assertTrue(ex.getMessage().contains("exceeds the per-request limit"));
  }

  // ── Fix #3: referencePatientMutatedGenes in response ─────────────────────

  @Test
  void execute_includesReferencePatientMutatedGenesInResult() {
    when(mutationRepository.getMutationsInMultipleMolecularProfiles(any(), any(), any(), any()))
        .thenReturn(
            List.of(mutation("REF", "TP53"), mutation("REF", "KRAS"), mutation("P1", "TP53")));

    GenomicSimilarityResult result =
        useCase.execute(STUDY_ID, buildRequest("REF", List.of("TP53", "KRAS", "BRAF"), 10));

    assertEquals(List.of("KRAS", "TP53"), result.referencePatientMutatedGenes());
  }

  @Test
  void execute_referencePatientMutatedGenesContainsOnlyQueriedGenes() {
    when(mutationRepository.getMutationsInMultipleMolecularProfiles(any(), any(), any(), any()))
        .thenReturn(
            List.of(
                mutation("REF", "TP53"),
                mutation("REF", "PTEN"), // outside queried panel
                mutation("P1", "TP53")));

    GenomicSimilarityResult result =
        useCase.execute(STUDY_ID, buildRequest("REF", List.of("TP53"), 10));

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
    req.setMolecularProfileId(PROFILE_ID);
    req.setReferencePatientId(referencePatientId);
    req.setHugoGeneSymbols(genes);
    req.setTopN(topN);
    return req;
  }
}
