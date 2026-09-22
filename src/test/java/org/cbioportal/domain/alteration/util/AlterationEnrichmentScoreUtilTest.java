package org.cbioportal.domain.alteration.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.legacy.model.AlterationEnrichment;
import org.cbioportal.legacy.model.CountSummary;
import org.junit.Test;

public class AlterationEnrichmentScoreUtilTest {

  @Test
  public void calculateEnrichmentScore() {
    // create molecularProfileCaseSet1, molecularProfileCaseSet2 list of entities

    AlterationEnrichment alterationEnrichment = new AlterationEnrichment();

    List<CountSummary> countSummaries = new ArrayList<>();
    CountSummary countSummary1 = new CountSummary();
    CountSummary countSummary2 = new CountSummary();
    CountSummary countSummary3 = new CountSummary();
    CountSummary countSummary4 = new CountSummary();
    countSummary1.setAlteredCount(0);
    countSummary1.setProfiledCount(943);
    countSummary1.setName("groupD");

    countSummary2.setAlteredCount(1);
    countSummary2.setProfiledCount(2103);
    countSummary2.setName("groupB");

    countSummary3.setAlteredCount(0);
    countSummary3.setProfiledCount(680);
    countSummary3.setName("groupC");

    countSummary4.setAlteredCount(0);
    countSummary4.setProfiledCount(6144);
    countSummary4.setName("groupD");

    countSummaries.add(countSummary1);
    countSummaries.add(countSummary2);
    countSummaries.add(countSummary3);
    countSummaries.add(countSummary4);
    alterationEnrichment.setEntrezGeneId(2);
    alterationEnrichment.setCounts(countSummaries);

    var pValue = AlterationEnrichmentScoreUtil.calculateEnrichmentScore(alterationEnrichment);
    assertEquals(0.2964987551514857, pValue.doubleValue(), 1e-10);
  }

  /**
   * A group with {@code profiledCount == 0} (added by {@code
   * addMissingCountsToAlterationEnrichment} for every group whose gene panels do not cover the
   * gene) must not change the p-value: it carries no information about the gene. The guard above
   * the test already excludes such groups through {@code filteredCounts}; the tests themselves have
   * to use the same list.
   *
   * <p>On master the Chi-square branch iterates over {@code counts}, so the zero-profiled group
   * becomes a {@code {0, 0}} row. commons-math3 then returns {@code NaN}, which the NaN guard turns
   * into p = 1.0, and a strongly enriched gene silently looks unenriched.
   */
  @Test
  public void calculateEnrichmentScore_ignoresZeroProfiledGroup_chiSquare() {
    var withoutUnprofiledGroup =
        AlterationEnrichmentScoreUtil.calculateEnrichmentScore(
            enrichment(group("A", 40, 100), group("B", 5, 100), group("C", 10, 100)));
    var withUnprofiledGroup =
        AlterationEnrichmentScoreUtil.calculateEnrichmentScore(
            enrichment(
                group("A", 40, 100), group("B", 5, 100), group("C", 10, 100), group("D", 0, 0)));

    // chi-square over the three profiled groups; master returns 1.0 for the second call
    assertEquals(4.035882739117369e-11, withoutUnprofiledGroup.doubleValue(), 1e-20);
    assertEquals(withoutUnprofiledGroup.doubleValue(), withUnprofiledGroup.doubleValue(), 1e-20);
  }

  /**
   * With two profiled groups and one zero-profiled group, the comparison is really a two-group
   * comparison, so it must use Fisher's exact test like any other two-group comparison. On master
   * it takes the Chi-square branch because {@code counts.size()} is 3, and returns p = 1.0.
   */
  @Test
  public void calculateEnrichmentScore_ignoresZeroProfiledGroup_fisher() {
    var twoGroups =
        AlterationEnrichmentScoreUtil.calculateEnrichmentScore(
            enrichment(group("A", 10, 100), group("B", 5, 80)));
    var twoGroupsAndUnprofiledGroup =
        AlterationEnrichmentScoreUtil.calculateEnrichmentScore(
            enrichment(group("A", 10, 100), group("B", 5, 80), group("C", 0, 0)));

    assertEquals(0.42565609994341924, twoGroups.doubleValue(), 1e-12);
    assertEquals(twoGroups.doubleValue(), twoGroupsAndUnprofiledGroup.doubleValue(), 1e-12);
  }

  private static AlterationEnrichment enrichment(CountSummary... counts) {
    AlterationEnrichment alterationEnrichment = new AlterationEnrichment();
    alterationEnrichment.setEntrezGeneId(42);
    alterationEnrichment.setCounts(List.of(counts));
    return alterationEnrichment;
  }

  private static CountSummary group(String name, int alteredCount, int profiledCount) {
    CountSummary countSummary = new CountSummary();
    countSummary.setName(name);
    countSummary.setAlteredCount(alteredCount);
    countSummary.setProfiledCount(profiledCount);
    return countSummary;
  }
}
