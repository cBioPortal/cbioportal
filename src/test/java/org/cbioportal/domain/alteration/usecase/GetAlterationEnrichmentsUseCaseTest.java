package org.cbioportal.domain.alteration.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cbioportal.domain.alteration.repository.AlterationRepository;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.GenePanelToGene;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.AsyncTaskExecutor;

@ExtendWith(MockitoExtension.class)
class GetAlterationEnrichmentsUseCaseTest {

  @Mock private AlterationRepository alterationRepository;

  @Mock private AsyncTaskExecutor threadPoolTaskExecutor;

  private GetAlterationEnrichmentsUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new GetAlterationEnrichmentsUseCase(alterationRepository, threadPoolTaskExecutor);
  }

  @Test
  void testCalculateProfiledCasesPerGene_singlePanelSingleGene() throws Exception {
    // Setup test data
    Map<String, List<String>> panelCombinationToEntityList = new HashMap<>();
    panelCombinationToEntityList.put("panel1", Arrays.asList("sample1", "sample2", "sample3"));

    Map<String, Map<String, GenePanelToGene>> panelToGeneMap = new HashMap<>();
    Map<String, GenePanelToGene> panel1Genes = new HashMap<>();

    GenePanelToGene tp53Gene = new GenePanelToGene();
    tp53Gene.setHugoGeneSymbol("TP53");
    tp53Gene.setEntrezGeneId(7157);
    panel1Genes.put("TP53", tp53Gene);

    panelToGeneMap.put("panel1", panel1Genes);

    // Call the private method using reflection
    Method method =
        GetAlterationEnrichmentsUseCase.class.getDeclaredMethod(
            "calculateProfiledCasesPerGene", Map.class, Map.class);
    method.setAccessible(true);

    @SuppressWarnings("unchecked")
    Map<String, AlterationCountByGene> result =
        (Map<String, AlterationCountByGene>)
            method.invoke(useCase, panelCombinationToEntityList, panelToGeneMap);

    // Verify results
    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(result.containsKey("TP53"));

    AlterationCountByGene tp53Count = result.get("TP53");
    assertEquals("TP53", tp53Count.getHugoGeneSymbol());
    assertEquals(7157, tp53Count.getEntrezGeneId());
    assertEquals(3, tp53Count.getNumberOfProfiledCases()); // 3 samples in panel1
    assertEquals(
        0,
        tp53Count.getNumberOfAlteredCases()); // Should be 0 as this method only calculates profiled
    // cases
  }

  @Test
  void testCalculateProfiledCasesPerGene_multiplePanelsMultipleGenes() throws Exception {
    // Setup test data with multiple panels and genes
    Map<String, List<String>> panelCombinationToEntityList = new HashMap<>();
    panelCombinationToEntityList.put("panel1,panel2", Arrays.asList("sample1", "sample2"));
    panelCombinationToEntityList.put("panel1", Arrays.asList("sample3", "sample4", "sample5"));

    Map<String, Map<String, GenePanelToGene>> panelToGeneMap = new HashMap<>();

    // Panel 1 has TP53 and BRCA1
    Map<String, GenePanelToGene> panel1Genes = new HashMap<>();
    GenePanelToGene tp53Gene = new GenePanelToGene();
    tp53Gene.setHugoGeneSymbol("TP53");
    tp53Gene.setEntrezGeneId(7157);
    panel1Genes.put("TP53", tp53Gene);

    GenePanelToGene brca1Gene = new GenePanelToGene();
    brca1Gene.setHugoGeneSymbol("BRCA1");
    brca1Gene.setEntrezGeneId(672);
    panel1Genes.put("BRCA1", brca1Gene);

    panelToGeneMap.put("panel1", panel1Genes);

    // Panel 2 has TP53 and KRAS (TP53 is in both panels - should be union)
    Map<String, GenePanelToGene> panel2Genes = new HashMap<>();
    GenePanelToGene tp53Gene2 = new GenePanelToGene();
    tp53Gene2.setHugoGeneSymbol("TP53");
    tp53Gene2.setEntrezGeneId(7157);
    panel2Genes.put("TP53", tp53Gene2);

    GenePanelToGene krasGene = new GenePanelToGene();
    krasGene.setHugoGeneSymbol("KRAS");
    krasGene.setEntrezGeneId(3845);
    panel2Genes.put("KRAS", krasGene);

    panelToGeneMap.put("panel2", panel2Genes);

    // Call the private method using reflection
    Method method =
        GetAlterationEnrichmentsUseCase.class.getDeclaredMethod(
            "calculateProfiledCasesPerGene", Map.class, Map.class);
    method.setAccessible(true);

    @SuppressWarnings("unchecked")
    Map<String, AlterationCountByGene> result =
        (Map<String, AlterationCountByGene>)
            method.invoke(useCase, panelCombinationToEntityList, panelToGeneMap);

    // Verify results
    assertNotNull(result);
    assertEquals(3, result.size()); // TP53, BRCA1, KRAS
    assertTrue(result.containsKey("TP53"));
    assertTrue(result.containsKey("BRCA1"));
    assertTrue(result.containsKey("KRAS"));

    // TP53 should be profiled in all samples (2 from panel1,panel2 + 3 from panel1 = 5)
    AlterationCountByGene tp53Count = result.get("TP53");
    assertEquals("TP53", tp53Count.getHugoGeneSymbol());
    assertEquals(7157, tp53Count.getEntrezGeneId());
    assertEquals(5, tp53Count.getNumberOfProfiledCases());
    assertEquals(0, tp53Count.getNumberOfAlteredCases());

    // BRCA1 should be profiled in all samples (2 from panel1,panel2 + 3 from panel1 = 5)
    AlterationCountByGene brca1Count = result.get("BRCA1");
    assertEquals("BRCA1", brca1Count.getHugoGeneSymbol());
    assertEquals(672, brca1Count.getEntrezGeneId());
    assertEquals(5, brca1Count.getNumberOfProfiledCases());
    assertEquals(0, brca1Count.getNumberOfAlteredCases());

    // KRAS should be profiled only in samples with panel2 (2 from panel1,panel2)
    AlterationCountByGene krasCount = result.get("KRAS");
    assertEquals("KRAS", krasCount.getHugoGeneSymbol());
    assertEquals(3845, krasCount.getEntrezGeneId());
    assertEquals(2, krasCount.getNumberOfProfiledCases());
    assertEquals(0, krasCount.getNumberOfAlteredCases());
  }

  @Test
  void testCalculateProfiledCasesPerGene_geneInMultiplePanelsSameCombination() throws Exception {
    // Test case where a gene appears in multiple panels within the same combination
    Map<String, List<String>> panelCombinationToEntityList = new HashMap<>();
    panelCombinationToEntityList.put("panel1,panel2", Arrays.asList("sample1", "sample2"));

    Map<String, Map<String, GenePanelToGene>> panelToGeneMap = new HashMap<>();

    // Both panels have the same gene (TP53)
    Map<String, GenePanelToGene> panel1Genes = new HashMap<>();
    GenePanelToGene tp53Gene1 = new GenePanelToGene();
    tp53Gene1.setHugoGeneSymbol("TP53");
    tp53Gene1.setEntrezGeneId(7157);
    panel1Genes.put("TP53", tp53Gene1);
    panelToGeneMap.put("panel1", panel1Genes);

    Map<String, GenePanelToGene> panel2Genes = new HashMap<>();
    GenePanelToGene tp53Gene2 = new GenePanelToGene();
    tp53Gene2.setHugoGeneSymbol("TP53");
    tp53Gene2.setEntrezGeneId(7157);
    panel2Genes.put("TP53", tp53Gene2);
    panelToGeneMap.put("panel2", panel2Genes);

    // Call the private method using reflection
    Method method =
        GetAlterationEnrichmentsUseCase.class.getDeclaredMethod(
            "calculateProfiledCasesPerGene", Map.class, Map.class);
    method.setAccessible(true);

    @SuppressWarnings("unchecked")
    Map<String, AlterationCountByGene> result =
        (Map<String, AlterationCountByGene>)
            method.invoke(useCase, panelCombinationToEntityList, panelToGeneMap);

    // Verify results - gene should only be counted once even though it's in both panels
    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(result.containsKey("TP53"));

    AlterationCountByGene tp53Count = result.get("TP53");
    assertEquals("TP53", tp53Count.getHugoGeneSymbol());
    assertEquals(7157, tp53Count.getEntrezGeneId());
    assertEquals(
        2, tp53Count.getNumberOfProfiledCases()); // Should be 2, not 4 (no double counting)
    assertEquals(0, tp53Count.getNumberOfAlteredCases());
  }

  @Test
  void testCalculateProfiledCasesPerGene_emptyPanelCombination() throws Exception {
    // Test with empty panel combination
    Map<String, List<String>> panelCombinationToEntityList = new HashMap<>();
    Map<String, Map<String, GenePanelToGene>> panelToGeneMap = new HashMap<>();

    // Call the private method using reflection
    Method method =
        GetAlterationEnrichmentsUseCase.class.getDeclaredMethod(
            "calculateProfiledCasesPerGene", Map.class, Map.class);
    method.setAccessible(true);

    @SuppressWarnings("unchecked")
    Map<String, AlterationCountByGene> result =
        (Map<String, AlterationCountByGene>)
            method.invoke(useCase, panelCombinationToEntityList, panelToGeneMap);

    // Verify results
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testCalculateProfiledCasesPerGene_nullPanelInMap() throws Exception {
    // Test case where a panel in the combination doesn't exist in panelToGeneMap
    // This should cause a NullPointerException due to the current implementation
    Map<String, List<String>> panelCombinationToEntityList = new HashMap<>();
    panelCombinationToEntityList.put("panel1,nonexistent_panel", Arrays.asList("sample1"));

    Map<String, Map<String, GenePanelToGene>> panelToGeneMap = new HashMap<>();
    Map<String, GenePanelToGene> panel1Genes = new HashMap<>();
    GenePanelToGene tp53Gene = new GenePanelToGene();
    tp53Gene.setHugoGeneSymbol("TP53");
    tp53Gene.setEntrezGeneId(7157);
    panel1Genes.put("TP53", tp53Gene);
    panelToGeneMap.put("panel1", panel1Genes);
    // Note: "nonexistent_panel" is not in panelToGeneMap

    // Call the private method using reflection
    Method method =
        GetAlterationEnrichmentsUseCase.class.getDeclaredMethod(
            "calculateProfiledCasesPerGene", Map.class, Map.class);
    method.setAccessible(true);

    // This should throw a NullPointerException with the current implementation
    try {
      method.invoke(useCase, panelCombinationToEntityList, panelToGeneMap);
      // If we get here, the method didn't throw as expected
      assertTrue(false, "Expected NullPointerException due to missing panel in panelToGeneMap");
    } catch (java.lang.reflect.InvocationTargetException e) {
      // Verify that the root cause is a NullPointerException
      assertTrue(
          e.getCause() instanceof NullPointerException,
          "Expected NullPointerException, but got: " + e.getCause().getClass().getSimpleName());
      assertTrue(
          e.getCause().getMessage().contains("Cannot invoke \"java.util.Map.values()\""),
          "Expected specific NPE message about Map.values(), but got: "
              + e.getCause().getMessage());
    }
  }
}
