package org.mskcc.cbio.portal.util;

import org.junit.Test;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mskcc.cbio.portal.util.TsvUtil.ensureHeaderAndRowMatch;

public class TsvUtilTest {

    @Test
    public void testEnsureHeaderAndRowMatch_headerHasGreaterLength() {
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> ensureHeaderAndRowMatch(new String[] {"header1", "header2"}, new String[] {"row1"}));
        assertTrue(illegalArgumentException.getMessage().contains("Found line with less fields"));
    }

    @Test
    public void testEnsureHeaderAndRowMatch_headerHasSmallerLength() {
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> ensureHeaderAndRowMatch(new String[] {"header1"}, new String[] {"row1", "row2"}));
        assertTrue(illegalArgumentException.getMessage().contains("Found line with more fields"));
    }

    @Test
    public void testEnsureHeaderAndRowMatch_headerHasSameLength() {
        ensureHeaderAndRowMatch(new String[] {"header1", "header2"}, new String[] {"row1", "row2"});
    }
}
