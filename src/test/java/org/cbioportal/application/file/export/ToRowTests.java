package org.cbioportal.application.file.export;

import org.cbioportal.application.file.model.MafRecord;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ToRowTests {
    @Test
    public void testMafRowToRow() {
        var mafRecord = new MafRecord();
        assertNotNull(mafRecord.toRow());
        assertEquals(37, mafRecord.toRow().size());
    }
}
