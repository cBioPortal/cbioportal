package org.cbioportal.domain.alteration.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cbioportal.domain.alteration.repository.AlterationRepository;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.AlterationEnrichment;
import org.cbioportal.legacy.model.CountSummary;
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

  @Test
  void testAddMissingCountsToAlterationEnrichment_allGroupsPresent() throws Exception {
    // Test case where all groups are already present - should not add any new counts
    AlterationEnrichment enrichment = new AlterationEnrichment();
    enrichment.setHugoGeneSymbol("TP53");
    enrichment.setEntrezGeneId(7157);
    enrichment.setCounts(new ArrayList<>());

    // Add existing counts for all groups
    CountSummary group1Count = new CountSummary();
    group1Count.setName("group1");
    group1Count.setAlteredCount(5);
    group1Count.setProfiledCount(10);
    enrichment.getCounts().add(group1Count);

    CountSummary group2Count = new CountSummary();
    group2Count.setName("group2");
    group2Count.setAlteredCount(3);
    group2Count.setProfiledCount(8);
    enrichment.getCounts().add(group2Count);

    Collection<String> groups = Arrays.asList("group1", "group2");

    // Call the private method using reflection
    Method method =
        GetAlterationEnrichmentsUseCase.class.getDeclaredMethod(
            "addMissingCountsToAlterationEnrichment", AlterationEnrichment.class, Collection.class);
    method.setAccessible(true);
    method.invoke(useCase, enrichment, groups);

    // Verify no new counts were added
    assertEquals(2, enrichment.getCounts().size());

    // Verify existing counts are unchanged
    Set<String> groupNames = new HashSet<>();
    for (CountSummary count : enrichment.getCounts()) {
      groupNames.add(count.getName());
      if ("group1".equals(count.getName())) {
        assertEquals(5, count.getAlteredCount());
        assertEquals(10, count.getProfiledCount());
      } else if ("group2".equals(count.getName())) {
        assertEquals(3, count.getAlteredCount());
        assertEquals(8, count.getProfiledCount());
      }
    }
    assertTrue(groupNames.contains("group1"));
    assertTrue(groupNames.contains("group2"));
  }

  @Test
  void testAddMissingCountsToAlterationEnrichment_someMissingGroups() throws Exception {
    // Test case where some groups are missing - should add counts with zeros
    AlterationEnrichment enrichment = new AlterationEnrichment();
    enrichment.setHugoGeneSymbol("BRCA1");
    enrichment.setEntrezGeneId(672);
    enrichment.setCounts(new ArrayList<>());

    // Add existing count for only one group
    CountSummary group1Count = new CountSummary();
    group1Count.setName("group1");
    group1Count.setAlteredCount(5);
    group1Count.setProfiledCount(10);
    enrichment.getCounts().add(group1Count);

    Collection<String> groups = Arrays.asList("group1", "group2", "group3");

    // Call the private method using reflection
    Method method =
        GetAlterationEnrichmentsUseCase.class.getDeclaredMethod(
            "addMissingCountsToAlterationEnrichment", AlterationEnrichment.class, Collection.class);
    method.setAccessible(true);
    method.invoke(useCase, enrichment, groups);

    // Verify new counts were added
    assertEquals(3, enrichment.getCounts().size());

    // Verify all groups are present
    Set<String> groupNames = new HashSet<>();
    for (CountSummary count : enrichment.getCounts()) {
      groupNames.add(count.getName());
      if ("group1".equals(count.getName())) {
        // Original count should be unchanged
        assertEquals(5, count.getAlteredCount());
        assertEquals(10, count.getProfiledCount());
      } else if ("group2".equals(count.getName()) || "group3".equals(count.getName())) {
        // New counts should be zero
        assertEquals(0, count.getAlteredCount());
        assertEquals(0, count.getProfiledCount());
      }
    }
    assertTrue(groupNames.contains("group1"));
    assertTrue(groupNames.contains("group2"));
    assertTrue(groupNames.contains("group3"));
  }

  @Test
  void testAddMissingCountsToAlterationEnrichment_allGroupsMissing() throws Exception {
    // Test case where no groups are present initially - should add all groups with zeros
    AlterationEnrichment enrichment = new AlterationEnrichment();
    enrichment.setHugoGeneSymbol("KRAS");
    enrichment.setEntrezGeneId(3845);
    enrichment.setCounts(new ArrayList<>());

    Collection<String> groups = Arrays.asList("groupA", "groupB", "groupC");

    // Call the private method using reflection
    Method method =
        GetAlterationEnrichmentsUseCase.class.getDeclaredMethod(
            "addMissingCountsToAlterationEnrichment", AlterationEnrichment.class, Collection.class);
    method.setAccessible(true);
    method.invoke(useCase, enrichment, groups);

    // Verify all counts were added
    assertEquals(3, enrichment.getCounts().size());

    // Verify all groups are present with zero counts
    Set<String> groupNames = new HashSet<>();
    for (CountSummary count : enrichment.getCounts()) {
      groupNames.add(count.getName());
      assertEquals(0, count.getAlteredCount());
      assertEquals(0, count.getProfiledCount());
    }
    assertTrue(groupNames.contains("groupA"));
    assertTrue(groupNames.contains("groupB"));
    assertTrue(groupNames.contains("groupC"));
  }

  @Test
  void testAddMissingCountsToAlterationEnrichment_emptyGroups() throws Exception {
    // Test case with empty groups collection - should not add any counts
    AlterationEnrichment enrichment = new AlterationEnrichment();
    enrichment.setHugoGeneSymbol("PIK3CA");
    enrichment.setEntrezGeneId(5290);
    enrichment.setCounts(new ArrayList<>());

    // Add one existing count
    CountSummary existingCount = new CountSummary();
    existingCount.setName("existingGroup");
    existingCount.setAlteredCount(7);
    existingCount.setProfiledCount(15);
    enrichment.getCounts().add(existingCount);

    Collection<String> groups = new ArrayList<>(); // Empty collection

    // Call the private method using reflection
    Method method =
        GetAlterationEnrichmentsUseCase.class.getDeclaredMethod(
            "addMissingCountsToAlterationEnrichment", AlterationEnrichment.class, Collection.class);
    method.setAccessible(true);
    method.invoke(useCase, enrichment, groups);

    // Verify no new counts were added and existing count is unchanged
    assertEquals(1, enrichment.getCounts().size());
    CountSummary count = enrichment.getCounts().get(0);
    assertEquals("existingGroup", count.getName());
    assertEquals(7, count.getAlteredCount());
    assertEquals(15, count.getProfiledCount());
  }

  @Test
  void testAddMissingCountsToAlterationEnrichment_duplicateGroupsInCollection() throws Exception {
    // Test case with duplicate groups in the collection - should handle gracefully
    AlterationEnrichment enrichment = new AlterationEnrichment();
    enrichment.setHugoGeneSymbol("APC");
    enrichment.setEntrezGeneId(324);
    enrichment.setCounts(new ArrayList<>());

    Collection<String> groups = Arrays.asList("group1", "group2", "group1", "group3", "group2");

    // Call the private method using reflection
    Method method =
        GetAlterationEnrichmentsUseCase.class.getDeclaredMethod(
            "addMissingCountsToAlterationEnrichment", AlterationEnrichment.class, Collection.class);
    method.setAccessible(true);
    method.invoke(useCase, enrichment, groups);

    // Verify only unique groups were added (no duplicates)
    assertEquals(3, enrichment.getCounts().size());

    Set<String> groupNames = new HashSet<>();
    for (CountSummary count : enrichment.getCounts()) {
      groupNames.add(count.getName());
      assertEquals(0, count.getAlteredCount());
      assertEquals(0, count.getProfiledCount());
    }
    assertTrue(groupNames.contains("group1"));
    assertTrue(groupNames.contains("group2"));
    assertTrue(groupNames.contains("group3"));
  }
}
