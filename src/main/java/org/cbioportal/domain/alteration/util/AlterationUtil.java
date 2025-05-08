package org.cbioportal.domain.alteration.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cbioportal.legacy.model.AlterationCountByGene;

public abstract class AlterationUtil {
  private AlterationUtil() {}

  public static List<AlterationCountByGene> combineAlterationCountsWithConflictingHugoSymbols(
      List<AlterationCountByGene> alterationCounts) {
    Map<String, AlterationCountByGene> alterationCountByGeneMap = new HashMap<>();
    for (var alterationCount : alterationCounts) {
      if (alterationCountByGeneMap.containsKey(alterationCount.getHugoGeneSymbol())) {
        AlterationCountByGene toUpdate =
            alterationCountByGeneMap.get(alterationCount.getHugoGeneSymbol());
        toUpdate.setNumberOfAlteredCases(
            toUpdate.getNumberOfAlteredCases() + alterationCount.getNumberOfAlteredCases());
        toUpdate.setTotalCount(toUpdate.getTotalCount() + alterationCount.getTotalCount());
      } else {
        alterationCountByGeneMap.put(alterationCount.getHugoGeneSymbol(), alterationCount);
      }
    }
    return alterationCountByGeneMap.values().stream().toList();
  }
}
