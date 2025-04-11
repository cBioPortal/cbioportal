package org.cbioportal.application.file.export;

import org.cbioportal.application.file.export.writers.TsvDataWriter;
import org.junit.Test;

import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;

import static org.junit.Assert.assertEquals;

public class TsvDataWriterTest {
    @Test
    public void testComposeRow() {
        StringWriter output = new StringWriter();

        SequencedMap<String, String> row1 = new LinkedHashMap<>();
        row1.put("1", "a");
        row1.put("2", null);
        row1.put("3", "c");
        SequencedMap<String, String> row2 = new LinkedHashMap<>();
        row2.put("1", "\t");
        row2.put("2", "d");
        row2.put("3", "");

        var rows = List.of(row1, row2).iterator();

        new TsvDataWriter(output).write(rows);

        assertEquals("a\t\tc\n\\t\td\t\n", output.toString());
    }
}
