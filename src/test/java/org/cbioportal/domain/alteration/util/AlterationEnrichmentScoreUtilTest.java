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

  @Test
  public void calculateEnrichmentScore_noPValueWhenOnlyOneGroupIsProfiled() {
    // the gene is not on the panel of any groupB sample, so there is nothing to compare
    AlterationEnrichment alterationEnrichment =
        alterationEnrichment(countSummary("groupA", 5, 50), countSummary("groupB", 0, 0));

    assertNull(AlterationEnrichmentScoreUtil.calculateEnrichmentScore(alterationEnrichment));
  }

  @Test
  public void calculateEnrichmentScore_noPValueWhenAlteredCountExceedsProfiledCount() {
    AlterationEnrichment alterationEnrichment =
        alterationEnrichment(countSummary("groupA", 12, 10), countSummary("groupB", 3, 40));

    assertNull(AlterationEnrichmentScoreUtil.calculateEnrichmentScore(alterationEnrichment));
  }

  private static AlterationEnrichment alterationEnrichment(CountSummary... counts) {
    AlterationEnrichment alterationEnrichment = new AlterationEnrichment();
    alterationEnrichment.setEntrezGeneId(2);
    alterationEnrichment.setCounts(List.of(counts));
    return alterationEnrichment;
  }

  private static CountSummary countSummary(String name, int alteredCount, int profiledCount) {
    CountSummary countSummary = new CountSummary();
    countSummary.setName(name);
    countSummary.setAlteredCount(alteredCount);
    countSummary.setProfiledCount(profiledCount);
    return countSummary;
  }
}
