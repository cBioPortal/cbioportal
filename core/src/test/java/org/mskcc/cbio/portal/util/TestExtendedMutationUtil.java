package org.mskcc.cbio.portal.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Created by Hongxin Zhang on 3/20/18.
 */
@RunWith(Parameterized.class)
public class TestExtendedMutationUtil {
    private String proteinChange;
    private String proteinStart;
    private String proteinEnd;

    public TestExtendedMutationUtil(String proteinChange, String proteinStart, String proteinEnd) {
        this.proteinChange = proteinChange;
        this.proteinStart = proteinStart;
        this.proteinEnd = proteinEnd;
    }

    @Parameterized.Parameters
    public static Collection<String[]> getParameters() {
        return Arrays.asList(
            new String[][]{
                // any
                {"449_514mut", "449", "514"},

                // Missense variant
                {"V600E", "600", "600"},
                {"F53_Q53delinsL", "53", "53"},
                {"D842_I843delinsIM", "842", "843"},
                {"IK744KI", "744", "745"},

                // feature_truncating variant
                {"D286_L292trunc", "286", "292"},

                // frameshift event
                {"N457Mfs*22", "457", "457"},
                {"*1069Ffs*5", "1069", "1069"},

                // inframe event
                {"T417_D419delinsI", "417", "419"},
                {"E102_I103del", "102", "103"},
                {"V600delinsYM", "600", "600"},
                {"I744_K745delinsKIPVAI", "744", "745"},
                {"762_823ins", "762", "823"},
                {"V561_I562insER", "561", "562"},
                {"IK744KIPVAI", "744", "745"},
                {"IK744K", "744", "745"},
                {"IKG744KIPVAI", "744", "746"},
                {"P68_C77dup", "68", "77"},

                // start_lost,
                {"M1I", "1", "1"},
                {"M1?", "1", "1"},

                // NA
                {"BCR-ABL1 Fusion", "-1", "-1"},

                // Splice
                {"X405_splice", "405", "405"},
                {"405_splice", "405", "405"},
                {"405splice", "405", "405"},
                {"X405_A500splice", "405", "500"},
                {"X405_A500_splice", "405", "500"},
                {"405_500_splice", "405", "500"},
                {"405_500splice", "405", "500"},

                // Stop gained
                {"R2109*", "2109", "2109"},

                // Synonymous Variant
                {"G500G", "500", "500"},

            });
    }

    @Test
    public void getProteinPosStart() throws Exception {
        int start = ExtendedMutationUtil.getProteinPosStart("", proteinChange);
        assertEquals(proteinChange + ": Protein start should be " + proteinStart + ", but got: " + start, proteinStart, Integer.toString(start));
    }

    @Test
    public void getProteinPosEnd() throws Exception {
        int end = ExtendedMutationUtil.getProteinPosEnd("", proteinChange);
        assertEquals(proteinChange + ": Protein end should be " + proteinEnd + ", but got: " + end, proteinEnd, Integer.toString(end));
    }

}