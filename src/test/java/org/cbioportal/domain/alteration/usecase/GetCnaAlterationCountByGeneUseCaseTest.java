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
  void combineCopyNumberCounts_mergesDuplicates() throws Exception {
    // TP53 appears twice (same hugo+alteration) — counts should be summed
    List<CopyNumberCountByGene> input =
        List.of(
            makeCna("TP53", -2, 3, 3),
            makeCna("KRAS", 2, 10, 10),
            makeCna("TP53", -2, 4, 4));

    List<CopyNumberCountByGene> result = invokeCombine(input);

    assertEquals(2, result.size());
    CopyNumberCountByGene tp53 =
        result.stream().filter(c -> "TP53".equals(c.getHugoGeneSymbol())).findFirst().orElseThrow();
    assertEquals(7, tp53.getTotalCount());
    assertEquals(7, tp53.getNumberOfAlteredCases());
    CopyNumberCountByGene kras =
        result.stream()
            .filter(c -> "KRAS".equals(c.getHugoGeneSymbol()))
            .findFirst()
            .orElseThrow();
    assertEquals(10, kras.getTotalCount());
  }

  @Test
  void combineCopyNumberCounts_preservesNonDuplicates() throws Exception {
    List<CopyNumberCountByGene> input =
        List.of(makeCna("KRAS", 2, 5, 5), makeCna("TP53", -2, 10, 10));

    List<CopyNumberCountByGene> result = invokeCombine(input);

    assertEquals(2, result.size());
  }

  @Test
  void combineCopyNumberCounts_emptyInputReturnsEmptyList() throws Exception {
    List<CopyNumberCountByGene> result = invokeCombine(List.of());
    assertEquals(0, result.size());
  }
}
