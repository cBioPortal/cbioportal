package org.mskcc.portal.test.util;

import junit.framework.TestCase;
import org.mskcc.portal.util.SkinUtil;

/**
 * Tests the SkinUtil Class.
 *
 * @author Ethan Cerami.
 */
public class TestSkinUtil extends TestCase {

    public void test1() {
        String contactEmail = SkinUtil.getEmailContact();
        assertEquals("<span class=\"mailme\" title=\"Contact us\">cancergenomics at " +
                "cbio dot mskcc dot org</span>", contactEmail);
    }
}
