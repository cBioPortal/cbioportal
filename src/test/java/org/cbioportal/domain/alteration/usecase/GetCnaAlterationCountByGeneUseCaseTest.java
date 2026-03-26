package org.cbioportal.domain.alteration.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.cbioportal.domain.alteration.repository.AlterationRepository;
import org.cbioportal.domain.cancerstudy.usecase.GetFilteredStudyIdsUseCase;
import org.cbioportal.domain.generic_assay.usecase.GetFilteredMolecularProfilesByAlterationType;
import org.cbioportal.legacy.model.CopyNumberCountByGene;
import org.cbioportal.legacy.service.SignificantCopyNumberRegionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetCnaAlterationCountByGeneUseCaseTest {

  @Mock private AlterationRepository alterationRepository;

  @Mock
  private GetFilteredMolecularProfilesByAlterationType getFilteredMolecularProfilesByAlterationType;

  @Mock private GetFilteredStudyIdsUseCase getFilteredStudyIdsUseCase;

  @Mock private SignificantCopyNumberRegionService significantCopyNumberRegionService;

  private GetCnaAlterationCountByGeneUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase =
        new GetCnaAlterationCountByGeneUseCase(
            alterationRepository,
            getFilteredMolecularProfilesByAlterationType,
            getFilteredStudyIdsUseCase,
            significantCopyNumberRegionService);
  }

  private CopyNumberCountByGene makeCna(String symbol, int entrezId, int alteration, int altered) {
    CopyNumberCountByGene cna = new CopyNumberCountByGene();
    cna.setHugoGeneSymbol(symbol);
    cna.setEntrezGeneId(entrezId);
    cna.setAlteration(alteration);
    cna.setNumberOfAlteredCases(altered);
    cna.setTotalCount(100);
    return cna;
  }

  /**
   * Verifies that combineCopyNumberCountsWithConflictingHugoSymbols preserves the insertion order
   * of the first occurrence of each (hugoGeneSymbol, alteration) key. This is critical so that the
   * SQL ORDER BY applied by getCnaGenes is not discarded.
   */
  @Test
  void combineCopyNumberCounts_preservesInsertionOrder() throws Exception {
    // Build an ordered list: TP53(-2), BRCA1(-2), KRAS(2)
    List<CopyNumberCountByGene> input =
        Arrays.asList(
            makeCna("TP53", 7157, -2, 50),
            makeCna("BRCA1", 672, -2, 30),
            makeCna("KRAS", 3845, 2, 10));

    Method method =
        GetCnaAlterationCountByGeneUseCase.class.getDeclaredMethod(
            "combineCopyNumberCountsWithConflictingHugoSymbols", List.class);
    method.setAccessible(true);

    @SuppressWarnings("unchecked")
    List<CopyNumberCountByGene> result =
        (List<CopyNumberCountByGene>) method.invoke(useCase, input);

    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("TP53", result.get(0).getHugoGeneSymbol());
    assertEquals("BRCA1", result.get(1).getHugoGeneSymbol());
    assertEquals("KRAS", result.get(2).getHugoGeneSymbol());
  }

  /**
   * Verifies that counts for duplicate (hugoGeneSymbol, alteration) keys are summed, and that the
   * resulting entry appears in the position of the first occurrence (insertion order preserved).
   */
  @Test
  void combineCopyNumberCounts_sumsDuplicatesAndPreservesOrder() throws Exception {
    // TP53(-2) appears twice; the combined entry should retain TP53's position
    List<CopyNumberCountByGene> input =
        Arrays.asList(
            makeCna("TP53", 7157, -2, 50), // first TP53/-2
            makeCna("BRCA1", 672, -2, 30),
            makeCna("TP53", 1, -2, 20)); // duplicate key with different entrez id

    Method method =
        GetCnaAlterationCountByGeneUseCase.class.getDeclaredMethod(
            "combineCopyNumberCountsWithConflictingHugoSymbols", List.class);
    method.setAccessible(true);

    @SuppressWarnings("unchecked")
    List<CopyNumberCountByGene> result =
        (List<CopyNumberCountByGene>) method.invoke(useCase, input);

    assertNotNull(result);
    assertEquals(2, result.size());

    // TP53 should still be first (insertion order)
    CopyNumberCountByGene tp53 = result.get(0);
    assertEquals("TP53", tp53.getHugoGeneSymbol());
    assertEquals(70, tp53.getNumberOfAlteredCases()); // 50 + 20

    // BRCA1 should be second
    assertEquals("BRCA1", result.get(1).getHugoGeneSymbol());
  }
}
