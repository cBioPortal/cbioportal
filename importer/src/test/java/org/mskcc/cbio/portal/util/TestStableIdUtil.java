/*
 * Copyright (c) 2016 The Hyve B.V.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.util;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Tests the StableIdUtil Class.
 *
 * @author pieter lukasse.
 */
public class TestStableIdUtil {
	
	@Test
    public void testId1() {
        String barcode = "TCGA-13-1479-01A-01W";
        String sampleId = StableIdUtil.getSampleId(barcode);
        assertEquals ("TCGA-13-1479-01", sampleId);
    }


}
