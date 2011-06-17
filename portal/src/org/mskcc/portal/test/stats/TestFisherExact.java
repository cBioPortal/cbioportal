package org.mskcc.portal.test.stats;

import junit.framework.TestCase;
import org.mskcc.portal.stats.FisherExact;

public class TestFisherExact extends TestCase {

    public void testFisherExact() {
        FisherExact fisher = new FisherExact(43 + 2 + 17 + 7);
        double p = fisher.getCumlativeP(43, 2, 17, 7);
        assertEquals(0.006653, p, 0.00001);
    }
}
