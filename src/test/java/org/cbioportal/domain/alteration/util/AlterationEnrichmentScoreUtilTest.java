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
   * Regression test for a bug where the Chi-square test was fed the raw {@code counts} list (which
   * can contain groups with {@code profiledCount == 0}) instead of the pre-filtered {@code
   * filteredCounts} list. Apache Math's {@code ChiSquareTest} requires every row in the contingency
   * table to have a strictly-positive sum; a zero-profiled group produces a {@code {0, 0}} row
   * whose sum is 0, causing a {@code MathIllegalArgumentException}.
   *
   * <p>This test uses three groups where the third has {@code profiledCount = 0}. Before the fix,
   * this threw an exception. After the fix it should complete and return a valid p-value.
   */
  @Test
  public void calculateEnrichmentScore_withZeroProfiledGroup_doesNotThrow() {
    AlterationEnrichment alterationEnrichment = new AlterationEnrichment();

    CountSummary groupA = new CountSummary();
    groupA.setName("groupA");
    groupA.setAlteredCount(10);
    groupA.setProfiledCount(100);

    CountSummary groupB = new CountSummary();
    groupB.setName("groupB");
    groupB.setAlteredCount(5);
    groupB.setProfiledCount(80);

    CountSummary groupC = new CountSummary();
    groupC.setName("groupC");
    groupC.setAlteredCount(0);
    groupC.setProfiledCount(0); // zero-profiled , was causing MathIllegalArgumentException

    alterationEnrichment.setEntrezGeneId(42);
    alterationEnrichment.setCounts(List.of(groupA, groupB, groupC));

    // Must not throw; should return a valid (non-negative) p-value
    var pValue = AlterationEnrichmentScoreUtil.calculateEnrichmentScore(alterationEnrichment);
    assertNotNull(pValue);
    assertTrue("p-value must be non-negative", pValue.doubleValue() >= 0.0);
    assertTrue("p-value must be at most 1", pValue.doubleValue() <= 1.0);
  }
}
