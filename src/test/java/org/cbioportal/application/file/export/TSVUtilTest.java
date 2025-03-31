package org.cbioportal.application.file.export;

import org.cbioportal.application.file.utils.TSVUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TSVUtilTest {
    @Test
    public void testComposeRow() {
        List row = new ArrayList();
        row.add("a");
        row.add(null);
        row.add("c");
        assertEquals("a\t\tc\n", TSVUtil.composeRow(row));
    }
}
