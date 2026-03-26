package org.cbioportal.domain.alteration.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
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

  private CopyNumberCountByGene makeCna(String hugo, int alteration, int totalCount, int altered) {
    CopyNumberCountByGene cna = new CopyNumberCountByGene();
    cna.setHugoGeneSymbol(hugo);
    cna.setAlteration(alteration);
    cna.setTotalCount(totalCount);
    cna.setNumberOfAlteredCases(altered);
    return cna;
  }

  @SuppressWarnings("unchecked")
  private List<CopyNumberCountByGene> invokeCombine(List<CopyNumberCountByGene> input)
      throws Exception {
    Method method =
        GetCnaAlterationCountByGeneUseCase.class.getDeclaredMethod(
            "combineCopyNumberCountsWithConflictingHugoSymbols", List.class);
    method.setAccessible(true);
    return (List<CopyNumberCountByGene>) method.invoke(useCase, input);
  }

  @Test
  void combineCopyNumberCounts_sortsByTotalCountDesc() throws Exception {
    List<CopyNumberCountByGene> input =
        List.of(
            makeCna("KRAS", 2, 5, 5),
            makeCna("TP53", -2, 10, 10),
            makeCna("BRCA1", 2, 3, 3));

    List<CopyNumberCountByGene> result = invokeCombine(input);

    assertEquals(3, result.size());
    assertEquals("TP53", result.get(0).getHugoGeneSymbol());
    assertEquals("KRAS", result.get(1).getHugoGeneSymbol());
    assertEquals("BRCA1", result.get(2).getHugoGeneSymbol());
  }

  @Test
  void combineCopyNumberCounts_sortsByHugoSymbolAscOnTie() throws Exception {
    List<CopyNumberCountByGene> input =
        List.of(makeCna("TP53", -2, 5, 5), makeCna("BRCA1", 2, 5, 5));

    List<CopyNumberCountByGene> result = invokeCombine(input);

    assertEquals(2, result.size());
    assertEquals("BRCA1", result.get(0).getHugoGeneSymbol());
    assertEquals("TP53", result.get(1).getHugoGeneSymbol());
  }

  @Test
  void combineCopyNumberCounts_mergesDuplicatesAndSorts() throws Exception {
    // TP53 appears twice (same hugo+alteration) — counts should be summed then sorted
    List<CopyNumberCountByGene> input =
        List.of(
            makeCna("TP53", -2, 3, 3),
            makeCna("KRAS", 2, 10, 10),
            makeCna("TP53", -2, 4, 4));

    List<CopyNumberCountByGene> result = invokeCombine(input);

    assertEquals(2, result.size());
    // KRAS stays first (totalCount=10), merged TP53 comes second (totalCount=7)
    assertEquals("KRAS", result.get(0).getHugoGeneSymbol());
    assertEquals(10, result.get(0).getTotalCount());
    assertEquals("TP53", result.get(1).getHugoGeneSymbol());
    assertEquals(7, result.get(1).getTotalCount());
    assertEquals(7, result.get(1).getNumberOfAlteredCases());
  }

  @Test
  void combineCopyNumberCounts_sortsByAlterationAscOnFullTie() throws Exception {
    // Same hugo+different alteration, same totalCount and hugo symbol → sort by alteration
    List<CopyNumberCountByGene> input =
        List.of(makeCna("TP53", 2, 5, 5), makeCna("TP53", -2, 5, 5));

    List<CopyNumberCountByGene> result = invokeCombine(input);

    assertEquals(2, result.size());
    assertEquals(-2, result.get(0).getAlteration());
    assertEquals(2, result.get(1).getAlteration());
  }

  @Test
  void combineCopyNumberCounts_emptyInputReturnsEmptyList() throws Exception {
    List<CopyNumberCountByGene> result = invokeCombine(List.of());
    assertEquals(0, result.size());
  }
}
