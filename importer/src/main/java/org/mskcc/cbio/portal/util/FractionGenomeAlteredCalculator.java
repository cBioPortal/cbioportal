package org.mskcc.cbio.portal.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Computes FRACTION_GENOME_ALTERED from segments as they are read from a seg
 * file, so the importer does not have to read its own inserts back from the
 * database.
 */
public class FractionGenomeAlteredCalculator {

    public static final double SEGMENT_MEAN_CUTOFF = 0.2;

    // sample id -> {measured length, altered length}
    private final Map<Integer, long[]> lengthsBySample = new HashMap<>();

    public void addSegment(int sampleId, long start, long end, double segmentMean) {
        long length = end - start;
        long[] lengths = lengthsBySample.computeIfAbsent(sampleId, id -> new long[2]);
        lengths[0] += length;
        if (Math.abs(segmentMean) >= SEGMENT_MEAN_CUTOFF) {
            lengths[1] += length;
        }
    }

    /**
     * @return fraction genome altered per sample, rounded half-up to exactly
     * four decimal places. Samples without positive measured length are omitted.
     */
    public Map<Integer, String> getFractionGenomeAltered() {
        Map<Integer, String> result = new HashMap<>();
        for (Map.Entry<Integer, long[]> entry : lengthsBySample.entrySet()) {
            long measured = entry.getValue()[0];
            if (measured <= 0) {
                continue;
            }
            long altered = entry.getValue()[1];
            // Fixed four decimal places, as MySQL DECIMAL division stored it before the
            // ClickHouse port (e.g. "0.0002", not the Double.toString form "2.0E-4")
            BigDecimal fraction = BigDecimal.valueOf(altered)
                    .divide(BigDecimal.valueOf(measured), 4, RoundingMode.HALF_UP);
            result.put(entry.getKey(), fraction.toPlainString());
        }
        return result;
    }
}
