package org.cbioportal.domain.alteration.util;

import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.cbioportal.legacy.model.AlterationEnrichment;
import org.cbioportal.legacy.model.CountSummary;
import org.cbioportal.legacy.service.util.FisherExactTestCalculator;

public abstract class AlterationEnrichmentScoreUtil {

    private AlterationEnrichmentScoreUtil() {}

    public static BigDecimal calculateEnrichmentScore(AlterationEnrichment alterationEnrichment) {
        double pValue = 0;

        List<CountSummary> counts = alterationEnrichment.getCounts();
        List<CountSummary> filteredCounts = counts
            .stream()
            .filter(groupCaseCount -> groupCaseCount.getProfiledCount() > 0)
            .toList();

        // groups where number of altered cases is greater than profiled cases.
        // This is a temporary fix for https://github.com/cBioPortal/cbioportal/issues/7274
        // and https://github.com/cBioPortal/cbioportal/issues/7418
        long invalidDataGroups = filteredCounts
            .stream()
            .filter(
                groupCasesCount ->
                    groupCasesCount.getAlteredCount() > groupCasesCount.getProfiledCount()
            )
            .count();

        // calculate p-value only if more than one group have profile cases count
        // greater than 0
        if (filteredCounts.size() > 1 && invalidDataGroups == 0) {
            // if groups size is two do Fisher Exact test else do Chi-Square test
            if (counts.size() == 2) {
                int alteredInNoneCount =
                    counts.get(1).getProfiledCount() - counts.get(1).getAlteredCount();
                int alteredOnlyInQueryGenesCount =
                    counts.get(0).getProfiledCount() - counts.get(0).getAlteredCount();

                var fisherExactTestCalculator = new FisherExactTestCalculator();
                pValue = fisherExactTestCalculator.getTwoTailedPValue(
                    alteredInNoneCount,
                    counts.get(1).getAlteredCount(),
                    alteredOnlyInQueryGenesCount,
                    counts.get(0).getAlteredCount()
                );
            } else {
                long[][] array = counts
                    .stream()
                    .map(count ->
                        new long[] {
                            count.getAlteredCount(),
                            count.getProfiledCount() - count.getAlteredCount(),
                        }
                    )
                    .toArray(long[][]::new);

                ChiSquareTest chiSquareTest = new ChiSquareTest();
                pValue = chiSquareTest.chiSquareTest(array);

                // set p-value to 1 when the cases in all groups are altered
                if (Double.isNaN(pValue)) {
                    pValue = 1;
                }
            }
        }
        return BigDecimal.valueOf(pValue);
    }
}
