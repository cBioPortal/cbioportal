package org.mskcc.cbio.portal.util;

import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestFractionGenomeAlteredCalculator {

    @Test
    public void weightsAlteredSegmentsByLength() {
        FractionGenomeAlteredCalculator calculator = new FractionGenomeAlteredCalculator();
        calculator.addSegment(1, 0, 100, 0.5);
        calculator.addSegment(1, 100, 400, 0.1);
        calculator.addSegment(2, 0, 50, -0.2);
        calculator.addSegment(2, 50, 100, -0.3);
        calculator.addSegment(3, 0, 10, 0.19);

        assertEquals(Map.of(1, "0.2500", 2, "1.0000", 3, "0.0000"), calculator.getFractionGenomeAltered());
    }

    @Test
    public void neverUsesScientificNotation() {
        FractionGenomeAlteredCalculator calculator = new FractionGenomeAlteredCalculator();
        calculator.addSegment(1, 0, 2, 1.0);
        calculator.addSegment(1, 2, 10000, 0.0);

        assertEquals(Map.of(1, "0.0002"), calculator.getFractionGenomeAltered());
    }

    @Test
    public void roundsToFourDecimalPlaces() {
        FractionGenomeAlteredCalculator calculator = new FractionGenomeAlteredCalculator();
        calculator.addSegment(1, 0, 1, 1.0);
        calculator.addSegment(1, 1, 3, 0.0);

        assertEquals(Map.of(1, "0.3333"), calculator.getFractionGenomeAltered());
    }

    @Test
    public void omitsSamplesWithoutMeasuredLength() {
        FractionGenomeAlteredCalculator calculator = new FractionGenomeAlteredCalculator();
        calculator.addSegment(1, 5, 5, 1.0);

        assertEquals(Map.of(), calculator.getFractionGenomeAltered());
    }
}
