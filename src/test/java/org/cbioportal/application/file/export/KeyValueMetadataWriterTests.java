package org.cbioportal.application.file.export;

import org.cbioportal.application.file.export.writers.KeyValueMetadataWriter;
import org.junit.Test;

import java.io.StringWriter;
import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;

public class KeyValueMetadataWriterTests {

    @Test
    public void testEmptyMetadata() {
        StringWriter output = new StringWriter();
        LinkedHashMap<String, String> metadata = new LinkedHashMap<>();

        new KeyValueMetadataWriter(output).write(metadata);

        assertEquals("", output.toString());
    }

    @Test
    public void testNullMetadataValues() {
        StringWriter output = new StringWriter();
        LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
        metadata.put("key1", null);

        new KeyValueMetadataWriter(output).write(metadata);

        assertEquals("", output.toString());
    }

    @Test
    public void testBlankMetadataValues() {
        StringWriter output = new StringWriter();
        LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
        metadata.put("key1", "");

        new KeyValueMetadataWriter(output).write(metadata);

        assertEquals("key1: \n", output.toString());
    }

    @Test
    public void testEscapeNewlineInMetadataValue() {
        StringWriter output = new StringWriter();
        LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
        metadata.put("key1", "\n");

        new KeyValueMetadataWriter(output).write(metadata);

        assertEquals("key1: \\n\n", output.toString());
    }
 
    @Test
    public void testMultipleKeys() {
        StringWriter output = new StringWriter();
        LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
        metadata.put("key1", "key #1");
        metadata.put("key2", "key #2");

        new KeyValueMetadataWriter(output).write(metadata);

        assertEquals("key1: key #1\nkey2: key #2\n", output.toString());
    }
}
