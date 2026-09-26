package org.cbioportal.domain.mutation.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.cbioportal.domain.mutation.GenomicSimilarityResult;
import org.cbioportal.domain.mutation.PatientGenePanel;
import org.cbioportal.domain.mutation.PatientMutatedGene;
import org.cbioportal.domain.mutation.PatientSimilarityScore;
import org.cbioportal.domain.mutation.repository.MutationRepository;
import org.cbioportal.legacy.model.GenePanelToGene;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.service.exception.PatientNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetPatientGenomicSimilarityUseCaseTest {

  private static final String STUDY_ID = "brca_tcga";
  private static final String PROFILE_ID = "brca_tcga_mutations";
  private static final String REFERENCE = "REF";

  @Mock private MutationRepository mutationRepository;

  private GetPatientGenomicSimilarityUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new GetPatientGenomicSimilarityUseCase(mutationRepository);
  }

  @Test
  void ranksPatientsByJaccardSimilarityOfTheirMutatedGenes() throws Exception {
    givenMutationProfile();
    givenMutations(
        mutated(REFERENCE, "TP53"),
        mutated(REFERENCE, "KRAS"),
        mutated("P2", "TP53"),
        mutated("P2", "KRAS"),
        mutated("P2", "BRAF"),
        mutated("P3", "TP53"),
        mutated("P4", "EGFR"));
    givenWholeExome(List.of("BRAF", "EGFR", "KRAS", "TP53"), REFERENCE, "P2", "P3", "P4");

    GenomicSimilarityResult result = execute(List.of("TP53", "KRAS", "BRAF", "EGFR"), 10);

    assertEquals(List.of("KRAS", "TP53"), result.referencePatientMutatedGenes());
    assertEquals(
        List.of(
            new PatientSimilarityScore("P2", 2.0 / 3.0, List.of("KRAS", "TP53")),
            new PatientSimilarityScore("P3", 0.5, List.of("TP53")),
            new PatientSimilarityScore("P4", 0.0, List.of())),
        result.similarPatients());
  }

  @Test
  void breaksTiesByPatientId() throws Exception {
    givenMutationProfile();
    givenMutations(mutated(REFERENCE, "TP53"), mutated("P2", "TP53"), mutated("P1", "TP53"));
    givenWholeExome(List.of("TP53"), REFERENCE, "P1", "P2");

    GenomicSimilarityResult result = execute(List.of("TP53"), 10);

    assertEquals(List.of("P1", "P2"), patientIds(result));
  }

  @Test
  void returnsAtMostTopNPatients() throws Exception {
    givenMutationProfile();
    givenMutations(
        mutated(REFERENCE, "TP53"),
        mutated("P1", "TP53"),
        mutated("P2", "TP53"),
        mutated("P3", "TP53"));
    givenWholeExome(List.of("TP53"), REFERENCE, "P1", "P2", "P3");

    GenomicSimilarityResult result = execute(List.of("TP53"), 2);

    assertEquals(List.of("P1", "P2"), patientIds(result));
  }

  @Test
  void comparesPatientsOnlyOnGenesProfiledInBoth() throws Exception {
    givenMutationProfile();
    givenMutations(mutated(REFERENCE, "TP53"), mutated(REFERENCE, "BRCA1"), mutated("P1", "TP53"));
    givenGenePanels(genePanel(REFERENCE, "WES"), genePanel("P1", "IMPACT"));
    givenGenePanelGenes(
        genePanelGene("WES", "TP53"),
        genePanelGene("WES", "BRCA1"),
        genePanelGene("IMPACT", "TP53"));

    GenomicSimilarityResult result = execute(List.of("TP53", "BRCA1"), 10);

    // BRCA1 is not on P1's panel, so it cannot count against P1 (1/2 without panel awareness)
    assertEquals(
        List.of(new PatientSimilarityScore("P1", 1.0, List.of("TP53"))), result.similarPatients());
  }

  @Test
  void countsAGeneWithAMutationAsProfiledEvenOffThePanel() throws Exception {
    givenMutationProfile();
    givenMutations(mutated(REFERENCE, "BRCA1"), mutated("P1", "BRCA1"));
    givenGenePanels(genePanel(REFERENCE, "WES"), genePanel("P1", "IMPACT"));
    givenGenePanelGenes(
        genePanelGene("WES", "TP53"),
        genePanelGene("WES", "BRCA1"),
        genePanelGene("IMPACT", "TP53"));

    GenomicSimilarityResult result = execute(List.of("TP53", "BRCA1"), 10);

    assertEquals(
        List.of(new PatientSimilarityScore("P1", 1.0, List.of("BRCA1"))), result.similarPatients());
  }

  @Test
  void leavesOutPatientsWithoutMutationsInTheGenesProfiledInBoth() throws Exception {
    givenMutationProfile();
    givenMutations(mutated(REFERENCE, "TP53"), mutated("P1", "BRCA1"));
    givenGenePanels(genePanel(REFERENCE, "TP53_ONLY"), genePanel("P1", "KRAS_ONLY"));
    givenGenePanelGenes(genePanelGene("TP53_ONLY", "TP53"), genePanelGene("KRAS_ONLY", "KRAS"));

    GenomicSimilarityResult result = execute(List.of("TP53", "KRAS", "BRCA1"), 10);

    assertTrue(result.similarPatients().isEmpty());
  }

  @Test
  void returnsNoSimilarPatientsWhenTheReferencePatientHasNoMutationsInTheGenes() throws Exception {
    givenMutationProfile();
    givenMutations(mutated("P1", "TP53"));
    givenGenePanels(genePanel(REFERENCE, "WES"), genePanel("P1", "WES"));

    GenomicSimilarityResult result = execute(List.of("TP53"), 10);

    assertEquals(List.of(), result.referencePatientMutatedGenes());
    assertEquals(List.of(), result.similarPatients());
  }

  @Test
  void normalisesTheGenesAndTheReferencePatientId() throws Exception {
    givenMutationProfile();
    givenMutations(mutated(REFERENCE, "TP53"), mutated("P1", "TP53"));
    givenWholeExome(List.of("KRAS", "TP53"), REFERENCE, "P1");

    GenomicSimilarityResult result =
        useCase.execute(STUDY_ID, PROFILE_ID, " REF ", List.of("tp53", " TP53 ", "kras"), 10);

    assertEquals(List.of("P1"), patientIds(result));
    verify(mutationRepository).getMutatedGenesOfPatients(PROFILE_ID, List.of("KRAS", "TP53"));
    verify(mutationRepository)
        .getGenePanelsOfPatients(STUDY_ID, PROFILE_ID, List.of("KRAS", "TP53"), REFERENCE);
  }

  @Test
  void throwsWhenTheStudyHasNoSuchMutationProfile() {
    when(mutationRepository.isMutationMolecularProfileOfStudy(STUDY_ID, PROFILE_ID))
        .thenReturn(false);

    MolecularProfileNotFoundException exception =
        assertThrows(MolecularProfileNotFoundException.class, () -> execute(List.of("TP53"), 10));

    assertEquals(PROFILE_ID, exception.getMolecularProfileId());
    verify(mutationRepository).isMutationMolecularProfileOfStudy(STUDY_ID, PROFILE_ID);
    verifyNoMoreInteractions(mutationRepository);
  }

  @Test
  void throwsWhenTheReferencePatientWasNotProfiled() {
    givenMutationProfile();
    givenMutations(mutated("P1", "TP53"));
    givenGenePanels(genePanel("P1", "WES"));

    PatientNotFoundException exception =
        assertThrows(PatientNotFoundException.class, () -> execute(List.of("TP53"), 10));

    assertEquals(STUDY_ID, exception.getStudyId());
    assertEquals(REFERENCE, exception.getPatientId());
  }

  private GenomicSimilarityResult execute(List<String> genes, int topN) throws Exception {
    return useCase.execute(STUDY_ID, PROFILE_ID, REFERENCE, genes, topN);
  }

  private void givenMutationProfile() {
    when(mutationRepository.isMutationMolecularProfileOfStudy(STUDY_ID, PROFILE_ID))
        .thenReturn(true);
  }

  private void givenMutations(PatientMutatedGene... mutatedGenes) {
    when(mutationRepository.getMutatedGenesOfPatients(eq(PROFILE_ID), anyList()))
        .thenReturn(List.of(mutatedGenes));
  }

  private void givenGenePanels(PatientGenePanel... genePanels) {
    when(mutationRepository.getGenePanelsOfPatients(
            eq(STUDY_ID), eq(PROFILE_ID), anyList(), anyString()))
        .thenReturn(List.of(genePanels));
  }

  private void givenGenePanelGenes(GenePanelToGene... genePanelGenes) {
    when(mutationRepository.getGenePanelGenes(anyCollection(), anyList()))
        .thenReturn(List.of(genePanelGenes));
  }

  /** Profiles every patient with the whole exome, which covers all the given genes. */
  private void givenWholeExome(List<String> genes, String... patientIds) {
    givenGenePanels(
        Arrays.stream(patientIds)
            .map(patientId -> genePanel(patientId, "WES"))
            .toArray(PatientGenePanel[]::new));
    givenGenePanelGenes(
        genes.stream().map(gene -> genePanelGene("WES", gene)).toArray(GenePanelToGene[]::new));
  }

  private static PatientMutatedGene mutated(String patientId, String hugoGeneSymbol) {
    return new PatientMutatedGene(patientId, hugoGeneSymbol);
  }

  private static PatientGenePanel genePanel(String patientId, String genePanelId) {
    return new PatientGenePanel(patientId, genePanelId);
  }

  private static GenePanelToGene genePanelGene(String genePanelId, String hugoGeneSymbol) {
    GenePanelToGene genePanelGene = new GenePanelToGene();
    genePanelGene.setGenePanelId(genePanelId);
    genePanelGene.setHugoGeneSymbol(hugoGeneSymbol);
    return genePanelGene;
  }

  private static List<String> patientIds(GenomicSimilarityResult result) {
    return result.similarPatients().stream().map(PatientSimilarityScore::patientId).toList();
  }
}
