package org.cbioportal.domain.alteration.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.junit.Test;

public class AlterationUtilTest {

  @Test
  public void combineWithEmptyListReturnsEmptyList() {
    List<AlterationCountByGene> result =
        AlterationUtil.combineAlterationCountsWithConflictingHugoSymbols(new ArrayList<>());

    assertTrue(result.isEmpty());
  }

  @Test
  public void combineWithNoConflictingSymbolsLeavesValuesUnchanged() {
    AlterationCountByGene tp53 = createAlterationCount("TP53", 7157, 10, 12);
    AlterationCountByGene kras = createAlterationCount("KRAS", 3845, 5, 6);
    AlterationCountByGene egfr = createAlterationCount("EGFR", 1956, 3, 4);

    List<AlterationCountByGene> result =
        AlterationUtil.combineAlterationCountsWithConflictingHugoSymbols(List.of(tp53, kras, egfr));

    assertEquals(3, result.size());
    Map<String, AlterationCountByGene> bySymbol = indexBySymbol(result);
    assertSame(tp53, bySymbol.get("TP53"));
    assertSame(kras, bySymbol.get("KRAS"));
    assertSame(egfr, bySymbol.get("EGFR"));
    assertCounts(tp53, 10, 12);
    assertCounts(kras, 5, 6);
    assertCounts(egfr, 3, 4);
  }

  @Test
  public void combineWithConflictingSymbolsSumsCountsIntoFirstSeenObject() {
    AlterationCountByGene first = createAlterationCount("TP53", 7157, 10, 12);
    AlterationCountByGene kras = createAlterationCount("KRAS", 3845, 5, 6);
    AlterationCountByGene duplicate = createAlterationCount("TP53", 7157, 4, 7);

    List<AlterationCountByGene> result =
        AlterationUtil.combineAlterationCountsWithConflictingHugoSymbols(
            List.of(first, kras, duplicate));

    // duplicates are collapsed into one entry per Hugo symbol
    assertEquals(2, result.size());
    Map<String, AlterationCountByGene> bySymbol = indexBySymbol(result);

    // the first-seen object is kept and mutated in place
    assertSame(first, bySymbol.get("TP53"));
    assertCounts(first, 14, 19);

    // the duplicate is only read from, never mutated
    assertCounts(duplicate, 4, 7);

    // non-conflicting entries are untouched
    assertSame(kras, bySymbol.get("KRAS"));
    assertCounts(kras, 5, 6);
  }

  @Test
  public void combineWithMultipleConflictsForSameSymbolSumsAllOccurrences() {
    AlterationCountByGene first = createAlterationCount("TP53", 7157, 1, 2);
    AlterationCountByGene second = createAlterationCount("TP53", 7157, 10, 20);
    AlterationCountByGene third = createAlterationCount("TP53", 7157, 100, 200);

    List<AlterationCountByGene> result =
        AlterationUtil.combineAlterationCountsWithConflictingHugoSymbols(
            List.of(first, second, third));

    assertEquals(1, result.size());
    assertSame(first, result.get(0));
    assertCounts(first, 111, 222);
    assertCounts(second, 10, 20);
    assertCounts(third, 100, 200);
  }

  private static Map<String, AlterationCountByGene> indexBySymbol(
      List<AlterationCountByGene> counts) {
    return counts.stream()
        .collect(Collectors.toMap(AlterationCountByGene::getHugoGeneSymbol, Function.identity()));
  }

  private static void assertCounts(
      AlterationCountByGene actual, int expectedAlteredCases, int expectedTotalCount) {
    assertEquals(
        "numberOfAlteredCases of " + actual.getHugoGeneSymbol(),
        Integer.valueOf(expectedAlteredCases),
        actual.getNumberOfAlteredCases());
    assertEquals(
        "totalCount of " + actual.getHugoGeneSymbol(),
        Integer.valueOf(expectedTotalCount),
        actual.getTotalCount());
  }

  private static AlterationCountByGene createAlterationCount(
      String hugoGeneSymbol, int entrezGeneId, int numberOfAlteredCases, int totalCount) {
    AlterationCountByGene alterationCount = new AlterationCountByGene();
    alterationCount.setHugoGeneSymbol(hugoGeneSymbol);
    alterationCount.setEntrezGeneId(entrezGeneId);
    alterationCount.setNumberOfAlteredCases(numberOfAlteredCases);
    alterationCount.setTotalCount(totalCount);
    return alterationCount;
  }
}
