package org.cbioportal.domain.coexpression.usecase;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import org.cbioportal.domain.coexpression.repository.CoExpressionRepository;
import org.cbioportal.infrastructure.repository.clickhouse.coexpression.CoExpressionResult;
import org.cbioportal.legacy.model.CoExpression;
import org.cbioportal.legacy.model.EntityType;
import org.cbioportal.legacy.model.Gene;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.persistence.SampleListRepository;
import org.cbioportal.legacy.service.GeneService;
import org.cbioportal.legacy.service.MolecularProfileService;
import org.cbioportal.legacy.service.exception.GeneWithMultipleEntrezIdsException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FetchCoExpressionsUseCaseTest {

  @InjectMocks private FetchCoExpressionsUseCase fetchCoExpressionsUseCase;

  @Mock private CoExpressionRepository coExpressionRepository;
  @Mock private MolecularProfileService molecularProfileService;
  @Mock private SampleListRepository sampleListRepository;
  @Mock private GeneService geneService;

  // --- computePValue tests ---

  @Test
  public void testComputePValueNormalCase() {
    double pValue = FetchCoExpressionsUseCase.computePValue(0.5, 100);
    assertTrue(pValue > 0.0 && pValue < 1.0);
  }

  @Test
  public void testComputePValueTooFewSamples() {
    assertEquals(1.0, FetchCoExpressionsUseCase.computePValue(0.9, 2), 0.0);
    assertEquals(1.0, FetchCoExpressionsUseCase.computePValue(0.9, 1), 0.0);
    assertEquals(1.0, FetchCoExpressionsUseCase.computePValue(0.9, 0), 0.0);
  }

  @Test
  public void testComputePValuePerfectCorrelation() {
    assertEquals(0.0, FetchCoExpressionsUseCase.computePValue(1.0, 100), 0.0);
    assertEquals(0.0, FetchCoExpressionsUseCase.computePValue(-1.0, 100), 0.0);
  }

  @Test
  public void testComputePValueStrongCorrelationDoesNotUnderflow() {
    // This was a bug: 2*(1-cdf(|t|)) underflows to 0 for strong correlations
    double pValue = FetchCoExpressionsUseCase.computePValue(0.95, 500);
    assertTrue("Strong correlation p-value should be > 0", pValue > 0.0);
  }

  @Test
  public void testComputePValueNegativeCorrelation() {
    double pPositive = FetchCoExpressionsUseCase.computePValue(0.5, 100);
    double pNegative = FetchCoExpressionsUseCase.computePValue(-0.5, 100);
    assertEquals(pPositive, pNegative, 1e-15);
  }

  @Test
  public void testComputePValueWeakCorrelationHighPValue() {
    double pValue = FetchCoExpressionsUseCase.computePValue(0.01, 50);
    assertTrue("Weak correlation should have high p-value", pValue > 0.5);
  }

  // --- execute() with sampleListId tests ---

  @Test
  public void testExecuteWithSampleListIdEmptyList() throws Exception {
    when(sampleListRepository.getAllSampleIdsInSampleList("study_all"))
        .thenReturn(Collections.emptyList());

    List<CoExpression> result =
        fetchCoExpressionsUseCase.execute("study_mrna", "study_mrna", 3845, "study_all", 0.3);

    assertTrue(result.isEmpty());
    verifyNoInteractions(coExpressionRepository);
  }

  @Test
  public void testExecuteWithSampleListIdDelegatesToSampleIdOverload() throws Exception {
    when(sampleListRepository.getAllSampleIdsInSampleList("acc_tcga_all"))
        .thenReturn(List.of("TCGA-OR-A5J1-01", "TCGA-OR-A5J2-01"));

    MolecularProfile profile = new MolecularProfile();
    profile.setCancerStudyIdentifier("acc_tcga");
    when(molecularProfileService.getMolecularProfile("acc_tcga_rna_seq_v2_mrna"))
        .thenReturn(profile);

    Gene gene = new Gene();
    gene.setHugoGeneSymbol("KRAS");
    when(geneService.getGene("3845")).thenReturn(gene);

    when(coExpressionRepository.getCoExpressions(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(Collections.emptyList());

    fetchCoExpressionsUseCase.execute(
        "acc_tcga_rna_seq_v2_mrna", "acc_tcga_rna_seq_v2_mrna", 3845, "acc_tcga_all", 0.3);

    verify(coExpressionRepository)
        .getCoExpressions(
            "acc_tcga",
            "rna_seq_v2_mrna",
            "acc_tcga",
            "rna_seq_v2_mrna",
            "KRAS",
            List.of("acc_tcga_TCGA-OR-A5J1-01", "acc_tcga_TCGA-OR-A5J2-01"),
            0.3);
  }

  // --- execute() with sampleIds tests ---

  @Test
  public void testExecuteSameProfile() throws Exception {
    MolecularProfile profile = new MolecularProfile();
    profile.setCancerStudyIdentifier("acc_tcga");
    when(molecularProfileService.getMolecularProfile("acc_tcga_rna_seq_v2_mrna"))
        .thenReturn(profile);

    Gene gene = new Gene();
    gene.setHugoGeneSymbol("EGFR");
    when(geneService.getGene("1956")).thenReturn(gene);

    CoExpressionResult result1 = new CoExpressionResult();
    result1.setEntrezGeneId(3845);
    result1.setSpearmansCorrelation(0.75);
    result1.setNumSamples(79);

    when(coExpressionRepository.getCoExpressions(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(List.of(result1));

    List<CoExpression> results =
        fetchCoExpressionsUseCase.execute(
            "acc_tcga_rna_seq_v2_mrna",
            "acc_tcga_rna_seq_v2_mrna",
            1956,
            List.of("TCGA-OR-A5J1-01"),
            0.3);

    assertEquals(1, results.size());
    CoExpression coExp = results.get(0);
    assertEquals("3845", coExp.getGeneticEntityId());
    assertEquals(EntityType.GENE, coExp.getGeneticEntityType());
    assertEquals(0.75, coExp.getSpearmansCorrelation().doubleValue(), 1e-10);
    assertTrue("p-value should be positive", coExp.getpValue().doubleValue() > 0);

    // Same profile: getMolecularProfile should only be called once
    verify(molecularProfileService, times(1)).getMolecularProfile(any());
  }

  @Test
  public void testExecuteDifferentProfiles() throws Exception {
    MolecularProfile profileA = new MolecularProfile();
    profileA.setCancerStudyIdentifier("acc_tcga");
    MolecularProfile profileB = new MolecularProfile();
    profileB.setCancerStudyIdentifier("acc_tcga");

    when(molecularProfileService.getMolecularProfile("acc_tcga_rna_seq_v2_mrna"))
        .thenReturn(profileA);
    when(molecularProfileService.getMolecularProfile("acc_tcga_rna_seq_v2_mrna_median_Zscores"))
        .thenReturn(profileB);

    Gene gene = new Gene();
    gene.setHugoGeneSymbol("KRAS");
    when(geneService.getGene("3845")).thenReturn(gene);

    when(coExpressionRepository.getCoExpressions(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(Collections.emptyList());

    fetchCoExpressionsUseCase.execute(
        "acc_tcga_rna_seq_v2_mrna",
        "acc_tcga_rna_seq_v2_mrna_median_Zscores",
        3845,
        List.of("SAMPLE1"),
        0.3);

    verify(coExpressionRepository)
        .getCoExpressions(
            "acc_tcga",
            "rna_seq_v2_mrna",
            "acc_tcga",
            "rna_seq_v2_mrna_median_Zscores",
            "KRAS",
            List.of("acc_tcga_SAMPLE1"),
            0.3);

    // Different profiles: getMolecularProfile should be called twice
    verify(molecularProfileService, times(2)).getMolecularProfile(any());
  }

  @Test
  public void testExecuteProfileTypeDerivation() throws Exception {
    MolecularProfile profile = new MolecularProfile();
    profile.setCancerStudyIdentifier("brca_tcga_pan_can_atlas_2018");
    when(molecularProfileService.getMolecularProfile(
            "brca_tcga_pan_can_atlas_2018_rna_seq_v2_mrna"))
        .thenReturn(profile);

    Gene gene = new Gene();
    gene.setHugoGeneSymbol("CDKN2A");
    when(geneService.getGene("1029")).thenReturn(gene);

    when(coExpressionRepository.getCoExpressions(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(Collections.emptyList());

    fetchCoExpressionsUseCase.execute(
        "brca_tcga_pan_can_atlas_2018_rna_seq_v2_mrna",
        "brca_tcga_pan_can_atlas_2018_rna_seq_v2_mrna",
        1029,
        List.of("SAMPLE1"),
        0.3);

    // Verify profile type is correctly derived by stripping study identifier prefix
    verify(coExpressionRepository)
        .getCoExpressions(
            eq("brca_tcga_pan_can_atlas_2018"),
            eq("rna_seq_v2_mrna"),
            any(),
            any(),
            any(),
            any(),
            any());
  }

  @Test
  public void testExecuteSampleUniqueIdConstruction() throws Exception {
    MolecularProfile profile = new MolecularProfile();
    profile.setCancerStudyIdentifier("kirc_tcga");
    when(molecularProfileService.getMolecularProfile("kirc_tcga_rna_seq_v2_mrna"))
        .thenReturn(profile);

    Gene gene = new Gene();
    gene.setHugoGeneSymbol("BRAF");
    when(geneService.getGene("673")).thenReturn(gene);

    when(coExpressionRepository.getCoExpressions(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(Collections.emptyList());

    fetchCoExpressionsUseCase.execute(
        "kirc_tcga_rna_seq_v2_mrna",
        "kirc_tcga_rna_seq_v2_mrna",
        673,
        List.of("TCGA-A3-01", "TCGA-A3-02", "TCGA-A3-03"),
        0.3);

    verify(coExpressionRepository)
        .getCoExpressions(
            any(),
            any(),
            any(),
            any(),
            any(),
            eq(List.of("kirc_tcga_TCGA-A3-01", "kirc_tcga_TCGA-A3-02", "kirc_tcga_TCGA-A3-03")),
            any());
  }

  @Test
  public void testExecuteGeneWithMultipleEntrezIdsReturnsEmpty() throws Exception {
    MolecularProfile profile = new MolecularProfile();
    profile.setCancerStudyIdentifier("acc_tcga");
    when(molecularProfileService.getMolecularProfile("acc_tcga_rna_seq_v2_mrna"))
        .thenReturn(profile);

    when(geneService.getGene("999")).thenThrow(new GeneWithMultipleEntrezIdsException("999"));

    List<CoExpression> results =
        fetchCoExpressionsUseCase.execute(
            "acc_tcga_rna_seq_v2_mrna", "acc_tcga_rna_seq_v2_mrna", 999, List.of("SAMPLE1"), 0.3);

    assertTrue(results.isEmpty());
    verifyNoInteractions(coExpressionRepository);
  }

  @Test
  public void testExecuteResultMapping() throws Exception {
    MolecularProfile profile = new MolecularProfile();
    profile.setCancerStudyIdentifier("acc_tcga");
    when(molecularProfileService.getMolecularProfile("acc_tcga_rna_seq_v2_mrna"))
        .thenReturn(profile);

    Gene gene = new Gene();
    gene.setHugoGeneSymbol("KRAS");
    when(geneService.getGene("3845")).thenReturn(gene);

    CoExpressionResult r1 = new CoExpressionResult();
    r1.setEntrezGeneId(7157);
    r1.setSpearmansCorrelation(0.85);
    r1.setNumSamples(79);

    CoExpressionResult r2 = new CoExpressionResult();
    r2.setEntrezGeneId(1956);
    r2.setSpearmansCorrelation(-0.42);
    r2.setNumSamples(79);

    when(coExpressionRepository.getCoExpressions(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(List.of(r1, r2));

    List<CoExpression> results =
        fetchCoExpressionsUseCase.execute(
            "acc_tcga_rna_seq_v2_mrna", "acc_tcga_rna_seq_v2_mrna", 3845, List.of("SAMPLE1"), 0.3);

    assertEquals(2, results.size());

    // First result
    assertEquals("7157", results.get(0).getGeneticEntityId());
    assertEquals(EntityType.GENE, results.get(0).getGeneticEntityType());
    assertEquals(0.85, results.get(0).getSpearmansCorrelation().doubleValue(), 1e-10);
    assertTrue(results.get(0).getpValue().doubleValue() > 0);
    assertTrue(results.get(0).getpValue().doubleValue() < 1);

    // Second result — negative correlation
    assertEquals("1956", results.get(1).getGeneticEntityId());
    assertEquals(-0.42, results.get(1).getSpearmansCorrelation().doubleValue(), 1e-10);
  }
}
